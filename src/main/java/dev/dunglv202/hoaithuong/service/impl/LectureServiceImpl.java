package dev.dunglv202.hoaithuong.service.impl;

import dev.dunglv202.hoaithuong.dto.LectureDTO;
import dev.dunglv202.hoaithuong.dto.NewLectureDTO;
import dev.dunglv202.hoaithuong.dto.UpdatedLecture;
import dev.dunglv202.hoaithuong.entity.*;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.mapper.LectureMapper;
import dev.dunglv202.hoaithuong.model.ReportRange;
import dev.dunglv202.hoaithuong.model.criteria.LectureCriteria;
import dev.dunglv202.hoaithuong.repository.LectureRepository;
import dev.dunglv202.hoaithuong.repository.ScheduleRepository;
import dev.dunglv202.hoaithuong.repository.TutorClassRepository;
import dev.dunglv202.hoaithuong.service.ConfigService;
import dev.dunglv202.hoaithuong.service.LectureService;
import dev.dunglv202.hoaithuong.service.NotificationService;
import dev.dunglv202.hoaithuong.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dev.dunglv202.hoaithuong.model.criteria.LectureCriteria.*;

@Service
@RequiredArgsConstructor
public class LectureServiceImpl implements LectureService {
    private final TutorClassRepository tutorClassRepository;
    private final LectureRepository lectureRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleService scheduleService;
    private final NotificationService notificationService;
    private final AuthHelper authHelper;
    private final ConfigService configService;

    @Override
    @Transactional
    public void addNewLecture(NewLectureDTO newLectureDTO) {
        Lecture lecture = LectureMapper.INSTANCE.toLecture(newLectureDTO);

        // set class & teacher code
        TutorClass tutorClass = tutorClassRepository.findByIdAndTeacher(
            newLectureDTO.getClassId(),
            authHelper.getSignedUser()
        ).orElseThrow();
        lecture.setTutorClass(tutorClass);
        lecture.setTeacherCode(configService.getConfigsByUser(tutorClass.getTeacher()).getTeacherCode());

        // update learned
        int learned = tutorClass.getLearned() + 1;
        if (learned > tutorClass.getTotalLecture()) {
            throw new ClientVisibleException("{tutor_class.lecture.exceed}");
        }
        tutorClass.setLearned(learned);

        // set schedule for lecture
        Schedule schedule;
        if (newLectureDTO.getScheduleId() != null) {
            schedule = scheduleRepository.findById(newLectureDTO.getScheduleId())
                .orElseThrow(() -> new ClientVisibleException("{schedule.not_found}"));

            if (schedule.getLecture() != null) {
                throw new ClientVisibleException("{schedule.attached_to_lecture}");
            }
        } else {
            // create new schedule
            schedule = scheduleService.addSingleScheduleForClass(
                lecture.getTutorClass(),
                newLectureDTO.getStartTime()
            );
        }
        lecture.setSchedule(schedule);

        // set lecture no
        List<Lecture> lecturesAfterThis = lectureRepository.findAll(
            LectureCriteria.ofClass(lecture.getTutorClass()).and(from(lecture.getStartTime())),
            Sort.by(Sort.Direction.ASC, Lecture_.LECTURE_NO)
        );
        if (lecturesAfterThis.isEmpty()) {
            lecture.setLectureNo(learned);
        } else {
            // update lecture no for ones after this lecture
            lecture.setLectureNo(lecturesAfterThis.get(0).getLectureNo());
            lecturesAfterThis.forEach(lec -> lec.setLectureNo(lec.getLectureNo() + 1));
            lectureRepository.saveAll(lecturesAfterThis);
        }

        // send alert notification
        if (tutorClass.getLearned() == 18 || tutorClass.getLearned() == 8) {
            String noti = String.format(
                "Your class: %s has reached lecture of %d/%d",
                tutorClass.getName(),
                tutorClass.getLearned(),
                tutorClass.getTotalLecture()
            );
            notificationService.addNotification(
                Notification.forUser(tutorClass.getTeacher()).content(noti)
            );
        }
        if (tutorClass.getLearned() == tutorClass.getTotalLecture()) {
            String noti = String.format(
                "%s has just reached its last lecture. Do you want to renew this class?",
                tutorClass.getName()
            );
            notificationService.addNotification(
                Notification.forUser(tutorClass.getTeacher()).content(noti)
            );
        }

        lectureRepository.save(lecture);
        tutorClassRepository.save(tutorClass);
    }

    @Override
    public List<LectureDTO> getAllLectures(ReportRange range) {
        Specification<Lecture> criteria = Specification.allOf(
            inRange(range),
            sortByStartTime(Sort.Direction.DESC)
        );
        return lectureRepository.findAll(LectureCriteria.ofTeacher(authHelper.getSignedUser()).and(joinFetch()).and(criteria))
            .stream()
            .map(LectureMapper.INSTANCE::toLectureDTO)
            .toList();
    }

    @Override
    public void updateLecture(UpdatedLecture updatedLecture) {
        Lecture lecture = lectureRepository.findByIdAndTeacher(updatedLecture.getId(), authHelper.getSignedUser())
            .orElseThrow();
        lectureRepository.save(lecture.merge(updatedLecture));
    }
}
