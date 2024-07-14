package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.NewLectureDTO;
import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.repository.LectureRepository;
import dev.dunglv202.hoaithuong.repository.TutorClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureService {
    private final TutorClassRepository tutorClassRepository;
    private final LectureRepository lectureRepository;

    @Transactional
    public void addNewLecture(NewLectureDTO newLectureDTO) {
        Lecture lecture = newLectureDTO.toEntity();

        // validate duration
        if (newLectureDTO.getStartTime().isAfter(newLectureDTO.getEndTime())) {
            throw new ClientVisibleException("{tutor_class.duration.invalid}");
        }

        // set class
        TutorClass tutorClass = tutorClassRepository.findById(newLectureDTO.getClassId())
            .orElseThrow(() -> new ClientVisibleException("{tutor_class.not_exist}"));
        lecture.setTutorClass(tutorClass);

        // update learned
        int learned = tutorClass.getLearned() + 1;
        if (learned > tutorClass.getTotalLecture()) {
            throw new ClientVisibleException("{tutor_class.lecture.exceed}");
        }
        tutorClass.setLearned(learned);

        // set lecture no
        List<Lecture> lecturesAfterThis = lectureRepository.findAllByClassIdFromTime(
            lecture.getTutorClass(),
            lecture.getStartTime()
        );
        if (lecturesAfterThis.isEmpty()) {
            lecture.setLectureNo(learned);
        } else {
            // update lecture no for ones after this lecture
            lecture.setLectureNo(lecturesAfterThis.get(0).getLectureNo());
            lecturesAfterThis.forEach(lec -> lec.setLectureNo(lecture.getLectureNo() + 1));
            lectureRepository.saveAll(lecturesAfterThis);
        }

        lectureRepository.save(lecture);
        tutorClassRepository.save(tutorClass);
    }
}
