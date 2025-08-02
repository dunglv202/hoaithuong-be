package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.constant.TutorClassType;
import dev.dunglv202.hoaithuong.dto.ConfirmationDTO;
import dev.dunglv202.hoaithuong.dto.ReportDTO;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.model.ReportRange;
import org.springframework.core.io.Resource;

public interface ReportService {
    Resource downloadXlsx(ReportRange range);

    ReportDTO getReport(ReportRange range, TutorClassType classType, String keyword);

    void exportGoogleSheet(ReportRange range);

    void exportGoogleSheet(User user, ReportRange range);

    String uploadConfirmation(int year, int month, ConfirmationDTO confirmationDTO);

    void createIfNotExist(User teacher, int year, int month);

    void deleteConfirmation(int year, int month, long studentId);
}
