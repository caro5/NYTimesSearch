package com.example.cwong.nytimessearch.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.cwong.nytimessearch.ArticleArrayAdapter;
import com.example.cwong.nytimessearch.EndlessScrollListener;
import com.example.cwong.nytimessearch.R;
import com.example.cwong.nytimessearch.fragments.SettingsFragment;
import com.example.cwong.nytimessearch.models.Article;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity implements SettingsFragment.SettingsDialogListener{
    private final int REQUEST_SETTINGS_CODE = 50;
    String url = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
    int queryPage = 0;

    GridView gvResults;
    Toolbar toolbar;
    AsyncHttpClient client;

    ArrayList<Article> articles;
    ArticleArrayAdapter adapter;

    String queryTerm;
    String dateString;
    String sortOrder;
    ArrayList<String> newsArrayValues;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dateString = "";
        sortOrder = "";
        queryTerm = "";
        newsArrayValues = new ArrayList<>();
        setupViews();

    }

    public void setupViews() {
        gvResults = (GridView) findViewById(R.id.gvResults);
        articles = new ArrayList<>();
        adapter = new ArticleArrayAdapter(this, articles);
        gvResults.setAdapter(adapter);
        client = new AsyncHttpClient();

        //hook up listener for grid click
        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //create intent to display article
                Intent i = new Intent(getApplicationContext(), ArticleActivity.class);
                Article article = articles.get(position);
                i.putExtra("article", article);
                startActivity(i);
            }
        });
        gvResults.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                if (page == 5) {
                    Toast.makeText(getApplicationContext(), "Reached max articles", Toast.LENGTH_LONG).show();
                    return true;
                }
                queryPage = page;
                articleSearch();
                return true;
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                queryTerm = query;
                adapter.clear();
                queryPage = 0;
                articleSearch();
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            FragmentManager fm = getSupportFragmentManager();
            SettingsFragment settingsFragment = SettingsFragment.newInstance(dateString, sortOrder, newsArrayValues);
            settingsFragment.show(fm, "fragment_settings");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void articleSearch() {
        if (!isOnline() || !isNetworkAvailable()) {
            Toast.makeText(this, "Please check internet connection", Toast.LENGTH_LONG).show();
            return;
        }

        RequestParams params = new RequestParams();
        params.put("api-key", "86aa25661ed0464cb226e368461d527d");
        params.put("page", queryPage);
        params.put("q", queryTerm);

        if (dateString.length() > 0) {
            params.put("beginDate", formatDateQuery(dateString));
        }
        if (sortOrder.length() > 0) {
            params.put("sort", sortOrder.toLowerCase());
        }
        if (newsArrayValues.size() > 0) {
            String newsDeskQuery = TextUtils.join(" ", newsArrayValues);
            params.put("fq", "news_desk:(" + newsDeskQuery + ")");
        }

        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    adapter.addAll(Article.fromJSONArray(articleJsonResults));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }
    public void onFinishSettingsDialog(String date, String sortOrderString, ArrayList<String> newsDeskValues) {
        dateString = date;
        sortOrder = sortOrderString;
        newsArrayValues = newsDeskValues;
        queryPage = 0;
        adapter.clear();
        articleSearch();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_SETTINGS_CODE) {
            dateString = data.getStringExtra("date");
            sortOrder = data.getStringExtra("sortOrder");
            newsArrayValues = data.getStringArrayListExtra("newsDesk");
            queryPage = 0;
            adapter.clear();
            articleSearch();
        }
    }

    public String formatDateQuery(String dateString) {
        String format = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        final Calendar c = Calendar.getInstance();

        try {
            c.setTime(sdf.parse(dateString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return "" + year + month + day;
    }
    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
