package ru.kulikovman.tasklist.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.util.Calendar;

import io.realm.Realm;
import ru.kulikovman.tasklist.CallbackDialogFragment;
import ru.kulikovman.tasklist.DateHelper;
import ru.kulikovman.tasklist.R;
import ru.kulikovman.tasklist.models.Task;

public class DateDialog extends CallbackDialogFragment {
    private Realm mRealm;
    private Task mTask;

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

        // Подключаем базу и получаем задачу
        mRealm = Realm.getDefaultInstance();
        mTask = mRealm.where(Task.class).equalTo(Task.ID, taskId).findFirst();

        // Строки для списка вариантов
        String date[] = getResources().getStringArray(R.array.date_array);

        // Создаем диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.date_title)
                .setItems(date, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Открываем транзакцию
                        mRealm.beginTransaction();

                        // Получаем текущую дату
                        Calendar calendar = DateHelper.getTodayCalendarWithoutTime();

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

        // Запускаем код в активити
        mListener.onDialogFinish(DateDialog.this);
    }
}