package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.LectureDTO;
import dev.dunglv202.hoaithuong.dto.NewLectureDTO;
import dev.dunglv202.hoaithuong.entity.Lecture;
import dev.dunglv202.hoaithuong.entity.Lecture_;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.model.LectureCriteria;
import dev.dunglv202.hoaithuong.model.ReportRange;
import dev.dunglv202.hoaithuong.repository.LectureRepository;
import dev.dunglv202.hoaithuong.repository.TutorClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dev.dunglv202.hoaithuong.model.LectureCriteria.from;

@Service
@RequiredArgsConstructor
public class LectureService {
    private final TutorClassRepository tutorClassRepository;
    private final LectureRepository lectureRepository;

    @Transactional
    public void addNewLecture(NewLectureDTO newLectureDTO) {
        Lecture lecture = newLectureDTO.toEntity();

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

        lectureRepository.save(lecture);
        tutorClassRepository.save(tutorClass);
    }

    public List<LectureDTO> getAllLectures(ReportRange range) {
        Sort sortByStartTimeDesc = Sort.by(Sort.Direction.DESC, Lecture_.START_TIME);

        return lectureRepository.findAll(LectureCriteria.inRange(range), sortByStartTimeDesc)
            .stream()
            .map(LectureDTO::new)
            .toList();
    }
}
