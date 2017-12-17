package ru.kulikovman.tasklist;


import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class MyMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        // Получаем текущую структуру
        RealmSchema schema = realm.getSchema();

        // Вносим изменения
        if (!schema.get("Group").hasField("mTasks")) {
            schema.get("Group").addRealmListField("mTasks", schema.get("Task"));
        }

        if (!schema.get("Group").hasField("TASKS")) {
            schema.get("Group").addRealmListField("TASKS", String.class);
        }
    }
}
