package dev.dunglv202.hoaithuong.entity;

import dev.dunglv202.hoaithuong.dto.ConfigsDTO;
import dev.dunglv202.hoaithuong.helper.SheetHelper;
import dev.dunglv202.hoaithuong.mapper.ConfigMapper;
import dev.dunglv202.hoaithuong.model.SheetInfo;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Accessors(chain = true)
public class Configuration extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    private String teacherCode;

    private String googleAccessToken;

    private String googleRefreshToken;

    /**
     * General report spreadsheet id
     */
    private String generalReportId;

    /**
     * General report sheet name
     */
    private String generalReportSheet;

    /**
     * Detail report spreadsheet id
     */
    private String detailReportId;

    /**
     * Detail report sheet name
     */
    private String detailReportSheet;

    /**
     * Google calendar id
     */
    private String calendarId;

    /**
     * OneDrive item ID where video uploaded
     */
    private String videoSource;

    /**
     * OneDrive item ID where video stored after attached to lectures
     */
    private String processedVideos;

    public Configuration mergeWith(ConfigsDTO configs) {
        ConfigMapper.INSTANCE.merge(this, configs);
        return this;
    }

    public SheetInfo getGeneralSheetInfo() {
        return new SheetInfo(generalReportId, generalReportSheet);
    }

    public SheetInfo getDetailSheetInfo() {
        return new SheetInfo(detailReportId, detailReportSheet);
    }

    public String getGeneralReportUrl() {
        return SheetHelper.bindToSpreadsheetURL(generalReportId);
    }

    public String getDetailReportUrl() {
        return SheetHelper.bindToSpreadsheetURL(detailReportId);
    }
}
