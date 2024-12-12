package dev.dunglv202.hoaithuong.service.impl;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import dev.dunglv202.hoaithuong.constant.ApiErrorCode;
import dev.dunglv202.hoaithuong.dto.ConfirmationDTO;
import dev.dunglv202.hoaithuong.dto.ReportDTO;
import dev.dunglv202.hoaithuong.entity.*;
import dev.dunglv202.hoaithuong.exception.AuthenticationException;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.exception.InvalidConfigException;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.helper.DateTimeFmt;
import dev.dunglv202.hoaithuong.helper.GoogleHelper;
import dev.dunglv202.hoaithuong.helper.SheetHelper;
import dev.dunglv202.hoaithuong.model.ReportRange;
import dev.dunglv202.hoaithuong.model.sheet.standard.*;
import dev.dunglv202.hoaithuong.repository.*;
import dev.dunglv202.hoaithuong.service.ConfigService;
import dev.dunglv202.hoaithuong.service.ReportService;
import dev.dunglv202.hoaithuong.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static dev.dunglv202.hoaithuong.helper.SheetHelper.columnIndexToLetter;
import static dev.dunglv202.hoaithuong.model.criteria.LectureCriteria.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {
    private final LectureRepository lectureRepository;
    private final ScheduleRepository scheduleRepository;
    private final AuthHelper authHelper;
    private final GoogleHelper googleHelper;
    private final ConfigService configService;
    private final ReportRepository reportRepository;
    private final GoogleDriveService driveService;
    private final StudentRepository studentRepository;
    private final ConfirmationRepository confirmationRepository;
    private final StorageService storageService;

    @Override
    public ReportDTO getReport(ReportRange range) {
        User teacher = authHelper.getSignedUser();
        Specification<Lecture> criteria = Specification.allOf(
            ofTeacher(teacher),
            inRange(range),
            sortByStartTime(Sort.Direction.DESC)
        );

        List<Lecture> lectures = lectureRepository.findAll(criteria.and(joinFetch()));
        List<Confirmation> confirmations = List.of();
        Optional<Report> report = reportRepository.findByTimeAndTeacher(range.getYear(), range.getMonth(), teacher);
        if (report.isPresent()) {
            confirmations = confirmationRepository.findAllByReport(report.get());
        }

        ReportDTO reportDTO = new ReportDTO(lectures, confirmations);
        int estimatedTotal = scheduleRepository.getEstimatedTotalInRange(teacher, range.getFrom(), range.getTo());
        reportDTO.setEstimatedTotal(estimatedTotal);
        report.ifPresent(value -> reportDTO.setEvidenceUrl(value.getConfirmationsUrl()));

        return reportDTO;
    }

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
    public void exportGoogleSheet(ReportRange range) {
        User signedUser = authHelper.getSignedUser();
        exportGoogleSheet(signedUser, range);
    }

    @Override
    public void exportGoogleSheet(User user, ReportRange range) {
        try {
            // check config
            Configuration config = configService.getConfigsByUser(user);
            if (config.getGeneralReportId() == null || config.getGeneralReportSheet() == null) {
                throw new InvalidConfigException("{general_report.sheet.required}");
            }
            if (config.getDetailReportId() == null || config.getDetailReportSheet() == null) {
                throw new InvalidConfigException("{detail_report.sheet.required}");
            }

            // do export
            Sheets sheetsService = googleHelper.getSheetService(user);
            List<Lecture> lectures = getLecturesForReport(user, range);
            exportDetailToGgSheet(sheetsService, range, lectures, config);
            exportGeneralToGgSheet(sheetsService, lectures, config);
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED) {
                throw new AuthenticationException(ApiErrorCode.REQUIRE_GOOGLE_AUTH);
            }
            throw new RuntimeException(e);
        } catch (ClientVisibleException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public void uploadConfirmation(int year, int month, ConfirmationDTO confirmationDTO) {
        User teacher = authHelper.getSignedUser();

        // Check if student have lecture on that month
        Student student = studentRepository.findByIdAndCreatedBy(confirmationDTO.getStudentId(), teacher)
            .orElseThrow(() -> new ClientVisibleException("{student.not_found}"));
        TutorClass tutorClass = lectureRepository.findByTimeAndStudentForTeacher(year, month, teacher, student)
            .stream()
            .map(Lecture::getTutorClass)
            .findFirst()
            .orElseThrow(() -> new ClientVisibleException("{student.no_class_in_time}"));

        // Add to report folder
        Report report = reportRepository.findByTimeAndTeacher(year, month, teacher)
            .orElseGet(() -> createReport(year, month, teacher));
        confirmationRepository.findByReportAndStudent(report, student)
            .ifPresent(confirmation -> {
                // Delete old one if exist
                driveService.deleteFile(teacher, confirmation.getFileId());
                confirmationRepository.delete(confirmation);
            });
        String fileId = driveService.uploadToFolder(
            teacher,
            report.getConfirmation(),
            confirmationDTO.getConfirmation(),
            tutorClass.getName()
        );

        // Store to storage for preview
        String url = storageService.storeFile(confirmationDTO.getConfirmation());

        // Save confirmation info
        Confirmation confirmation = new Confirmation();
        confirmation.setReport(report);
        confirmation.setStudent(student);
        confirmation.setFileId(fileId);
        confirmation.setUrl(url);
        confirmationRepository.save(confirmation);
    }

    @Override
    public void createIfNotExist(User teacher, int year, int month) {
        Optional<Report> report = reportRepository.findByTimeAndTeacher(year, month, teacher);
        if (report.isEmpty()) {
            createReport(year, month, teacher);
        }
    }

    @Override
    @Transactional
    public void deleteConfirmation(int year, int month, long studentId) {
        User teacher = authHelper.getSignedUser();

        // verify existence
        Report report = reportRepository.findByTimeAndTeacher(year, month, teacher)
            .orElseThrow(() -> new ClientVisibleException("{report.not_found}"));
        Confirmation confirmation = confirmationRepository.findByReportAndStudent(
            report,
            studentRepository.getReferenceById(studentId)
        ).orElseThrow(() -> new ClientVisibleException("{confirmation.not_found}"));

        // remove from drive & storage
        driveService.deleteFile(teacher, confirmation.getFileId());
        new Thread(() -> {
            storageService.deleteFile(confirmation.getUrl());
        }).start();

        confirmationRepository.delete(confirmation);
    }

    private Report createReport(int year, int month, User teacher) {
        Report report = new Report();
        report.setYear(year);
        report.setMonth(month);
        report.setTeacher(teacher);
        String folderId = driveService.createDriveFolder(teacher, "Xác nhận PH tháng " + month + " - " + year);
        report.setConfirmation(folderId);
        return reportRepository.save(report);
    }

    private void exportDetailToGgSheet(Sheets sheetsService, ReportRange range, List<Lecture> lectures, Configuration config) throws IOException {
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

        // get row indexes of each class, each can have multiple rows stored in `classRowIndex` map
        List<List<Object>> codeValues = sheetsService.spreadsheets().values()
            .get(spreadsheetId, sheetName + "!A:A").execute().getValues();
        Set<String> classCodes = lectures.stream().map(lec -> lec.getTutorClass().getCode()).collect(Collectors.toSet());
        Map<String, List<Integer>> classRowIndex = new HashMap<>();
        for (int rowIdx=0; rowIdx < codeValues.size(); rowIdx++) {
            List<Object> row = codeValues.get(rowIdx);
            if (row.isEmpty()) continue;
            String code = (String) row.get(0);
            if (classCodes.contains((String) row.get(0))) {
                classRowIndex.putIfAbsent(code, new ArrayList<>());
                classRowIndex.get(code).add(rowIdx);
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
        var batchUpdateRequest = new BatchUpdateValuesRequest().setValueInputOption("RAW").setData(new ArrayList<>());
        groupByClass(lectures).forEach((tutorClass, classLectures) -> {
            String classCode = tutorClass.getCode();
            if (classRowIndex.get(classCode) == null) {
                throw new ClientVisibleException("{report.general.class_code_not_found} : " + classCode);
            }
            classLectures.forEach(lec -> {
                /* why offset -> each class can have multiple row. e.g: class with 22 lectures, but there's only 20
                   columns to write so 21st and 22nd lecture should be in second row */
                int offset = (lec.getLectureNo() - 1) / lectureNoColumnIndexes.size();
                if (offset >= classRowIndex.get(classCode).size()) {
                    // no valid row for this lecture => skip with no error
                    log.info("No valid row for {}, lecture {}", lec.getTutorClass().getName(), lec.getLectureNo());
                    return;
                }
                int rowIndex = classRowIndex.get(classCode).get(offset);
                int colIndex = lectureNoColumnIndexes.get((lec.getLectureNo() - 1) % lectureNoColumnIndexes.size());
                var value = new ValueRange()
                    .setRange(String.format("%s!%s%s", sheetName, columnIndexToLetter(colIndex), rowIndex + 1))
                    .setValues(List.of(List.of(DateTimeFmt.D_M_YYYY.format(lec.getStartTime()) + "\n" + lec.getGeneratedCode())));
                batchUpdateRequest.getData().add(value);
            });
        });
        sheetsService.spreadsheets().values().batchUpdate(spreadsheetId, batchUpdateRequest).execute();
    }

    /**
     * @return List of lectures for current signed teacher
     */
    private List<Lecture> getLecturesForReport(User teacher, ReportRange range) {
        Specification<Lecture> specification = Specification.allOf(
            ofTeacher(teacher),
            inRange(range),
            sortByStartTime(Sort.Direction.ASC)
        );
        return lectureRepository.findAll(specification.and(joinFetch()));
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
            .setWrapText(true)
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
        header.addCell().setValue("Video buổi học").setStyle(headerStyle);
        header.addCell().setValue("Trung tâm tính lương").setStyle(headerStyle);

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
            .setAttribute(new SheetCellAttribute().setColspan(12))
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

            SheetCell video = row.addCell();
            video.setValue(lecture.getVideo()).getAttribute().setLink(true);

            // add approved earning column
            row.addCell();

            // set border
            row.getCells().forEach(c -> c.setStyle(bodyStyle));
        }

        return range;
    }

    private Workbook generateReportFile(ReportRange range) {
        Workbook workbook = new XSSFWorkbook();
        List<Lecture> lectures = getLecturesForReport(authHelper.getSignedUser(), range);

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
