package com.johannlau.dilbert_app.ui;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.johannlau.dilbert_app.logs.NoLoggingTree;
import com.johannlau.dilbert_app.utils.DateUtil;
import com.johannlau.dilbert_app.viewmodelfactory.MainPageViewModelFactory;
import com.johannlau.dilbert_app.viewmodels.MainViewModel;
import com.johannlau.test_app.BuildConfig;
import com.johannlau.test_app.R;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

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

    private MainViewModel mainViewModel;
    private LiveData<ArrayList<Bitmap>> mImagesList;

    private ArrayList<String> urlList = new ArrayList<>();

    private Date date = new Date();

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");;

    GregorianCalendar mCalendar = new GregorianCalendar();

    private String todayImageUrl;

    private String randomImageUrl;

//    private PhotoViewAttacher mPhotoView;
//
//    private PhotoViewAttacher mRandomPhotoView;

    private boolean mCurrentImageViewFlag = false;

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);

        }

//        mCurrentImageView.setScaleX(3f);
//        mCurrentImageView.setScaleY(3f);


//        mPhotoView = new PhotoViewAttacher(mCurrentImageView);
//
//        mPhotoView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                Toast.makeText(MainActivity.this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
//                MediaStore.Images.Media.insertImage(getContentResolver(), ((BitmapDrawable) mCurrentImageView.getDrawable()).getBitmap(), urlList.get(0) , urlList.get(0));
//                return true;
//            }
//        });
//
//        mPhotoView.update();
//
//        mRandomPhotoView = new PhotoViewAttacher(mRandomImageView);
//
//        mRandomPhotoView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                Toast.makeText(MainActivity.this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
//                MediaStore.Images.Media.insertImage(getContentResolver(), ((BitmapDrawable) mCurrentImageView.getDrawable()).getBitmap(), urlList.get(1) , urlList.get(1));
//                return true;
//            }
//        });
//
//        mRandomPhotoView.update();

        mCalendar.setTime(date);

        String previousDate = SubOneDay(mCalendar);
        todayImageUrl = getString(R.string.dilbertURL) + dateFormat.format(date);
        randomImageUrl = getString(R.string.dilbertURL) + previousDate;

        setDates();

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
            setDates();
            setupViewModel();
        }
        return super.onOptionsItemSelected(item);
    }


    public void onClickWebpage(View v){
        openDilbertMainPage();
    }

    public void onClickRandomButton(View v) {

        int firstPlace = 1;

        String randomDateString = DateUtil.returnRandomDate();
        randomImageUrl = getString(R.string.dilbertURL) + randomDateString;
        urlList.set(firstPlace,randomImageUrl);
        mRandomDate.setText(randomDateString);

        mainViewModel.returnBitmaps(urlList);
    }

    private void setupViewModel() {

        final int dailyBitmap = 0;
        final int changingBitmap = 1;

        if(mainViewModel == null ){
            mainViewModel = ViewModelProviders.of(this,new MainPageViewModelFactory(this.getApplication(), urlList)).get(MainViewModel.class);
        }

        mImagesList = mainViewModel.returnBitmaps(urlList);

        mImagesList.observe(this, new Observer<ArrayList<Bitmap>>() {
            @Override
            public void onChanged(@Nullable final ArrayList<Bitmap> bitmaps) {

                if(!mCurrentImageViewFlag) {
                    mCurrentImageViewFlag = true;
                }
                mCurrentImageView.setImageBitmap(bitmaps.get(dailyBitmap));
                mCurrentImageView.setVisibility(View.VISIBLE);
                mCurrentImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        Bitmap bmp = bitmaps.get(0);
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

                        byte[] byteArray = stream.toByteArray();
                        Intent intent = new Intent(MainActivity.this , FullScreenImageActivity.class);
                        intent.putExtra("bmp",byteArray);
                        startActivity(intent);
                    }
                });
                mRandomImageView.setImageBitmap(bitmaps.get(changingBitmap));
                mRandomImageView.setVisibility(View.VISIBLE);
            }
        });

    }

    private String SubOneDay(GregorianCalendar calendar){

        calendar.add(Calendar.DATE,-1);
        return dateFormat.format(calendar.getTime());
    }

    private void openDilbertMainPage(){
        Uri dilbertSite = Uri.parse(getString(R.string.dilbertURL));
        Intent intent = new Intent(Intent.ACTION_VIEW, dilbertSite);
        if(intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
    }

    private void setDates() {

        mDate.setText(dateFormat.format(date));

        String previousDate = SubOneDay(new GregorianCalendar());

        todayImageUrl = getString(R.string.dilbertURL) + dateFormat.format(date);

        mRandomDate.setText(previousDate);
    }

}
