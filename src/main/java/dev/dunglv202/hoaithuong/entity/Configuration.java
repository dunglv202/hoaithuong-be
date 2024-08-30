package dev.dunglv202.hoaithuong.entity;

import dev.dunglv202.hoaithuong.dto.ConfigsDTO;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class Configuration extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    private String googleAccessToken;

    private String googleRefreshToken;

    private String reportSheetId;

    public void mergeWith(ConfigsDTO configs) {
        this.reportSheetId = configs.getReportSheetId();
    }
}
