package dev.dunglv202.hoaithuong.service.impl;

import com.microsoft.graph.models.DriveItem;
import dev.dunglv202.hoaithuong.dto.*;
import dev.dunglv202.hoaithuong.entity.*;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.mapper.LectureMapper;
import dev.dunglv202.hoaithuong.model.Range;
import dev.dunglv202.hoaithuong.model.ReportRange;
import dev.dunglv202.hoaithuong.model.criteria.LectureCriteria;
import dev.dunglv202.hoaithuong.repository.LectureRepository;
import dev.dunglv202.hoaithuong.repository.ScheduleRepository;
import dev.dunglv202.hoaithuong.repository.TutorClassRepository;
import dev.dunglv202.hoaithuong.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static dev.dunglv202.hoaithuong.model.criteria.LectureCriteria.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LectureServiceImpl implements LectureService {
    private static final String DEFAULT_LECTURE_COMMENT = "Con ngoan, tập trung, tích cực trong giờ học";

    private final TutorClassRepository tutorClassRepository;
    private final LectureRepository lectureRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleService scheduleService;
    private final NotificationService notificationService;
    private final AuthHelper authHelper;
    private final ConfigService configService;
    private final ReportService reportService;
    private final VideoStorageService videoStorageService;
    private final TaskExecutor taskExecutor;

    @Override
    @Transactional
    public void addNewLecture(NewLectureDTO newLectureDTO) {
        Lecture lecture = LectureMapper.INSTANCE.toLecture(newLectureDTO);
        lecture.setComment(DEFAULT_LECTURE_COMMENT);

        // set class & teacher code
        TutorClass tutorClass = tutorClassRepository.findByIdAndTeacher(
            newLectureDTO.getClassId(),
            authHelper.getSignedUserRef()
        ).orElseThrow();
        lecture.setTutorClass(tutorClass);
        lecture.setTeacherCode(configService.getConfigsByUser(tutorClass.getTeacher()).getTeacherCode());

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

        // update learned
        int learned = tutorClass.getLearned() + 1;
        if (learned > tutorClass.getTotalLecture()) {
            throw new ClientVisibleException("{tutor_class.lecture.exceed}");
        }
        tutorClass.setLearned(learned);

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

        // trigger create report for first lecture of month
        reportService.createIfNotExist(
            authHelper.getSignedUserRef(),
            schedule.getStartTime().getYear(),
            schedule.getStartTime().getMonth().getValue()
        );

        lectureRepository.save(lecture);
        tutorClassRepository.save(tutorClass);
    }

    @Override
    public List<LectureDTO> getAllLectures(ReportRange range) {
        Specification<Lecture> criteria = Specification.allOf(
            inRange(range),
            sortByStartTime(Sort.Direction.DESC)
        );
        return lectureRepository.findAll(LectureCriteria.ofTeacher(authHelper.getSignedUserRef()).and(joinFetch()).and(criteria))
            .stream()
            .map(LectureMapper.INSTANCE::toLectureDTO)
            .toList();
    }

    @Override
    public void updateLecture(UpdatedLecture updatedLecture) {
        Lecture lecture = lectureRepository.findByIdAndTeacher(updatedLecture.getId(), authHelper.getSignedUserRef())
            .orElseThrow();

        if (updatedLecture.getVideo() != null && !updatedLecture.getVideo().equals(lecture.getVideo())) {
            // preview become unavailable after video modification
            lecture.setVideoId(null);
            lecture.setVideoExpiryDate(null);
        }

        lectureRepository.save(lecture.merge(updatedLecture));
    }

    @Override
    public void syncMyLectureVideos(ReportRange range) {
        User teacher = authHelper.getSignedUserRef();
        taskExecutor.execute(() -> {
            syncLectureVideos(teacher, range);
            Notification notification = Notification.forUser(teacher)
                .content("{lecture.video_synchronization.done}");
            notificationService.addNotification(notification);
        });
    }

    @Override
    public void syncLectureVideos(User teacher, Range<LocalDate> range) {
        List<Lecture> lectures = lectureRepository.findAllNoVideoInRangeByTeacher(teacher, range);
        if (lectures.isEmpty()) return;

        Configuration config = configService.getConfigsByUser(teacher);
        lectures.forEach(lecture -> {
            try {
                Optional<DriveItem> video = videoStorageService.findLectureVideo(lecture, config.getVideoSource());
                if (video.isPresent()) {
                    // get & set video for lecture
                    LocalDate paymentDate = lecture.getStartTime().plusMonths(1).withDayOfMonth(20).toLocalDate();
                    lecture.setVideo(videoStorageService.createSharableLink(
                        video.get(),
                        OffsetDateTime.of(paymentDate.atStartOfDay(), ZoneOffset.ofHours(7))
                    ));
                    lecture.setVideoId(video.get().getId());
                    lecture.setVideoExpiryDate(paymentDate);
                    lectureRepository.save(lecture);

                    // move video folder to processed area
                    assert video.get().getParentReference() != null;
                    DriveItem lectureFolder = new DriveItem();
                    lectureFolder.setId(video.get().getParentReference().getId());
                    videoStorageService.moveToFolder(lectureFolder, config.getProcessedVideos());
                } else {
                    log.info("No video found for lecture {}", lecture);
                }
            } catch (Exception e) {
                log.error("Error occurred while syncing lecture video - lecture: {}", lecture, e);
            }
        });
    }

    @Override
    @Transactional
    public void deleteLecture(long id) {
        Lecture lecture = lectureRepository.findByIdAndTeacher(id, authHelper.getSignedUserRef())
            .orElseThrow(() -> new ClientVisibleException("{lecture.not_found}"));

        // delete lecture info
        lectureRepository.delete(lecture);

        // update tutor class
        TutorClass tutorClass = lecture.getTutorClass();
        tutorClass.setLearned(tutorClass.getLearned() - 1);
        tutorClassRepository.save(tutorClass);

        // delete attached schedule then create new one for class
        scheduleService.deleteSchedule(lecture.getSchedule().getId());
    }

    @Override
    public LectureDetails getLectureDetails(long id) {
        Lecture lecture = lectureRepository.findByIdAndTeacher(id, authHelper.getSignedUserRef())
            .orElseThrow(() -> new ClientVisibleException("{lecture.not_found}"));
        return LectureMapper.INSTANCE.toLectureDetails(lecture);
    }

    @Override
    public String getVideoPreview(long id) {
        Lecture lecture = lectureRepository.findByIdAndTeacher(id, authHelper.getSignedUserRef())
            .orElseThrow(() -> new ClientVisibleException("{lecture.not_found}"));
        if (lecture.getVideoId() == null) return null;
        return videoStorageService.createPreviewLink(lecture.getVideoId());
    }

    @Override
    public LectureVideoDTO getLectureVideo(String classCode, int lectureNo) {
        Lecture lecture = lectureRepository.findByClassCodeAndLectureNo(classCode, lectureNo)
            .orElseThrow(() -> new ClientVisibleException(HttpStatus.NOT_FOUND, "404", "Invalid lecture"));
        String url = lecture.getVideoId() != null
            ? videoStorageService.createPreviewLink(lecture.getVideoId())
            : lecture.getVideo();
        return LectureVideoDTO.builder()
            .url(url)
            .isIframe(lecture.getVideoId() != null)
            .build();
    }
}
