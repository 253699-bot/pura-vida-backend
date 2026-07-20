package com.puravida.modules.reports.application.port.out;

import com.puravida.modules.reports.application.dto.WeeklyReportSummary;

public interface WeeklyReportSnapshotCodecPort {
    String serialize(WeeklyReportSummary summary);
    WeeklyReportSummary deserialize(String json, Integer version);
}
