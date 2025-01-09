package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.SpreadsheetInfoDTO;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.model.SheetInfo;
import org.springframework.core.io.Resource;

public interface SpreadsheetService {
    SpreadsheetInfoDTO getSpreadsheetInfo(String spreadsheetId);

    boolean isValidSheet(User user, SheetInfo sheet);

    Resource downloadSpreadsheet(String spreadsheetId);
}
