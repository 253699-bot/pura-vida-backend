package com.puravida.modules.reports.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.reports.application.dto.CreateWeeklyReportRequest;
import com.puravida.modules.reports.application.dto.WeeklyReportListItemResponse;
import com.puravida.modules.reports.application.dto.WeeklyReportSummary;
import com.puravida.modules.reports.application.port.in.CreateWeeklyReportPort;
import com.puravida.modules.reports.application.port.in.GetWeeklyReportSummaryPort;
import com.puravida.modules.reports.application.port.out.WeeklyReportRepositoryPort;
import com.puravida.modules.reports.application.port.out.WeeklyReportSnapshotCodecPort;
import com.puravida.modules.reports.domain.exception.WeeklyReportValidationException;
import com.puravida.modules.reports.domain.model.StoredWeeklyReport;
import com.puravida.modules.reports.domain.model.WeeklyReportRange;
import com.puravida.modules.users.domain.model.User;
import java.time.DayOfWeek;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateWeeklyReportUseCase implements CreateWeeklyReportPort {

    private static final int SNAPSHOT_VERSION = 1;
    private final WeeklyReportRepositoryPort repositoryPort;
    private final WeeklyReportSnapshotCodecPort snapshotCodecPort;
    private final GetWeeklyReportSummaryPort getSummaryPort;
    private final WeeklyReportRangeResolver rangeResolver;
    private final WeeklyReportAuthorizationService authorizationService;

    public CreateWeeklyReportUseCase(
            WeeklyReportRepositoryPort repositoryPort,
            WeeklyReportSnapshotCodecPort snapshotCodecPort,
            GetWeeklyReportSummaryPort getSummaryPort,
            WeeklyReportRangeResolver rangeResolver,
            WeeklyReportAuthorizationService authorizationService
    ) {
        this.repositoryPort = repositoryPort;
        this.snapshotCodecPort = snapshotCodecPort;
        this.getSummaryPort = getSummaryPort;
        this.rangeResolver = rangeResolver;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public WeeklyReportListItemResponse create(
            CreateWeeklyReportRequest request,
            AuthenticatedUser authenticatedUser
    ) {
        User actor = authorizationService.requireEncargada(authenticatedUser);
        if (request == null || request.weekStart() == null || request.weekStart().isBlank()) {
            throw new WeeklyReportValidationException("weekStart es obligatorio.");
        }
        WeeklyReportRange range = rangeResolver.resolve(request.weekStart());
        if (range.weekStart().getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new WeeklyReportValidationException("weekStart debe corresponder a un lunes.");
        }

        return createSnapshot(request.weekStart(), range, actor, authenticatedUser);
    }

    private WeeklyReportListItemResponse createSnapshot(
            String weekStart,
            WeeklyReportRange range,
            User actor,
            AuthenticatedUser authenticatedUser
    ) {
        WeeklyReportSummary summary = getSummaryPort.getSummary(weekStart.trim(), authenticatedUser);
        if (!Objects.equals(summary.semanaInicio(), range.weekStart())
                || !Objects.equals(summary.semanaFin(), range.weekEnd())) {
            throw new IllegalStateException("El resumen calculado devolvió un rango semanal inconsistente.");
        }
        long remoteOrders = summary.ventas().remotas().cantidad();
        if (remoteOrders > Integer.MAX_VALUE) {
            throw new WeeklyReportValidationException("El total de pedidos excede el límite soportado.");
        }
        Integer topDishId = summary.topPlatillos().isEmpty()
                ? null
                : summary.topPlatillos().get(0).platilloId();
        StoredWeeklyReport candidate = new StoredWeeklyReport(
                null,
                range.weekStart(),
                range.weekEnd(),
                (int) remoteOrders,
                summary.ventas().totalActivo(),
                topDishId,
                null,
                null,
                snapshotCodecPort.serialize(summary),
                SNAPSHOT_VERSION,
                actor.id(),
                summary.generadoEn()
        );
        return WeeklyReportListItemResponse.from(repositoryPort.create(candidate));
    }
}
