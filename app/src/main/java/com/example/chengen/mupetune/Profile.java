package com.example.chengen.mupetune;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
public class Profile extends Fragment implements View.OnClickListener {
    View v;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        v = inflater.inflate(R.layout.activity_profile, container, false);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        getActivity().getActionBar().hide();
        String name,username = null;
        if(sharedPreferences.contains("firstName"))
            name = sharedPreferences.getString("firstName", "");
        if(sharedPreferences.contains("username"))
            username = sharedPreferences.getString("username", "");
        ImageView pic = (ImageView) v.findViewById(R.id.profPic);
        TextView txtUsername = (TextView)v.findViewById(R.id.txtUsername);
        //txtUsername.setText(username);
        return v;
    }
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnHistory:
                break;
            case R.id.btnGroups:
                break;
            case R.id.btnFavGroups:
                break;
            case R.id.btnNotifications:
                break;
        }
    }
}