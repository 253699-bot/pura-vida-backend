package com.puravida.modules.reports.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.reports.application.dto.WeeklyReportPdf;
import com.puravida.modules.reports.application.dto.WeeklyReportSummary;
import com.puravida.modules.reports.application.port.in.GenerateStoredWeeklyReportPdfPort;
import com.puravida.modules.reports.application.port.out.WeeklyReportPdfGeneratorPort;
import com.puravida.modules.reports.application.port.out.WeeklyReportRepositoryPort;
import com.puravida.modules.reports.application.port.out.WeeklyReportSnapshotCodecPort;
import com.puravida.modules.reports.domain.model.StoredWeeklyReport;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenerateStoredWeeklyReportPdfUseCase implements GenerateStoredWeeklyReportPdfPort {

    private final WeeklyReportRepositoryPort repositoryPort;
    private final WeeklyReportSnapshotCodecPort snapshotCodecPort;
    private final WeeklyReportPdfGeneratorPort pdfGeneratorPort;
    private final WeeklyReportAuthorizationService authorizationService;

    public GenerateStoredWeeklyReportPdfUseCase(
            WeeklyReportRepositoryPort repositoryPort,
            WeeklyReportSnapshotCodecPort snapshotCodecPort,
            WeeklyReportPdfGeneratorPort pdfGeneratorPort,
            WeeklyReportAuthorizationService authorizationService
    ) {
        this.repositoryPort = repositoryPort;
        this.snapshotCodecPort = snapshotCodecPort;
        this.pdfGeneratorPort = pdfGeneratorPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyReportPdf generate(Integer reportId, AuthenticatedUser authenticatedUser) {
        authorizationService.requireEncargada(authenticatedUser);
        StoredWeeklyReport stored = repositoryPort.findById(reportId)
                .orElseThrow(() -> new NotFoundException("El reporte semanal no existe."));
        WeeklyReportSummary summary = snapshotCodecPort.deserialize(
                stored.resumenJson(), stored.versionFormato()
        );
        if (!Objects.equals(summary.semanaInicio(), stored.semanaInicio())
                || !Objects.equals(summary.semanaFin(), stored.semanaFin())) {
            throw new ConflictException("El rango del snapshot no coincide con el reporte almacenado.");
        }
        return new WeeklyReportPdf(pdfGeneratorPort.generate(summary), stored.semanaInicio());
    }
}
