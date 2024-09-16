package dev.dunglv202.hoaithuong.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.dunglv202.hoaithuong.helper.SheetHelper;
import dev.dunglv202.hoaithuong.validator.SpreadsheetURL;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigsDTO {
    private String calendarId;

    @SpreadsheetURL(message = "{report.general.invalid}")
    private String generalReportUrl;

    private String generalReportSheet;

    @SpreadsheetURL(message = "{report.detail.invalid}")
    private String detailReportUrl;

    private String detailReportSheet;

    @JsonIgnore
    public String getGeneralReportId() {
        return SheetHelper.extractSpreadsheetId(generalReportUrl);
    }

    @JsonIgnore
    public String getDetailReportId() {
        return SheetHelper.extractSpreadsheetId(detailReportUrl);
    }
}
