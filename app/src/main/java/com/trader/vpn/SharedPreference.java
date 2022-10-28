package com.trader.vpn;

import android.content.Context;
import android.content.SharedPreferences;

import com.trader.vpn.model.Server;

import static com.trader.vpn.Utils.getImgURL;

public class SharedPreference {

    private static final String APP_PREFS_NAME = "TraderVPNPreference";

    private SharedPreferences mPreference;
    private SharedPreferences.Editor mPrefEditor;
    private Context context;

    private static final String SERVER_COUNTRY = "server_country";
    private static final String SERVER_FLAG = "server_flag";
    private static final String SERVER_OVPN = "server_ovpn";
    private static final String SERVER_OVPN_USER = "server_ovpn_user";
    private static final String SERVER_OVPN_PASSWORD = "server_ovpn_password";
    private static final String SERVER_INDEX = "server_index";

    public SharedPreference(Context context) {
        this.mPreference = context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE);
        this.mPrefEditor = mPreference.edit();
        this.context = context;
    }

    /**
     * Save server details
     * @param server details of ovpn server
     */
    public void saveServer(Server server){
        mPrefEditor.putString(SERVER_COUNTRY, server.getCountry());
        mPrefEditor.putString(SERVER_FLAG, server.getFlagUrl());
        mPrefEditor.putString(SERVER_OVPN, server.getOvpn());
        mPrefEditor.putString(SERVER_OVPN_USER, server.getOvpnUserName());
        mPrefEditor.putString(SERVER_OVPN_PASSWORD, server.getOvpnUserPassword());
        mPrefEditor.commit();
    }

    public void saveServerIndex(int index) {
        mPrefEditor.putInt(SERVER_INDEX, index);
        mPrefEditor.commit();
    }

    public int getServerIndex() {
        return mPreference.getInt(SERVER_INDEX, 1);
    }

    /**
     * Get server data from shared preference
     * @return server model object
     */
    public Server getServer() {

        if (mPreference.getString(SERVER_OVPN,"").equalsIgnoreCase(""))
            return null;
        Server server = new Server(
                mPreference.getString(SERVER_COUNTRY,"Japan"),
                mPreference.getString(SERVER_FLAG,getImgURL(R.drawable.japan)),
                mPreference.getString(SERVER_OVPN,"japan.ovpn"),
                mPreference.getString(SERVER_OVPN_USER,"vpn"),
                mPreference.getString(SERVER_OVPN_PASSWORD,"vpn")
        );

        return server;
    }
}
