package com.example.siddhant.article_publisher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.siddhant.article_publisher.activities.DashboardActivity;
import com.example.siddhant.article_publisher.activities.RegistrationActivity;

/**
 * Created by siddhant on 1/2/16.
 */
public class DummyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sf = getSharedPreferences(Constants.UserInfoSharedPref, Context.MODE_PRIVATE);
        boolean a = sf.getBoolean(Constants.SignInDone, false);
        if (a) {
            startActivity(new Intent(this, DashboardActivity.class));
        } else {
            startActivity(new Intent(this, RegistrationActivity.class));
        }
        finish();
    }
}