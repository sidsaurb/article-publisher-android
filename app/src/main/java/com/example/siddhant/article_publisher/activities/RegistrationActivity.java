package com.example.siddhant.article_publisher.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.siddhant.article_publisher.Constants;
import com.example.siddhant.article_publisher.Globals;
import com.example.siddhant.article_publisher.HelperMethods;
import com.example.siddhant.article_publisher.R;
import com.example.siddhant.article_publisher.TypefaceSpan;
import com.example.siddhant.article_publisher.networkClasses.LoginUser;
import com.example.siddhant.article_publisher.networkClasses.LoginUserResponse;
import com.example.siddhant.article_publisher.networkClasses.RegisterUser;
import com.example.siddhant.article_publisher.networkClasses.RegisterUserResponse;
import com.google.gson.Gson;

import cn.pedant.SweetAlert.SweetAlertDialog;
import info.hoang8f.widget.FButton;

public class RegistrationActivity extends AppCompatActivity {


    private EditText emailEditText, emailEditText1, nameEditText, passwordEditText, passwordEditText1;
    private SweetAlertDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            SpannableString s = new SpannableString("Sign in..");
            s.setSpan(new TypefaceSpan(Globals.typeface), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            actionBar.setTitle(s);
        }
        progress = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progress.setTitleText("Processing");
        progress.getProgressHelper().setBarColor(R.color.light_green);
        progress.setCancelable(false);

        ((FButton) findViewById(R.id.registerButton)).setTypeface(Globals.typeface);
        ((FButton) findViewById(R.id.loginButton)).setTypeface(Globals.typeface);
        ((TextView) findViewById(R.id.loginTextView)).setTypeface(Globals.typeface);
        ((TextInputLayout) findViewById(R.id.nameLayout)).setTypeface(Globals.typeface);
        ((TextInputLayout) findViewById(R.id.emailLayout)).setTypeface(Globals.typeface);
        ((TextInputLayout) findViewById(R.id.emailLayout1)).setTypeface(Globals.typeface);
        ((TextInputLayout) findViewById(R.id.passwordLayout)).setTypeface(Globals.typeface);
        ((TextInputLayout) findViewById(R.id.passwordLayout1)).setTypeface(Globals.typeface);

        emailEditText = (EditText) findViewById(R.id.emailEditText);
        emailEditText1 = (EditText) findViewById(R.id.emailEditText1);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        passwordEditText1 = (EditText) findViewById(R.id.passwordEditText1);

        emailEditText.setTypeface(Globals.typeface);
        emailEditText1.setTypeface(Globals.typeface);
        passwordEditText.setTypeface(Globals.typeface);
        passwordEditText1.setTypeface(Globals.typeface);
        nameEditText.setTypeface(Globals.typeface);

        FButton registerButton = (FButton) findViewById(R.id.registerButton);
        FButton loginButton = (FButton) findViewById(R.id.loginButton);

        registerButton.setOnClickListener(registerListener);
        loginButton.setOnClickListener(loginListener);
    }

    private View.OnClickListener loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!emailEditText1.getText().toString().trim().isEmpty()
                    && !passwordEditText1.getText().toString().trim().isEmpty()) {
                progress.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LoginUser myReq = new LoginUser(emailEditText1.getText().toString().trim(),
                                passwordEditText1.getText().toString().trim());
                        String response = HelperMethods.makePostRequest(RegistrationActivity.this, Constants.LOGIN, myReq);
                        if (!response.isEmpty()) {
                            LoginUserResponse myResponse = new Gson().fromJson(response, LoginUserResponse.class);
                            if (myResponse.success) {
                                SharedPreferences sf = getSharedPreferences(Constants.UserInfoSharedPref, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sf.edit();
                                editor.putBoolean(Constants.SignInDone, true);
                                editor.putString(Constants.UserName, myResponse.name);
                                editor.putString(Constants.UserEmail, emailEditText1.getText().toString().trim());
                                editor.putString(Constants.UserPassword, passwordEditText1.getText().toString().trim());
                                editor.putInt(Constants.UserId, myResponse.id);
                                editor.putBoolean(Constants.IsReLogin, true);
                                editor.apply();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress.dismiss();
                                        Intent i = new Intent(RegistrationActivity.this, DashboardActivity.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress.dismiss();
                                        Toast.makeText(RegistrationActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progress.dismiss();
                                    Toast.makeText(RegistrationActivity.this, "There is some problem in reaching servers", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            } else {
                Toast.makeText(RegistrationActivity.this, "Please fill out every field", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener registerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!nameEditText.getText().toString().trim().isEmpty()
                    && !emailEditText.getText().toString().trim().isEmpty()
                    && !passwordEditText.toString().trim().isEmpty()) {
                progress.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SendRegistrationInfoToBackend();
                    }
                }).start();
            } else {
                Toast.makeText(RegistrationActivity.this, "Please fill out every field", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void SendRegistrationInfoToBackend() {
        RegisterUser myInfo = new RegisterUser(nameEditText.getText().toString().trim(),
                emailEditText.getText().toString().trim(),
                passwordEditText.getText().toString().trim());
        String response = HelperMethods.makePostRequest(RegistrationActivity.this.getApplicationContext(), Constants.ADD_PUBLISHER, myInfo);
        if (!response.equals("")) {
            RegisterUserResponse myResponse = new Gson().fromJson(response, RegisterUserResponse.class);
            if (myResponse.success) {
                SharedPreferences sf = getSharedPreferences(Constants.UserInfoSharedPref, MODE_PRIVATE);
                SharedPreferences.Editor editor = sf.edit();
                editor.putBoolean(Constants.SignInDone, true);
                editor.putString(Constants.UserName, nameEditText.getText().toString().trim());
                editor.putString(Constants.UserEmail, emailEditText.getText().toString().trim());
                editor.putString(Constants.UserPassword, passwordEditText.getText().toString().trim());
                editor.putInt(Constants.UserId, myResponse.id);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        Intent i = new Intent(RegistrationActivity.this, DashboardActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        Toast.makeText(RegistrationActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            showDialogAndExit();
        }
    }

    private void showDialogAndExit() {
        runOnUiThread(new Runnable() {
            public void run() {
                progress.dismiss();
                new SweetAlertDialog(RegistrationActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops..")
                        .setContentText("Can't connect to servers")
                        .show();
            }
        });
    }
}
