package com.johannlau.test_app;

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

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private TextView mDate;
    private TextView mRandomDate;


    private ImageView mCurrentImageView;
    private ImageView mRandomImageView;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Button mRandomImage;

    private ArrayList<String> urlList;

    private Date date;

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String dilbertQuery = "http://dilbert.com/";

    private String todayImageUrl;

    private String randomImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDate = findViewById(R.id.image_date);
        mRandomDate = findViewById(R.id.image_date1);

        mCurrentImageView = findViewById(R.id.image_view);

        mCurrentImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                MediaStore.Images.Media.insertImage(getContentResolver(), ((BitmapDrawable) mCurrentImageView.getDrawable()).getBitmap(), urlList.get(0) , urlList.get(0));
                return true;
            }
        });
        mRandomImage = findViewById(R.id.random_button);

        mRandomImageView = findViewById(R.id.image_view2);

        mRandomImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                MediaStore.Images.Media.insertImage(getContentResolver(), ((BitmapDrawable) mCurrentImageView.getDrawable()).getBitmap(), urlList.get(1) , urlList.get(1));
                return true;
            }
        });

        //

        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRandomImage = findViewById(R.id.random_button);

        mRandomImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalDate startDate = LocalDate.of(1996,4,16);
                LocalDate endDate = LocalDate.now();

                long start = startDate.toEpochDay();
                long end = endDate.toEpochDay();
                long randomDate = ThreadLocalRandom.current().nextLong(start,end);

                LocalDate randomStringDate = LocalDate.ofEpochDay(randomDate);
                String randomDateString = randomStringDate.format(formatter);
                randomImageUrl = dilbertQuery + randomDateString;
                urlList.set(1,randomImageUrl);
                mRandomDate.setText(randomDateString);
                new outputUrlTask().execute(urlList);
            }
        });

        date = new Date();
        urlList = new ArrayList<>();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);

        mDate.setText(dateFormat.format(date));
        String previousDate = SubOneDay(cal);
        todayImageUrl = dilbertQuery + dateFormat.format(date);

        randomImageUrl = dilbertQuery + previousDate;
        mRandomDate.setText(previousDate);

        ArrayList<String> urlInputs = new ArrayList<>();
        urlInputs.add(todayImageUrl);
        urlInputs.add(randomImageUrl);
        urlList = urlInputs;
        new outputUrlTask().execute(urlList);
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
            new outputUrlTask().execute(urlList);
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
        new outputUrlTask().execute(urlList);
    }


    public void onClickWebpage(View v){
        openDilbertMainPage();
    }

    public class outputUrlTask extends AsyncTask<ArrayList<String>,Void,ArrayList<Bitmap>> {

        @Override
        protected ArrayList<Bitmap> doInBackground(ArrayList<String>... strings) {
            ArrayList<String> comicUrls = strings[0];
            ArrayList<Bitmap> bitmaps = new ArrayList<>();
            mCurrentImageView.setVisibility(View.INVISIBLE);
            mRandomImageView.setVisibility(View.INVISIBLE);
            //mPreviousImageView2.setVisibility(View.INVISIBLE);
            //Bitmap comic = null;

                try {
                    for(String url : comicUrls) {

                        Log.d("outputUrlTask",url);
                        Document doc = Jsoup.connect(url).get();
                        //title = doc.title();


                        Elements divs = doc.select("div");
                        for (Element div : divs) {
                            if (div.attr("class").equals("img-comic-container")) {
                                Element image = div.select("a").first().select("img").first();
                                String imageUrl = image.attr("src");
                                // Ex. http://assets.amuniversal.com/9024a440e77a01351311005056a9545d
                                bitmaps.add(LoadImageFromWebOperations(imageUrl));
                            }
                        }
                    }

                }
                catch (IOException e) {
                    System.err.println(e.getMessage());
                }

            return bitmaps;
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> s) {
            if (s != null) {
                //iterate
                mCurrentImageView.setImageBitmap(s.get(0));
                mRandomImageView.setImageBitmap(s.get(1));
                mCurrentImageView.setVisibility(View.VISIBLE);
                mRandomImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    private Bitmap LoadImageFromWebOperations(String url) {
        Bitmap d = null;
        InputStream is = null;
        try {
            Log.d("LoadImageFromWebOperations", url);
            is = new URL(url).openStream();
            d = BitmapFactory.decodeStream(is);


        } catch (Exception e) {
            Log.v("Main", "Error: LoadImageFromWebOperations");
            e.printStackTrace();
        }
//        finally {
//            try{
//                is.close();
//            }
//            catch (Exception e) {
//                Log.v("Main", "Error: LoadImageFromWebOperations");
//                e.printStackTrace();
//            }
//            return d;
        return d;
    }
    //private Arraylist<Bitmap> return listof arraymap

    private String SubOneDay(GregorianCalendar calendar){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        calendar.add(Calendar.DATE,-1);
        return dateFormat.format(calendar.getTime());
    }

    private void openDilbertMainPage(){
        Uri dilbertSite = Uri.parse(dilbertQuery);
        Intent intent = new Intent(Intent.ACTION_VIEW, dilbertSite);
        if(intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
    }

}
