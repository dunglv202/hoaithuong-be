package dev.dunglv202.hoaithuong.service.impl;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.services.drive.model.File;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import dev.dunglv202.hoaithuong.dto.SpreadsheetInfoDTO;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.helper.GoogleHelper;
import dev.dunglv202.hoaithuong.model.SheetInfo;
import dev.dunglv202.hoaithuong.service.SpreadsheetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleSpreadsheetService implements SpreadsheetService {
    @Value("${drive.shared-folder}")
    private String sharedFolderId;

    private final AuthHelper authHelper;
    private final GoogleHelper googleHelper;
    private final GoogleDriveService googleDriveService;

    @Override
    public SpreadsheetInfoDTO getSpreadsheetInfo(String spreadsheetId) {
        Spreadsheet spreadsheet = getSpreadsheetInfoBeyondUser(authHelper.getSignedUserRef(), spreadsheetId);
        return SpreadsheetInfoDTO.builder()
            .id(spreadsheetId)
            .name(spreadsheet.getProperties().getTitle())
            .sheets(spreadsheet.getSheets().stream().map(sheet -> sheet.getProperties().getTitle()).toList())
            .build();
    }

    @Override
    public boolean isValidSheet(User user, SheetInfo sheet) {
        try {
            if (sheet.getSpreadsheetId() == null && (sheet.getSheetName() == null || sheet.getSheetName().isBlank())) {
                return true;
            }
            Spreadsheet spreadsheet = getSpreadsheetInfoBeyondUser(user, sheet.getSpreadsheetId());
            return spreadsheet.getSheets().stream().anyMatch(s -> s.getProperties().getTitle().equals(sheet.getSheetName()));
        } catch (ClientVisibleException e) {
            throw new ClientVisibleException("{config.google_sheet_id.could_not_be_updated}");
        } catch (Exception e) {
            log.error("Could not validate sheet id", e);
        }
        return false;
    }

    @Override
    public void pushToSharedArea(String spreadsheetId) {
        User user = authHelper.getSignedUser();
        String copyName = user.getEmail() + " - " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        File file = googleDriveService.makeCopy(user, spreadsheetId, copyName);
        googleDriveService.moveToFolder(user, file, sharedFolderId);
    }

    private Spreadsheet getSpreadsheetInfoBeyondUser(User user, String spreadSheetId) {
        try {
            Sheets sheetService = googleHelper.getSheetService(user);
            return sheetService.spreadsheets().get(spreadSheetId).execute();
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == HttpStatusCodes.STATUS_CODE_NOT_FOUND) throw new ClientVisibleException("{spreadsheet.not_found}");
            if (e.getStatusCode() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED) throw new ClientVisibleException("{google.auth.unauthenticated}");
            throw new RuntimeException("Could not get spreadsheet info", e);
        } catch (Exception e) {
            throw new RuntimeException("Could not get spreadsheet info", e);
        }
    }
}
