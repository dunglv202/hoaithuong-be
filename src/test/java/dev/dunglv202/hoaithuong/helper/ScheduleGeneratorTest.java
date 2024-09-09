package dev.dunglv202.hoaithuong.helper;

import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.model.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScheduleGeneratorTest {
    @Test
    void testGenerateReturnScheduleOnSameDayNextWeek() {
        // arrange
        TutorClass tutorClass = new TutorClass();
        List<TimeSlot> timeSlots = List.of(
            new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(10, 0)),
            new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(15, 0))
        );
        tutorClass.setTimeSlots(timeSlots);
        tutorClass.setDurationInMinute(60);
        LocalDateTime startTime = LocalDateTime.of(2024, 9, 9, 16, 0);

        // act
        List<Schedule> actual = new ScheduleGenerator().setClass(tutorClass).setStartTime(startTime).generate(1);

        // assert
        assertEquals(1, actual.size());
        assertEquals(
            LocalDateTime.of(2024, 9, 16, 10, 0),
            actual.get(0).getStartTime()
        );
        assertEquals(
            LocalDateTime.of(2024, 9, 16, 11, 0),
            actual.get(0).getEndTime()
        );
    }

    @Test
    void testGenerateReturnScheduleOnSameDay() {
        // arrange
        TutorClass tutorClass = new TutorClass();
        List<TimeSlot> timeSlots = List.of(
            new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(10, 0)),
            new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(15, 0))
        );
        tutorClass.setTimeSlots(timeSlots);
        tutorClass.setDurationInMinute(60);
        LocalDateTime startTime = LocalDateTime.of(2024, 9, 9, 15, 0);

        // act
        List<Schedule> actual = new ScheduleGenerator().setClass(tutorClass).setStartTime(startTime).generate(1);

        // assert
        assertEquals(1, actual.size());
        assertEquals(
            LocalDateTime.of(2024, 9, 9, 15, 0),
            actual.get(0).getStartTime()
        );
        assertEquals(
            LocalDateTime.of(2024, 9, 9, 16, 0),
            actual.get(0).getEndTime()
        );
    }

    @Test
    void testGenerateReturn2SchedulesOnSameDay() {
        // arrange
        TutorClass tutorClass = new TutorClass();
        List<TimeSlot> timeSlots = List.of(
            new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(10, 0)),
            new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(15, 0))
        );
        tutorClass.setTimeSlots(timeSlots);
        tutorClass.setDurationInMinute(60);
        LocalDateTime startTime = LocalDateTime.of(2024, 9, 9, 9, 0);

        // act
        List<Schedule> actual = new ScheduleGenerator().setClass(tutorClass).setStartTime(startTime).generate(2);

        // assert
        assertEquals(2, actual.size());
        assertEquals(
            LocalDateTime.of(2024, 9, 9, 10, 0),
            actual.get(0).getStartTime()
        );
        assertEquals(
            LocalDateTime.of(2024, 9, 9, 11, 0),
            actual.get(0).getEndTime()
        );
        assertEquals(
            LocalDateTime.of(2024, 9, 9, 15, 0),
            actual.get(1).getStartTime()
        );
        assertEquals(
            LocalDateTime.of(2024, 9, 9, 16, 0),
            actual.get(1).getEndTime()
        );
    }

    @Test
    void testGenerateReturn3Schedules() {
        // arrange
        TutorClass tutorClass = new TutorClass();
        List<TimeSlot> timeSlots = List.of(
            new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(10, 0)),
            new TimeSlot(DayOfWeek.TUESDAY, LocalTime.of(0, 0)),
            new TimeSlot(DayOfWeek.SUNDAY, LocalTime.of(5, 0))
        );
        tutorClass.setTimeSlots(timeSlots);
        tutorClass.setDurationInMinute(60);
        LocalDateTime startTime = LocalDateTime.of(2024, 9, 9, 15, 0);

        // act
        List<Schedule> actual = new ScheduleGenerator().setClass(tutorClass).setStartTime(startTime).generate(3);

        // assert
        assertEquals(3, actual.size());
        assertEquals(
            LocalDateTime.of(2024, 9, 10, 0, 0),
            actual.get(0).getStartTime()
        );
        assertEquals(
            LocalDateTime.of(2024, 9, 10, 1, 0),
            actual.get(0).getEndTime()
        );
        assertEquals(
            LocalDateTime.of(2024, 9, 15, 5, 0),
            actual.get(1).getStartTime()
        );
        assertEquals(
            LocalDateTime.of(2024, 9, 16, 10, 0),
            actual.get(2).getStartTime()
        );
    }
}