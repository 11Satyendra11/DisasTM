package com.satyendra.opencv;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

public class ExampleDialog extends AppCompatDialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("DisasTM")

                .setMessage("v0.1.10 \nTake snapshot of the accident event and sends it to the insurance company for verification" +
                        "\nMade by : Mr.Satyendra Chandan" +
                        "\nEmail: csatyendra02@gmail.com" +
                        "\nCopyright 2019 Software Freedom Conservancy Inc" +
                        "\nThis program comes with absolutely no warranty." +
                        "\nSee the GNU Lesser General Public License, version 2.1 or later for details.")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();

    }
}
