package com.teoinf.steganos.wifi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import com.teoinf.steganos.R;
import com.teoinf.steganos.error.ErrorManager;

public class WifiActivity extends Activity implements ChannelListener, DeviceListFragment.DeviceActionListener {

    private ImageButton _btnBack;
    private ImageButton _btnMenu;
    public String file_url;
    ProgressDialog progressDialog = null;
    public static final String TAG = "Steganos";
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;
    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver = null;



    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().hasExtra("file_url")) {
            file_url = getIntent().getExtras().getString("file_url");
        }
        setContentView(R.layout.activity_main_wifi);
        // add necessary intent values to be matched.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        _btnBack = (ImageButton) findViewById(R.id.btn_back_wifi);
        _btnMenu =(ImageButton)findViewById(R.id.btn_menu);
        _btnBack.setOnClickListener(onClickListener);
        _btnMenu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(WifiActivity.this, _btnMenu);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.menu_wifi, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.action_directEnable:
                                if (manager != null && channel != null) {

                                    // Since this is the system wireless settings activity, it's
                                    // not going to send us a result. We will be notified by
                                    // WiFiDeviceBroadcastReceiver instead.

                                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                                } else {
                                    //Log.e(TAG, "channel or manager is null");
                                }
                                return true;

                            case R.id.action_directDiscover:
                                if (!isWifiP2pEnabled) {
                                    Toast.makeText(WifiActivity.this, R.string.p2p_off_warning,
                                            Toast.LENGTH_SHORT).show();
                                    return true;
                                }
                                final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                                        .findFragmentById(R.id.fragment_list);
                                fragment.onInitiateDiscovery();
                                manager.discoverPeers(channel, new ActionListener() {

                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(WifiActivity.this, "Discovery Initiated",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(int reasonCode) {
                                        Toast.makeText(WifiActivity.this, "Discovery Failed : " + reasonCode,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return true;

                        }


                        return true;
                    }
                });

                popup.show();//showing popup menu
            }

        });//closing the setOnClickListener method++

        final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.fragment_list);
        fragment.onInitiateDiscovery();
        manager.discoverPeers(channel, new ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(WifiActivity.this, "Discovery Initiated",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(WifiActivity.this, "Discovery Failed : " + reasonCode,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }







    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            switch (arg0.getId()) {
                case R.id.btn_back_wifi:
                    finish();
                    break;

                default:
                    ErrorManager.getInstance().addErrorMessage("[Wifi Activity] Click event not known");
                    ErrorManager.getInstance().displayErrorMessages(arg0.getContext());
                    break;
            }
        }
    };
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }


    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_detail);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }





    @Override
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_detail);
        fragment.showDetails(device);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

    }

    public void showDialog(){

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(this, "Press back to cancel",
                "Connecting ...", true, true

        );
    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new ActionListener() {


            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.

            }

            @Override
            public void onFailure(int reason) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(WifiActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
        showDialog();
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                // Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }

        });
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void cancelDisconnect() {


        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.fragment_list);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {

                manager.cancelConnect(channel, new ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(WifiActivity.this, "Aborting connection",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(WifiActivity.this,
                                "Connect abort request failed. Reason Code: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }
}