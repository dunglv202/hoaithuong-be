package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.MinimalScheduleDTO;
import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.model.Range;
import dev.dunglv202.hoaithuong.model.TimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface ScheduleService {
    List<MinimalScheduleDTO> getSchedule(Range<LocalDate> range);

    void deleteSchedule(Long id);

    void addClassToMySchedule(TutorClass newClass, LocalDate startDate);

    /**
     * Add schedule to class schedule, if class have enough schedule for its all lectures
     * the last one is replaced by this new schedule
     *
     * @throws dev.dunglv202.hoaithuong.exception.ConflictScheduleException overlap with scheduled ones
     */
    Schedule addSingleScheduleForClass(TutorClass tutorClass, LocalDateTime startTime);

    /**
     * Update schedule since {@code startDate}, remove old schedule and replace with new time slots
     */
    void updateScheduleForClass(TutorClass tutorClass, LocalDate startDate, Set<TimeSlot> timeSlots);
}
