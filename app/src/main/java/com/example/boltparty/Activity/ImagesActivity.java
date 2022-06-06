package com.example.boltparty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class ImagesActivity extends AppCompatActivity {

    private ViewPager viewPager;

    int[] owlCafe = {
            R.drawable.owlcafe,
            R.drawable.owl2,
            R.drawable.owl3,
            R.drawable.owl4,
            R.drawable.owl5,
            R.drawable.owl6,
            R.drawable.owl7
    };
    int[] digginCafe = {
            R.drawable.diggincafe,
            R.drawable.diggin2,
            R.drawable.diggin3,
            R.drawable.diggin4,
            R.drawable.diggin5,
            R.drawable.diggin6,
            R.drawable.diggin7
    };

    ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        String cafeName = extras.getString("CafeName");
        viewPager = findViewById(R.id.mainViewPager);

        if (cafeName.equals("The Owl Cafe")) {
            viewPagerAdapter = new ViewPagerAdapter(this, owlCafe);
        } else if (cafeName.equals("Diggin Cafe")) {
            viewPagerAdapter = new ViewPagerAdapter(this, digginCafe);
        }

        viewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        int statusInfo = NetworkInfo.getNetworkStatus(this);
        if (statusInfo == NetworkInfo.NOT_CONNECTED) {
            startActivity(new Intent(this, NoNetworkActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Intent intent = new Intent(ImagesActivity.this, MainActivity.class);
                    startActivity(intent);
                    finishAffinity();
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }
}