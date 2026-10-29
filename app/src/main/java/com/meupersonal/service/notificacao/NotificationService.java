package com.meupersonal.service.notificacao;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Date;
import java.util.Objects;

public class NotificationService {
    private static final String CANAL_ID = "meupersonal_notificacoes";

    private final Context contexto;
    private final Long userId;
    private final String title;
    private final String message;
    private final NotificationType type;
    private final Date scheduleAt;
    private final int notificationId;

    public NotificationService(
            Context contexto,
            Long userId,
            String title,
            String message,
            NotificationType type,
            Date scheduleAt,
            int notificationId
    ) {
        this.contexto = Objects.requireNonNull(contexto)
                .getApplicationContext();
        this.userId = Objects.requireNonNull(userId);
        this.title = Objects.requireNonNull(title);
        this.message = Objects.requireNonNull(message);
        this.type = Objects.requireNonNull(type);
        this.scheduleAt = scheduleAt == null
                ? new Date()
                : new Date(scheduleAt.getTime());
        this.notificationId = notificationId;

        criarCanal();
    }

    public void send() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(
                contexto,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        android.app.Notification notificacao =
                new NotificationCompat.Builder(contexto, CANAL_ID)
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_info
                        )
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(message)
                        )
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .build();

        NotificationManagerCompat.from(contexto)
                .notify(notificationId, notificacao);
    }

    public void cancel() {
        NotificationManagerCompat.from(contexto)
                .cancel(notificationId);
    }

    private void criarCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID,
                    "Notificações do MeuPersonal",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            canal.setDescription(
                    "Lembretes de treino e notificações do aplicativo."
            );

            NotificationManager gerenciador =
                    contexto.getSystemService(NotificationManager.class);

            if (gerenciador != null) {
                gerenciador.createNotificationChannel(canal);
            }
        }
    }

    public Long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public NotificationType getType() {
        return type;
    }

    public Date getScheduleAt() {
        return scheduleAt == null
                ? null
                : new Date(scheduleAt.getTime());
    }

    public int getNotificationId() {
        return notificationId;
    }
}

