package com.example.examapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExamNotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "exam_reminders_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        android.util.Log.d("EXAM_RECEIVER", "onReceive chiamato");

        String examName = intent.getStringExtra("examName");
        String examId = intent.getStringExtra("examId");
        long examDateMillis = intent.getLongExtra("examDateMillis", 0);

        if (examName == null) examName = "Esame";

        createNotificationChannel(context);

        // Formatta data/ora esame
        String examTimeText = "";
        if (examDateMillis > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALY);
            examTimeText = sdf.format(new Date(examDateMillis));
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                examId != null ? examId.hashCode() : 0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("🔔 Promemoria esame")
                .setContentText(examName + " - " + examTimeText)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("È il momento per l'esame:\n" + examName + "\n📅 " + examTimeText))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        Notification notification = builder.build();

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            int notificationId = examId != null ? examId.hashCode() : (int) System.currentTimeMillis();
            manager.notify(notificationId, notification);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "Promemoria esami";
            String description = "Notifiche per gli esami/verifiche";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
