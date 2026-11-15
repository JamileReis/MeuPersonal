package com.meupersonal.service.notificacao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WorkoutReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context contexto, Intent intent) {
        if (intent == null) {
            return;
        }

        long userId = intent.getLongExtra("userId", -1L);
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        int notificationId = intent.getIntExtra(
                "notificationId",
                0
        );

        if (userId < 0 || title == null || message == null) {
            return;
        }

        NotificationService notificacao = new NotificationService(
                contexto,
                userId,
                title,
                message,
                NotificationType.WORKOUT_REMINDER,
                null,
                notificationId
        );

        notificacao.send();
    }
}