package ru.kulikovman.tasklist.Models;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Group extends RealmObject {
    public static final String ID = "mId";
    public static final String NAME = "mName";
    public static final String DESCRIPTION = "mDescription";
    public static final String COLOR = "mColor";

    @PrimaryKey
    private long mId;

    private String mName;
    private String mDescription;
    private String mColor;

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
}
