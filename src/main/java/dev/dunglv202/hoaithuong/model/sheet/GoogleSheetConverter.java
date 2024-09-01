package dev.dunglv202.hoaithuong.model.sheet;

import com.google.api.services.sheets.v4.model.*;
import dev.dunglv202.hoaithuong.model.sheet.standard.RGBAColor;
import dev.dunglv202.hoaithuong.model.sheet.standard.SheetCell;
import dev.dunglv202.hoaithuong.model.sheet.standard.SheetCellStyle;
import dev.dunglv202.hoaithuong.model.sheet.standard.SheetRow;

import javax.annotation.Nullable;

/**
 * Convert from sheet standard in {@link dev.dunglv202.hoaithuong.model.sheet.standard} to Google Sheet models
 */
public class GoogleSheetConverter {
    public Color convertColor(@Nullable RGBAColor color) {
        if (color == null) return null;

        return new Color()
            .setRed((float) color.getRed() / 255)
            .setGreen((float) color.getGreen() / 255)
            .setBlue((float) color.getBlue() / 255)
            .setAlpha(color.getAlpha());
    }

    public CellFormat convertStyle(@Nullable SheetCellStyle style) {
        if (style == null) return null;

        TextFormat textFormat = new TextFormat()
            .setBold(style.isBold())
            .setItalic(style.isItalic())
            .setForegroundColor(convertColor(style.getTextColor()));
        Border border = new Border().setColor(convertColor(style.getBorderColor())).setStyle("SOLID");

        return new CellFormat()
            .setTextFormat(textFormat)
            .setBackgroundColor(convertColor(style.getBackgroundColor()))
            .setBorders(new Borders().setTop(border).setBottom(border).setLeft(border).setRight(border));
    }

    public CellData convertCell(SheetCell cell) {
        ExtendedValue value = new ExtendedValue();

        if (cell.getValue() instanceof Boolean) {
            value.setBoolValue((Boolean) cell.getValue());
        } else if (cell.getValue() instanceof Number) {
            value.setNumberValue(Double.parseDouble(cell.getValue().toString()));
        } else {
            value.setStringValue(cell.getValue() != null ? cell.getValue().toString() : null);
        }

        return new CellData()
            .setUserEnteredValue(value)
            .setUserEnteredFormat(convertStyle(cell.getStyle()));
    }

    public RowData convertRow(SheetRow row) {
        return new RowData().setValues(row.getCells().stream().map(this::convertCell).toList());
    }
}
