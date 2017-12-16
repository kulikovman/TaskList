package ru.kulikovman.tasklist.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;

import ru.kulikovman.tasklist.R;

public class ColorDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String red = getString(R.string.color_1_red);
        final String orange = getString(R.string.color_2_orange);
        final String yellow = getString(R.string.color_3_yellow);
        final String green = getString(R.string.color_4_green);
        final String blue = getString(R.string.color_5_blue);
        final String violet = getString(R.string.color_6_violet);
        final String pink = getString(R.string.color_7_pink);
        final String without = getString(R.string.color_not);

        final String color[] = {red, orange, yellow, green, blue, violet, pink, without};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.color_title)
                .setItems(color, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("myLog", String.valueOf(which));

                        TextView colorField = (TextView) getActivity().findViewById(R.id.color_state);

                        switch (which) {
                            case 0:
                                colorField.setText(red);
                                break;
                            case 1:
                                colorField.setText(orange);
                                break;
                            case 2:
                                colorField.setText(yellow);
                                break;
                            case 3:
                                colorField.setText(green);
                                break;
                            case 4:
                                colorField.setText(blue);
                                break;
                            case 5:
                                colorField.setText(violet);
                                break;
                            case 6:
                                colorField.setText(pink);
                                break;
                            case 7:
                                colorField.setText(R.string.color_without);
                                break;
                        }
                    }
                });

        return builder.create();
    }
}
