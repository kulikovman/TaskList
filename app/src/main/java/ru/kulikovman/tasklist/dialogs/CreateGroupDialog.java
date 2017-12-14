package ru.kulikovman.tasklist.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmResults;
import ru.kulikovman.todoapp.R;
import ru.kulikovman.todoapp.messages.GroupIsExist;
import ru.kulikovman.todoapp.models.Group;


public class CreateGroupDialog extends DialogFragment {
    private Realm mRealm;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Подключаемся к базе
        mRealm = Realm.getDefaultInstance();

        // Это нужно для привязки к диалогу вью из макета
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogDescription = inflater.inflate(R.layout.dialog_input_text, null);

        // Инициализируем вью элементы
        final EditText dialogInputText = (EditText) dialogDescription.findViewById(R.id.dialog_input_text);

        // Формируем диалог при помощи конструктора
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.create_group_title)
                .setView(dialogDescription)
                .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Получаем введенный текст
                        String enteredName = dialogInputText.getText().toString().trim();

                        // Если название введено
                        if (enteredName.length() > 0) {
                            RealmResults<Group> existGroups = mRealm.where(Group.class).equalTo(Group.NAME, enteredName).findAll();

                            // Добавляем группу в базу
                            if (existGroups.size() == 0) {
                                Group group = new Group(enteredName);

                                mRealm.beginTransaction();
                                mRealm.insert(group);
                                mRealm.commitTransaction();

                                // Записываем название группы в соответствующее поле
                                TextView groupState = (TextView) getActivity().findViewById(R.id.group_state);
                                groupState.setText(enteredName);
                            } else {
                                // Показываем сообщение об ошибке
                                DialogFragment groupIsExist = new GroupIsExist();
                                groupIsExist.show(getActivity().getSupportFragmentManager(), "groupIsExist");
                            }

                        } else {
                            // Показываем диалог со списком групп
                            DialogFragment groupDialog = new GroupDialog();
                            groupDialog.show(getActivity().getSupportFragmentManager(), "groupDialog");
                        }
                    }
                });

        return builder.create();
    }
}
