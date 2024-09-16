package dev.dunglv202.hoaithuong.service.impl;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleSpreadsheetService implements SpreadsheetService {
    private final AuthHelper authHelper;
    private final GoogleHelper googleHelper;

    @Override
    public SpreadsheetInfoDTO getSpreadsheetInfo(String spreadsheetId) {
        Spreadsheet spreadsheet = getSpreadsheetInfoBeyondUser(authHelper.getSignedUser(), spreadsheetId);
        return SpreadsheetInfoDTO.builder()
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

    private Spreadsheet getSpreadsheetInfoBeyondUser(User user, String spreadSheetId) {
        try {
            Sheets sheetService = googleHelper.getSheetService(user);
            return sheetService.spreadsheets().get(spreadSheetId).execute();
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND.value()) throw new ClientVisibleException("{spreadsheet.not_found}");
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED.value()) throw new ClientVisibleException("{google.auth.unauthenticated}");
            throw new RuntimeException("Could not get spreadsheet info", e);
        } catch (Exception e) {
            throw new RuntimeException("Could not get spreadsheet info", e);
        }
    }
}
