package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.ScheduleDTO;
import dev.dunglv202.hoaithuong.model.Range;
import dev.dunglv202.hoaithuong.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;

    public List<ScheduleDTO> getSchedule(Range<LocalDate> range) {
        return scheduleRepository.findAllInRange(range)
            .stream()
            .map(ScheduleDTO::new)
            .toList();
    }
}
