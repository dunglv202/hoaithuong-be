package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.mapper.LectureMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ReportDTO {
    private int totalEarned;
    private int estimatedTotal;
    private int totalLectures;
    private int totalStudents;
    private List<LectureDTO> lectures;

    public ReportDTO(List<Lecture> lectures) {
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
        this.lectures = lectures.stream().map(LectureMapper.INSTANCE::toLectureDTO).toList();
    }
}
