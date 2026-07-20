package com.puravida.modules.reports.infrastructure.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puravida.modules.reports.application.dto.WeeklyReportSummary;
import com.puravida.modules.reports.application.port.out.WeeklyReportSnapshotCodecPort;
import com.puravida.shared.domain.exception.ConflictException;
import org.springframework.stereotype.Component;

@Component
public class JacksonWeeklyReportSnapshotCodec implements WeeklyReportSnapshotCodecPort {

    public static final int CURRENT_VERSION = 1;
    private final ObjectMapper objectMapper;

    public JacksonWeeklyReportSnapshotCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String serialize(WeeklyReportSummary summary) {
        try {
            return objectMapper.writeValueAsString(summary);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No se pudo serializar el reporte semanal.", exception);
        }
    }

    @Override
    public WeeklyReportSummary deserialize(String json, Integer version) {
        if (version == null || version != CURRENT_VERSION) {
            throw new ConflictException("La version del reporte almacenado no es compatible.");
        }
        if (json == null || json.isBlank()) {
            throw new ConflictException("El reporte historico no contiene un snapshot disponible.");
        }
        try {
            return objectMapper.readValue(json, WeeklyReportSummary.class);
        } catch (JsonProcessingException exception) {
            throw new ConflictException("El snapshot del reporte historico no es valido.");
        }
    }
}
