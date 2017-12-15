package ru.kulikovman.tasklist;

import android.support.v4.app.DialogFragment;


public class CallbackDialogFragment extends DialogFragment {
    CallbackDialogListener mListener;

    public interface CallbackDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
        public void onDialogFinish(DialogFragment dialog);
    }
}
