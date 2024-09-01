package dev.dunglv202.hoaithuong.model.sheet.standard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SheetCellStyle {
    private Boolean bold;
    private Boolean italic;
    private RGBAColor textColor;
    private RGBAColor backgroundColor;
    private RGBAColor borderColor;
    private String verticalAlignment;
    private String horizontalAlignment;
    private Integer fontSize;

    public SheetCellStyle setBold(boolean bold) {
        this.bold = bold;
        return this;
    }

    public SheetCellStyle setItalic(boolean italic) {
        this.italic = italic;
        return this;
    }

    public SheetCellStyle setTextColor(RGBAColor textColor) {
        this.textColor = textColor;
        return this;
    }

    public SheetCellStyle setBackgroundColor(RGBAColor backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public SheetCellStyle setBorderColor(RGBAColor borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public SheetCellStyle setVerticalAlignment(String alignment) {
        this.verticalAlignment = alignment;
        return this;
    }

    public SheetCellStyle setHorizontalAlignment(String alignment) {
        this.horizontalAlignment = alignment;
        return this;
    }

    public SheetCellStyle setFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }
}
