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
import ru.kulikovman.tasklist.models.Group;
import ru.kulikovman.tasklist.models.Task;

public class ColorDialog extends CallbackDialogFragment {
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
        final String color[] = getResources().getStringArray(R.array.color_array);

        // Создаем диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.color_title)
                .setItems(color, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Открываем транзакцию
                        mRealm.beginTransaction();

                        switch (which) {
                            case 0: // Чрезвычайный
                                mGroup.setColor("brown");
                                break;
                            case 1: // Высокий
                                mGroup.setColor("red");
                                break;
                            case 2: // Обычный
                                mGroup.setColor("orange");
                                break;
                            case 3: // Низкий
                                mGroup.setColor("yellow");
                                break;
                            case 4: // Самый низкий
                                mGroup.setColor("green");
                                break;
                            case 5: // Самый низкий
                                mGroup.setColor("blue");
                                break;
                            case 6: // Самый низкий
                                mGroup.setColor("turquoise");
                                break;
                            case 7: // Самый низкий
                                mGroup.setColor("violet");
                                break;
                            case 8: // Самый низкий
                                mGroup.setColor("pink");
                                break;
                            case 9: // Самый низкий
                                mGroup.setColor(null);
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
        mListener.onDialogFinish(ColorDialog.this);
    }
}
