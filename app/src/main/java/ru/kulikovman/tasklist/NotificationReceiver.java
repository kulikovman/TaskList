package ru.kulikovman.tasklist;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import io.realm.RealmResults;
import ru.kulikovman.tasklist.models.Task;
import ru.kulikovman.tasklist.notification.TaskNotification;
import ru.kulikovman.tasklist.notification.TaskNotificationList;


public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("log", "Запущен onReceive в NotificationReceiver");

        // Получаем список задач на сегодня с напоминаниями
        RealmResults<Task> tasksList = RealmHelper.get().getNotificationTasks();

        // Если список не пустой
        if (tasksList.size() != 0) {
            // Формируем заголовок и сообщение
            String title, message;

            if (tasksList.size() == 1) {
                title = "Напоминание о задаче";
                message = tasksList.get(0).getTitle();
            } else {
                title = "Напоминание о задачах - " + tasksList.size() + " шт.";
                message = "Нажмите, для просмотра";
            }

            //TaskNotification taskNotification = new TaskNotification();
            TaskNotificationList.notify(context, "Пример текста", 125);





            /*// Инициализация уведомления
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            Intent finishIntent = new Intent(context, TaskListActivity.class);
            finishIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, finishIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder builder = new Notification.Builder(context);
            builder.setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_event_black_24dp)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL);

            notificationManager.notify(100, builder.build());*/

        }
    }
}
