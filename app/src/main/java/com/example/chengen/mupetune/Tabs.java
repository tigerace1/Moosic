package com.example.chengen.mupetune;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
public class Tabs extends AppCompatActivity implements Communicator, ViewPager.OnPageChangeListener,TabHost.OnTabChangeListener {
    private ViewPager viewPager;
    private TabHost tabHost;
    List<Fragment> fragmentList;
    private int index;
    private MusicPlayer musicPlayer;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabs_layout);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setLogo(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.moo)));
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        if(sharedPreferences.contains("username"))
            getSupportActionBar().setTitle(sharedPreferences.getString("username",""));
        fragmentList = new ArrayList<>();
        musicPlayer = new MusicPlayer();
        fragmentList.add(new MoosicRoomList());
        fragmentList.add(new LocalMusics());
        fragmentList.add(musicPlayer);
        fragmentList.add(new Profile());
        index = 0;
        initPager();
        initHost();
    }
    @Override
    public void respond(int position, ArrayList<File> songs) {
        musicPlayer.getData(position, songs);
        tabHost.setCurrentTab(2);
        viewPager.setCurrentItem(2);
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
        String[] tabNames = {"rooms","home","playing","profile"};
        Bitmap list= BitmapFactory.decodeResource(getResources(), R.drawable.musiclist);
        Bitmap moose = BitmapFactory.decodeResource(getResources(), R.drawable.moo);
        Bitmap profile = BitmapFactory.decodeResource(getResources(), R.drawable.prof);
        Bitmap play = BitmapFactory.decodeResource(getResources(), R.drawable.plays);
        Drawable[] icons={new BitmapDrawable(getResources(),Bitmap.createScaledBitmap(moose,50,50,false)),
                new BitmapDrawable(getResources(),Bitmap.createScaledBitmap(list,50,50,false)),
                new BitmapDrawable(getResources(),Bitmap.createScaledBitmap(play,50,50,false))
                ,new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(profile,50,50,false))};
        for(int i=0;i<tabNames.length;i++){
            TabHost.TabSpec tabSpec;
            tabSpec = tabHost.newTabSpec(tabNames[i]);
            tabSpec.setIndicator(tabNames[i], icons[i]);
            tabSpec.setContent(new FakeContent(getApplicationContext()));
            tabHost.addTab(tabSpec);
            TextView x = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            ImageView imageView = (ImageView)tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.icon);
            imageView.setScaleY(25);
            imageView.setScaleX(25);
            x.setTextSize(12);
            x.setGravity(Gravity.TOP);
        }
        for(int i=0;i<tabHost.getTabWidget().getTabCount();i++)
            tabHost.getTabWidget().getChildAt(i).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tab2));
        tabHost.setCurrentTab(index);
        tabHost.getTabWidget().getChildAt(index).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tab));
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
        for(int i=0;i<tabHost.getTabWidget().getTabCount();i++)
            tabHost.getTabWidget().getChildAt(i).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tab2));
        tabHost.getTabWidget().getChildAt(selectedItem).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tab));
        viewPager.setCurrentItem(selectedItem);
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }
    @Override
    public void onPageSelected(int position) {
        tabHost.setCurrentTab(position);
        int selectedItem = tabHost.getCurrentTab();
        for(int i=0;i<tabHost.getTabWidget().getTabCount();i++)
            tabHost.getTabWidget().getChildAt(i).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tab2));
        tabHost.getTabWidget().getChildAt(selectedItem).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tab));
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
        if (item.getItemId() == R.id.search){
            Intent intent = new Intent(this,Search.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        }if(item.getItemId()==R.id.action_logout){
            SharedPreferences preferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
            startActivity(new Intent(this, LoginPage.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

