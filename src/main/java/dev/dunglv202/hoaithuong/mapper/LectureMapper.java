package dev.dunglv202.hoaithuong.mapper;

import dev.dunglv202.hoaithuong.dto.LectureDTO;
import dev.dunglv202.hoaithuong.dto.NewLectureDTO;
import dev.dunglv202.hoaithuong.dto.UpdatedLecture;
import dev.dunglv202.hoaithuong.entity.Lecture;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(uses = StudentMapper.class)
public interface LectureMapper {
    LectureMapper INSTANCE = Mappers.getMapper(LectureMapper.class);

    Lecture toLecture(NewLectureDTO newLecture);

    @Mapping(target = "classCode", source = "tutorClass.code")
    @Mapping(target = "totalLecture", source = "tutorClass.totalLecture")
    @Mapping(target = "student", source = "tutorClass.student")
    LectureDTO toLectureDTO(Lecture lecture);

    @Mapping(target = "id", ignore = true)
    void mergeLecture(@MappingTarget Lecture old, UpdatedLecture updated);
}
