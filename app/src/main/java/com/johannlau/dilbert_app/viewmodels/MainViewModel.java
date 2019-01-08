package com.johannlau.dilbert_app.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.johannlau.dilbert_app.utils.NetworkUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;

import timber.log.Timber;

public class MainViewModel extends AndroidViewModel {

    private static MutableLiveData<ArrayList<Bitmap>> mImageList = new MutableLiveData<>();
    private Application mApplication;
    private ArrayList<String> mUrl;

    private String mhttpProtocol = "https:";

    private static int mBitmapCapacity = 2;

    private final static ArrayList<Bitmap> mBitmaps = new ArrayList<>(mBitmapCapacity);

    public MainViewModel(Application application, ArrayList<String> url) {

        super(application);
        this.mApplication = application;
        this.mUrl = url;
    }

    public LiveData<ArrayList<Bitmap>> returnBitmaps(ArrayList<String> mUrl) {
        this.mUrl = mUrl;

        loadImagesFromWeb();

        return mImageList;
    }

    private void loadImagesFromWeb() {

        Context appContext = mApplication.getApplicationContext();

        final int bitmapZeroPlace = 0;

        final int bitmapFirstPlace = 1;

        int initialCapacity = 2;

        final ArrayList<Bitmap> bitmaps = new ArrayList<>(initialCapacity);

        for(int i =0;i<mBitmapCapacity;i++) {
            mBitmaps.add(null);
        }

        if(NetworkUtils.isAppOnline(appContext)){

            RequestQueue requestQueue = Volley.newRequestQueue(appContext);

            String todayImageURL = mUrl.get(0);

            String randomImageURL = mUrl.get(1);

            StringRequest todayImageNetRequest = new StringRequest(Request.Method.GET, todayImageURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            Document document = Jsoup.parse(response);

                            Elements divs = document.select("div");
                            for (Element div: divs) {

                                if(div.attr("class").equals("img-comic-container")) {
                                    Element image = div.select("a").first().select("img").first();
                                    String imageUrl = mhttpProtocol + image.attr("src");

                                    LoadImageAsyncTask loadImageAsyncTask = new LoadImageAsyncTask(mBitmaps,bitmapZeroPlace);
                                    loadImageAsyncTask.execute(imageUrl);
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            Timber.i(error.toString());
                        }
                    });

            requestQueue.add(todayImageNetRequest);

            StringRequest randomImageNetRequest = new StringRequest(Request.Method.GET, randomImageURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            Document document = Jsoup.parse(response);

                            Elements divs = document.select("div");
                            for (Element div: divs) {

                                if(div.attr("class").equals("img-comic-container")) {
                                    Element image = div.select("a").first().select("img").first();
                                    String imageUrl = mhttpProtocol + image.attr("src");

                                    LoadImageAsyncTask loadImageAsyncTask = new LoadImageAsyncTask(mBitmaps,bitmapFirstPlace);
                                    loadImageAsyncTask.execute(imageUrl);
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            Timber.i(error.toString());
                        }
                    });

            requestQueue.add(randomImageNetRequest);
        }
        else {
            Timber.v("App is Offline");
        }
    }

    private static class LoadImageAsyncTask extends AsyncTask<String,Void, Bitmap> {

        ArrayList<Bitmap> bitmapArrayList;
        int bitMapPlace;

        public LoadImageAsyncTask(ArrayList<Bitmap> bitmapArrayList,int arrayPlace) {
            super();
            this.bitmapArrayList = bitmapArrayList;
            this.bitMapPlace = arrayPlace;
        }
        @Override
        protected Bitmap doInBackground(String... strings) {
            String imageUrl = strings[0];

            Bitmap d = null;
            InputStream is;

            try {
                is = new URL(imageUrl).openStream();
                d = BitmapFactory.decodeStream(is);

            } catch (Exception e) {
                Timber.e("Error: LoadImageFromWebOperations");
                e.printStackTrace();
            }
            return d;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            mBitmaps.set(bitMapPlace,bitmap);

            mImageList.postValue(mBitmaps);

        }
    }
}
