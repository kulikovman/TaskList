package ru.kulikovman.tasklist;


import android.content.Context;

import java.util.Calendar;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import ru.kulikovman.tasklist.models.Group;
import ru.kulikovman.tasklist.models.Task;

public class RealmHelper {
    private static RealmHelper sRealmHelper;
    private Realm mRealm;

    private Context mContext;

    private RealmResults<Task> mTaskResult;
    private RealmResults<Group> mGroupResult;

    public static RealmHelper get(Context context) {
        if (sRealmHelper == null) {
            sRealmHelper = new RealmHelper(context);
        }
        return sRealmHelper;
    }

    private RealmHelper(Context context) {
        mContext = context.getApplicationContext();
        mRealm = Realm.getDefaultInstance();
    }

    public OrderedRealmCollection<Task> getTasksByGroup(long groupId) {
        return mRealm.where(Task.class)
                .equalTo(Task.DONE, false)
                .equalTo(Task.GROUP_ID, groupId)
                .findAll()
                .sort(new String[]{Task.TARGET_DATE, Task.PRIORITY, Task.TITLE},
                        new Sort[]{Sort.ASCENDING, Sort.ASCENDING, Sort.ASCENDING});
    }


    public static Calendar convertLongToCalendar(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);

        return calendar;
    }
}
