package com.example.siddhant.article_publisher.activities;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.siddhant.article_publisher.Constants;
import com.example.siddhant.article_publisher.Globals;
import com.example.siddhant.article_publisher.HelperMethods;
import com.example.siddhant.article_publisher.R;
import com.example.siddhant.article_publisher.TypefaceSpan;
import com.example.siddhant.article_publisher.adapters.SpinnerAdapter;
import com.example.siddhant.article_publisher.networkClasses.Categories;
import com.example.siddhant.article_publisher.networkClasses.GetCategoriesResponse;
import com.example.siddhant.article_publisher.networkClasses.PublishArticle;
import com.example.siddhant.article_publisher.networkClasses.PublishArticleResponse;
import com.google.gson.Gson;

import javax.microedition.khronos.egl.EGLDisplay;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class PublishActivity extends AppCompatActivity {

    private SweetAlertDialog progress;
    SharedPreferences sf;
    GetCategoriesResponse myCategories;
    Spinner categorySpinner;
    EditText titleEditText, contentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            SpannableString s = new SpannableString("Publish new Article..");
            s.setSpan(new TypefaceSpan(Globals.typeface), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            actionBar.setTitle(s);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            upArrow.setColorFilter(ContextCompat.getColor(this, R.color.green), PorterDuff.Mode.SRC_ATOP);
            actionBar.setHomeAsUpIndicator(upArrow);
        }

        progress = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progress.setTitleText("Processing");
        progress.getProgressHelper().setBarColor(R.color.light_green);
        progress.setCancelable(false);

        titleEditText = (EditText) findViewById(R.id.titleEditText);
        contentEditText = (EditText) findViewById(R.id.contentEditText);
        titleEditText.setTypeface(Globals.typeface);
        contentEditText.setTypeface(Globals.typeface);
        categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
        ((TextView) findViewById(R.id.textView)).setTypeface(Globals.typeface);


        sf = getSharedPreferences(Constants.UserInfoSharedPref, MODE_PRIVATE);

        LoadCategories();
    }

    private void LoadCategories() {
        progress.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String categories = sf.getString(Constants.Categories, "");
                if (categories.isEmpty()) {
                    boolean result = HelperMethods.getAndSaveCategories(PublishActivity.this);
                    if (result) {
                        categories = sf.getString(Constants.Categories, "");
                        myCategories = new Gson().fromJson(categories, GetCategoriesResponse.class);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LoadSpinner();
                            }
                        });
                    } else {
                        showDialogAndExit();
                    }
                } else {
                    myCategories = new Gson().fromJson(categories, GetCategoriesResponse.class);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LoadSpinner();
                        }
                    });
                }

            }
        }).start();
    }

    private void LoadSpinner() {
        progress.dismiss();
        SpinnerAdapter myAdapter = new SpinnerAdapter(PublishActivity.this, R.layout.spinner_row, myCategories.data);
        categorySpinner.setAdapter(myAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_publish, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_done:
                publishArticle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDialogAndExit() {
        runOnUiThread(new Runnable() {
            public void run() {
                progress.dismiss();
                new SweetAlertDialog(PublishActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops..")
                        .setContentText("Can't connect to servers")
                        .setConfirmText("Ok")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                finish();
                            }
                        })
                        .show();
            }
        });
    }

    private void publishArticle() {
        if (!titleEditText.getText().toString().trim().isEmpty()
                && !contentEditText.getText().toString().trim().isEmpty()) {
            progress.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int category = ((Categories) categorySpinner.getSelectedItem()).id;
                    PublishArticle myReq = new PublishArticle(sf.getInt(Constants.UserId, -1),
                            category,
                            titleEditText.getText().toString().trim(),
                            contentEditText.getText().toString().trim());
                    String response = HelperMethods.makePostRequest(PublishActivity.this, Constants.POST_ARTICLE, myReq);
                    if (!response.isEmpty()) {
                        PublishArticleResponse myResponse = new Gson().fromJson(response, PublishArticleResponse.class);
                        if (myResponse.success) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progress.dismiss();
                                    SharedPreferences.Editor editor = sf.edit();
                                    editor.putBoolean(Constants.ReloadFeeds, true);
                                    editor.commit();
                                    new SweetAlertDialog(PublishActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("Success")
                                            .setContentText("Your article is published")
                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sDialog) {
                                                    sDialog.dismissWithAnimation();
                                                    finish();
                                                }
                                            })
                                            .show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progress.dismiss();
                                    Toast.makeText(PublishActivity.this, "There is some problem with the network. Please try again later", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                                Toast.makeText(PublishActivity.this, "There is some problem with the network. Please try again later", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).start();
        } else {
            Toast.makeText(PublishActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
        }
    }
}
