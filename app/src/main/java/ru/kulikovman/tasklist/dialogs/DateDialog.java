package ru.kulikovman.tasklist.dialogs;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ru.kulikovman.todoapp.Helper;
import ru.kulikovman.todoapp.R;

public class DateDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String today = getString(R.string.date_today);
        final String tomorrow = getString(R.string.date_tomorrow);
        final String pickDate = getString(R.string.date_pick_date);
        final String withoutDate = getString(R.string.date_not);

        final String date[] = {today, tomorrow, pickDate, withoutDate};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.date_title)
                .setItems(date, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("myLog", String.valueOf(which));

                        // Инициализируем необходимые вью
                        TextView dateState = (TextView) getActivity().findViewById(R.id.date_state);
                        TextView repeatState = (TextView) getActivity().findViewById(R.id.repeat_state);
                        TextView reminderState = (TextView) getActivity().findViewById(R.id.reminder_state);

                        // Получаем текущую дату
                        Calendar calendar = Helper.getTodayRoundCalendar();

                        switch (which) {
                            case 0: // На сегодня
                                dateState.setText(Helper.convertCalendarToLongTextDate(calendar));
                                break;
                            case 1: // На завтра
                                calendar.add(Calendar.DATE, 1);
                                dateState.setText(Helper.convertCalendarToLongTextDate(calendar));
                                break;
                            case 2: // Выбрать дату
                                DialogFragment datePickerFragment = new TaskDatePickerDialog();
                                datePickerFragment.show(getFragmentManager(), "datePicker");
                                break;
                            case 3: // Без даты
                                dateState.setText(R.string.date_without);
                                repeatState.setText(R.string.repeat_without);

                                // Выключаем напоминание
                                reminderState.setText(R.string.reminder_disabled);
                                break;
                        }
                    }
                });

        return builder.create();
    }
}
