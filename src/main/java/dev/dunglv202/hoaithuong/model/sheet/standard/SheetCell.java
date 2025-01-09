package dev.dunglv202.hoaithuong.model.sheet.standard;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class SheetCell {
    private Object value = null;
    private SheetCellStyle style = new SheetCellStyle();
    private SheetCellAttribute attribute = new SheetCellAttribute();
}
