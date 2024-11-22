package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.ConfirmationDTO;
import dev.dunglv202.hoaithuong.dto.ReportDTO;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.model.ReportRange;
import org.springframework.core.io.Resource;

public interface ReportService {
    Resource downloadXlsx(ReportRange range);

    ReportDTO getReport(ReportRange range);

    void exportGoogleSheet(ReportRange range);

    void exportGoogleSheet(User user, ReportRange range);

    void uploadConfirmation(int year, int month, ConfirmationDTO confirmationDTO);
}
