package com.ebizebiz.android.nignews;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.RoomDatabase;
import android.os.AsyncTask;

import java.util.List;

public class NewsRepository {

    private NewsDao mNewsDao;
    private LiveData<List<News>> mAllNews;

    NewsRepository(Application application) {
        NewsRoomDatabase db = NewsRoomDatabase.getDatabase(application);
        mNewsDao = db.newsDao();
        mAllNews = mNewsDao.getAllNews();
    }

    LiveData<List<News>> getAllNews() {
        return mAllNews;
    }

    public void insert(News news) {
        new insertAsyncTask(mNewsDao).execute(news);
    }

    public void update(News news)  {
        new updateNewsAsyncTask(mNewsDao).execute(news);
    }

    public void deleteAll()  {
        new deleteAllNewsAsyncTask(mNewsDao).execute();
    }

    // Must run off main thread
    public void deleteNews(News news) {

        new deleteNewsAsyncTask(mNewsDao).execute(news);
    }

    // Static inner classes below here to run database interactions in the background.

    /**
     * Inserts a word into the database.
     */
    private static class insertAsyncTask extends AsyncTask<News, Void, Void> {

        private NewsDao mAsyncTaskDao;

        insertAsyncTask(NewsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final News... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    /**
     * Deletes all words from the database (does not delete the table).
     */
    private static class deleteAllNewsAsyncTask extends AsyncTask<Void, Void, Void> {
        private NewsDao mAsyncTaskDao;

        deleteAllNewsAsyncTask(NewsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

    /**
     *  Deletes a single News from the database.
     */
    private static class deleteNewsAsyncTask extends AsyncTask<News, Void, Void> {
        private NewsDao mAsyncTaskDao;

        deleteNewsAsyncTask(NewsDao dao) {

            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final News... params) {
            mAsyncTaskDao.deleteNews(params[0]);
            return null;
        }
    }

    /**
     *  Updates a word in the database.
     */
    private static class updateNewsAsyncTask extends AsyncTask<News, Void, Void> {
        private NewsDao mAsyncTaskDao;

        updateNewsAsyncTask(NewsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final News... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }
}
