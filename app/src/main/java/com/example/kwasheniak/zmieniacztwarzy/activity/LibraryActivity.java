package com.example.kwasheniak.zmieniacztwarzy.activity;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.kwasheniak.zmieniacztwarzy.librarylayoututils.DirectoryFragment;
import com.example.kwasheniak.zmieniacztwarzy.R;
import com.example.kwasheniak.zmieniacztwarzy.utils.Variables;
import com.example.kwasheniak.zmieniacztwarzy.librarylayoututils.ViewPagerAdapter;

public class LibraryActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new DirectoryFragment().newInstance(Variables.APP_PICTURES_DIRECTORY.getAbsolutePath(), Variables.SHOW_PREVIEW_PICTURE_ACTIVITY), "Moje zdjęcia");
        viewPagerAdapter.addFragments(new DirectoryFragment().newInstance(Variables.CUSTOM_MASKS_DIRECTORY.getAbsolutePath(), Variables.SHOW_PREVIEW_PICTURE_ACTIVITY), "Moje maski");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }
}


