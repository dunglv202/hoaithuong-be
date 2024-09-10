package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.LectureDTO;
import dev.dunglv202.hoaithuong.dto.NewLectureDTO;
import dev.dunglv202.hoaithuong.dto.UpdatedLecture;
import dev.dunglv202.hoaithuong.model.ReportRange;

import java.util.List;

public interface LectureService {
    void addNewLecture(NewLectureDTO newLectureDTO);

    List<LectureDTO> getAllLectures(ReportRange range);

    void updateLecture(UpdatedLecture updatedLecture);
}
