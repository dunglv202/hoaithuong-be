package dev.dunglv202.hoaithuong.service.impl;

import com.microsoft.graph.models.DriveItem;
import dev.dunglv202.hoaithuong.dto.*;
import dev.dunglv202.hoaithuong.entity.*;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.helper.IdEncryptor;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
    private final TransactionTemplate transactionTemplate;
    private final IdEncryptor idEncryptor;

    @Override
    @Transactional
    public void addNewLecture(NewLectureDTO newLectureDTO) {
        TutorClass tutorClass = tutorClassRepository.findByIdAndTeacher(
            newLectureDTO.getClassId(),
            authHelper.getSignedUserRef()
        ).orElseThrow();
        Lecture lecture = instantiateNewLecture(newLectureDTO);

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

        if (tutorClass.isInternal()) {
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
        }

        lectureRepository.save(lecture);
        tutorClassRepository.save(tutorClass);

        syncLectureVideoAsync(lecture);
    }

    private void syncLectureVideoAsync(Lecture lecture) {
        taskExecutor.execute(() -> this.syncLectureVideo(lecture));
    }

    private Lecture instantiateNewLecture(NewLectureDTO newLectureDTO) {
        Lecture lecture = LectureMapper.INSTANCE.toLecture(newLectureDTO);
        lecture.setComment(DEFAULT_LECTURE_COMMENT);

        // set class & teacher code
        TutorClass tutorClass = tutorClassRepository.getReferenceById(newLectureDTO.getClassId());
        lecture.setTutorClass(tutorClass);
        lecture.setTeacherCode(configService.getConfigsByUser(tutorClass.getTeacher()).getTeacherCode());

        return lecture;
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
        lectures.forEach(this::syncLectureVideo);
    }

    private void syncLectureVideo(Lecture lecture) {
        transactionTemplate.executeWithoutResult((status) -> {
            try {
                Configuration config = configService.getConfigsByUser(lecture.getTeacher());
                Optional<DriveItem> video = videoStorageService.findLectureVideo(lecture, config.getVideoSource());
                if (video.isPresent()) {
                    // get & set video for lecture
                    lecture.setVideoId(video.get().getId());
                    lectureRepository.save(lecture);

                    log.info("Video synced for lecture {}", lecture);

                    // move video folder to processed area
                    assert video.get().getParentReference() != null;
                    DriveItem lectureFolder = new DriveItem();
                    lectureFolder.setId(video.get().getParentReference().getId());
                    videoStorageService.moveToFolder(lectureFolder, config.getProcessedVideos());
                } else {
                    log.info("No video found for lecture {}", lecture);
                }
            } catch (Exception e) {
                status.setRollbackOnly();
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

        // delete attached schedule then create new one for class if needed
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
    public LectureVideoDTO getLectureVideo(GetLectureVideoReq req) {
        // check existence & retrieve lecture
        Optional<TutorClass> tutorClass = req.getClassUid() != null
            ? tutorClassRepository.findById(Long.valueOf(idEncryptor.decrypt(req.getClassUid())))
            : tutorClassRepository.findByCode(req.getClassCode());
        if (tutorClass.isEmpty()) {
            throw new ClientVisibleException(HttpStatus.NOT_FOUND, "404", "{tutor_class.not_found}");
        }
        Specification<Lecture> specification = Specification.allOf(
            ofClass(tutorClass.get()),
            req.getLecture() != null ? withLectureNo(req.getLecture()) : startAt(req.getTimestamp())
        );
        Lecture lecture = lectureRepository.findOne(specification)
            .orElseThrow(() -> new ClientVisibleException(HttpStatus.NOT_FOUND, "404", "{lecture.not_found}"));

        // get lecture video details and return
        try {
            CompletableFuture<String> urlPromise = CompletableFuture.supplyAsync(
                () -> getVideoPreview(lecture),
                taskExecutor
            );
            CompletableFuture<LectureVideoMetadataDTO> metadataPromise = CompletableFuture.supplyAsync(
                () -> getVideoMetadata(lecture),
                taskExecutor
            );

            return LectureVideoDTO.builder()
                .url(urlPromise.get())
                .isIframe(lecture.getVideoId() != null)
                .metadata(metadataPromise.get())
                .build();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to fetch video info for lecture #{}", lecture.getId(), e);
            throw new RuntimeException(e);
        }
    }

    private String getVideoPreview(Lecture lecture) {
        return lecture.getVideoId() != null
            ? videoStorageService.createPreviewLink(lecture.getVideoId())
            : lecture.getVideo();
    }

    private LectureVideoMetadataDTO getVideoMetadata(Lecture lecture) {
        if (lecture.getVideo() != null) return fetchMetadataInfo(lecture.getVideo());
        if (lecture.getVideoId() != null) return videoStorageService.getMetadata(lecture.getVideoId());
        return LectureVideoMetadataDTO.empty();
    }

    private LectureVideoMetadataDTO fetchMetadataInfo(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            String title = doc.select("meta[property=og:title]").attr("content");
            String description = doc.select("meta[property=og:description]").attr("content");
            String image = doc.select("meta[property=og:image]").attr("content");

            return LectureVideoMetadataDTO.builder()
                .title(title)
                .description(description)
                .thumbnailUrl(image)
                .build();
        }  catch (Exception e) {
            log.error("Error occurred while fetching metadata info - url: {}", url, e);
            throw new RuntimeException(e);
        }
    }
}
