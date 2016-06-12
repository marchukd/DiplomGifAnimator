package com.marchukdmytro.android.gifanimator.filepicker;

import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import com.marchukdmytro.android.gifanimator.R;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;

/**
 * Created by Dmytro on 12.06.2016.
 */
public class FileExplorerHelper {
    public static Intent getFolderPickerIntent(Context context) {
        Intent i = new Intent(context, FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to get paths to the SD-card or
        // internal memory.
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
        return i;
    }

    public static void onActivityResult(FilePickerCallback callback, Intent data) {

        if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
            // For JellyBean and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clip = data.getClipData();

                if (clip != null) {
                    for (int i = 0; i < clip.getItemCount(); i++) {
                        Uri uri = clip.getItemAt(i).getUri();
                        callback.pick(uri.getPath());
                    }
                }
                // For Ice Cream Sandwich
            } else {
                ArrayList<String> paths = data.getStringArrayListExtra
                        (FilePickerActivity.EXTRA_PATHS);

                if (paths != null) {
                    for (String path : paths) {
                        Uri uri = Uri.parse(path);
                        callback.pick(uri.getPath());
                    }
                }
            }

        } else {
            Uri uri = data.getData();
            callback.pick(uri.getPath());
        }
    }
}
