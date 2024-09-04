package dev.dunglv202.hoaithuong.service.impl;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import dev.dunglv202.hoaithuong.dto.ReportDTO;
import dev.dunglv202.hoaithuong.entity.Configuration;
import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.helper.DateTimeFmt;
import dev.dunglv202.hoaithuong.helper.GoogleHelper;
import dev.dunglv202.hoaithuong.helper.SheetHelper;
import dev.dunglv202.hoaithuong.model.LectureCriteria;
import dev.dunglv202.hoaithuong.model.ReportRange;
import dev.dunglv202.hoaithuong.model.sheet.standard.*;
import dev.dunglv202.hoaithuong.repository.LectureRepository;
import dev.dunglv202.hoaithuong.repository.ScheduleRepository;
import dev.dunglv202.hoaithuong.service.ConfigService;
import dev.dunglv202.hoaithuong.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static dev.dunglv202.hoaithuong.model.LectureCriteria.*;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final LectureRepository lectureRepository;
    private final ScheduleRepository scheduleRepository;
    private final AuthHelper authHelper;
    private final GoogleHelper googleHelper;
    private final ConfigService configService;

    @Override
    public Resource downloadXlsx(ReportRange range) {
        try (Workbook workbook = generateReportFile(range)) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ReportDTO getReport(ReportRange range) {
        Specification<Lecture> criteria = Specification.allOf(
            inRange(range),
            sortByStartTime(Sort.Direction.DESC)
        );

        List<Lecture> lectures = lectureRepository.findAll(LectureCriteria.joinFetch().and(criteria));
        ReportDTO report = new ReportDTO(lectures);
        int estimatedTotal = scheduleRepository.getEstimatedTotalInRange(range.getFrom(), range.getTo());
        report.setEstimatedTotal(estimatedTotal);

        return report;
    }

    @Override
    public void exportGoogleSheet(ReportRange range) {
        User signedUser = authHelper.getSignedUser();
        exportGoogleSheet(signedUser, range);
    }

    @Override
    public void exportGoogleSheet(User user, ReportRange range) {
        try {
            Configuration config = configService.getConfigsByUser(user);
            Sheets sheetsService = googleHelper.getSheetService(user);
            List<Lecture> lectures = getLecturesForReport(range);

            exportDetailToGgSheet(sheetsService, range, lectures, config);
            exportGeneralToGgSheet(sheetsService, lectures, config);
        } catch (ClientVisibleException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void exportDetailToGgSheet(Sheets sheetsService, ReportRange range, List<Lecture> lectures, Configuration config) throws IOException {
        if (config.getDetailReportId() == null) {
            throw new ClientVisibleException("{export.google_sheet_id.required}");
        }
        Spreadsheet spreadsheet = sheetsService.spreadsheets().get(config.getDetailReportId()).execute();
        String reportSheetName = config.getDetailReportSheet();
        var reportSheet = spreadsheet.getSheets().stream()
            .filter(sheet -> reportSheetName.equals(sheet.getProperties().getTitle()))
            .findFirst();
        Integer reportSheetId = reportSheet.orElseThrow().getProperties().getSheetId();

        // calculate report position
        int reportOffset = 0;
        List<List<Object>> values = sheetsService.spreadsheets().values().get(
            config.getDetailReportId(),
            String.format("%s!A:A", reportSheetName)
        ).execute().getValues();
        if (values != null) {
            String detailReportTitle = getDetailReportTitle(range.getMonth(), range.getYear());
            reportOffset = values.stream().map(row -> row.isEmpty() ? null : row.get(0))
                .toList()
                .indexOf(detailReportTitle) - 1;
            if (reportOffset < 0) reportOffset = values.size() + 3;
        }

        // generate report data
        SheetRange data = generateDetailReportData(lectures);

        // update sheet
        var gridRange = new GridRange().setSheetId(reportSheetId)
            .setStartRowIndex(reportOffset)
            .setStartColumnIndex(0);
        sheetsService.spreadsheets().batchUpdate(
            spreadsheet.getSpreadsheetId(),
            SheetHelper.makeUpdateRequest(data, gridRange)
        ).execute();
    }

    private void exportGeneralToGgSheet(Sheets sheetsService, List<Lecture> lectures, Configuration config) throws IOException {
        String spreadsheetId = config.getGeneralReportId();
        String sheetName = config.getGeneralReportSheet();
        Integer sheetId = sheetsService.spreadsheets().get(spreadsheetId).execute().getSheets().stream()
            .filter(s -> sheetName.equals(s.getProperties().getTitle()))
            .findFirst().orElseThrow(() -> new ClientVisibleException("{report.general.sheet_not_exist}"))
            .getProperties().getSheetId();

        // get row indexes of each classes
        List<List<Object>> codeValues = sheetsService.spreadsheets().values()
            .get(spreadsheetId, sheetName + "!A:A").execute().getValues();
        Set<String> classCodes = lectures.stream().map(lec -> lec.getTutorClass().getCode()).collect(Collectors.toSet());
        Map<String, Integer> classRowIndex = new HashMap<>();
        for (int i=0; i < codeValues.size(); i++) {
            List<Object> row = codeValues.get(i);
            if (!row.isEmpty() && classCodes.contains((String) row.get(0))) {
                classRowIndex.put((String) row.get(0), i);
            }
            if (classRowIndex.size() == classCodes.size()) {
                // enough, no need finding more
                break;
            }
        }

        // get lecture number column index
        List<Integer> lectureNoColumnIndexes = new ArrayList<>();
        List<List<Object>> headerValues = sheetsService.spreadsheets().values()
            .get(spreadsheetId, sheetName + "!1:1").execute().getValues();
        if (headerValues.isEmpty()) throw new ClientVisibleException("{report.general.sheet.no_header}");
        for (int i=0; i < headerValues.get(0).size(); i++) {
            String headerValue = (String) headerValues.get(0).get(i);
            if (headerValue != null && headerValue.matches("^Buổi\\s*\\d+$")) {
                lectureNoColumnIndexes.add(i);
            }
        }

        // generate report data & write to sheet
        var batchUpdateRequest = new BatchUpdateSpreadsheetRequest().setRequests(new ArrayList<>());
        groupByClass(lectures).forEach((tutorClass, classLectures) -> {
            String classCode = tutorClass.getCode();
            int rowIndex = Optional.ofNullable(classRowIndex.get(classCode)).orElseThrow(
                () -> new ClientVisibleException("{report.general.class_code_not_found} : " + classCode)
            );
            classLectures.forEach(lec -> {
                int colIndex = lectureNoColumnIndexes.get(lec.getLectureNo() - 1);
                GridRange range = new GridRange().setSheetId(sheetId)
                    .setStartRowIndex(rowIndex)
                    .setEndRowIndex(rowIndex + 1)
                    .setStartColumnIndex(colIndex)
                    .setEndColumnIndex(colIndex + 1);
                CellData cellData = new CellData().setUserEnteredValue(
                    new ExtendedValue().setStringValue(DateTimeFmt.D_M_YYYY.format(lec.getStartTime()) + "\n" + lec.getGeneratedCode())
                );
                batchUpdateRequest.getRequests().add(new Request().setUpdateCells(
                    new UpdateCellsRequest().setFields("*").setRange(range).setRows(List.of(new RowData().setValues(List.of(cellData))))
                ));
            });
        });
        sheetsService.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute();
    }

    private List<Lecture> getLecturesForReport(ReportRange range) {
        return lectureRepository.findAll(joinFetch().and(inRange(range)).and(sortByStartTime(Sort.Direction.ASC)));
    }

    private SheetRange generateGeneralReportData(List<Lecture> lectures) {
        SheetRange range = new SheetRange();
        Map<TutorClass, List<Lecture>> classWithLectures = groupByClass(lectures);

        SheetRow header = range.addRow();
        header.addCell().setValue("Mã");
        header.addCell().setValue("Tên");
        int maxNumOfLecture = 0;

        // write record for each class
        for (var entry : classWithLectures.entrySet()) {
            SheetRow row = range.addRow();
            TutorClass tutorClass = entry.getKey();
            List<Lecture> classLectures = entry.getValue();

            SheetCell code = row.addCell();
            code.setValue(tutorClass.getCode());

            SheetCell student = row.addCell();
            student.setValue(tutorClass.getStudent().getName());

            classLectures.forEach((lec) -> {
                SheetCell lecture = row.addCell();
                String cellValue = DateTimeFmt.D_M_YYYY.format(lec.getStartTime()) + "\n" + lec.getGeneratedCode();
                lecture.setValue(cellValue);
            });

            maxNumOfLecture = Math.max(maxNumOfLecture, classLectures.size());
        }

        // write missing header
        for (int i = 0; i < maxNumOfLecture; i++) {
            header.addCell().setValue("Buổi " + (i + 1));
        }

        return range;
    }

    private SheetRange generateDetailReportData(List<Lecture> lectures) {
        SheetRange range = new SheetRange();
        SheetRow header = range.addRow();
        var headerStyle = new SheetCellStyle()
            .setBold(true)
            .setBackgroundColor(new RGBAColor(213, 166, 189, 1f));
        header.addCell().setValue("STT").setStyle(headerStyle);
        header.addCell().setValue("Ngày").setStyle(headerStyle);
        header.addCell().setValue("Học viên").setStyle(headerStyle);
        header.addCell().setValue("Lớp").setStyle(headerStyle);
        header.addCell().setValue("Giờ học").setStyle(headerStyle);
        header.addCell().setValue("Buổi").setStyle(headerStyle);
        header.addCell().setValue("Nội dung").setStyle(headerStyle);
        header.addCell().setValue("Nhận xét").setStyle(headerStyle);
        header.addCell().setValue("Thời lượng (phút)").setStyle(headerStyle);
        header.addCell().setValue("Số tiền (1000đ)").setStyle(headerStyle);

        SheetCellStyle reportMonthHeaderStyle = new SheetCellStyle().setBold(true)
            .setFontSize(15)
            .setBackgroundColor(new RGBAColor(255, 255, 0, 1))
            .setHorizontalAlignment("CENTER");
        Lecture firstLecture = lectures.isEmpty() ? null : lectures.get(0);
        String title = firstLecture != null
            ? getDetailReportTitle(firstLecture.getStartTime().getMonthValue(), firstLecture.getStartTime().getYear())
            : null;
        range.addRow().addCell()
            .setValue(title)
            .setAttribute(new SheetCellAttribute().setColspan(10))
            .setStyle(reportMonthHeaderStyle);

        var bodyStyle = new SheetCellStyle()
            .setBorderColor(new RGBAColor(0, 0, 0, 1));
        for (int i = 0; i < lectures.size(); i++) {
            Lecture lecture = lectures.get(i);

            SheetRow row = range.addRow();
            TutorClass tutorClass = lecture.getTutorClass();

            SheetCell cell = row.addCell();
            cell.setValue(i+1);

            SheetCell date = row.addCell();
            DateTimeFormatter dateFmt = DateTimeFmt.D_M_YYYY;
            date.setValue(dateFmt.format(lecture.getStartTime()));

            SheetCell student = row.addCell();
            String studentStr = tutorClass.getStudent().getName() + " - " + tutorClass.getCode();
            student.setValue(studentStr);

            SheetCell level = row.addCell();
            level.setValue(tutorClass.getLevel());

            SheetCell time = row.addCell();
            DateTimeFormatter timeFmt = DateTimeFmt.H_M;
            String timeStr = timeFmt.format(lecture.getStartTime())
                + "-"
                + timeFmt.format(lecture.getEndTime());
            time.setValue(timeStr);

            SheetCell code = row.addCell();
            code.setValue(lecture.getGeneratedCode());

            SheetCell topic = row.addCell();
            topic.setValue(lecture.getTopic());

            SheetCell notes = row.addCell();
            notes.setValue(lecture.getComment());

            SheetCell duration = row.addCell();
            duration.setValue(lecture.getTutorClass().getDurationInMinute());

            SheetCell paid = row.addCell();
            paid.setValue((double) lecture.getTutorClass().getPayForLecture() / 1000);

            // set border
            row.getCells().forEach(c -> c.setStyle(bodyStyle));
        }

        return range;
    }

    private Workbook generateReportFile(ReportRange range) {
        Workbook workbook = new XSSFWorkbook();
        List<Lecture> lectures = getLecturesForReport(range);

        // write report data
        writeGeneralReportSheet(workbook, lectures);
        writeDetailReportSheet(workbook, lectures);

        return workbook;
    }

    private void writeGeneralReportSheet(Workbook workbook, List<Lecture> lectures) {
        SheetRange sheetRange = generateGeneralReportData(lectures);
        Sheet sheet = workbook.createSheet("GENERAL");
        bindToSheet(sheet, sheetRange);
    }

    private void writeDetailReportSheet(Workbook workbook, List<Lecture> lectures) {
        SheetRange sheetRange = generateDetailReportData(lectures);
        Sheet sheet = workbook.createSheet("Detail");
        bindToSheet(sheet, sheetRange);
    }

    private Map<TutorClass, List<Lecture>> groupByClass(List<Lecture> lectures) {
        Map<TutorClass, List<Lecture>> classWithLectures = new HashMap<>();

        for (Lecture lecture : lectures) {
            TutorClass tutorClass = lecture.getTutorClass();

            if (!classWithLectures.containsKey(tutorClass)) {
                classWithLectures.put(tutorClass, new ArrayList<>());
            }

            classWithLectures.get(tutorClass).add(lecture);
        }

        return classWithLectures;
    }

    private void bindToSheet(Sheet dest, SheetRange source) {
        for (int i = 0; i < source.getRows().size(); i++) {
            SheetRow sheetRow = source.getRow(i);
            Row row = dest.createRow(i);
            for (int j=0; j < sheetRow.getCells().size(); j++) {
                Cell cell = row.createCell(j);
                Object value = sheetRow.getCell(j).getValue();
                cell.setCellValue(value == null ? null : value.toString());
            }
        }
    }

    private String getDetailReportTitle(int month, int year) {
        return String.format("THÁNG %s/%s", month, year);
    }
}
