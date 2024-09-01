package dev.dunglv202.hoaithuong.helper;

import com.google.api.services.sheets.v4.model.*;
import dev.dunglv202.hoaithuong.model.sheet.GoogleSheetConverter;
import dev.dunglv202.hoaithuong.model.sheet.standard.SheetRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SheetHelper {
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
}
