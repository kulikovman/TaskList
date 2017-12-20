package ru.kulikovman.tasklist.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import ru.kulikovman.tasklist.R;


public class InfoDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Это нужно для привязки к диалогу вью из макета
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogDescription = inflater.inflate(R.layout.test_layout, null);

        // Создаем диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogDescription);

        return builder.create();
    }
}
