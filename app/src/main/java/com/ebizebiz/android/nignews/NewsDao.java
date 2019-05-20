package com.ebizebiz.android.nignews;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;


@Dao
public interface NewsDao {


        @Insert(onConflict = OnConflictStrategy.IGNORE)
        void insert(News news);

        @Query("DELETE FROM news_table")
        void deleteAll();

        @Delete
        void deleteNews(News news);

        @Query("SELECT * from news_table LIMIT 1")
        News[] getAnyNews();

        @Query("SELECT * from news_table ORDER BY name ASC")
        LiveData<List<News>> getAllNews();

        @Update
        void update(News... news);
}
