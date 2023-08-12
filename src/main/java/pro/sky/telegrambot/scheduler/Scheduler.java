package pro.sky.telegrambot.scheduler;

import org.springframework.stereotype.Component;
import pro.sky.telegrambot.model.Notification;
import pro.sky.telegrambot.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Scheduler {
    private NotificationRepository notificationRepository;

    public Scheduler(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> viewTask() {
        return notificationRepository.findAll().stream()
                .filter(task -> task.getDate().equals(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)))
                .collect(Collectors.toList());
    }
}
