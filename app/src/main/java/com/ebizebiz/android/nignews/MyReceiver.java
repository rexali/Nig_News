package com.ebizebiz.android.nignews;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import static android.support.v4.content.ContextCompat.startActivity;

public class MyReceiver extends BroadcastReceiver {

    // String constant that defines the custom broadcast Action.
    private static final String ACTION_CUSTOM_BROADCAST =
            BuildConfig.APPLICATION_ID + ".ACTION_CUSTOM_BROADCAST";
    private static final String ACTION_SHARE =
            BuildConfig.APPLICATION_ID + ".ACTION_SHARE";
    private static final String ACTION_DAILY_TRUST =
            BuildConfig.APPLICATION_ID + ".ACTION_DAILY_TRUST";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String intentAction = intent.getAction();
        String url = intent.getDataString();
        String toastMessage = context.getString(R.string.unknown_action);

        if (intentAction != null) {

            switch (intentAction) {

                case ACTION_SHARE:

                    if (url!=null){

                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        //shareIntent.setPackage("com.android.chrome");
                        shareIntent.putExtra(Intent.EXTRA_TEXT,url);
                        Intent chooserIntent = Intent.createChooser(shareIntent,"Share url");
                        chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(chooserIntent);

                        //toastMessage = context.getString(R.string.custom_broadcast_share);

                    }

                    break;

                case ACTION_DAILY_TRUST:

                        Intent dailytrustIntent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://dailytrust.com.ng"));
                        //dailytrustIntent.setAction();
                        //shareIntent.setType("text/plain");
                        //shareIntent.setPackage("com.android.chrome");
                        //shareIntent.putExtra(Intent.EXTRA_TEXT,url);
                        //Intent chooserIntent = Intent.createChooser(shareIntent,"Share url");
                        dailytrustIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(dailytrustIntent);

                        //toastMessage = context.getString(R.string.custom_broadcast_share)

                    break;

                case ACTION_CUSTOM_BROADCAST:
                    toastMessage =
                            context.getString(R.string.custom_broadcast);
                    break;

                default:
                    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
            }


        }
        
    }




}