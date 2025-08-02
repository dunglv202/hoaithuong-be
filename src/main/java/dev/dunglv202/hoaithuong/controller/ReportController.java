package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.constant.TutorClassType;
import dev.dunglv202.hoaithuong.dto.ConfirmationDTO;
import dev.dunglv202.hoaithuong.dto.ReportDTO;
import dev.dunglv202.hoaithuong.model.ReportRange;
import dev.dunglv202.hoaithuong.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ReportDTO getReport(@Valid ReportRange range, TutorClassType classType, String keyword) {
        return reportService.getReport(range, classType, keyword);
    }

    @GetMapping("/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadXlsx(@Valid ReportRange range) {
        Resource xlsx = this.reportService.downloadXlsx(range);
        String filename = "Report-" + range.getMonth() + "-" + range.getYear();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename  + ".xlsx")
            .body(xlsx);
    }

    @PostMapping("/export")
    @PreAuthorize("isAuthenticated()")
    public void exportReport(@Valid ReportRange range) {
        this.reportService.exportGoogleSheet(range);
    }

    @PostMapping("/confirmations")
    @PreAuthorize("isAuthenticated()")
    public String uploadConfirmation(@RequestParam int month, @RequestParam int year, @Valid ConfirmationDTO confirmationDTO) {
        return this.reportService.uploadConfirmation(year, month, confirmationDTO);
    }

    @DeleteMapping("/confirmations")
    @PreAuthorize("isAuthenticated()")
    public void deleteConfirmation(@RequestParam int year, @RequestParam int month, @RequestParam long studentId) {
        this.reportService.deleteConfirmation(year, month, studentId);
    }
}
