package app.web.mapper;

import app.model.Notification;
import app.model.NotificationPreference;
import app.model.NotificationType;
import app.web.dto.NotificationPreferenceResponse;
import app.web.dto.NotificationResponse;
import app.web.dto.NotificationTypeRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public NotificationType fromNotificationTypeRequest(NotificationTypeRequest notificationTypeRequest) {
       return switch (notificationTypeRequest) {
            case EMAIL -> NotificationType.EMAIL;
        };
    }

    public NotificationPreferenceResponse fromNotificationPreference(NotificationPreference notificationPreference) {
        return NotificationPreferenceResponse.builder()
                .id(notificationPreference.getId())
                .userId(notificationPreference.getUserId())
                .notificationType(notificationPreference.getNotificationType())
                .contactInfo(notificationPreference.getContactInfo())
                .enabled(notificationPreference.isEnabled())
                .build();
    }

    public NotificationResponse fromNotification(Notification notification) {
        return NotificationResponse.builder()
                .subject(notification.getSubject())
                .status(notification.getStatus())
                .type(notification.getType())
                .createdOn(notification.getCreatedOn())
                .build();
    }
}
