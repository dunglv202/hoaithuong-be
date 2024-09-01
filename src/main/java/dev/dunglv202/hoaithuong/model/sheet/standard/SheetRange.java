package dev.dunglv202.hoaithuong.model.sheet.standard;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SheetRange {
    private final List<SheetRow> rows = new ArrayList<>();

    public SheetRow addRow() {
        SheetRow row = new SheetRow();
        rows.add(row);
        return row;
    }

    public SheetRow getRow(int offset) {
        return rows.get(offset);
    }

    public SheetRange union(SheetRange another) {
        this.rows.addAll(another.getRows());
        return this;
    }

    public List<List<Object>> getValues() {
        return rows.stream().map(SheetRow::getValues).toList();
    }

    public SheetRange addEmptyRow() {
        addRow();
        return this;
    }
}
