package com.example.cwong.nytimessearch.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.example.cwong.nytimessearch.ArticleArrayAdapter;
import com.example.cwong.nytimessearch.EndlessScrollListener;
import com.example.cwong.nytimessearch.R;
import com.example.cwong.nytimessearch.models.Article;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity {
    private final int REQUEST_SETTINGS_CODE = 50;
    String url = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
    int queryPage = 0;

    EditText etQuery;
    GridView gvResults;
    Button btnSearch;
    Toolbar toolbar;
    AsyncHttpClient client;

    ArrayList<Article> articles;
    ArticleArrayAdapter adapter;

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
        newsArrayValues = new ArrayList<>();
        setupViews();
    }

    public void setupViews() {
        etQuery = (EditText) findViewById(R.id.etQuery);
        gvResults = (GridView) findViewById(R.id.gvResults);
        btnSearch = (Button) findViewById(R.id.btnSearch);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivityForResult(i, REQUEST_SETTINGS_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onNewArticleSearch(View v) {
        adapter.clear();
        queryPage = 0;
        articleSearch();
    }

    public void articleSearch() {
        String query = etQuery.getText().toString();

        RequestParams params = new RequestParams();
        params.put("api-key", "86aa25661ed0464cb226e368461d527d");
        params.put("page", queryPage);
        params.put("q", query);

        if (dateString.length() > 0) {
            params.put("beginDate", formatDateQuery(dateString));
        }
        if (sortOrder.length() > 0) {
            params.put("sort", sortOrder);
        }
        if (newsArrayValues.size() > 0) {
            String newsDeskQuery = TextUtils.join(" ", newsArrayValues);
            params.put("fq", "news_desk:(" + newsDeskQuery + ")");
        }

        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray articleJsonResults = null;
                try {
                    articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    adapter.addAll(Article.fromJSONArray(articleJsonResults));
                    Log.d("DEBUG", articles.toString());
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

}
