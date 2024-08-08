package dev.dunglv202.hoaithuong.mapper;

import dev.dunglv202.hoaithuong.dto.ScheduleDTO;
import dev.dunglv202.hoaithuong.entity.Schedule;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {LectureMapper.class, TutorClassMapper.class})
public interface ScheduleMapper {
    ScheduleMapper INSTANCE = Mappers.getMapper(ScheduleMapper.class);

    ScheduleDTO toScheduleDTO(Schedule schedule);
}
