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
import android.widget.TextView;

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

        // Строки для списка вариантов

        // Это нужно для привязки к диалогу вью из макета
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogDescription = inflater.inflate(R.layout.dialog_input_text, null);

        // Инициализируем вью элементы
        final EditText dialogInputText = (EditText) dialogDescription.findViewById(R.id.dialog_input_text);
        final TextView descriptionState = (TextView) getActivity().findViewById(R.id.description_state);

        // Получаем строки для последующего сравнения
        String currentDescription = descriptionState.getText().toString();
        String withoutDescription = getString(R.string.without_description);

        // Передаем текст текущего описания в поле диалога
        if (!currentDescription.equals(withoutDescription)) {
            dialogInputText.setText(currentDescription);
        }

        // Формируем диалог при помощи конструктора
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.description_title)
                .setView(dialogDescription)
                .setPositiveButton(R.string.save_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Получаем введенный текст и очищаем от лишних пробелов
                        String updatedDescription = dialogInputText.getText().toString().trim();

                        // Сохраняем введенный текст в поле с описанием группы
                        if (updatedDescription.length() > 0) {
                            descriptionState.setText(updatedDescription);
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
