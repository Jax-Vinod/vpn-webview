package com.trader.vpn.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.trader.vpn.R;


public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView bottomNavigationView;

    public static final String TAG = "MainActivity";
    private String currentTag = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initNavigation();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameContainer,
                    new BrowserFragment()).commit();
        }
    }

    private void initNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
//        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    public void replaceFragment(Fragment someFragment, String tag) {

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(tag);
        Fragment fragment2 = fm.findFragmentById(R.id.frameContainer);

        currentTag = tag;

        if (fragment != fragment2) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.right_enter, R.anim.left_out, R.anim.left_enter, R.anim.right_out);
            transaction.replace(R.id.frameContainer, someFragment, tag);
            transaction.addToBackStack(tag);
            transaction.commit();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        String tag = "";
        switch (item.getItemId()) {
            case R.id.browser:
                fragment = new BrowserFragment();
                tag = "browserFragment";
                break;
            case R.id.vpn:
                fragment = new VpnFragment();
                tag = "vpnFragment";
                break;
        }
        if (fragment != null) {
            replaceFragment(fragment, tag);
        }
        return true;
    }
}
