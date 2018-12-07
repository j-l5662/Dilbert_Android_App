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
import java.net.URL;
import java.util.ArrayList;

import timber.log.Timber;

public class MainViewModel extends AndroidViewModel {

    private static MutableLiveData<ArrayList<Bitmap>> mImageList = new MutableLiveData<>();
    private Application mApplication;
    private ArrayList<String> mUrl;

    private String mHTTPprotocal = "https:";

    public MainViewModel(Application application, ArrayList<String> url) {

        super(application);
        this.mApplication = application;
        this.mUrl = url;

        loadImagesFromWeb();
    }

    public LiveData<ArrayList<Bitmap>> returnBitmaps() { return mImageList; }

    private void loadImagesFromWeb() {

        Context appContext = mApplication.getApplicationContext();

        final ArrayList<Bitmap> bitmaps = new ArrayList<>();

        Timber.i(mUrl.get(0));

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
                                    String imageUrl = mHTTPprotocal + image.attr("src");

                                    Timber.i(imageUrl);


                                    LoadImageAsyncTask loadImageAsyncTask = new LoadImageAsyncTask(bitmaps);
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

            StringRequest randomImageNetRequest = new StringRequest(Request.Method.GET, randomImageURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            Document document = Jsoup.parse(response);

                            Elements divs = document.select("div");
                            for (Element div: divs) {

                                if(div.attr("class").equals("img-comic-container")) {
                                    Element image = div.select("a").first().select("img").first();
                                    String imageUrl = mHTTPprotocal + image.attr("src");

                                    Timber.i(imageUrl);


                                    LoadImageAsyncTask loadImageAsyncTask = new LoadImageAsyncTask(bitmaps);
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
            requestQueue.add(randomImageNetRequest);
        }
        else {
            Timber.v("App is Offline");
        }
    }

    private static class LoadImageAsyncTask extends AsyncTask<String,Void, Bitmap> {

        ArrayList<Bitmap> bitmapArrayList;

        public LoadImageAsyncTask(ArrayList<Bitmap> bitmapArrayList) {
            super();
            this.bitmapArrayList = bitmapArrayList;
        }
        @Override
        protected Bitmap doInBackground(String... strings) {
            String imageUrl = strings[0];

            Bitmap d = null;
            InputStream is;

            try {
                Log.d("LoadImageFromWebOperations", imageUrl);
                is = new URL(imageUrl).openStream();
                d = BitmapFactory.decodeStream(is);


            } catch (Exception e) {
                Log.v("Main", "Error: LoadImageFromWebOperations");
                e.printStackTrace();
            }
            return d;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            bitmapArrayList.add(bitmap);

            mImageList.postValue(bitmapArrayList);

        }
    }
}
