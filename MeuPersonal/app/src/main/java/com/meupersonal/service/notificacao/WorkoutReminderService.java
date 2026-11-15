package com.meupersonal.service.notificacao;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WorkoutReminderService {

    private final Context contexto;
    private final AlarmManager gerenciadorAlarmes;
    private final Map<Integer, NotificationService> lembretes =
            new HashMap<>();

    public WorkoutReminderService(Context contexto) {
        this.contexto = Objects.requireNonNull(contexto)
                .getApplicationContext();

        this.gerenciadorAlarmes =
                (AlarmManager) this.contexto.getSystemService(
                        Context.ALARM_SERVICE
                );
    }

    public void createReminder(Long userId, Long workoutId) {
        Date dataAgendada = new Date(
                System.currentTimeMillis() + 60L * 60L * 1000L
        );

        createReminder(userId, workoutId, dataAgendada);
    }

    public void createReminder(
            Long userId,
            Long workoutId,
            Date dataAgendada
    ) {
        Objects.requireNonNull(userId, "O ID do usuário é obrigatório.");
        Objects.requireNonNull(workoutId, "O ID do treino é obrigatório.");
        Objects.requireNonNull(
                dataAgendada,
                "A data do lembrete é obrigatória."
        );

        if (dataAgendada.getTime() <= System.currentTimeMillis()) {
            throw new IllegalArgumentException(
                    "A data do lembrete deve estar no futuro."
            );
        }

        int idNotificacao = Objects.hash(userId, workoutId);

        NotificationService notificacao = new NotificationService(
                contexto,
                userId,
                "Hora de treinar!",
                "Está na hora de realizar seu treino.",
                NotificationType.WORKOUT_REMINDER,
                dataAgendada,
                idNotificacao
        );

        Intent intent = new Intent(
                contexto,
                WorkoutReminderReceiver.class
        );

        intent.putExtra("userId", userId);
        intent.putExtra("workoutId", workoutId);
        intent.putExtra("notificationId", idNotificacao);
        intent.putExtra("title", notificacao.getTitle());
        intent.putExtra("message", notificacao.getMessage());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                contexto,
                idNotificacao,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );

        if (gerenciadorAlarmes == null) {
            throw new IllegalStateException(
                    "O serviço de alarmes não está disponível."
            );
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && !gerenciadorAlarmes.canScheduleExactAlarms()) {
            gerenciadorAlarmes.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    dataAgendada.getTime(),
                    pendingIntent
            );
        } else {
            gerenciadorAlarmes.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    dataAgendada.getTime(),
                    pendingIntent
            );
        }

        lembretes.put(idNotificacao, notificacao);
    }

    public void cancelReminder(Long reminderId) {
        if (reminderId == null) {
            throw new IllegalArgumentException(
                    "O ID do lembrete não pode ser nulo."
            );
        }

        int id = reminderId.intValue();

        Intent intent = new Intent(
                contexto,
                WorkoutReminderReceiver.class
        );

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                contexto,
                id,
                intent,
                PendingIntent.FLAG_NO_CREATE
                        | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            if (gerenciadorAlarmes != null) {
                gerenciadorAlarmes.cancel(pendingIntent);
            }

            pendingIntent.cancel();
        }

        NotificationService notificacao = lembretes.remove(id);

        if (notificacao != null) {
            notificacao.cancel();
        }
    }

    public List<NotificationService> listReminders() {
        return new ArrayList<>(lembretes.values());
    }
}
