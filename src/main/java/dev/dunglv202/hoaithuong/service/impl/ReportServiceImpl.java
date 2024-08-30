package dev.dunglv202.hoaithuong.service.impl;

import com.google.api.client.auth.oauth2.Credential;
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
import dev.dunglv202.hoaithuong.model.LectureCriteria;
import dev.dunglv202.hoaithuong.model.ReportRange;
import dev.dunglv202.hoaithuong.model.sheet.SheetCell;
import dev.dunglv202.hoaithuong.model.sheet.SheetRange;
import dev.dunglv202.hoaithuong.model.sheet.SheetRow;
import dev.dunglv202.hoaithuong.repository.ConfigurationRepository;
import dev.dunglv202.hoaithuong.repository.LectureRepository;
import dev.dunglv202.hoaithuong.repository.ScheduleRepository;
import dev.dunglv202.hoaithuong.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.dunglv202.hoaithuong.helper.GoogleHelper.HTTP_TRANSPORT;
import static dev.dunglv202.hoaithuong.helper.GoogleHelper.JSON_FACTORY;
import static dev.dunglv202.hoaithuong.model.LectureCriteria.*;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    @Value("${spring.application.name}")
    private String applicationName;

    private final LectureRepository lectureRepository;
    private final ScheduleRepository scheduleRepository;
    private final AuthHelper authHelper;
    private final GoogleHelper googleHelper;
    private final ConfigurationRepository configurationRepository;

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
        try {
            // get sheet instance (create if not exist)
            User signedUser = authHelper.getSignedUser();
            Configuration config = configurationRepository.findByUser(signedUser)
                .orElseThrow(() -> new ClientVisibleException("{user.no_config}"));
            Credential credential = googleHelper.getCredential(signedUser);
            Sheets sheetsService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(applicationName)
                .build();
            Spreadsheet spreadsheet = sheetsService.spreadsheets().get(config.getReportSheetId()).execute();
            String sheetName = getReportSheetName(range);
            if (spreadsheet.getSheets().stream().noneMatch(sheet -> sheetName.equals(sheet.getProperties().getTitle()))) {
                // not exist => add new sheet
                addNewSheetToGoogleSpreadSheet(sheetsService, config.getReportSheetId(), sheetName);
            }

            // clear report sheet
            sheetsService.spreadsheets().values().clear(
                config.getReportSheetId(),
                getReportSheetName(range),
                new ClearValuesRequest()
            ).execute();

            // generate report data
            List<Lecture> lectures = getLecturesForReport(range);
            ValueRange valueRange = new ValueRange();
            SheetRange data = generateGeneralReportData(lectures)
                .addEmptyRow()
                .union(generateDetailReportData(lectures));
            valueRange.setValues(data.getValues());

            // update sheet
            sheetsService.spreadsheets().values().update(
                config.getReportSheetId(),
                getReportSheetName(range),
                valueRange
            ).setValueInputOption("RAW").execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addNewSheetToGoogleSpreadSheet(Sheets sheetService, String reportSheetId, String newSheetName) {
        try {
            // add new sheet request
            AddSheetRequest addSheetRequest = new AddSheetRequest();
            addSheetRequest.setProperties(new SheetProperties().setTitle(newSheetName));

            // execute update
            BatchUpdateSpreadsheetRequest batchUpdateSpreadsheetRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(List.of(new Request().setAddSheet(addSheetRequest)));
            sheetService.spreadsheets().batchUpdate(reportSheetId, batchUpdateSpreadsheetRequest).execute();
        } catch (IOException e) {
            throw new RuntimeException("Could not add new sheet", e);
        }
    }

    private String getReportSheetName(ReportRange range) {
        return String.format("THÁNG %d - %d", range.getMonth(), range.getYear());
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
        header.addCell().setValue("STT");
        header.addCell().setValue("Ngày");
        header.addCell().setValue("Học viên");
        header.addCell().setValue("Lớp");
        header.addCell().setValue("Giờ học");
        header.addCell().setValue("Buổi");
        header.addCell().setValue("Nội dung");
        header.addCell().setValue("Nhận xét");
        header.addCell().setValue("Thời lượng (phút)");
        header.addCell().setValue("Số tiền (1000đ)");

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
}
