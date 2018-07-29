package com.shivora.puwifimanager.utils;

import android.app.Activity;

import com.andrognito.flashbar.Flashbar;
import com.shivora.puwifimanager.R;

public class FlashbarUtils {
    private static Flashbar.Builder flashbarBuilder;

    private FlashbarUtils() {
    }

    private static Flashbar.Builder getInstance(Activity context) {
        if (flashbarBuilder == null) {
            return new Flashbar.Builder(context).gravity(Flashbar.Gravity.BOTTOM);
        }
        return flashbarBuilder;
    }

    public static void showMessageDialog(Activity context, String msg) {
        getInstance(context).gravity(Flashbar.Gravity.BOTTOM)
                .message(msg)
                .build()
                .show();
    }

    public static void showMessageDialog(Activity context, String title, String msg) {
        getInstance(context)
                .title(title)
                .message(msg)
                .build()
                .show();
    }

    public static void showErrorDialog(Activity context,String message){
        getInstance(context)
                .message(message)
                .build()
                .show();
    }

    public static void showErrorDialog(Activity context, String title, String message){
        getInstance(context)
                .title(title)
                .message(message)
                .build()
                .show();
    }
    public static void showConfirmationDialog(Activity context, String message, Flashbar.OnActionTapListener positiveTapListener, Flashbar.OnActionTapListener negativeTapListener) {
        getInstance(context)
                .message(message)
                .positiveActionText(android.R.string.yes)
                .negativeActionText(android.R.string.no)
                .positiveActionTextColorRes(R.color.colorAccent)
                .negativeActionTextColorRes(R.color.colorAccent)
                .positiveActionTapListener(positiveTapListener)
                .negativeActionTapListener(negativeTapListener)
                .build()
                .show();
    }

    public static void showConfirmationDialog(Activity context, String title, String message, Flashbar.OnActionTapListener positiveTapListener, Flashbar.OnActionTapListener negativeTapListener) {
        getInstance(context)
                .title(title)
                .message(message)
                .positiveActionText(android.R.string.yes)
                .negativeActionText(android.R.string.no)
                .positiveActionTextColorRes(R.color.colorAccent)
                .negativeActionTextColorRes(R.color.colorAccent)
                .positiveActionTapListener(positiveTapListener)
                .negativeActionTapListener(negativeTapListener)
                .build()
                .show();
    }
    public static void showProgressDialog(Activity context, String message){
        getInstance(context)
                .message(message)
                .showProgress(Flashbar.ProgressPosition.LEFT)
                .build()
                .show();
    }

    public static void showProgressDialog(Activity context, String title, String message){
        getInstance(context)
                .title(title)
                .message(message)
                .showProgress(Flashbar.ProgressPosition.LEFT)
                .build()
                .show();
    }
}
