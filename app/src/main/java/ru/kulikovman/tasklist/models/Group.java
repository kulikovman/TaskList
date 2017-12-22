package ru.kulikovman.tasklist.models;


import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Group extends RealmObject {
    public static final String ID = "mId";
    public static final String NAME = "mName";
    public static final String COLOR = "mColor";
    public static final String TASK_COUNTER = "mTaskCounter";
    public static final String TASK_LIST = "mTaskList";

    @PrimaryKey
    private long mId;

    private String mName;
    private String mColor;
    private RealmList<Task> mTaskList;
    private int mTaskCounter;

    public Group(long id, String name, String color) {
        mId = id;
        mName = name;
        mColor = color;
    }

    public Group(String name, String color) {
        mId = System.currentTimeMillis();
        mName = name;
        mColor = color;
    }

    public Group(String name) {
        mId = System.currentTimeMillis();
        mName = name;
    }

    public Group() {
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getColor() {
        return mColor;
    }

    public void setColor(String color) {
        mColor = color;
    }

    public RealmList<Task> getTaskList() {
        return mTaskList;
    }

    public void setTaskList(RealmList<Task> taskList) {
        mTaskList = taskList;
    }

    public int getTaskCounter() {
        return mTaskCounter;
    }

    public void setTaskCounter(int taskCounter) {
        mTaskCounter = taskCounter;
    }


    // Вспомогательные методы
    public void increaseCountTask() {
        mTaskCounter++;
    }

    public void decreaseCountTask() {
        mTaskCounter--;
    }

    public void addTask(Task task) {
        mTaskList.add(task);
        mTaskCounter = mTaskList.size();
    }

    public void removeTask(Task task) {
        if (mTaskList.contains(task)) {
            mTaskList.remove(task);
            mTaskCounter = mTaskList.size();
        }
    }

    public void deleteAllTasks(Realm realm) {
        realm.beginTransaction();
        mTaskList.deleteAllFromRealm();
        realm.commitTransaction();

        mTaskList = null;
        mTaskCounter = 0;
    }
}
