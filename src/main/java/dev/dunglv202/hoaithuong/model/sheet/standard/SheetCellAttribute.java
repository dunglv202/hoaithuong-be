package dev.dunglv202.hoaithuong.model.sheet.standard;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class SheetCellAttribute {
    private Integer colspan;
    private Integer rowspan;
    private String hyperlink;

    public boolean isLink() {
        return hyperlink != null;
    }
}
