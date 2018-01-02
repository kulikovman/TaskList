package ru.kulikovman.tasklist.dialogs;


import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import io.realm.Realm;
import ru.kulikovman.tasklist.CallbackDialogFragment;
import ru.kulikovman.tasklist.R;
import ru.kulikovman.tasklist.models.Task;

public class EditTaskDialog extends CallbackDialogFragment {
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

        // Это нужно для привязки к диалогу вью из макета
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View editTextLayout = inflater.inflate(R.layout.edit_text_long, null);

        // Инициализируем вью элементы
        final EditText editText = editTextLayout.findViewById(R.id.edit_text_field);
        ImageButton clearButton = editTextLayout.findViewById(R.id.clear_text_button);

        // Вставляем в поле описание задачи
        String oldTaskTitle = mTask.getTitle();
        editText.setText(oldTaskTitle);

        // Запрашиваем фокус
        editText.requestFocus();

        // Слушатель для кнопки очищения поля
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(null);
            }
        });

        // Создаем диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.edit_task_title)
                .setView(editTextLayout)
                .setPositiveButton(R.string.edit_task_save_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Получаем измененное описание и очищаем от лишних пробелов
                        String taskTitle = editText.getText().toString().trim();

                        // Сохраняем описание задачи
                        if (taskTitle.length() > 0) {
                            mRealm.beginTransaction();
                            mTask.setTitle(taskTitle);
                            mRealm.commitTransaction();
                        }
                    }
                });

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();

        // Запускаем код в активити
        mListener.onDialogFinish(EditTaskDialog.this);
    }
}
