package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.entity.TutorClass;
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
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        try (Workbook workbook = new XSSFWorkbook()) {
            List<Lecture> lectures = lectureRepository.findAllInRange(range);
            Sheet general = workbook.createSheet();

            for (int i = 1; i <= lectures.size(); i++) {
                Row row = general.createRow(i);
                Lecture lecture = lectures.get(i);
                TutorClass tutorClass = lecture.getTutorClass();

                Cell date = row.createCell(1);
                date.setCellValue(lecture.getStartTime().toLocalDate());

                Cell student = row.createCell(2);
                String studentStr = tutorClass.getStudent().getName() + " - " + tutorClass.getCode();
                student.setCellValue(studentStr);

                Cell level = row.createCell(3);
                level.setCellValue(tutorClass.getLevel().getLabel());

                Cell time = row.createCell(4);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H'h':m");
                String timeStr = formatter.format(lecture.getStartTime()) + " - " + formatter.format(lecture.getEndTime());
                time.setCellValue(timeStr);

                Cell topic = row.createCell(5);
                topic.setCellValue(lecture.getTopic());

                Cell notes = row.createCell(6);
                notes.setCellValue(lecture.getNotes());
            }

            return workbook;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
