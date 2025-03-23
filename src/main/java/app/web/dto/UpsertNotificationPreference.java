package app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpsertNotificationPreference {

    @NotNull
    private UUID userId;

    private boolean notificationEnabled;

    @NotNull
    private NotificationTypeRequest notificationType;

    @NotBlank
    private String contactInfo;
}
