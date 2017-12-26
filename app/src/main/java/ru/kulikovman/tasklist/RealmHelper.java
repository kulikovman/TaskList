package ru.kulikovman.tasklist;


import java.util.HashMap;
import java.util.Map;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import ru.kulikovman.tasklist.models.Group;
import ru.kulikovman.tasklist.models.Task;

public class RealmHelper {
    private static RealmHelper sRealmHelper;
    private Realm mRealm;

    public static RealmHelper get() {
        if (sRealmHelper == null) {
            sRealmHelper = new RealmHelper();
        }
        return sRealmHelper;
    }

    private RealmHelper() {
        mRealm = Realm.getDefaultInstance();
    }


    OrderedRealmCollection<Task> getUnfinishedTasks() {
        return mRealm.where(Task.class)
                .equalTo(Task.DONE, false)
                .findAll()
                .sort(new String[]{Task.TARGET_DATE, Task.PRIORITY, Task.TITLE},
                        new Sort[]{Sort.ASCENDING, Sort.DESCENDING, Sort.ASCENDING});
    }

    OrderedRealmCollection<Task> getTasksByGroup(long groupId) {
        return mRealm.where(Task.class)
                .equalTo(Task.DONE, false)
                .equalTo(Task.GROUP_ID, groupId)
                .findAll()
                .sort(new String[]{Task.TARGET_DATE, Task.PRIORITY, Task.TITLE},
                        new Sort[]{Sort.ASCENDING, Sort.DESCENDING, Sort.ASCENDING});
    }

    OrderedRealmCollection<Task> getIncomeTasks() {
        return mRealm.where(Task.class)
                .equalTo(Task.DONE, false)
                .equalTo(Task.GROUP_ID, 0)
                .findAll()
                .sort(new String[]{Task.TARGET_DATE, Task.PRIORITY, Task.TITLE},
                        new Sort[]{Sort.ASCENDING, Sort.DESCENDING, Sort.ASCENDING});
    }

    OrderedRealmCollection<Task> getTodayTasks() {
        long todayDate = DateHelper.getTodayCalendarWithoutTime().getTimeInMillis();
        return mRealm.where(Task.class)
                .equalTo(Task.DONE, false)
                .lessThanOrEqualTo(Task.TARGET_DATE, todayDate)
                .findAll()
                .sort(new String[]{Task.TARGET_DATE, Task.PRIORITY, Task.TITLE},
                        new Sort[]{Sort.ASCENDING, Sort.DESCENDING, Sort.ASCENDING});
    }

    OrderedRealmCollection<Task> getMonthTasks() {
        long monthDate = DateHelper.getAfterMonthCalendarWithoutTime().getTimeInMillis();
        return mRealm.where(Task.class)
                .equalTo(Task.DONE, false)
                .lessThanOrEqualTo(Task.TARGET_DATE, monthDate)
                .or()
                .equalTo(Task.DONE, false)
                .equalTo(Task.TARGET_DATE, Long.MAX_VALUE)
                .findAll()
                .sort(new String[]{Task.TARGET_DATE, Task.PRIORITY, Task.TITLE},
                        new Sort[]{Sort.ASCENDING, Sort.DESCENDING, Sort.ASCENDING});
    }

    OrderedRealmCollection<Group> getNotEmptyGroups() {
        return mRealm.where(Group.class)
                .notEqualTo(Group.TASK_COUNTER, 0)
                .findAll()
                .sort(new String[]{Group.TASK_COUNTER, Group.NAME},
                        new Sort[]{Sort.DESCENDING, Sort.ASCENDING});
    }

    Map<String, Integer> getTaskCounters() {
        Map<String, Integer> counters = new HashMap<>();

        // Получаем даты на сегодня и плюс месяц
        long todayDate = DateHelper.getTodayCalendarWithoutTime().getTimeInMillis();
        long monthDate = DateHelper.getAfterMonthCalendarWithoutTime().getTimeInMillis();

        // Получаем количество незавершенных задач
        RealmResults<Task> unfinishedTasks = mRealm.where(Task.class)
                .equalTo(Task.DONE, false)
                .findAll();

        // Сохраняем количество
        counters.put("unfinishedTasks", unfinishedTasks.size());
        counters.put("incomeTasks", unfinishedTasks.where().equalTo(Task.GROUP_ID, 0).findAll().size());
        counters.put("todayTasks", unfinishedTasks.where().lessThanOrEqualTo(Task.TARGET_DATE, todayDate).findAll().size());
        counters.put("monthTasks", unfinishedTasks.where().lessThanOrEqualTo(Task.TARGET_DATE, monthDate).findAll().size());

        return counters;
    }
}
