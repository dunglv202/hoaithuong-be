package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.dto.ReportDTO;
import dev.dunglv202.hoaithuong.model.ReportRange;
import dev.dunglv202.hoaithuong.service.impl.ReportServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportServiceImpl reportService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ReportDTO getReport(@Valid ReportRange range) {
        return reportService.getReport(range);
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
}
