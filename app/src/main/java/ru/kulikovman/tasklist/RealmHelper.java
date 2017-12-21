package ru.kulikovman.tasklist;


import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
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

    private RealmResults<Task> mTaskResult;
    private RealmResults<Group> mGroupResult;

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
                        new Sort[]{Sort.ASCENDING, Sort.ASCENDING, Sort.ASCENDING});
    }

    OrderedRealmCollection<Task> getTasksByGroup(long groupId) {
        return mRealm.where(Task.class)
                .equalTo(Task.DONE, false)
                .equalTo(Task.GROUP_ID, groupId)
                .findAll()
                .sort(new String[]{Task.TARGET_DATE, Task.PRIORITY, Task.TITLE},
                        new Sort[]{Sort.ASCENDING, Sort.ASCENDING, Sort.ASCENDING});
    }

    OrderedRealmCollection<Task> getIncomeTasks() {
        return mRealm.where(Task.class)
                .equalTo(Task.DONE, false)
                .equalTo(Task.GROUP_ID, 0)
                .findAll()
                .sort(new String[]{Task.TARGET_DATE, Task.PRIORITY, Task.TITLE},
                        new Sort[]{Sort.ASCENDING, Sort.ASCENDING, Sort.ASCENDING});
    }

    OrderedRealmCollection<Task> getTodayTasks() {
        Calendar currentDate = Helper.getTodayCalendarWithoutTime();
        return mRealm.where(Task.class)
                .equalTo(Task.DONE, false)
                .lessThanOrEqualTo(Task.TARGET_DATE, currentDate.getTimeInMillis())
                .findAll()
                .sort(new String[]{Task.TARGET_DATE, Task.PRIORITY, Task.TITLE},
                        new Sort[]{Sort.ASCENDING, Sort.ASCENDING, Sort.ASCENDING});
    }

    OrderedRealmCollection<Task> getMonthTasks() {
        Calendar afterMonthDate = Helper.getAfterMonthCalendarWithoutTime();
        return mRealm.where(Task.class)
                .equalTo(Task.DONE, false)
                .lessThanOrEqualTo(Task.TARGET_DATE, afterMonthDate.getTimeInMillis())
                .findAll()
                .sort(new String[]{Task.TARGET_DATE, Task.PRIORITY, Task.TITLE},
                        new Sort[]{Sort.ASCENDING, Sort.ASCENDING, Sort.ASCENDING});
    }

    OrderedRealmCollection<Group> getNotEmptyGroups() {
        return mRealm.where(Group.class)
                .notEqualTo(Group.COUNT_TASK, 0)
                .findAll()
                .sort(new String[]{Group.COUNT_TASK, Group.NAME},
                        new Sort[]{Sort.DESCENDING, Sort.ASCENDING});
    }

    public Map<String, Integer> getTaskCounters() {
        Map<String, Integer> counters = new HashMap<>();

        // Получаем даты на сегодня и плюс месяц
        long todayDate = Helper.getTodayCalendarWithoutTime().getTimeInMillis();
        long monthDate = Helper.getAfterMonthCalendarWithoutTime().getTimeInMillis();

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
