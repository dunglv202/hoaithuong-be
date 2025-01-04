package dev.dunglv202.hoaithuong.helper;

import dev.dunglv202.hoaithuong.entity.Schedule;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.model.TimeSlot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleGenerator {
    private LocalDateTime startTime;
    private TutorClass tutorClass;
    private List<TimeSlot> timeSlots;

    public ScheduleGenerator setClass(TutorClass tutorClass) {
        this.tutorClass = tutorClass;
        this.timeSlots = tutorClass.getTimeSlots().stream().distinct().sorted().toList();
        return this;
    }

    public ScheduleGenerator setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Generate schedule in ascending order for tutor class
     */
    public List<Schedule> generate(int limit) {
        if (timeSlots == null || timeSlots.isEmpty()) {
            throw new RuntimeException("Timeslots are required to generate schedule");
        }

        List<Schedule> schedules = new ArrayList<>();
        LocalDateTime current = startTime;
        for (int i = 1; i <= limit; i++) {
            // find next time slot
            LocalDateTime curr = current;
            TimeSlot nextTimeSlot = timeSlots.stream()
                .filter(timeSlot -> isGreaterOrEqualsInSameWeek(timeSlot, curr))
                .findFirst()
                .orElse(timeSlots.get(0));

            // next time slot could be in next week or current week, need to plus 7 days in case it is in next week
            int rawDiff = nextTimeSlot.getWeekday().getValue() - current.getDayOfWeek().getValue();
            int daysDiff = isGreaterOrEqualsInSameWeek(nextTimeSlot, curr) ? rawDiff : rawDiff + 7;

            // make schedule and move to that time for next generation point
            LocalDateTime scheduleTime = current.plusDays(daysDiff).toLocalDate().atTime(nextTimeSlot.getStartTime());
            Schedule schedule = makeSchedule(scheduleTime);
            schedules.add(schedule);
            current = schedule.getEndTime();
        }
        return schedules;
    }

    /**
     * Check if timeslot {@code a} is greater than {@code b} in the same week
     */
    private boolean isGreaterOrEqualsInSameWeek(TimeSlot timeSlot, LocalDateTime dateTime) {
        return timeSlot.getWeekday() == dateTime.getDayOfWeek()
            ? !timeSlot.getStartTime().isBefore(dateTime.toLocalTime())
            : timeSlot.getWeekday().compareTo(dateTime.getDayOfWeek()) > 0;
    }

    /**
     * Make schedule instance for class
     */
    private Schedule makeSchedule(LocalDateTime startTime) {
        Schedule schedule = new Schedule();
        schedule.setStartTime(startTime);
        schedule.setEndTime(schedule.getStartTime().plus(tutorClass.getDuration()));
        schedule.setTutorClass(tutorClass);
        return schedule;
    }
}
