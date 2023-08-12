package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.telegrambot.model.Notification;
import pro.sky.telegrambot.repository.NotificationRepository;
import pro.sky.telegrambot.scheduler.Scheduler;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);


    private TelegramBot telegramBot;
    private NotificationRepository notificationRepository;
    private Scheduler scheduler;

    public TelegramBotUpdatesListener(TelegramBot telegramBot,
                                      NotificationRepository notificationRepository,
                                      Scheduler scheduler) {
        this.telegramBot = telegramBot;
        this.notificationRepository = notificationRepository;
        this.scheduler = scheduler;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            if (update.message().text().equalsIgnoreCase("/start")) {
                SendMessage greeting = new SendMessage(update.message().chat().id(),
                        "Привет, " + update.message().from().firstName() + "!");
                SendResponse response = telegramBot.execute(greeting);
            } else {
                String task = update.message().text();
                Pattern pattern = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\W+]+)");
                Matcher matcher = pattern.matcher(task);
                if (matcher.matches()) {
                    System.out.println(matcher.group(1));
                    System.out.println(matcher.group(3));


                    LocalDateTime dateTime;
                    try {
                        dateTime = LocalDateTime.parse(matcher.group(1),
                                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                        String outTask = matcher.group(3);

                        System.out.println(dateTime);
                        System.out.println(outTask);

                        Notification notification = new Notification();
                        notification.setChatId(update.message().chat().id());
                        notification.setDate(dateTime);
                        notification.setTask("НАПОМИНАНИЕ :  " + outTask);
                        notificationRepository.save(notification);
                    }catch (DateTimeParseException e) {
                        SendMessage wrongInput = new SendMessage(update.message().chat().id(),
                                "НЕВЕРНЫЙ ФОРМАТ ВВОДА!\n ВВЕДИТЕ - <<01.01.2022 20:00 ТЕКСТ СООБЩЕНИЯ>>");
                        SendResponse response = telegramBot.execute(wrongInput);
                    }
                }

            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    @Transactional
    public void checkNotification() {
        scheduler.viewTask()
                .forEach((notification -> {
                    SendMessage message = new SendMessage(notification.getChatId(),
                            notification.getTask());
                    SendResponse response = telegramBot.execute(message);
                    notificationRepository.delete(notification);
                }));
    }
}
