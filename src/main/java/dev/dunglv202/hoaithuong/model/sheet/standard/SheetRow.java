package dev.dunglv202.hoaithuong.model.sheet.standard;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SheetRow {
    private final List<SheetCell> cells = new ArrayList<>();

    public SheetCell addCell() {
        SheetCell cell = new SheetCell();
        cells.add(cell);
        return cell;
    }

    public SheetCell getCell(int index) {
        return cells.get(index);
    }

    public void mergeWith(List<Object> cells) {
        for (int i = 0; i < Math.min(this.cells.size(), cells.size()); i++) {
            Object value = this.cells.get(i).getValue();
            if (value == null || (value instanceof String str && str.isBlank())) {
                // if cell empty, keep old value
                this.cells.get(i).setValue(cells.get(i));
            }
        }
    }
}
