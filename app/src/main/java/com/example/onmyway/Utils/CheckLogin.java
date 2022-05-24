package com.example.onmyway.Utils;

import android.content.Context;
import android.content.Intent;

import com.example.onmyway.General.Login;

public final class CheckLogin {

    public static boolean toLogin(final Context context) {
        if (false) {
            context.startActivity(new Intent(context, Login.class));
            return true;
        }

        return false;
    }
}
