package dev.dunglv202.hoaithuong.mapper;

import dev.dunglv202.hoaithuong.dto.NotificationDTO;
import dev.dunglv202.hoaithuong.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface NotificationMapper {
    NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

    NotificationDTO toNotificationDTO(Notification notification);
}
