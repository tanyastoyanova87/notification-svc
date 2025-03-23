package app.service;

import app.model.Notification;
import app.model.NotificationPreference;
import app.model.NotificationStatus;
import app.model.NotificationType;
import app.repository.NotificationPreferenceRepository;
import app.repository.NotificationRepository;
import app.web.dto.NotificationRequest;
import app.web.dto.UpsertNotificationPreference;
import app.web.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final NotificationRepository notificationRepository;
    private final MailSender mailSender;

    @Autowired
    public NotificationService(NotificationPreferenceRepository notificationPreferenceRepository, NotificationRepository notificationRepository, MailSender mailSender) {
        this.notificationPreferenceRepository = notificationPreferenceRepository;
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
    }

    public NotificationPreference upsertPreference(UpsertNotificationPreference upsertNotificationPreference) {
        Optional<NotificationPreference> optionalNotificationPreference = notificationPreferenceRepository.findByUserId(upsertNotificationPreference.getUserId());

        if (optionalNotificationPreference.isPresent()) {
            NotificationPreference notificationPreference = optionalNotificationPreference.get();
            notificationPreference.setNotificationType(DtoMapper.fromNotificationTypeRequest(upsertNotificationPreference.getNotificationType()));
            notificationPreference.setEnabled(upsertNotificationPreference.isNotificationEnabled());
            notificationPreference.setContactInfo(upsertNotificationPreference.getContactInfo());
            notificationPreference.setUpdatedOn(LocalDateTime.now());
            return notificationPreferenceRepository.save(notificationPreference);
        }

        NotificationPreference notificationPreference = NotificationPreference.builder()
                .userId(upsertNotificationPreference.getUserId())
                .notificationType(DtoMapper.fromNotificationTypeRequest(upsertNotificationPreference.getNotificationType()))
                .isEnabled(upsertNotificationPreference.isNotificationEnabled())
                .contactInfo(upsertNotificationPreference.getContactInfo())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        return notificationPreferenceRepository.save(notificationPreference);
    }

    public NotificationPreference getPreferenceByUserId(UUID userId) {
        return notificationPreferenceRepository
                .findByUserId(userId)
                .orElseThrow(() -> new NullPointerException("Notification preference for user with id %s was not found."
                        .formatted(userId)));
    }

    public Notification sendNotification(NotificationRequest notificationRequest) throws IllegalAccessException {

        UUID userId = notificationRequest.getUserId();
        NotificationPreference notificationPreference = getPreferenceByUserId(userId);

        if (!notificationPreference.isEnabled()) {
            throw new IllegalAccessException("User with id %s does not allow notifications.".formatted(userId));
        }

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(notificationPreference.getContactInfo());
        mailMessage.setSubject(notificationRequest.getSubject());
        mailMessage.setText(notificationRequest.getBody());

        Notification notification = Notification.builder()
                .userId(userId)
                .subject(notificationRequest.getSubject())
                .body(notificationRequest.getBody())
                .createdOn(LocalDateTime.now())
                .type(NotificationType.EMAIL)
                .deleted(false)
                .build();

        try {
            mailSender.send(mailMessage);
            notification.setStatus(NotificationStatus.SUCCEEDED);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
        }

        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationHistory(UUID userId) {
       return notificationRepository.findAllByUserIdAndDeletedFalse(userId);
    }
}
