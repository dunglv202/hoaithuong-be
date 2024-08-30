package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.ReportDTO;
import dev.dunglv202.hoaithuong.dto.SheetExportResultDTO;
import dev.dunglv202.hoaithuong.model.ReportRange;
import org.springframework.core.io.Resource;

public interface ReportService {
    Resource downloadXlsx(ReportRange range);

    ReportDTO getReport(ReportRange range);

    SheetExportResultDTO exportGoogleSheet(ReportRange range);
}
