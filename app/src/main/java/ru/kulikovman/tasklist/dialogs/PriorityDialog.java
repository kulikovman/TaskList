package ru.kulikovman.tasklist.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import io.realm.Realm;
import ru.kulikovman.tasklist.R;
import ru.kulikovman.tasklist.models.Task;

public class PriorityDialog extends DialogFragment {
    private Realm mRealm;
    private Task mTask;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Получаем аргументы
        long taskId = getArguments().getLong("taskId");

        // Подключаем базу и получаем задачу
        mRealm = Realm.getDefaultInstance();
        mTask = mRealm.where(Task.class).equalTo(Task.ID, taskId).findFirst();

        // Строки для списка вариантов
        final String emergency = getString(R.string.priority_emergency);
        final String high = getString(R.string.priority_high);
        final String common = getString(R.string.priority_common);
        final String low = getString(R.string.priority_low);
        final String lowest = getString(R.string.priority_lowest);

        final String priority[] = {emergency, high, common, low, lowest};

        // Создаем диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.priority_title)
                .setItems(priority, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Открываем транзакцию
                        mRealm.beginTransaction();

                        switch (which) {
                            case 0: // Чрезвычайный
                                mTask.setPriority(0);
                                break;
                            case 1: // Высокий
                                mTask.setPriority(1);
                                break;
                            case 2: // Обычный
                                mTask.setPriority(2);
                                break;
                            case 3: // Низкий
                                mTask.setPriority(3);
                                break;
                            case 4: // Самый низкий
                                mTask.setPriority(4);
                                break;
                        }

                        // Закрываем транзакцию
                        mRealm.commitTransaction();

                        //getActivity().m
                    }
                });

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();
    }
}