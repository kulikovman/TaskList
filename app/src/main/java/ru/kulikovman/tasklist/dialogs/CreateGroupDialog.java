package ru.kulikovman.tasklist.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import io.realm.Realm;
import io.realm.RealmResults;
import ru.kulikovman.tasklist.R;
import ru.kulikovman.tasklist.messages.GroupIsExist;
import ru.kulikovman.tasklist.models.Group;
import ru.kulikovman.tasklist.models.Task;


public class CreateGroupDialog extends DialogFragment {
    private Realm mRealm;
    private Task mTask;
    private Group mGroup;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Получаем аргументы
        long taskId = getArguments().getLong("taskId");

        // Подключаем базу и получаем задачу
        mRealm = Realm.getDefaultInstance();
        mTask = mRealm.where(Task.class).equalTo(Task.ID, taskId).findFirst();

        // Это нужно для привязки к диалогу вью из макета
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogDescription = inflater.inflate(R.layout.dialog_input_text, null);

        // Инициализируем вью элементы
        final EditText dialogInputText = (EditText) dialogDescription.findViewById(R.id.dialog_input_text);

        // Создаем диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.create_group_title)
                .setView(dialogDescription)
                .setPositiveButton(R.string.create_group_button_ok, new DialogInterface.OnClickListener() {
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

                                // Добавляем группу в базу
                                mRealm.beginTransaction();
                                mRealm.insert(group);
                                mRealm.commitTransaction();

                                // Сохраняем группу в задачу
                                mRealm.beginTransaction();
                                mGroup = mRealm.where(Group.class).equalTo(Group.NAME, enteredName).findFirst();
                                mTask.setGroup(mGroup);
                                mRealm.commitTransaction();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();
    }
}
