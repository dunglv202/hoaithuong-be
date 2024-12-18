package dev.dunglv202.hoaithuong.mapper;

import dev.dunglv202.hoaithuong.dto.*;
import dev.dunglv202.hoaithuong.entity.Lecture;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(uses = { StudentMapper.class, TutorClassMapper.class })
public interface LectureMapper {
    LectureMapper INSTANCE = Mappers.getMapper(LectureMapper.class);

    Lecture toLecture(NewLectureDTO newLecture);

    @Mapping(target = "classCode", source = "tutorClass.code")
    @Mapping(target = "totalLecture", source = "tutorClass.totalLecture")
    @Mapping(target = "student", source = "tutorClass.student")
    LectureDTO toLectureDTO(Lecture lecture);

    ScheduleLectureDTO toScheduleLectureDTO(Lecture lecture);

    @Mapping(target = "id", ignore = true)
    void mergeLecture(@MappingTarget Lecture old, UpdatedLecture updated);

    @Mapping(target = "classCode", source = "tutorClass.code")
    @Mapping(target = "totalLecture", source = "tutorClass.totalLecture")
    @Mapping(target = "student", source = "tutorClass.student.name")
    LectureInReportDTO toLectureInReportDTO(Lecture lecture);

    @Mapping(target = "startTime", source = "schedule.startTime")
    @Mapping(target = "hasPreview", expression = "java(lecture.getVideoId() != null)")
    LectureDetails toLectureDetails(Lecture lecture);
}
