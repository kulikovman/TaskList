package ru.kulikovman.tasklist.dialogs;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;

import io.realm.Realm;
import ru.kulikovman.tasklist.models.Task;

public class TaskDatePickerDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private Realm mRealm;
    private Task mTask;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Получаем аргументы
        long taskId = getArguments().getLong("taskId");

        // Подключаем базу и получаем задачу
        mRealm = Realm.getDefaultInstance();
        mTask = mRealm.where(Task.class).equalTo(Task.ID, taskId).findFirst();

        // Получаем сегодняшнюю дату
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Создаем и возвращаем новый DatePickerDialog
        DatePickerDialog datePicker = new DatePickerDialog(getActivity(), this, year, month, day);
        datePicker.getDatePicker().setMinDate(calendar.getTimeInMillis());

        return datePicker;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Получаем выбранную дату
        Calendar calendar = new GregorianCalendar(view.getYear(), view.getMonth(), view.getDayOfMonth());

        // Сохраняем дату в задаче
        mRealm.beginTransaction();
        mTask.setTargetDate(calendar.getTimeInMillis());
        mRealm.commitTransaction();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();
    }
}
