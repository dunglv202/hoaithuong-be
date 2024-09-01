package dev.dunglv202.hoaithuong.model.sheet.standard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SheetCellStyle {
    private boolean bold;
    private boolean italic;
    private RGBAColor textColor;
    private RGBAColor backgroundColor;
    private RGBAColor borderColor;

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
}
