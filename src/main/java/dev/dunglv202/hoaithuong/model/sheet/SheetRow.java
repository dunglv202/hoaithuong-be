package dev.dunglv202.hoaithuong.model.sheet;

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

    public List<Object> getValues() {
        return cells.stream().map(SheetCell::getValue).toList();
    }
}
