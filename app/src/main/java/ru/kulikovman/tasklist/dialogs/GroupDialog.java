package ru.kulikovman.tasklist.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import ru.kulikovman.tasklist.CallbackDialogFragment;
import ru.kulikovman.tasklist.GroupListActivity;
import ru.kulikovman.tasklist.R;
import ru.kulikovman.tasklist.models.Group;
import ru.kulikovman.tasklist.models.Task;

public class GroupDialog extends CallbackDialogFragment {
    private Realm mRealm;
    private Task mTask;
    private RealmResults<Group> mGroups;

    CallbackDialogListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (CallbackDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement CallbackDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Получаем аргументы
        long taskId = getArguments().getLong("taskId");

        // Подключаем базу, получаем задачу и группы
        mRealm = Realm.getDefaultInstance();
        mTask = mRealm.where(Task.class).equalTo(Task.ID, taskId).findFirst();
        mGroups = mRealm.where(Group.class).findAll()
                .sort(new String[]{Group.TASK_COUNTER, Group.NAME},
                        new Sort[]{Sort.DESCENDING, Sort.ASCENDING});

        // Проверяем наличие групп
        if (mGroups.size() == 0) {
            // Если список пуст, то создаем диалог с сообщением
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.group_message)
                    .setPositiveButton(R.string.group_open_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Переходим к списку групп
                            Intent intent = new Intent(getActivity(), GroupListActivity.class);
                            startActivity(intent);
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

                            // Удаляем задачу из группы
                            if (mTask.getGroup() != null) {
                                mTask.getGroup().removeTask(mTask);
                            }

                            // Удаляем группу из задачи
                            mTask.setGroup(null);

                            // Назначаем новую группу
                            if (which < names.length - 1) {
                                mTask.setGroup(mGroups.get(which));
                                mTask.getGroup().addTask(mTask);
                            }

                            // Старый вариант реализации
                            /*// Понижаем счетчик группы
                            if (mTask.getGroup() != null) {
                                mTask.getGroup().decreaseCountTask();
                            }

                            // Удаляем группу из задачи
                            mTask.setGroup(null);

                            // Назначаем группу и повышаем в ней счетчик
                            if (which < names.length - 1) {
                                mTask.setGroup(mGroups.get(which));
                                mTask.getGroup().increaseCountTask();
                            }*/

                            // Закрываем транзакцию
                            mRealm.commitTransaction();
                        }
                    })
                    .setPositiveButton(R.string.group_open_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Переходим к списку групп
                            Intent intent = new Intent(getActivity(), GroupListActivity.class);
                            startActivity(intent);
                        }
                    });

            return builder.create();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();

        // Запускаем код в активити
        mListener.onDialogFinish(GroupDialog.this);
    }
}
