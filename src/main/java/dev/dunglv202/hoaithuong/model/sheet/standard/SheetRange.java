package dev.dunglv202.hoaithuong.model.sheet.standard;

import com.google.api.services.sheets.v4.model.ValueRange;
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

    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return rows.stream().map(r -> r.getCells().size()).max(Integer::compareTo).orElse(0);
    }

    public void mergeWith(ValueRange oldValues) {
        if (oldValues.getValues() == null) return;
        for (int i = 0; i < Math.min(this.rows.size(), oldValues.getValues().size()); i++) {
            rows.get(i).mergeWith(oldValues.getValues().get(i));
        }
    }
}
