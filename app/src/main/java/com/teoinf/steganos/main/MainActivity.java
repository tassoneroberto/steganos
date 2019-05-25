package com.teoinf.steganos.main;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.VideoView;

import com.teoinf.steganos.R;
import com.teoinf.steganos.activities.AboutActivity;
import com.teoinf.steganos.activities.ChoiceActivity;
import com.teoinf.steganos.activities.DecodeActivity;
import com.teoinf.steganos.activities.EncodeActivity;
import com.teoinf.steganos.activities.SettingsActivity;
import com.teoinf.steganos.configuration.Configuration;
import com.teoinf.steganos.configuration.Preferences;
import com.teoinf.steganos.error.ErrorManager;
import com.teoinf.steganos.wifi.WifiActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private Button _btnEncode;
    private Button _btnDecode;
    private Button _btnSettings;
    private Button _btnAbout;
    private Button _btnSendVideo;

    private VideoView videoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Configuration.getInstance().loadData(this);
        Preferences.getInstance(this).loadData();

        _btnEncode = (Button) findViewById(R.id.btn_main_choose_encode);
        _btnDecode = (Button) findViewById(R.id.btn_main_choose_decode);
        _btnSettings = (Button) findViewById(R.id.btn_main_choose_settings);
        _btnAbout = (Button) findViewById(R.id.btn_main_choose_about);
        _btnSendVideo = (Button) findViewById(R.id.btn_main_send_video);

        _btnSendVideo.setOnClickListener(onClickListener);
        _btnEncode.setOnClickListener(onClickListener);
        _btnDecode.setOnClickListener(onClickListener);
        _btnSettings.setOnClickListener(onClickListener);
        _btnAbout.setOnClickListener(onClickListener);

        checkAndRequestPermissions();

        videoView= (VideoView) findViewById(R.id.videoview);
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.videoloop);
        videoView.setVideoURI(uri);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        videoView.start();


    }
@Override
public void onResume(){
        super.onResume();
        videoView.start();
}


    private  boolean checkAndRequestPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA);
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (readExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),1);
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void createActivities(Class<?> claz) {
        Intent intent = new Intent(getApplicationContext(), claz);
        startActivity(intent);
    }

    private OnClickListener onClickListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            switch (arg0.getId()) {
                case R.id.btn_main_choose_encode:
                    createActivities(EncodeActivity.class);
                    break;
                case R.id.btn_main_choose_decode:
                    createActivities(DecodeActivity.class);
                    break;
                case R.id.btn_main_choose_settings:
                    createActivities(SettingsActivity.class);
                    break;
                case R.id.btn_main_choose_about:
                    createActivities(AboutActivity.class);
                    break;
                case R.id.btn_main_send_video:
                    createActivities(WifiActivity.class);
                    break;
                default:
                    ErrorManager.getInstance().addErrorMessage("[Main Activity] Requested activity not known");
                    ErrorManager.getInstance().displayErrorMessages(arg0.getContext());
                    break;
            }
        }
    };


}
