package com.ebizebiz.android.nignews;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class NewsViewModel extends AndroidViewModel {

    private NewsRepository mRepository;

    private LiveData<List<News>> mAllNews;

    public NewsViewModel(Application application) {
        super(application);
        mRepository = new NewsRepository(application);
        mAllNews = mRepository.getAllNews();
    }


    LiveData<List<News>> getAllNews() {
        return mAllNews;
    }

    public void insert(News news) {
        mRepository.insert(news);
    }

    public void deleteAll() {
        mRepository.deleteAll();
    }

    public void deleteWord(News word) {
        mRepository.deleteNews(word);
    }

    public void update(News news) {
        mRepository.update(news);
    }
}
