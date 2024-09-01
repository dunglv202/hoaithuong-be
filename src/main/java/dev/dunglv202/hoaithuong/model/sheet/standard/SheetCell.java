package dev.dunglv202.hoaithuong.model.sheet.standard;

import lombok.Getter;

@Getter
public class SheetCell {
    private Object value;
    private SheetCellStyle style;
    private SheetCellAttribute attribute;

    public SheetCell setStyle(SheetCellStyle style) {
        this.style = style;
        return this;
    }

    public SheetCell setAttribute(SheetCellAttribute attribute) {
        this.attribute = attribute;
        return this;
    }

    public SheetCell setValue(Object value) {
        this.value = value;
        return this;
    }
}
