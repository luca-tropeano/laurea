package com.example.examapplication;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Calendar;

public class DailyNotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "DailyNotificationChannel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                "android.intent.action.MY_PACKAGE_REPLACED".equals(action)) {
            // Reimposta la notifica quotidiana dopo il riavvio o aggiornamento app
            resetAlarmFromPreferences(context);
        } else {
            int dayOfWeek = intent.getIntExtra("dayOfWeek", -1);
            showNotification(context, dayOfWeek);
        }
    }

    private void showNotification(Context context, int dayOfWeek) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Crea canale per Oreo+ solo se necessario
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Daily Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        notificationManager.createNotificationChannel(channel);

        String dayString = dayOfWeek != -1 ? getDayOfWeekString(dayOfWeek) : "oggi";

        Notification notification = new Notification.Builder(context, CHANNEL_ID)
                .setContentTitle("Ricorda di studiare!")
                .setContentText("È il momento di studiare il " + dayString + ".")
                .setSmallIcon(R.drawable.ic_notification_icon)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private String getDayOfWeekString(int dayOfWeek) {
        String[] daysOfWeek = {
                "Domenica", "Lunedì", "Martedì", "Mercoledì",
                "Giovedì", "Venerdì", "Sabato"
        };
        if (dayOfWeek >= Calendar.SUNDAY && dayOfWeek <= Calendar.SATURDAY) {
            return daysOfWeek[dayOfWeek - Calendar.SUNDAY];
        }
        return "oggi";
    }

    // Questa funzione ripristina l'allarme usando le preferenze salvate (ora/minuto)
    private void resetAlarmFromPreferences(Context context) {
        android.content.SharedPreferences prefs = context.getSharedPreferences(
                context.getPackageName() + "_preferences", Context.MODE_PRIVATE
        );
        int hour = prefs.getInt("reminderHour", -1);
        int minute = prefs.getInt("reminderMinute", -1);
        if (hour != -1 && minute != -1) {
            scheduleDailyNotification(context, hour, minute);
        }
    }

    // Pianifica la notifica giornaliera
    private void scheduleDailyNotification(Context context, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(context, DailyNotificationReceiver.class);
        intent.putExtra("dayOfWeek", calendar.get(Calendar.DAY_OF_WEEK));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }
}
