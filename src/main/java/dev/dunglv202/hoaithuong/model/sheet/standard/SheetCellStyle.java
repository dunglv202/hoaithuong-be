package dev.dunglv202.hoaithuong.model.sheet.standard;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class SheetCellStyle {
    private Boolean bold;
    private Boolean italic;
    private RGBAColor textColor;
    private RGBAColor backgroundColor;
    private RGBAColor borderColor;
    private String verticalAlignment;
    private String horizontalAlignment;
    private Integer fontSize;
    private Boolean wrapText;
}
