package ru.kulikovman.tasklist.dialogs;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;

import ru.kulikovman.todoapp.Helper;
import ru.kulikovman.todoapp.R;

public class ReminderDatePickerDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Получаем дату задачи
        TextView dateState = (TextView) getActivity().findViewById(R.id.date_state);
        String taskDate = dateState.getText().toString();

        final Calendar calendar = Helper.convertLongTextDateToCalendar(taskDate);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Получаем сегодняшнюю дату
        Calendar currentDate = Calendar.getInstance();

        // Создаем и возвращаем новый DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.getDatePicker().setMinDate(currentDate.getTimeInMillis());

        return datePickerDialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Получаем выбранную дату
        Calendar calendar = new GregorianCalendar(view.getYear(), view.getMonth(), view.getDayOfMonth());

        // Инициализируем вью и записываем в него дату
        TextView reminderState = (TextView) getActivity().findViewById(R.id.reminder_state);
        reminderState.setText(Helper.convertCalendarToLongTextDate(calendar));
    }
}
