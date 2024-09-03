package dev.dunglv202.hoaithuong.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class SheetInfo {
    private String spreadsheetId;
    private String sheetName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SheetInfo sheetInfo = (SheetInfo) o;
        return Objects.equals(spreadsheetId, sheetInfo.spreadsheetId)
            && Objects.equals(sheetName, sheetInfo.sheetName);
    }
}
