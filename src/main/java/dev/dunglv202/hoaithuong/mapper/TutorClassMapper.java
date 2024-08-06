package dev.dunglv202.hoaithuong.mapper;

import dev.dunglv202.hoaithuong.dto.DetailClassDTO;
import dev.dunglv202.hoaithuong.dto.UpdatedTutorClassDTO;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TutorClassMapper {
    TutorClassMapper INSTANCE = Mappers.getMapper(TutorClassMapper.class);

    @Mapping(target = "id", ignore = true)
    void mergeTutorClass(@MappingTarget TutorClass old, UpdatedTutorClassDTO updated);

    DetailClassDTO toDetailClassDTO(TutorClass tutorClass);
}
