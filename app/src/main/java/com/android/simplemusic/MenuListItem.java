package com.android.simplemusic;

import android.graphics.drawable.Drawable;

public class MenuListItem {
    private Drawable icon;
    private String title;

    public MenuListItem(Drawable icon, String title) {
        this.icon = icon;
        this.title = title;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }
}