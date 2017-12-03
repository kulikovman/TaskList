package ru.kulikovman.tasklist.models;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Task extends RealmObject {
    public static final String ID = "mId";
    public static final String TITLE = "mTitle";
    public static final String PRIORITY = "mPriority";
    public static final String DONE = "mDone";
    public static final String TARGET_DATE = "mTargetDate";
    public static final String COMPLETION_DATE = "mCompletionDate";
    public static final String REPEAT = "mRepeat";
    public static final String REMINDER = "mReminder";
    public static final String GROUP = "mGroup";

    @PrimaryKey
    private long mId;

    private String mTitle;
    private long mTargetDate;
    private int mPriority;
    private Group mGroup;
    private String mRepeat;
    private boolean mReminder;
    private boolean mDone;
    private long mCompletionDate;

    public Task(String title) {
        mId = System.currentTimeMillis();
        mTitle = title;
        mTargetDate = Long.MAX_VALUE;
        mDone = false;
    }

    public Task() {
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public int getPriority() {
        return mPriority;
    }

    public void setPriority(int priority) {
        mPriority = priority;
    }

    public boolean isDone() {
        return mDone;
    }

    public void setDone(boolean done) {
        mDone = done;
    }

    public long getTargetDate() {
        return mTargetDate;
    }

    public void setTargetDate(long targetDate) {
        mTargetDate = targetDate;
    }

    public long getCompletionDate() {
        return mCompletionDate;
    }

    public void setCompletionDate(long completionDate) {
        mCompletionDate = completionDate;
    }

    public String getRepeat() {
        return mRepeat;
    }

    public void setRepeat(String repeat) {
        mRepeat = repeat;
    }

    public boolean getReminder() {
        return mReminder;
    }

    public void setReminder(boolean reminder) {
        mReminder = reminder;
    }

    public Group getGroup() {
        return mGroup;
    }

    public void setGroup(Group group) {
        mGroup = group;
    }
}
