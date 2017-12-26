package ru.kulikovman.tasklist.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import ru.kulikovman.tasklist.R;
import ru.kulikovman.tasklist.TaskListActivity;

public class TaskNotification {

    private static final String NOTIFICATION_TAG = "taskNotification";

    public static void notify(final Context context, final String importantTasks, final int todayTasks) {
        final Resources res = context.getResources();

        final String ticker = importantTasks;
        final String title = res.getString(R.string.task_notification_title);
        final String text = res.getString(R.string.task_notification_message, importantTasks);

        final Notification.Builder builder = new Notification.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_event_note_white_24dp)
                .setColor(res.getColor(R.color.notification_icon_background))
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setTicker(ticker)
                .setNumber(todayTasks)
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        new Intent(context, TaskListActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))

                // Show expanded text content on devices running Android 4.1 or later.
                .setStyle(new Notification.BigTextStyle()
                        .bigText(text)
                        .setBigContentTitle(title)
                        .setSummaryText(res.getString(R.string.task_notification_counter_title)))

                // Automatically dismiss the notification when it is touched.
                .setAutoCancel(true);

        notify(context, builder.build());
    }

    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert nm != null;
        nm.notify(NOTIFICATION_TAG, 0, notification);

    }

    /**
     * Cancels any notifications of this type previously shown using
     * {@link #notify(Context, String, int)}.
     */
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert nm != null;
        nm.cancel(NOTIFICATION_TAG, 0);
    }
}
