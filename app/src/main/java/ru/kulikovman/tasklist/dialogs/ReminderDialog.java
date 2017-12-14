package ru.kulikovman.tasklist.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;

import java.util.Calendar;

import ru.kulikovman.todoapp.Helper;
import ru.kulikovman.todoapp.R;

public class ReminderDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Строки для формирования списка
        final String onTheDay = getString(R.string.reminder_on_the_day);
        final String dayBefore = getString(R.string.reminder_day_before);
        final String twoDayBefore = getString(R.string.reminder_two_day_before);
        final String weekBefore = getString(R.string.reminder_week_before);
        final String monthBefore = getString(R.string.reminder_month_before);
        final String not = getString(R.string.reminder_not);

        // Инициализируем нужные вью
        final TextView repeatState = (TextView) getActivity().findViewById(R.id.repeat_state);
        final TextView reminderState = (TextView) getActivity().findViewById(R.id.reminder_state);
        final TextView dateState = (TextView) getActivity().findViewById(R.id.date_state);

        // Получаем вид повтора и формируем список возможных напоминаний
        String repeat = repeatState.getText().toString();
        final String reminder[];

        // Если есть повтор, то смотрим на него, если нет, то на дату задачи
        if (!repeat.equals(getString(R.string.repeat_without))) {
            if (repeat.equals(getString(R.string.repeat_day))) {
                reminder = new String[]{onTheDay, not};
            } else if (repeat.equals(getString(R.string.repeat_week))) {
                reminder = new String[]{onTheDay, dayBefore, twoDayBefore, not};
            } else if (repeat.equals(getString(R.string.repeat_month))) {
                reminder = new String[]{onTheDay, dayBefore, twoDayBefore, weekBefore, not};
            } else {
                reminder = new String[]{onTheDay, dayBefore, twoDayBefore, weekBefore, monthBefore, not};
            }
        } else {
            // Получаем дату задачи и сегодняшнюю дату
            Calendar taskDate = Helper.convertLongTextDateToCalendar(dateState.getText().toString());
            Calendar todayDate = Helper.getTodayRoundCalendar();

            // Вычисляем разницу в днях
            int daysBeforeTaskDate = (int) ((taskDate.getTimeInMillis() - todayDate.getTimeInMillis()) / 1000 / 60 / 60 / 24);
            Log.d("log", "Разница в днях: " + daysBeforeTaskDate);

            if (daysBeforeTaskDate == 0) { // Сегодня
                reminder = new String[]{onTheDay, not};
            } else if (daysBeforeTaskDate == 1) { // Завтра
                reminder = new String[]{onTheDay, dayBefore, not};
            } else if (daysBeforeTaskDate <= 7) { // Меньше недели
                reminder = new String[]{onTheDay, dayBefore, twoDayBefore, not};
            } else if (daysBeforeTaskDate <= 31) { // Меньше месяца
                reminder = new String[]{onTheDay, dayBefore, twoDayBefore, weekBefore, not};
            } else {
                reminder = new String[]{onTheDay, dayBefore, twoDayBefore, weekBefore, monthBefore, not};
            }
        }

        // Создаем и запускаем диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.reminder_title)
                .setItems(reminder, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("myLog", String.valueOf(which));

                        // Записываем в поле выбранную дату
                        if (which == reminder.length - 1) {
                            reminderState.setText(R.string.reminder_without);
                        } else {
                            reminderState.setText(reminder[which]);
                        }
                    }
                });

        return builder.create();
    }
}
