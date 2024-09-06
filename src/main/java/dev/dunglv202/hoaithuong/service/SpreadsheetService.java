package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.SpreadsheetInfoDTO;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.model.SheetInfo;

public interface SpreadsheetService {
    SpreadsheetInfoDTO getSpreadsheetInfo(String spreadsheetId);

    boolean isValidSheet(User user, SheetInfo sheet);
}
