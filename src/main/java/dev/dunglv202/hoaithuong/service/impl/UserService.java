package dev.dunglv202.hoaithuong.service.impl;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import dev.dunglv202.hoaithuong.dto.DetailProfileDTO;
import dev.dunglv202.hoaithuong.dto.UpdatedDetailProfileDTO;
import dev.dunglv202.hoaithuong.dto.UserInfoDTO;
import dev.dunglv202.hoaithuong.entity.Configuration;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.helper.GoogleHelper;
import dev.dunglv202.hoaithuong.model.AppUser;
import dev.dunglv202.hoaithuong.model.SheetInfo;
import dev.dunglv202.hoaithuong.repository.UserRepository;
import dev.dunglv202.hoaithuong.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
    private final AuthHelper authHelper;
    private final UserRepository userRepository;
    private final ConfigService configService;
    private final GoogleHelper googleHelper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException(username));
        return new AppUser(user);
    }

    public UserInfoDTO getSignedUserInfo() {
        User signedUser = userRepository.findById(authHelper.getSignedUser().getId())
            .orElseThrow();
        return new UserInfoDTO(signedUser);
    }

    public DetailProfileDTO getDetailProfile() {
        User user = userRepository.findById(authHelper.getSignedUser().getId()).orElseThrow();
        Configuration configs = configService.getConfigsByUser(user);
        return new DetailProfileDTO(user, configs);
    }

    public void updateDetailProfile(UpdatedDetailProfileDTO updateDTO) {
        User signedUser = authHelper.getSignedUser();
        Configuration configs = configService.getConfigsByUser(signedUser);

        // check valid sheet
        SheetInfo generalReport = new SheetInfo(
            updateDTO.getConfigs().getGeneralReportId(),
            updateDTO.getConfigs().getGeneralReportSheet()
        );
        if (!generalReport.equals(configs.getGeneralSheetInfo()) && !isValidSheet(generalReport)) {
            throw new ClientVisibleException("{report.general.spreadsheet.invalid}");
        }
        SheetInfo detailReport = new SheetInfo(
            updateDTO.getConfigs().getDetailReportId(),
            updateDTO.getConfigs().getDetailReportSheet()
        );
        if (!detailReport.equals(configs.getDetailSheetInfo()) && !isValidSheet(detailReport)) {
            throw new ClientVisibleException("{report.detail.spreadsheet.invalid}");
        }

        configs.mergeWith(updateDTO.getConfigs());
        configService.saveConfigs(configs);
    }

    private boolean isValidSheet(SheetInfo sheet) {
        try {
            if (sheet.getSpreadsheetId() == null && (sheet.getSheetName() == null || sheet.getSheetName().isBlank())) {
                return true;
            }
            Sheets sheetService = googleHelper.getSheetService(authHelper.getSignedUser());
            Spreadsheet spreadsheet = sheetService.spreadsheets().get(sheet.getSpreadsheetId()).execute();
            return spreadsheet.getSheets().stream().anyMatch(s -> s.getProperties().getTitle().equals(sheet.getSheetName()));
        } catch (ClientVisibleException e) {
            throw new ClientVisibleException("{config.google_sheet_id.could_not_be_updated}");
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND.value()) return false;
            log.error("Could not validate sheet id", e);
        } catch (Exception e) {
            log.error("Could not validate sheet id", e);
        }
        return false;
    }
}
