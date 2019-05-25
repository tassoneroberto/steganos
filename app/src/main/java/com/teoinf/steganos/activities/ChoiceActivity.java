package com.teoinf.steganos.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.teoinf.steganos.R;
import com.teoinf.steganos.error.ErrorManager;
import com.teoinf.steganos.wifi.WifiActivity;

public class ChoiceActivity extends Activity {
Button share;
Button p2p;
    public String file_url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choice_send);
        share=(Button)findViewById(R.id.send_share);
        p2p=(Button)findViewById(R.id.send_p2p);
        if(getIntent().hasExtra("file_url")) {
            file_url = getIntent().getExtras().getString("file_url");
            Log.i("STEGA", "file url "+file_url);

        }
        share.setOnClickListener(onClickListener);
        p2p.setOnClickListener(onClickListener);
    }


    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            switch (arg0.getId()) {
                case R.id.send_share:
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("video/*");
                    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file_url));
                    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    sendIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivity(Intent.createChooser(sendIntent,"Share video to: "));
                    //finish();
                    break;
                case R.id.send_p2p:
                    Log.i("STEGA", "Start encode");
                    Intent intent = new Intent(ChoiceActivity.this, WifiActivity.class);
                    intent.putExtra("file_url",file_url);
                    startActivity(intent);
                    break;

            }
        }
    };
}
