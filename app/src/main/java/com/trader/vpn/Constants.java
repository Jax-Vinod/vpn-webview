package com.trader.vpn;

import com.trader.vpn.model.Server;

import java.util.ArrayList;

public class Constants {
    public static ArrayList getServerList() {

        ArrayList<Server> servers = new ArrayList<>();

        servers.add(new Server("United States",
                Utils.getImgURL(R.drawable.usa_flag),
                "us.ovpn",
                "freeopenvpn",
                "416248023"
        ));
        servers.add(new Server("Japan",
                Utils.getImgURL(R.drawable.japan),
                "japan.ovpn",
                "vpn",
                "vpn"
        ));
        servers.add(new Server("Japan1",
                Utils.getImgURL(R.drawable.japan),
                "japan1.ovpn",
                "vpn",
                "vpn"
        ));

        return servers;
    }
}
