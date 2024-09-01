package dev.dunglv202.hoaithuong.model.sheet.standard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SheetCellAttribute {
    private Integer colspan;
    private Integer rowspan;

    public SheetCellAttribute setColspan(int colspan) {
        this.colspan = colspan;
        return this;
    }

    public SheetCellAttribute setRowspan(int rowspan) {
        this.rowspan = rowspan;
        return this;
    }
}
