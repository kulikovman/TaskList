package ru.kulikovman.tasklist.messages;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import ru.kulikovman.tasklist.R;
import ru.kulikovman.tasklist.models.Group;
import ru.kulikovman.tasklist.models.Task;

public class GroupHasTasks extends DialogFragment {
    private Realm mRealm;
    private Group mGroup;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Получаем аргументы
        final long groupId = getArguments().getLong("groupId");

        // Подключаем базу и получаем группу
        mRealm = Realm.getDefaultInstance();
        mGroup = mRealm.where(Group.class).equalTo(Group.ID, groupId).findFirst();

        // Создаем диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.message_group_has_tasks)
                .setPositiveButton(R.string.delete_group_save_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Удаляем только группу
                        mRealm.beginTransaction();
                        mGroup.deleteFromRealm();
                        mRealm.commitTransaction();
                    }
                })
                .setNegativeButton(R.string.delete_group_delete_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Открываем транзакцию
                        mRealm.beginTransaction();

                        // Удаляем задачи связанные с группой
                        RealmResults<Task> tasks = mRealm.where(Task.class)
                                .equalTo(Task.DONE, false)
                                .equalTo(Task.GROUP_ID, mGroup.getId())
                                .findAll();

                        tasks.deleteAllFromRealm();

                        // Удаляем группу
                        mGroup.deleteFromRealm();

                        // Закрываем транзакцию
                        mRealm.commitTransaction();
                    }
                });

        return builder.create();
    }
}
