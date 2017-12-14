package ru.kulikovman.tasklist.dialogs;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmResults;
import ru.kulikovman.tasklist.R;
import ru.kulikovman.tasklist.models.Group;
import ru.kulikovman.tasklist.models.Task;

public class GroupDialog extends DialogFragment {
    private Realm mRealm;
    private Task mTask;
    private RealmResults<Group> mGroups;
    private DialogFragment mCreateGroup;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Получаем аргументы
        long taskId = getArguments().getLong("taskId");

        // Подключаем базу, получаем задачу и группы
        mRealm = Realm.getDefaultInstance();
        mTask = mRealm.where(Task.class).equalTo(Task.ID, taskId).findFirst();
        mGroups = mRealm.where(Group.class).findAll();

        // Готовим запуск диалога создания группы
        mCreateGroup = new CreateGroupDialog();
        Bundle args = new Bundle();
        args.putLong("taskId", mTask.getId());
        mCreateGroup.setArguments(args);

        // Проверяем наличие групп
        if (mGroups.size() == 0) {
            // Если список пуст, то создаем диалог с сообщением
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.group_message)
                    .setPositiveButton(R.string.group_button_create, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Запускаем диалог создания группы
                            mCreateGroup.show(getActivity().getSupportFragmentManager(), "mCreateGroup");
                        }
                    });

            return builder.create();

        } else {
            // Если есть группы, то готовим список
            final String names[] = new String[mGroups.size() + 1];
            for (int i = 0; i < mGroups.size(); i++) {
                names[i] = mGroups.get(i).getName();
            }

            // Добавляем элемент "Без группы"
            names[names.length - 1] = getString(R.string.group_not);

            // Создаем диалог
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.group_title)
                    .setItems(names, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Открываем транзакцию
                            mRealm.beginTransaction();

                            // Сохраняем группу в задачу
                            if (which == names.length - 1) {
                                mTask.setGroup(null);
                            } else {
                                mTask.setGroup(mGroups.get(which));
                            }

                            // Закрываем транзакцию
                            mRealm.commitTransaction();
                        }
                    })
                    .setPositiveButton(R.string.group_button_create, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Запускаем диалог создания группы
                            mCreateGroup.show(getActivity().getSupportFragmentManager(), "mCreateGroup");
                        }
                    });

            return builder.create();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();
    }
}
