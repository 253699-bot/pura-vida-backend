package com.puravida.modules.sales.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.sales.application.dto.CreateManualSaleItemRequest;
import com.puravida.modules.sales.application.dto.CreateManualSaleRequest;
import com.puravida.modules.sales.application.dto.SaleResponse;
import com.puravida.modules.sales.application.port.in.CreateManualSalePort;
import com.puravida.modules.sales.application.port.out.ManualSaleContextRepositoryPort;
import com.puravida.modules.sales.application.port.out.SaleRepositoryPort;
import com.puravida.modules.sales.domain.exception.SaleValidationException;
import com.puravida.modules.sales.domain.model.ManualSaleLine;
import com.puravida.modules.sales.domain.model.ManualSaleMenuItem;
import com.puravida.modules.sales.domain.model.Sale;
import com.puravida.modules.sales.domain.model.SaleSource;
import com.puravida.modules.users.domain.model.User;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateManualSaleUseCase implements CreateManualSalePort {

    private static final BigDecimal MAX_PERSISTED_AMOUNT = new BigDecimal("99999999.99");

    private final SaleRepositoryPort saleRepositoryPort;
    private final ManualSaleContextRepositoryPort contextRepositoryPort;
    private final SalesAuthorizationService authorizationService;

    public CreateManualSaleUseCase(
            SaleRepositoryPort saleRepositoryPort,
            ManualSaleContextRepositoryPort contextRepositoryPort,
            SalesAuthorizationService authorizationService
    ) {
        this.saleRepositoryPort = saleRepositoryPort;
        this.contextRepositoryPort = contextRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public SaleResponse create(
            String idempotencyKey,
            CreateManualSaleRequest request,
            AuthenticatedUser authenticatedUser
    ) {
        User actor = authorizationService.requireEncargada(authenticatedUser);
        String normalizedKey = normalizeIdempotencyKey(idempotencyKey);
        LogicalPayload payload = validateAndNormalize(request);

        Sale existingSale = saleRepositoryPort.findByActorAndIdempotencyKey(actor.id(), normalizedKey)
                .orElse(null);
        if (existingSale != null) {
            return replayOrConflict(existingSale, payload);
        }

        LocalDate today = LocalDate.now();
        validateBusinessIsOpen(today);
        List<ManualSaleLine> lines = resolveLines(payload, today);

        existingSale = saleRepositoryPort
                .findByActorAndIdempotencyKeyForUpdate(actor.id(), normalizedKey)
                .orElse(null);
        if (existingSale != null) {
            return replayOrConflict(existingSale, payload);
        }

        BigDecimal total = lines.stream()
                .map(ManualSaleLine::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        validatePersistableAmount(total, "El total");
        Sale savedSale = saleRepositoryPort.save(Sale.createManual(
                total,
                actor.id(),
                payload.observations(),
                normalizedKey
        ));
        List<ManualSaleLine> savedLines = saleRepositoryPort.saveLines(lines.stream()
                .map(line -> line.assignTo(savedSale.id()))
                .toList());
        return SaleResponse.from(savedSale, savedLines);
    }

    private LogicalPayload validateAndNormalize(CreateManualSaleRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new SaleValidationException("La venta debe contener al menos un item.");
        }
        if (!request.unknownFieldNames().isEmpty()) {
            throw new SaleValidationException("El body contiene campos no permitidos.");
        }

        Map<Integer, Integer> quantities = new LinkedHashMap<>();
        for (CreateManualSaleItemRequest item : request.items()) {
            if (item == null || item.menuItemId() == null || item.cantidad() == null || item.cantidad() <= 0) {
                throw new SaleValidationException("Cada item debe tener un menuItemId y una cantidad positiva.");
            }
            if (quantities.putIfAbsent(item.menuItemId(), item.cantidad()) != null) {
                throw new SaleValidationException("No se permiten items de menu duplicados en una venta.");
            }
        }
        return new LogicalPayload(quantities, normalizeObservations(request.observaciones()));
    }

    private void validateBusinessIsOpen(LocalDate today) {
        Boolean open = contextRepositoryPort.lockBusinessOpenByDate(today)
                .orElseThrow(() -> new ConflictException(
                        "El estado de la fonda aun no ha sido configurado para hoy."));
        if (!open) {
            throw new ConflictException("La fonda esta cerrada y no puede registrar ventas.");
        }
    }

    private List<ManualSaleLine> resolveLines(LogicalPayload payload, LocalDate today) {
        List<Integer> requestedIds = payload.quantities().keySet().stream().sorted().toList();
        Map<Integer, ManualSaleMenuItem> menuById = contextRepositoryPort
                .lockMenuItemsByIds(requestedIds)
                .stream()
                .collect(Collectors.toMap(ManualSaleMenuItem::id, Function.identity()));

        return payload.quantities().entrySet().stream().map(entry -> {
            ManualSaleMenuItem menuItem = menuById.get(entry.getKey());
            if (menuItem == null || !today.equals(menuItem.date())) {
                throw new NotFoundException("Uno de los items no pertenece al menu de hoy.");
            }
            if (!menuItem.published()) {
                throw new ConflictException("Uno de los items no esta publicado en el menu de hoy.");
            }
            if (!menuItem.dishActive()) {
                throw new ConflictException("Uno de los platillos seleccionados esta inactivo.");
            }
            if (!menuItem.available()) {
                throw new ConflictException("Uno de los items seleccionados no esta disponible.");
            }
            ManualSaleLine line = ManualSaleLine.create(menuItem, entry.getValue());
            validatePersistableAmount(line.subtotal(), "El subtotal de un item");
            return line;
        }).toList();
    }

    private void validatePersistableAmount(BigDecimal amount, String fieldName) {
        if (amount.scale() > 2 || amount.compareTo(BigDecimal.ZERO) < 0
                || amount.compareTo(MAX_PERSISTED_AMOUNT) > 0) {
            throw new SaleValidationException(
                    fieldName + " excede el rango persistible DECIMAL(10,2)."
            );
        }
    }

    private SaleResponse replayOrConflict(Sale sale, LogicalPayload payload) {
        List<ManualSaleLine> storedLines = saleRepositoryPort.findLinesBySaleId(sale.id());
        if (sale.source() != SaleSource.MANUAL_FONDA
                || !Objects.equals(normalizeObservations(sale.observaciones()), payload.observations())
                || !storedQuantities(storedLines).equals(payload.quantities())) {
            throw new ConflictException("La clave de idempotencia ya fue usada con otro payload.");
        }
        return SaleResponse.from(sale, storedLines);
    }

    private Map<Integer, Integer> storedQuantities(List<ManualSaleLine> lines) {
        Map<Integer, Integer> quantities = new LinkedHashMap<>();
        Set<Integer> duplicates = new HashSet<>();
        for (ManualSaleLine line : lines) {
            if (quantities.putIfAbsent(line.menuItemId(), line.quantity()) != null) {
                duplicates.add(line.menuItemId());
            }
        }
        return duplicates.isEmpty() ? quantities : Map.of();
    }

    private String normalizeIdempotencyKey(String key) {
        if (key == null) {
            throw new SaleValidationException("El header Idempotency-Key es obligatorio.");
        }
        String normalized = key.trim();
        if (normalized.isEmpty() || normalized.length() > 100
                || normalized.chars().anyMatch(character -> character < 0x20 || character > 0x7e)) {
            throw new SaleValidationException(
                    "Idempotency-Key debe tener entre 1 y 100 caracteres ASCII imprimibles.");
        }
        return normalized;
    }

    private String normalizeObservations(String observations) {
        return observations == null || observations.isBlank() ? null : observations.trim();
    }

    private record LogicalPayload(Map<Integer, Integer> quantities, String observations) {
    }
}
