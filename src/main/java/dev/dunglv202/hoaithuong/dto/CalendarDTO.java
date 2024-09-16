package dev.dunglv202.hoaithuong.dto;

import com.google.api.services.calendar.model.CalendarListEntry;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalendarDTO {
    private String id;
    private String color;
    private String name;
    private String description;

    public CalendarDTO(CalendarListEntry calendar) {
        this.id = calendar.getId();
        this.color = calendar.getBackgroundColor();
        this.name = calendar.getSummary();
        this.description = calendar.getDescription();
    }
}
