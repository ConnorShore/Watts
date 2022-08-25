package com.dabloons.wattsapp.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.databinding.ActivityMainBinding;
import com.dabloons.wattsapp.ui.BaseActivity;
import com.dabloons.wattsapp.ui.main.fragment.AccountFragment;
import com.dabloons.wattsapp.ui.main.fragment.HomeFragment;
import com.dabloons.wattsapp.ui.main.fragment.ConnectFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends BaseActivity<ActivityMainBinding> implements NavigationBarView.OnItemSelectedListener{

    private final String LOG_TAG = "MainActivity";

    private HomeFragment homeFragment;
    private ConnectFragment connectFragment;
    private AccountFragment accountFragment;

    private BottomNavigationView bottomMenu;

    @Override
    protected ActivityMainBinding getViewBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homeFragment = new HomeFragment();
        connectFragment = new ConnectFragment();
        accountFragment = new AccountFragment();

        bottomMenu = findViewById(R.id.bottom_navigation);

        bottomMenu.setOnItemSelectedListener(this);

        Bundle extras = getIntent().getExtras();
        int intentFragment= 0;
        if(extras!= null)
        {
            intentFragment = extras.getInt("fragToLoad");
        }


        switch (intentFragment){
            case 2:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, connectFragment).commit();
                break;
            case 3:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, accountFragment).commit();
                break;
            default:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, homeFragment).commit();
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        homeFragment.updateUI(false);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.page_1:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, homeFragment).commit();
                return true;

            case R.id.page_2:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, connectFragment).commit();
                return true;

            case R.id.page_3:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, accountFragment).commit();
                return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}