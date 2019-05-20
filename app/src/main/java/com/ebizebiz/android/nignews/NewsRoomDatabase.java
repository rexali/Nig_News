package com.ebizebiz.android.nignews;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

@Database(entities = {News.class}, version = 1, exportSchema = false)

public abstract class NewsRoomDatabase extends RoomDatabase {

    public abstract NewsDao newsDao();

    private static NewsRoomDatabase INSTANCE;

    public static NewsRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (NewsRoomDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here.
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            NewsRoomDatabase.class, "word_database")
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            // Migration is not part of this practical.
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // This callback is called when the database has opened.
    // In this case, use PopulateDbAsync to populate the database
    // with the initial data set if the database has no entries.
    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){

                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };

    // Populate the database with the initial data set
    // only if the database has no entries.
    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final NewsDao mDao;

        // Initial data set
        private static String [] newsName = {"Punch News", "Sun News", "The Nation News","Tribune News","Leadership News", "Daily Trust News","Thisday News","Vanguard News"};
        private static String [] newsUrl = {"punch.ng", "sunnewsonline.com", "thenationonlineng.net","tribuneonlineng.com","leadership.ng", "dailyTrust.com.ng","thisdaylive.com","vanguardngr.com"};

        PopulateDbAsync(NewsRoomDatabase db) {
            mDao = db.newsDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            // If we have no words, then create the initial list of words.
            if (mDao.getAnyNews().length < 1) {

                for (int i = 0; i <= newsName.length - 1; i++) {
                    News news = new News(newsName[i],newsUrl[i]);

                    mDao.insert(news);
                }
            }
            return null;
        }
    }
}
