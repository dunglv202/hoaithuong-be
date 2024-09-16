package dev.dunglv202.hoaithuong.service.impl;

import dev.dunglv202.hoaithuong.dto.DetailClassDTO;
import dev.dunglv202.hoaithuong.dto.NewTutorClassDTO;
import dev.dunglv202.hoaithuong.dto.TutorClassDTO;
import dev.dunglv202.hoaithuong.dto.UpdatedTutorClassDTO;
import dev.dunglv202.hoaithuong.entity.Student;
import dev.dunglv202.hoaithuong.entity.TutorClass;
import dev.dunglv202.hoaithuong.entity.TutorClass_;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.exception.ClientVisibleException;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.mapper.TutorClassMapper;
import dev.dunglv202.hoaithuong.model.Page;
import dev.dunglv202.hoaithuong.model.Pagination;
import dev.dunglv202.hoaithuong.model.criteria.TutorClassCriteria;
import dev.dunglv202.hoaithuong.repository.ScheduleRepository;
import dev.dunglv202.hoaithuong.repository.StudentRepository;
import dev.dunglv202.hoaithuong.repository.TutorClassRepository;
import dev.dunglv202.hoaithuong.service.ScheduleService;
import dev.dunglv202.hoaithuong.service.TutorClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class TutorClassServiceImpl implements TutorClassService {
    private final TutorClassRepository tutorClassRepository;
    private final StudentRepository studentRepository;
    private final ScheduleService scheduleService;
    private final ScheduleRepository scheduleRepository;
    private final AuthHelper authHelper;

    @Override
    @Transactional
    public void addNewClass(NewTutorClassDTO newTutorClassDTO) {
        User signedUser = authHelper.getSignedUser();
        TutorClass tutorClass = TutorClassMapper.INSTANCE.toTutorClass(newTutorClassDTO);
        tutorClass.setTeacher(signedUser);

        Student student = studentRepository.findById(newTutorClassDTO.getStudentId())
            .orElseThrow(() -> new ClientVisibleException("{student.not_found}"));
        tutorClass.setStudent(student);
        
        if (tutorClassRepository.existsByCode(newTutorClassDTO.getCode())) {
            throw new ClientVisibleException("{tutor_class.code.existed}");
        }

        tutorClassRepository.save(tutorClass);
        scheduleService.addSchedulesForClass(tutorClass, newTutorClassDTO.getStartDate());
    }

    @Override
    public Page<TutorClassDTO> getAllClasses(TutorClassCriteria criteria, Pagination pagination) {
        Sort activeFirst = Sort.by(Sort.Direction.DESC, TutorClass_.ACTIVE);
        Sort moreLearnedFirst = Sort.by(Sort.Direction.DESC, TutorClass_.LEARNED);

        Pageable pageable = pagination.withSort(activeFirst.and(moreLearnedFirst)).pageable();
        Specification<TutorClass> specification = TutorClassCriteria.ofTeacher(authHelper.getSignedUser())
            .and(criteria.toSpecification());

        return new Page<>(
            tutorClassRepository.findAll(specification, pageable)
                .map(TutorClassMapper.INSTANCE::toTutorClassDTO)
        );
    }

    @Override
    @Transactional
    public void updateClass(UpdatedTutorClassDTO updated) {
        User signedUser = authHelper.getSignedUser();
        TutorClass old = tutorClassRepository.findByIdAndTeacher(updated.getId(), signedUser).orElseThrow();

        if (!old.isActive()) {
            throw new ClientVisibleException("{tutor_class.update_not_allowed}");
        }

        if (!old.getCode().equals(updated.getCode()) && tutorClassRepository.existsByCode(updated.getCode())) {
            throw new ClientVisibleException("{tutor_class.code.existed}");
        }

        if (!updated.getTimeSlots().equals(new HashSet<>(old.getTimeSlots()))) {
            scheduleService.updateScheduleForClass(old, updated.getStartDate(), updated.getTimeSlots());
        }

        old.merge(updated);
    }

    @Override
    public DetailClassDTO getDetailClass(long id) {
        TutorClass tutorClass = tutorClassRepository.findByIdAndTeacher(id, authHelper.getSignedUser()).orElseThrow();
        return TutorClassMapper.INSTANCE.toDetailClassDTO(tutorClass);
    }

    @Override
    @Transactional
    public void stopClass(long id, LocalDate effectiveDate) {
        User signedUser = authHelper.getSignedUser();
        TutorClass tutorClass = tutorClassRepository.findByIdAndTeacher(id, signedUser).orElseThrow();
        if (!tutorClass.isActive()) {
            throw new ClientVisibleException("{class.inactive}");
        }
        scheduleRepository.deleteAllFromDateByClass(tutorClass, effectiveDate);
        tutorClass.setActive(false);
        tutorClassRepository.save(tutorClass);
    }

    @Override
    @Transactional
    public void resumeClass(long id, LocalDate effectiveDate) {
        User signedUser = authHelper.getSignedUser();
        TutorClass tutorClass = tutorClassRepository.findByIdAndTeacher(id, signedUser).orElseThrow();
        if (tutorClass.isActive()) {
            throw new ClientVisibleException("{class.still_active}");
        }
        scheduleService.addSchedulesForClass(tutorClass, effectiveDate);
        tutorClass.setActive(true);
        tutorClassRepository.save(tutorClass);
    }
}