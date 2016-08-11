package com.example.cwong.nytimessearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cwong.nytimessearch.models.Article;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by cwong on 8/8/16.
 */
public class ArticleArrayAdapter extends RecyclerView.Adapter<ArticleArrayAdapter.ViewHolder> implements View.OnClickListener, Target {
    DynamicHeightImageView articleImageView;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivImage) DynamicHeightImageView imageView;
        @BindView(R.id.tvTitle) TextView titleView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private List<Article> articles;
    private Context context;

    public ArticleArrayAdapter(Context c, List<Article> art) {
        context = c;
        articles = art;
    }
    private Context getContext() {
        return context;
    }

    @Override
    public ArticleArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View articleView = inflater.inflate(R.layout.item_articlegrid, parent, false);
        ViewHolder viewHolder = new ViewHolder(articleView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ArticleArrayAdapter.ViewHolder viewHolder, int position) {
        articleImageView = viewHolder.imageView;
        Article article = articles.get(position);
        if (article.getThumbnail().length() > 0) {
            Picasso.with(getContext()).load(article.getThumbnail()).into(articleImageView);
        }
        TextView articleTitleView = viewHolder.titleView;
        if (article.getHeadline().length() > 0) {
            articleTitleView.setText(article.getHeadline());
        }
    }
    @Override
    public int getItemCount() {
        return articles.size();
    }
    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        // Calculate the image ratio of the loaded bitmap
        float ratio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
        // Set the ratio for the image
        articleImageView.setHeightRatio(ratio);
        // Load the image into the view
        articleImageView.setImageBitmap(bitmap);
    }
    @Override
    public void onBitmapFailed(Drawable d) {}

    @Override
    public void onPrepareLoad(Drawable d) {}

    @Override
    public void onClick(View v) {}
}
