package ru.kulikovman.tasklist.dialogs.task;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.Calendar;

import io.realm.Realm;
import ru.kulikovman.tasklist.Helper;
import ru.kulikovman.tasklist.R;
import ru.kulikovman.tasklist.models.Task;

public class DateDialog extends DialogFragment {
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
        final String today = getString(R.string.date_today);
        final String tomorrow = getString(R.string.date_tomorrow);
        final String pickDate = getString(R.string.date_pick_date);
        final String withoutDate = getString(R.string.date_not);

        final String date[] = {today, tomorrow, pickDate, withoutDate};

        // Создаем диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.date_title)
                .setItems(date, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("log", String.valueOf(which));

                        // Открываем транзакцию
                        mRealm.beginTransaction();

                        // Получаем текущую дату
                        Calendar calendar = Helper.getTodayCalendarWithoutTime();

                        switch (which) {
                            case 0: // На сегодня
                                mTask.setTargetDate(calendar.getTimeInMillis());
                                break;
                            case 1: // На завтра
                                calendar.add(Calendar.DATE, 1);
                                mTask.setTargetDate(calendar.getTimeInMillis());
                                break;
                            case 2: // Выбрать дату
                                DialogFragment datePickerFragment = new TaskDatePickerDialog();
                                Bundle args = new Bundle();
                                args.putLong("taskId", mTask.getId());
                                datePickerFragment.setArguments(args);
                                datePickerFragment.show(getFragmentManager(), "datePicker");
                                break;
                            case 3: // Без даты
                                mTask.setTargetDate(Long.MAX_VALUE);
                                mTask.setReminder(false);
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
