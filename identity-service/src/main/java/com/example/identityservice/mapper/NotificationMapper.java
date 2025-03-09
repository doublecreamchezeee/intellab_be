package com.example.identityservice.mapper;

import com.example.identityservice.dto.response.notification.NotificationResponse;
import com.example.identityservice.model.Notification;
import org.mapstruct.Mapper;

@Mapper( componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toNotificationResponse(Notification notification);
}
