package com.johannlau.dilbert_app.viewmodelfactory;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.johannlau.dilbert_app.viewmodels.MainViewModel;

import java.util.ArrayList;

public class MainPageViewModelFactory extends ViewModelProvider.NewInstanceFactory{

    private Application mApplication;
    private ArrayList<String> mURL;

    public MainPageViewModelFactory(Application application,ArrayList<String> url){

        this.mApplication = application;
        this.mURL = url;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MainViewModel(mApplication,mURL);
    }
}
