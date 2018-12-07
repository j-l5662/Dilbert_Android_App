package com.johannlau.dilbert_app.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.johannlau.dilbert_app.logs.NoLoggingTree;
import com.johannlau.dilbert_app.utils.DateUtil;
import com.johannlau.dilbert_app.viewmodelfactory.MainPageViewModelFactory;
import com.johannlau.dilbert_app.viewmodels.MainViewModel;
import com.johannlau.test_app.BuildConfig;
import com.johannlau.test_app.R;

import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {


    @BindView(R.id.image_date)
    public TextView mDate;

    @BindView(R.id.image_date1)
    public TextView mRandomDate;

    @BindView(R.id.image_view)
    public ImageView mCurrentImageView;

    @BindView(R.id.image_view2)
    public ImageView mRandomImageView;

    @BindView(R.id.random_button)
    public Button mRandomImage;

    @BindView(R.id.swipe_refresh)
    public SwipeRefreshLayout mSwipeRefreshLayout;

    private LiveData<ArrayList<Bitmap>> mImagesList;

    private ArrayList<String> urlList = new ArrayList<>();

    private Date date = new Date();

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");;

    private String todayImageUrl;

    private String randomImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        else{
            Timber.plant(new NoLoggingTree());
        }

        mCurrentImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(MainActivity.this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
                MediaStore.Images.Media.insertImage(getContentResolver(), ((BitmapDrawable) mCurrentImageView.getDrawable()).getBitmap(), urlList.get(0) , urlList.get(0));
                return true;
            }
        });


        mRandomImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(MainActivity.this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
                MediaStore.Images.Media.insertImage(getContentResolver(), ((BitmapDrawable) mCurrentImageView.getDrawable()).getBitmap(), urlList.get(1) , urlList.get(1));
                return true;
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRandomImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String randomDateString = DateUtil.returnRandomDate();
                randomImageUrl = getString(R.string.dilbertURL) + randomDateString;
                urlList.set(1,randomImageUrl);
                mRandomDate.setText(randomDateString);
                setupViewModel();
            }
        });

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);

        mDate.setText(dateFormat.format(date));
        String previousDate = SubOneDay(cal);
        todayImageUrl = getString(R.string.dilbertURL) + dateFormat.format(date);

        randomImageUrl = getString(R.string.dilbertURL) + previousDate;
        mRandomDate.setText(previousDate);

        ArrayList<String> urlInputs = new ArrayList<>();
        urlInputs.add(todayImageUrl);
        urlInputs.add(randomImageUrl);
        urlList = urlInputs;
        setupViewModel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItemSelected = item.getItemId();
        if(menuItemSelected == R.id.refreshImage){
            setupViewModel();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
//        Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);
        setupViewModel();
    }


    public void onClickWebpage(View v){
        openDilbertMainPage();
    }

    private void setupViewModel() {

        MainViewModel mainViewModel = ViewModelProviders.of(this,new MainPageViewModelFactory(this.getApplication(), urlList)).get(MainViewModel.class);

        mImagesList = mainViewModel.returnBitmaps();

        mImagesList.observe(this, new Observer<ArrayList<Bitmap>>() {
            @Override
            public void onChanged(@Nullable ArrayList<Bitmap> bitmaps) {
                mCurrentImageView.setVisibility(View.INVISIBLE);
                mRandomImageView.setVisibility(View.INVISIBLE);
                mCurrentImageView.setImageBitmap(bitmaps.get(0));
                mRandomImageView.setImageBitmap(bitmaps.get(1));
                mCurrentImageView.setVisibility(View.VISIBLE);
                mRandomImageView.setVisibility(View.VISIBLE);
            }
        });

    }

    private String SubOneDay(GregorianCalendar calendar){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        calendar.add(Calendar.DATE,-1);
        return dateFormat.format(calendar.getTime());
    }

    private void openDilbertMainPage(){
        Uri dilbertSite = Uri.parse(getString(R.string.dilbertURL));
        Intent intent = new Intent(Intent.ACTION_VIEW, dilbertSite);
        if(intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
    }

}
