package ru.kulikovman.tasklist;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.realm.OrderedRealmCollection;
import io.realm.RealmResults;
import ru.kulikovman.tasklist.models.Task;
import ru.kulikovman.tasklist.notification.TaskNotification;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Получаем нужные списки задач
        OrderedRealmCollection<Task> todayTasks = RealmHelper.get().getTodayTasks();
        RealmResults<Task> reminderTasks = todayTasks.where().equalTo(Task.REMINDER, true).findAll();

        if (reminderTasks.size() != 0) {
            // Формируем сообщение
            int count = reminderTasks.size();
            String message = context.getResources()
                    .getQuantityString(R.plurals.task_notification_important_tasks, count, count);

            // Запускаем уведомление
            TaskNotification.notify(context, message, todayTasks.size());
        }
    }
}
