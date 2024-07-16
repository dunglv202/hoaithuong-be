package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.ReportDTO;
import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.entity.Lecture_;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.helper.DateTimeFmt;
import dev.dunglv202.hoaithuong.model.LectureCriteria;
import dev.dunglv202.hoaithuong.model.ReportRange;
import dev.dunglv202.hoaithuong.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.dunglv202.hoaithuong.constant.LectureStatus.COMPLETED;
import static dev.dunglv202.hoaithuong.model.LectureCriteria.inRange;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final LectureRepository lectureRepository;

    public Resource exportXlsx(ReportRange range) {
        try (Workbook workbook = generateReportFile(range)) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Workbook generateReportFile(ReportRange range) {
        Workbook workbook = new XSSFWorkbook();
        List<Lecture> lectures = lectureRepository.findAll(
            LectureCriteria.hasStatus(COMPLETED).and(inRange(range)),
            Sort.by(Sort.Direction.ASC, Lecture_.START_TIME)
        );

        // write report data
        writeGeneralReportSheet(workbook, lectures, range.getTimeZone());
        writeDetailReportSheet(workbook, lectures, range.getTimeZone());

        return workbook;
    }

    private void writeGeneralReportSheet(Workbook workbook, List<Lecture> lectures, ZoneId timeZone) {
        Map<TutorClass, List<Lecture>> classWithLectures = groupByClass(lectures);
        Sheet general = workbook.createSheet("General");

        Row header = general.createRow(0);
        header.createCell(0).setCellValue("Mã");
        header.createCell(1).setCellValue("Tên");
        int maxNumOfLecture = 0;

        // write record for each class
        int currentRow = 1;
        for (var entry : classWithLectures.entrySet()) {
            Row row = general.createRow(currentRow);
            TutorClass tutorClass = entry.getKey();
            List<Lecture> classLectures = entry.getValue();

            Cell code = row.createCell(0);
            code.setCellValue(tutorClass.getCode());

            Cell student = row.createCell(1);
            student.setCellValue(tutorClass.getStudent().getName());

            for (int i = 0; i < classLectures.size(); i++) {
                Cell lecture = row.createCell(i + 2);
                String cellValue = DateTimeFmt.D_M_YYYY.format(classLectures.get(i).getStartTime().atZone(timeZone)) + "\n"
                    + classLectures.get(i).getGeneratedCode(timeZone);
                lecture.setCellValue(cellValue);
            }

            maxNumOfLecture = Math.max(maxNumOfLecture, classLectures.size());
            currentRow++;
        }

        // write missing header
        for (int i = 0; i < maxNumOfLecture; i++) {
            header.createCell(i + 2).setCellValue("Buổi " + (i + 1));
        }
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

    private void writeDetailReportSheet(Workbook workbook, List<Lecture> lectures, ZoneId timeZone) {
        Sheet sheet = workbook.createSheet("Detail");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("STT");
        header.createCell(1).setCellValue("Ngày");
        header.createCell(2).setCellValue("Học viên");
        header.createCell(3).setCellValue("Lớp");
        header.createCell(4).setCellValue("Giờ học");
        header.createCell(5).setCellValue("Buổi");
        header.createCell(6).setCellValue("Nội dung");
        header.createCell(7).setCellValue("Nhận xét");
        header.createCell(8).setCellValue("Thời lượng (phút)");
        header.createCell(9).setCellValue("Số tiền (1000đ)");

        for (int i = 1; i <= lectures.size(); i++) {
            Row row = sheet.createRow(i);
            Lecture lecture = lectures.get(i-1);
            TutorClass tutorClass = lecture.getTutorClass();

            Cell cell = row.createCell(0);
            cell.setCellValue(i);

            Cell date = row.createCell(1);
            DateTimeFormatter dateFmt = DateTimeFmt.D_M_YYYY;
            date.setCellValue(dateFmt.format(lecture.getStartTime().atZone(timeZone)));

            Cell student = row.createCell(2);
            String studentStr = tutorClass.getStudent().getName() + " - " + tutorClass.getCode();
            student.setCellValue(studentStr);

            Cell level = row.createCell(3);
            level.setCellValue(tutorClass.getLevel());

            Cell time = row.createCell(4);
            DateTimeFormatter timeFmt = DateTimeFmt.H_M;
            String timeStr = timeFmt.format(lecture.getStartTime().atZone(timeZone))
                + "-"
                + timeFmt.format(lecture.getEndTime().atZone(timeZone));
            time.setCellValue(timeStr);

            Cell code = row.createCell(5);
            code.setCellValue(lecture.getGeneratedCode(timeZone));

            Cell topic = row.createCell(6);
            topic.setCellValue(lecture.getTopic());

            Cell notes = row.createCell(7);
            notes.setCellValue(lecture.getNotes());

            Cell duration = row.createCell(8);
            duration.setCellValue(lecture.getTutorClass().getDurationInMinute());

            Cell paid = row.createCell(9);
            paid.setCellValue(lecture.getTutorClass().getPayForLecture());
        }
    }

    public ReportDTO getReport(ReportRange range) {
        ReportDTO reportDTO = new ReportDTO();
        int totalEarned = lectureRepository.getTotalEarnedByRange(range);
        reportDTO.setTotalEarned(totalEarned);

        return reportDTO;
    }
}
