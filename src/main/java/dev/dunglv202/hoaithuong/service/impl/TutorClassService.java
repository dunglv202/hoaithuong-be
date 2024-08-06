package dev.dunglv202.hoaithuong.service.impl;

import dev.dunglv202.hoaithuong.dto.DetailClassDTO;
import dev.dunglv202.hoaithuong.dto.NewTutorClassDTO;
import dev.dunglv202.hoaithuong.dto.TutorClassDTO;
import dev.dunglv202.hoaithuong.dto.UpdatedTutorClassDTO;
import dev.dunglv202.hoaithuong.entity.Student;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.entity.TutorClass_;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.mapper.TutorClassMapper;
import dev.dunglv202.hoaithuong.model.Page;
import dev.dunglv202.hoaithuong.model.Pagination;
import dev.dunglv202.hoaithuong.model.TutorClassCriteria;
import dev.dunglv202.hoaithuong.repository.StudentRepository;
import dev.dunglv202.hoaithuong.repository.TutorClassRepository;
import dev.dunglv202.hoaithuong.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class TutorClassService {
    private final TutorClassRepository tutorClassRepository;
    private final StudentRepository studentRepository;
    private final ScheduleService scheduleService;

    @Transactional
    public void addNewClass(NewTutorClassDTO newTutorClassDTO) {
        TutorClass tutorClass = newTutorClassDTO.toEntity();

        Student student = studentRepository.findById(newTutorClassDTO.getStudentId())
            .orElseThrow(() -> new ClientVisibleException("{student.not_found}"));
        tutorClass.setStudent(student);
        
        if (tutorClassRepository.existsByCode(newTutorClassDTO.getCode())) {
            throw new ClientVisibleException("{tutor_class.code.existed}");
        }

        tutorClassRepository.save(tutorClass);
        scheduleService.addClassToMySchedule(tutorClass, newTutorClassDTO.getStartDate());
    }

    public Page<TutorClassDTO> getAllClasses(TutorClassCriteria criteria, Pagination pagination) {
        Sort activeFirst = Sort.by(Sort.Direction.DESC, TutorClass_.ACTIVE);
        Sort moreLearnedFirst = Sort.by(Sort.Direction.DESC, TutorClass_.LEARNED);
        Pageable pageable = pagination.withSort(activeFirst.and(moreLearnedFirst)).pageable();
        return new Page<>(
            tutorClassRepository.findAll(criteria.toSpecification(), pageable)
                .map(TutorClassDTO::new)
        );
    }

    @Transactional
    public void updateClass(UpdatedTutorClassDTO updated) {
        TutorClass old = tutorClassRepository.findById(updated.getId()).orElseThrow();

        if (!old.getCode().equals(updated.getCode()) && tutorClassRepository.existsByCode(updated.getCode())) {
            throw new ClientVisibleException("{tutor_class.code.existed}");
        }

        if (!updated.getTimeSlots().equals(new HashSet<>(old.getTimeSlots()))) {
            scheduleService.updateScheduleForClass(old, updated.getStartDate(), updated.getTimeSlots());
        }

        old.merge(updated);
    }

    public DetailClassDTO getDetailClass(long id) {
        TutorClass tutorClass = tutorClassRepository.findById(id).orElseThrow();
        return TutorClassMapper.INSTANCE.toDetailClassDTO(tutorClass);
    }
}
