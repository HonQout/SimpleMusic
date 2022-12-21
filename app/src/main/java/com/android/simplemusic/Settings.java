package com.android.simplemusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class Settings extends AppCompatActivity {
    private int UiMode;

    private Toolbar toolBar2;
    private ListView settingsList;
    private List<MenuListItem> menuItems;
    private MenuListAdapter menuListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getWindow().setStatusBarColor(getResources().getColor(R.color.toolbar_background, getTheme()));
        UiMode = getApplicationContext().getResources().getConfiguration().uiMode;
        if ((UiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        toolBar2 = (Toolbar) findViewById(R.id.toolBar2);
        toolBar2.setTitle("");
        if ((UiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            toolBar2.setNavigationIcon(R.drawable.arrow_back_black);
        } else {
            toolBar2.setNavigationIcon(R.drawable.arrow_back_white);
        }
        toolBar2.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        settingsList = (ListView) findViewById(R.id.settingsList);
        @SuppressLint("UseCompatLoadingForDrawables") Drawable[] icons = {getDrawable(R.drawable.equalizer_black)};
        String[] titles = {getString(R.string.equalizer)};
        menuItems = new ArrayList<MenuListItem>();
        for (int i = 0; i < icons.length; i++) {
            menuItems.add(new MenuListItem(icons[i], titles[i]));
        }
        menuListAdapter = new MenuListAdapter(Settings.this, R.layout.menu_item, menuItems);
        settingsList.setAdapter(menuListAdapter);
        settingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    default:
                        break;
                }
            }
        });
    }
}