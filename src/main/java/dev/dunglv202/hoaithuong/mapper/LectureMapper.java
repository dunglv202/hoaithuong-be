package dev.dunglv202.hoaithuong.mapper;

import dev.dunglv202.hoaithuong.dto.UpdatedLecture;
import dev.dunglv202.hoaithuong.entity.Lecture;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LectureMapper {
    LectureMapper INSTANCE = Mappers.getMapper(LectureMapper.class);

    @Mapping(target = "id", ignore = true)
    void mergeLecture(@MappingTarget Lecture old, UpdatedLecture updated);
}
