package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.ScheduleDTO;
import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.model.Range;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleService {
    List<ScheduleDTO> getSchedule(Range<LocalDate> range);

    void deleteSchedule(Long id);

    void addClassToMySchedule(TutorClass newClass);

    /**
     * Add schedule to class schedule, if class have enough schedule for its all lectures
     * the last one is replaced by this new schedule
     *
     * @throws dev.dunglv202.hoaithuong.exception.ConflictScheduleException overlap with scheduled ones
     */
    Schedule addScheduleForClass(TutorClass tutorClass, LocalDateTime startTime);
}
