package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);

        CatImages catImages = new CatImages();
        catImages.execute();
    }

    private class CatImages extends AsyncTask<Void, Integer, Void> {

        private Bitmap currentImage;

        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                try {
                    URL url = new URL("https://cataas.com/cat?json=true");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) !=null) {
                        response.append(line);
                    }

                    JSONObject jsonObj = new JSONObject(response.toString());
                    if (jsonObj.has("_id")) {
                        String catId = jsonObj.getString("_id");

                        File localFile = new File(getFilesDir(), catId + ".jpg");
                        if (localFile.exists()) {
                            currentImage = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        } else {
                            String imageUrl = jsonObj.getString("url");
                            URL imageDownloadUrl = new URL("https://cataas.com" + imageUrl);
                            HttpURLConnection imageConnection = (HttpURLConnection) imageDownloadUrl.openConnection();
                            imageConnection.connect();

                            InputStream imageInputStream = imageConnection.getInputStream();
                            OutputStream outputStream = new FileOutputStream(localFile);
                            byte [] buffer = new byte[1024];
                            int length;
                            while ((length = imageInputStream.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, length);
                            }
                            outputStream.close();
                            inputStream.close();

                            currentImage = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        }
                    }
                    for (int i = 0; i < 100; i++) {
                        publishProgress(i);
                        Thread.sleep(30);
                    }
                } catch (IOException e) {
                    System.out.println(e);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
            imageView.setImageBitmap(currentImage);
        }
    }
}
