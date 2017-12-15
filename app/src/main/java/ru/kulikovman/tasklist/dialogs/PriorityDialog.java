package ru.kulikovman.tasklist.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import io.realm.Realm;
import ru.kulikovman.tasklist.CallbackDialogFragment;
import ru.kulikovman.tasklist.R;
import ru.kulikovman.tasklist.models.Task;

public class PriorityDialog extends CallbackDialogFragment {
    private Realm mRealm;
    private Task mTask;

    CallbackDialogListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (CallbackDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
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
                    }
                });

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();

        // Запускаем событие при завершении
        mListener.onDialogFinish(PriorityDialog.this);
    }
}