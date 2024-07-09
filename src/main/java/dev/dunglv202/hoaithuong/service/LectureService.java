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

@Service
@RequiredArgsConstructor
public class LectureService {
    private final TutorClassRepository tutorClassRepository;
    private final LectureRepository lectureRepository;

    @Transactional
    public void addNewLecture(NewLectureDTO newLectureDTO) {
        Lecture lecture = newLectureDTO.toEntity();

        if (newLectureDTO.getStartTime().isAfter(newLectureDTO.getEndTime())) {
            throw new ClientVisibleException("{tutor_class.duration.invalid}");
        }

        TutorClass tutorClass = tutorClassRepository.findById(newLectureDTO.getClassId())
            .orElseThrow(() -> new ClientVisibleException("{tutor_class.not_exist}"));
        lecture.setTutorClass(tutorClass);

        int learned = tutorClass.getLearned() + 1;
        if (learned > tutorClass.getTotalLecture()) {
            throw new ClientVisibleException("{tutor_class.lecture.exceed}");
        } else {
            tutorClass.setLearned(learned);
        }

        lectureRepository.save(lecture);
        tutorClassRepository.save(tutorClass);
    }
}
