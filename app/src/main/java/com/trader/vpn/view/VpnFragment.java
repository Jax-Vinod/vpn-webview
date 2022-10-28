package com.trader.vpn.view;

import static android.app.Activity.RESULT_OK;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.trader.vpn.CheckInternetConnection;
import com.trader.vpn.Constants;
import com.trader.vpn.R;
import com.trader.vpn.SharedPreference;
import com.trader.vpn.adapter.ServerListRVAdapter;
import com.trader.vpn.databinding.FragmentVpnBinding;
import com.trader.vpn.interfaces.ChangeServer;
import com.trader.vpn.model.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;

public class VpnFragment extends Fragment implements ChangeServer, View.OnClickListener {

    private static final String TAG = "VpnFragment";
    private Server server;
    private CheckInternetConnection connection;

    private OpenVPNThread vpnThread = new OpenVPNThread();
    private OpenVPNService vpnService = new OpenVPNService();
    boolean vpnStart = false;
    private SharedPreference preference;

    private FragmentVpnBinding binding;
    private ServerListRVAdapter adapter;
    private int serverIndex = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentVpnBinding.inflate(getLayoutInflater());

        View view = binding.getRoot();
        initializeAll();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Checking is vpn already running or not
        isServiceRunning();
        VpnStatus.initLogCache(getActivity().getCacheDir());

        binding.btnConnect.setOnClickListener(this);
    }

    /**
     * Initialize all variable and object
     */
    private void initializeAll() {
        preference = new SharedPreference(getContext());

        connection = new CheckInternetConnection();

        adapter = new ServerListRVAdapter(getContext(), R.id.sServer, Constants.getServerList());
        binding.sServer.setAdapter(adapter);
        binding.sServer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                server = adapter.getItem(position);
                serverIndex = position;
                preference.saveServerIndex(position);
//                if (server != null) {
//                    preference.saveServer(server);
//                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.sServer.setSelection(serverIndex);
    }

    /**
     * Prepare for vpn connect with required permission
     */
    private void prepareVpn() {
        if (server == null) {
            showToast("Please select a sever!");
            return;
        }
        if (!vpnStart) {
            if (getInternetStatus()) {

                // Checking permission for network monitor
                Intent intent = VpnService.prepare(getContext());

                if (intent != null) {
                    startActivityForResult(intent, 1);
                } else startVpn();//have already permission

                // Update confection status
                status("connecting");

            } else {

                // No internet connection available
                showToast("you have no internet connection !!");
            }

        } else if (stopVpn()) {

            // VPN is stopped, show a Toast message.
            showToast("Disconnect Successfully");
        }
    }

    /**
     * Stop vpn
     * @return boolean: VPN status
     */
    public boolean stopVpn() {
        try {
            vpnThread.stop();

            status("connect");
            vpnStart = false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Taking permission for network access
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            //Permission granted, start the VPN
            startVpn();
        } else {
            showToast("Permission Deny !! ");
        }
    }

    /**
     * Internet connection status.
     */
    public boolean getInternetStatus() {
        return connection.netCheck(getContext());
    }

    /**
     * Get service status
     */
    public void isServiceRunning() {
        setStatus(vpnService.getStatus());
    }

    /**
     * Start the VPN
     */
    private void startVpn() {
        try {
            Log.d(TAG, server.getCountry());
            // .ovpn file
            InputStream conf = getActivity().getAssets().open(server.getOvpn());
            InputStreamReader isr = new InputStreamReader(conf);
            BufferedReader br = new BufferedReader(isr);
            String config = "";
            String line;

            while (true) {
                line = br.readLine();
                if (line == null) break;
                config += line + "\n";
            }

            br.readLine();
            OpenVpnApi.startVpn(getContext(), config, server.getCountry(), server.getOvpnUserName(), server.getOvpnUserPassword());

            // Update log
            binding.logTv.setText("Connecting...");
//            vpnStart = true;

        } catch (IOException | RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Status change with corresponding vpn connection status
     * @param connectionState
     */
    public void setStatus(String connectionState) {
        if (connectionState!= null)
            switch (connectionState) {
                case "DISCONNECTED":
                    status("connect");
                    vpnStart = false;
                    vpnService.setDefaultStatus();
                    binding.logTv.setText("");
                    break;
                case "CONNECTED":
                    vpnStart = true;// it will use after restart this activity
                    status("connected");
                    binding.logTv.setText("");
                    serverIndex = preference.getServerIndex();
                    Log.d(TAG, "selected index: "+serverIndex);
                    binding.sServer.setSelection(serverIndex);
                    break;
                case "WAIT":
                    binding.logTv.setText("Waiting for server connection!!");
                    break;
                case "AUTH":
                    binding.logTv.setText("Server authenticating!!");
                    break;
                case "RECONNECTING":
                    status("connecting");
                    binding.logTv.setText("Reconnecting...");
                    break;
                case "NONETWORK":
                    binding.logTv.setText("No network connection");
                    break;
            }

    }

    /**
     * Change button background color and text
     * @param status: VPN current status
     */
    public void status(String status) {
        if (status.equals("connect")) {
            binding.btnConnect.setText(getContext().getString(R.string.connect));
        } else if (status.equals("connecting")) {
            binding.btnConnect.setText(getContext().getString(R.string.connecting));
        } else if (status.equals("connected")) {

            binding.btnConnect.setText(getContext().getString(R.string.disconnect));

        } else if (status.equals("tryDifferentServer")) {

            binding.btnConnect.setBackgroundResource(R.drawable.button_connected);
            binding.btnConnect.setText("Try Different\nServer");
        } else if (status.equals("loading")) {
            binding.btnConnect.setBackgroundResource(R.drawable.button);
            binding.btnConnect.setText("Loading Server..");
        } else if (status.equals("invalidDevice")) {
            binding.btnConnect.setBackgroundResource(R.drawable.button_connected);
            binding.btnConnect.setText("Invalid Device");
        } else if (status.equals("authenticationCheck")) {
            binding.btnConnect.setBackgroundResource(R.drawable.button_connecting);
            binding.btnConnect.setText("Authentication \n Checking...");
        }


    }

    /**
     * Receive broadcast message
     */
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                setStatus(intent.getStringExtra("state"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                String duration = intent.getStringExtra("duration");
                String lastPacketReceive = intent.getStringExtra("lastPacketReceive");
                String byteIn = intent.getStringExtra("byteIn");
                String byteOut = intent.getStringExtra("byteOut");

                if (duration == null) duration = "00:00:00";
                if (lastPacketReceive == null) lastPacketReceive = "0";
                if (byteIn == null) byteIn = " ";
                if (byteOut == null) byteOut = " ";
                updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    /**
     * Update status UI
     * @param duration: running time
     * @param lastPacketReceive: last packet receive time
     * @param byteIn: incoming data
     * @param byteOut: outgoing data
     */
    public void updateConnectionStatus(String duration, String lastPacketReceive, String byteIn, String byteOut) {
        binding.durationTv.setText("Duration: " + duration);
        binding.lastPacketReceiveTv.setText("Packet Received: " + lastPacketReceive + " second ago");
        binding.byteInTv.setText("Bytes In: " + byteIn);
        binding.byteOutTv.setText("Bytes Out: " + byteOut);
    }

    /**
     * Show toast message
     * @param message: toast message
     */
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * VPN server country icon change
     * @param serverIcon: icon URL
     */
    public void updateCurrentServerIcon(String serverIcon) {
//        Glide.with(getContext())
//                .load(serverIcon)
//                .into(binding.selectedServerIcon);
    }

    /**
     * Change server when user select new server
     * @param server ovpn server details
     */
    @Override
    public void newServer(Server server) {
        this.server = server;
        updateCurrentServerIcon(server.getFlagUrl());

        // Stop previous connection
        if (vpnStart) {
            stopVpn();
        }

        prepareVpn();
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter("connectionState"));
        serverIndex = preference.getServerIndex();
        Log.d(TAG, "save index:"+serverIndex);
        server = (Server) Constants.getServerList().get(serverIndex-1);
        super.onResume();
    }

    @Override
    public void onPause() {
//            preference.saveServer(server);
            preference.saveServerIndex(serverIndex);
        super.onPause();
    }

    /**
     * Save current selected server on local shared preference
     */
    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        preference.saveServerIndex(serverIndex);
        if (server != null) {
//            preference.saveServer(server);

        }

        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConnect:
                // Vpn is running, user would like to disconnect current connection.
                if (vpnStart) {
                    startVpn();
                }else {
                    prepareVpn();
                }
        }
    }
}