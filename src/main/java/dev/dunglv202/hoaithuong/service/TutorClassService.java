package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.DetailClassDTO;
import dev.dunglv202.hoaithuong.dto.NewTutorClassDTO;
import dev.dunglv202.hoaithuong.dto.TutorClassDTO;
import dev.dunglv202.hoaithuong.dto.UpdatedTutorClassDTO;
import dev.dunglv202.hoaithuong.model.Page;
import dev.dunglv202.hoaithuong.model.Pagination;
import dev.dunglv202.hoaithuong.model.criteria.TutorClassCriteria;

import java.time.LocalDate;

public interface TutorClassService {
    void addNewClass(NewTutorClassDTO newTutorClassDTO);

    Page<TutorClassDTO> getAllClasses(TutorClassCriteria criteria, Pagination pagination);

    DetailClassDTO getDetailClass(long id);

    void updateClass(UpdatedTutorClassDTO updated);

    void stopClass(long id, LocalDate effectiveDate);

    void resumeClass(long id, LocalDate effectiveDate);
}
