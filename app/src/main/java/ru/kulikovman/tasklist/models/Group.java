package ru.kulikovman.tasklist.models;


import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Group extends RealmObject {
    public static final String ID = "mId";
    public static final String NAME = "mName";
    public static final String DESCRIPTION = "mDescription";
    public static final String COLOR = "mColor";
    public static final String TASKS = "mTasks";
    public static final String COUNT_TASK = "mCountTask";

    @PrimaryKey
    private long mId;

    private String mName;
    private String mDescription;
    private String mColor;
    private RealmList<Task> mTasks;
    private int mCountTask;

    public Group(long id, String name, String description, String color) {
        mId = id;
        mName = name;
        mDescription = description;
        mColor = color;
    }

    public Group(String name, String description, String color) {
        mId = System.currentTimeMillis();
        mName = name;
        mDescription = description;
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

    public RealmList<Task> getTasks() {
        return mTasks;
    }

    public int getCountTask() {
        return mCountTask;
    }

    public void setCountTask(int countTask) {
        mCountTask = countTask;
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

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }


    // Вспомогательные методы
    public void addTask(Task task) {
        mTasks.add(task);
        mCountTask++;
    }

    public void deleteTask(Task task) {
        if (mTasks.contains(task)) {
            mTasks.remove(task);
            mCountTask--;
        }
    }

    public int getUnfinishedTasks() {
        int count = 0;
        for (Task task : mTasks) {
            if (!task.isDone()) {
                count++;
            }
        }
        return count;
    }
}
