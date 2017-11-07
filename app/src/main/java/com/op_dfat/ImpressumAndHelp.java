package com.op_dfat;

import android.content.Context;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.text.Html;


/**
 * Created by Michael Krapp
 */


class ImpressumAndHelp {

    static void setupHelpDialog (Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.help);

        String text = context.getResources().getString(R.string.HelpText);

        if (Build.VERSION.SDK_INT >= 24){
            builder.setMessage(Html.fromHtml((text), Html.FROM_HTML_MODE_LEGACY));//for 24 API and More
        }

        builder.show();
    }

    static void setupImpressumDialog (Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.impressum);

        String text = context.getResources().getString(R.string.ImpressumText);

        if (Build.VERSION.SDK_INT >= 24){
            builder.setMessage(Html.fromHtml((text), Html.FROM_HTML_MODE_LEGACY));//for 24 API and More
        } else
            builder.setMessage(Html.fromHtml((text)));

        builder.show();
    }
}
