package com.example.siddhant.article_publisher.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.siddhant.article_publisher.Constants;
import com.example.siddhant.article_publisher.Globals;
import com.example.siddhant.article_publisher.HelperMethods;
import com.example.siddhant.article_publisher.R;
import com.example.siddhant.article_publisher.TypefaceSpan;
import com.example.siddhant.article_publisher.adapters.FeedAdapter;
import com.example.siddhant.article_publisher.networkClasses.Article;
import com.example.siddhant.article_publisher.networkClasses.GetArticles;
import com.example.siddhant.article_publisher.networkClasses.GetArticlesResponse;
import com.google.gson.Gson;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DashboardActivity extends AppCompatActivity {

    //    private SweetAlertDialog progress;
    SharedPreferences sf;
    ListView feedListView;
    RelativeLayout refreshingFeedRelativeLayout, problemRelativeLayout;
    SwipeRefreshLayout myRefreshLayout;
    ArrayList<Article> currentArticles;
    FeedAdapter myFeedAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            SpannableString s = new SpannableString("Published Articles");
            s.setSpan(new TypefaceSpan(Globals.typeface), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            actionBar.setTitle(s);
        }

//        progress = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
//        progress.setTitleText("Processing");
//        progress.getProgressHelper().setBarColor(R.color.light_green);
//        progress.setCancelable(false);

        feedListView = (ListView) findViewById(R.id.feedListView);
        refreshingFeedRelativeLayout = (RelativeLayout) findViewById(R.id.refreshingFeedRelativeLayout);
        problemRelativeLayout = (RelativeLayout) findViewById(R.id.problemRelativeLayout);

        ((TextView) findViewById(R.id.textView)).setTypeface(Globals.typeface);
        ((TextView) findViewById(R.id.tryAgainTextView)).setTypeface(Globals.typeface);

        sf = getSharedPreferences(Constants.UserInfoSharedPref, MODE_PRIVATE);

        if (sf.getString(Constants.Categories, "").equals("")) {
            saveCategories();
        }

        LoadListView();

        problemRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadListView();
            }
        });

        myRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        myRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);
        myRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LoadListView();
            }
        });


    }

    private void LoadListView() {
        refreshingFeedRelativeLayout.setVisibility(View.VISIBLE);
        problemRelativeLayout.setVisibility(View.GONE);
        feedListView.setVisibility(View.GONE);
        feedListView.setAdapter(null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                GetArticles myReq = new GetArticles(sf.getInt(Constants.UserId, -1));
                final String response = HelperMethods.makePostRequest(DashboardActivity.this, Constants.GET_ARTICLES, myReq);
                if (!response.equals("")) {
                    final GetArticlesResponse myResponse = new Gson().fromJson(response, GetArticlesResponse.class);
                    if (myResponse.success) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                feedListView.setVisibility(View.VISIBLE);
                                refreshingFeedRelativeLayout.setVisibility(View.GONE);
                                problemRelativeLayout.setVisibility(View.GONE);
                                currentArticles = myResponse.data;
                                myFeedAdapter = new FeedAdapter(DashboardActivity.this, currentArticles);
                                feedListView.setAdapter(myFeedAdapter);
                                if (currentArticles.size() == 0) {
                                    Toast.makeText(DashboardActivity.this, "No articles found. Please publish a new article", Toast.LENGTH_SHORT).show();
                                }
                                myRefreshLayout.setRefreshing(false);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                feedListView.setVisibility(View.GONE);
                                refreshingFeedRelativeLayout.setVisibility(View.GONE);
                                problemRelativeLayout.setVisibility(View.VISIBLE);
                                myRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            feedListView.setVisibility(View.GONE);
                            refreshingFeedRelativeLayout.setVisibility(View.GONE);
                            problemRelativeLayout.setVisibility(View.VISIBLE);
                            myRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        }).start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dashboard, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_publish:
                startActivity(new Intent(DashboardActivity.this, PublishActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveCategories() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HelperMethods.getAndSaveCategories(DashboardActivity.this);
            }
        }).start();
    }

    @Override
    protected void onResume() {
        if (sf.getBoolean(Constants.ReloadFeeds, false)) {
            LoadListView();
            SharedPreferences.Editor editor = sf.edit();
            editor.putBoolean(Constants.ReloadFeeds, false);
            editor.apply();
        }
        super.onResume();
    }
}
