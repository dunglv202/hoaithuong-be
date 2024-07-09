package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.model.ReportRange;
import dev.dunglv202.hoaithuong.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/export")
    public ResponseEntity<Resource> exportXlsx(ReportRange range) {
        Resource xlsx = this.reportService.exportXlsx(range);
        String filename = "Report-" + range.getMonth() + " - " + range.getYear();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename  + ".xlsx")
            .body(xlsx);
    }
}
