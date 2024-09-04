package dev.dunglv202.hoaithuong.helper;

import com.google.api.services.sheets.v4.model.*;
import dev.dunglv202.hoaithuong.model.sheet.GoogleSheetConverter;
import dev.dunglv202.hoaithuong.model.sheet.standard.SheetRange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SheetHelper {
    public static final String SPREADSHEET_URL_FORM = "https://docs.google.com/spreadsheets/d/%s";
    public static final Pattern SPREADSHEET_URL_PATTERN = Pattern.compile(
        "https://docs\\.google\\.com/spreadsheets/d/([a-zA-Z0-9-_]+).*"
    );

    /**
     * Write data from {@code src} to {@code dest} of sheet
     */
    public static BatchUpdateSpreadsheetRequest makeUpdateRequest(SheetRange src, GridRange dest) {
        var converter = new GoogleSheetConverter();

        // prepare layout, merge cells
        List<MergeCellsRequest> mergeRequests = new ArrayList<>();
        for (int i = 0; i < src.getRows().size(); i++) {
            var row = src.getRow(i);
            for (int j = 0; j < row.getCells().size(); j++) {
                var cell = row.getCell(j);
                var attr = cell.getAttribute();
                if (attr != null && (attr.getColspan() != null || attr.getRowspan() != null)) {
                    // add merge cell req
                    int startRow = dest.getStartRowIndex() + i, startCol = dest.getStartColumnIndex() + j;
                    GridRange rangeToMerge = dest.clone()
                        .setStartRowIndex(startRow)
                        .setEndRowIndex(startRow + 1 + Optional.ofNullable(attr.getRowspan()).orElse(0))
                        .setStartColumnIndex(startCol)
                        .setEndColumnIndex(startCol + Optional.ofNullable(attr.getColspan()).orElse(0));
                    mergeRequests.add(new MergeCellsRequest()
                        .setRange(rangeToMerge)
                        .setMergeType("MERGE_ALL")
                    );
                }
            }
        }

        // write cells
        var updateRequest = new UpdateCellsRequest().setFields("*")
            .setRange(dest)
            .setRows(src.getRows().stream().map(converter::convertRow).toList());

        // make request batch
        List<Request> reqs = new ArrayList<>(mergeRequests.stream().map((mr) -> new Request().setMergeCells(mr)).toList());
        reqs.add(new Request().setUpdateCells(updateRequest));

        return new BatchUpdateSpreadsheetRequest().setRequests(reqs);
    }

    /**
     * Extract spreadsheet id, if {@code spreadsheetUrl} is null or blank string then return {@code null}
     */
    public static String extractSpreadsheetId(String spreadsheetUrl) {
        if (spreadsheetUrl == null || spreadsheetUrl.isBlank()) return null;
        Matcher matcher = SPREADSHEET_URL_PATTERN.matcher(spreadsheetUrl);
        if (!matcher.matches()) {
            throw new RuntimeException("{spreadsheet.url.bad_malformed}: " + spreadsheetUrl);
        };
        return matcher.group(1);
    }

    /**
     * Converts a zero-based column index to an Excel column letter.
     *
     * @param columnIndex Zero-based index of the column (0 for A, 1 for B, etc.).
     * @return Excel column letter(s) corresponding to the given index.
     */
    public static String columnIndexToLetter(int columnIndex) {
        StringBuilder columnName = new StringBuilder();
        while (columnIndex >= 0) {
            int mod = columnIndex % 26;
            columnName.insert(0, (char) (mod + 'A'));
            columnIndex = (columnIndex / 26) - 1;
        }
        return columnName.toString();
    }

    public static String bindToSpreadsheetURL(String spreadsheetId) {
        return spreadsheetId == null ? null : String.format(SPREADSHEET_URL_FORM, spreadsheetId);
    }
}
