package com.ebizebiz.android.nignews;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter for the RecyclerView that displays a list of words.
 */

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.WordViewHolder> {

    private final LayoutInflater mInflater;
    private List<News> mNews; // Cached copy of words
    private static ClickListener clickListener;

    NewsListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public WordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new WordViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(WordViewHolder holder, int position) {
        if (mNews != null) {
            News current = mNews.get(position);
            holder.newsItemView.setText(current.getName());
        } else {
            // Covers the case of data not being ready yet.
            holder.newsItemView.setText(R.string.no_word);
        }
    }

    /**
     * Associates a list of words with this adapter
     */
    void setNews(List<News> news) {
        mNews = news;
        notifyDataSetChanged();
    }

    /**
     * getItemCount() is called many times, and when it is first called,
     * mWords has not been updated (means initially, it's null, and we can't return null).
     */
    @Override
    public int getItemCount() {
        if (mNews != null)
            return mNews.size();
        else return 0;
    }

    /**
     * Gets the word at a given position.
     * This method is useful for identifying which word
     * was clicked or swiped in methods that handle user events.
     *
     * @param position The position of the word in the RecyclerView
     * @return The word at the given position
     */
    public News getNewsAtPosition(int position) {
        return mNews.get(position);
    }

    class WordViewHolder extends RecyclerView.ViewHolder {
        private final TextView newsItemView;

        private WordViewHolder(View itemView) {
            super(itemView);
            newsItemView = itemView.findViewById(R.id.textview);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onItemClick(view, getAdapterPosition());
                }
            });
        }
    }

    //
    public void setOnItemClickListener(ClickListener clickListener) {
        NewsListAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(View v, int position);
    }

}

