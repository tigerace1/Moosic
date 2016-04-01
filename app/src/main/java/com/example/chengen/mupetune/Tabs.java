package com.example.chengen.mupetune;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;

import java.util.ArrayList;
import java.util.List;
public class Tabs extends AppCompatActivity implements ViewPager.OnPageChangeListener,TabHost.OnTabChangeListener {
    private ViewPager viewPager;
    private TabHost tabHost;
    List<Fragment> fragmentList;
    private int index;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabs_layout);
        new  Intent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setLogo(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.moo)));
        fragmentList = new ArrayList<>();
        fragmentList.add(new LocalMusics());
        index = 0;
        initPager();
        initHost();
    }
    private class FakeContent implements TabHost.TabContentFactory{
        Context context;
        public FakeContent(Context context){
            this.context=context;
        }
        @Override
        public View createTabContent(String tag) {
            View fakeview = new View(context);
            fakeview.setMinimumHeight(0);
            fakeview.setMinimumWidth(0);
            return fakeview;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }
    private void initHost(){
        tabHost=(TabHost)findViewById(R.id.tabHost);
        tabHost.setup();
        String[] tabNames = {"Rooms","Music list","Profile"};
        Bitmap list= BitmapFactory.decodeResource(getResources(), R.drawable.musiclist);
        Bitmap moose = BitmapFactory.decodeResource(getResources(), R.drawable.moo);
        Bitmap profile = BitmapFactory.decodeResource(getResources(), R.drawable.prof);;
        Drawable[] icons={new BitmapDrawable(getResources(),Bitmap.createScaledBitmap(moose,50,50,false)),
                new BitmapDrawable(getResources(),Bitmap.createScaledBitmap(list,50,50,false))
                ,new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(profile,50,50,false))};
        for(int i=0;i<tabNames.length;i++){
            TabHost.TabSpec tabSpec;
            tabSpec = tabHost.newTabSpec(tabNames[i]);
            tabSpec.setIndicator("",icons[i]);
            tabSpec.setContent(new FakeContent(getApplicationContext()));
            tabHost.addTab(tabSpec);
        }
        tabHost.setCurrentTab(index);
        tabHost.setOnTabChangedListener(this);
    }
    private void initPager(){
        FramentPageAdapter framentPageAdapter = new FramentPageAdapter(getSupportFragmentManager(), fragmentList);
        viewPager =(ViewPager)findViewById(R.id.viewpager);
        viewPager.setAdapter(framentPageAdapter);
        viewPager.setCurrentItem(index);
        viewPager.setOnPageChangeListener(this);
    }
    @Override
    public void onTabChanged(String tabId) {
        int selectedItem = tabHost.getCurrentTab();
        viewPager.setCurrentItem(selectedItem);
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }
    @Override
    public void onPageSelected(int position) {
        tabHost.setCurrentTab(position);
    }
    @Override
    public void onPageScrollStateChanged(int state) {

    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Do you want to Exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                System.exit(0);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.isplaying){
            Intent intent = new Intent(this,MusicPlayer.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

