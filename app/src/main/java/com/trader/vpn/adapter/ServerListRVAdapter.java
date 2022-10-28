package com.trader.vpn.adapter;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.trader.vpn.R;
import com.trader.vpn.interfaces.NavItemClickListener;
import com.trader.vpn.model.Server;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class ServerListRVAdapter extends ArrayAdapter<Server> {

    private ArrayList<Server> serverLists;
    private Context mContext;
    private LayoutInflater layoutInflater;

    public ServerListRVAdapter(@NonNull Context context, int resource, ArrayList<Server> serverLists) {
        super(context, resource);
        this.serverLists = serverLists;
        this.mContext = context;

        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.server_list_view, parent, false);
        } else {
            view = convertView;
        }

        setServer(view, getItem(position));

        return view;
    }

    @Override
    public Server getItem(int position) {
        if (position == 0) {
            return null;
        }
        return this.serverLists.get(position-1);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;

        if (position == 0) {
            view = layoutInflater.inflate(R.layout.header_server, parent, false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View root = parent.getRootView();
                    root.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                    root.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                }
            });
        } else {
            view = layoutInflater.inflate(R.layout.server_list_view, parent, false);
            setServer(view, getItem(position));
        }

        return view;
    }

    @Override
    public int getCount() {
        return this.serverLists.size() + 1;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0;
    }

    public void setServer(View view, Server server) {

        TextView tvServer = view.findViewById(R.id.countryTv);
        ImageView icon = view.findViewById(R.id.iconImg);

        if (server != null) {
            tvServer.setText(server.getCountry());
            Glide.with(getContext())
                    .load(server.getFlagUrl())
                    .into(icon);
        }
    }
}
