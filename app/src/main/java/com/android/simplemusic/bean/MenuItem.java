package com.android.simplemusic.bean;

import android.graphics.drawable.Drawable;

public class MenuItem {
    private Drawable icon;
    private String title;

    public MenuItem(Drawable icon, String title) {
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
