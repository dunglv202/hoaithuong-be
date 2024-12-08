package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.entity.Confirmation;
import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.entity.Student;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.mapper.LectureMapper;
import dev.dunglv202.hoaithuong.mapper.StudentMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
public class ReportDTO {
    private String evidenceUrl;
    private int totalEarned;
    private int estimatedTotal;
    private int totalLectures;
    private int totalStudents;
    private List<StudentInReportDTO> students;

    public ReportDTO(List<Lecture> lectures, List<Confirmation> confirmations) {
        // aggregate report figures
        this.totalEarned = lectures.stream()
            .map(Lecture::getTutorClass)
            .map(TutorClass::getPayForLecture)
            .reduce(Integer::sum)
            .orElse(0);
        this.totalLectures = lectures.size();
        this.totalStudents = lectures.stream()
            .map(Lecture::getTutorClass)
            .map(TutorClass::getStudent)
            .collect(Collectors.toSet())
            .size();

        // group lectures by student
        Map<Student, List<Lecture>> lecturesByStudent = lectures.stream()
            .collect(Collectors.groupingBy((lec) -> lec.getTutorClass().getStudent()));
        this.students = lecturesByStudent.entrySet().stream()
            .map(entry -> {
                StudentInReportDTO student = StudentMapper.INSTANCE.toStudentInReportDTO(entry.getKey());

                List<LectureInReportDTO> studentLectures = entry.getValue().stream()
                    .map(LectureMapper.INSTANCE::toLectureInReportDTO).toList();
                student.setLectures(studentLectures);

                Optional<Confirmation> confirmation = confirmations.stream()
                    .filter(c -> c.getStudent().equals(entry.getKey()))
                    .findFirst();
                student.setConfirmationUrl(confirmation.map(Confirmation::getUrl).orElse(null));

                return student;
            }).toList();
    }
}
