package ru.kulikovman.tasklist.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import io.realm.Realm;
import ru.kulikovman.tasklist.CallbackDialogFragment;
import ru.kulikovman.tasklist.R;
import ru.kulikovman.tasklist.models.Group;
import ru.kulikovman.tasklist.models.Task;

public class DescriptionDialog extends CallbackDialogFragment {
    private Realm mRealm;
    private Group mGroup;

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
        mGroup = mRealm.where(Group.class).equalTo(Task.ID, taskId).findFirst();

        // Это нужно для привязки к диалогу вью из макета
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogDescription = inflater.inflate(R.layout.dialog_input_text, null);

        // Инициализируем поле с описанием
        final EditText dialogInputText = dialogDescription.findViewById(R.id.dialog_input_text);

        // Создаем диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.description_title)
                .setView(dialogDescription)
                .setPositiveButton(R.string.description_save_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Получаем введенный текст и очищаем от лишних пробелов
                        String updatedDescription = dialogInputText.getText().toString().trim();

                        // Сохраняем введенный текст в поле с описанием группы
                        if (updatedDescription.length() > 0) {
                            // Сохраняем новое описание
                            mRealm.beginTransaction();
                            mGroup.setDescription(updatedDescription);
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
        mListener.onDialogFinish(DescriptionDialog.this);
    }
}
