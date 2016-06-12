package com.marchukdmytro.android.gifanimator.filepicker;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import com.marchukdmytro.android.gifanimator.R;

/**
 * Created by Dmytro on 12.06.2016.
 */
public class EnterStringDialog extends AlertDialog {
    private final FilePickerCallback listener;
    EditText editText;
    DialogInterface.OnClickListener callback = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == BUTTON_POSITIVE) {
                listener.pick(editText.getText().toString());
            }
        }
    };

    public EnterStringDialog(Context context, FilePickerCallback listener) {
        super(context);
        this.listener = listener;
        editText = new EditText(context);
        setMessage(context.getString(R.string.alert_select_file_message));
        setButton(BUTTON_POSITIVE, context.getString(R.string.ok), callback);
        setButton(BUTTON_NEUTRAL, context.getString(R.string.cancel), callback);
        setView(editText);
    }
}
