package ru.kulikovman.tasklist.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import io.realm.Realm;
import ru.kulikovman.tasklist.R;
import ru.kulikovman.tasklist.models.Task;

public class RepeatDialog extends DialogFragment {
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
        final String day = getString(R.string.repeat_day);
        final String week = getString(R.string.repeat_week);
        final String month = getString(R.string.repeat_month);
        final String year = getString(R.string.repeat_year);
        final String not = getString(R.string.repeat_not);

        final String repeat[] = {day, week, month, year, not};

        // Создаем диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.repeat_title)
                .setItems(repeat, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Открываем транзакцию
                        mRealm.beginTransaction();

                        switch (which) {
                            case 0: // Ежедневно
                                mTask.setRepeat("day");
                                break;
                            case 1: // Каждую неделю
                                mTask.setRepeat("week");
                                break;
                            case 2: // Раз в месяц
                                mTask.setRepeat("month");
                                break;
                            case 3: // Через год
                                mTask.setRepeat("year");
                                break;
                            case 4: // Не повторять
                                mTask.setRepeat(null);
                                break;
                        }

                        // Закрываем транзакцию
                        mRealm.commitTransaction();
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
