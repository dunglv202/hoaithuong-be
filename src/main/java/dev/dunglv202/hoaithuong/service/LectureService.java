package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.LectureDTO;
import dev.dunglv202.hoaithuong.dto.NewLectureDTO;
import dev.dunglv202.hoaithuong.dto.UpdatedLecture;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.model.Range;
import dev.dunglv202.hoaithuong.model.ReportRange;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

public interface LectureService {
    void addNewLecture(NewLectureDTO newLectureDTO);

    List<LectureDTO> getAllLectures(ReportRange range);

    void updateLecture(UpdatedLecture updatedLecture);

    void syncMyLectureVideos(@Valid ReportRange range);

    void syncLectureVideos(User teacher, Range<LocalDate> range);
}
