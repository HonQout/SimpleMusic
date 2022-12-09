package com.android.simplemusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MenuListAdapter extends ArrayAdapter<MenuListItem> {
    private final int resourceId;
    List<MenuListItem> menuList;
    List<MenuListItem> menuList_bak;

    static class ViewHolder {
        ImageView icon;
        TextView title;

        public ViewHolder(ImageView icon, TextView title) {
            this.icon = icon;
            this.title = title;
        }
    }

    public MenuListAdapter(Context context, int resourceId, List<MenuListItem> menuList) {
        super(context, resourceId, menuList);
        this.resourceId = resourceId;
        this.menuList = menuList;
        this.menuList_bak = menuList;
    }

    @Override
    public int getCount() {
        return menuList.size();
    }

    @Override
    public MenuListItem getItem(int position) {
        if (position < menuList.size()) {
            return menuList.get(position);
        } else {
            return null;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MenuListItem menuListItem = menuList.get(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            ImageView menuIcon = view.findViewById(R.id.menuIcon);
            TextView menuTitle = view.findViewById(R.id.menuTitle);
            viewHolder = new ViewHolder(menuIcon, menuTitle);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        if (menuList != null) {
            viewHolder.icon.setImageDrawable(menuListItem.getIcon());
            viewHolder.title.setText(menuListItem.getTitle());
        }
        return view;
    }
}
