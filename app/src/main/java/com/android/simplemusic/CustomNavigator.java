package com.android.simplemusic;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigator;
import androidx.navigation.fragment.FragmentNavigator;

@Navigator.Name("customNavigator")
public class CustomNavigator extends FragmentNavigator {

    private static final String TAG = "CustomNavigator";

    private Context context;
    private FragmentManager fragmentManager;
    private int containerId;

    public CustomNavigator(@NonNull Context context, @NonNull FragmentManager fragmentManager, int containerId) {
        super(context, fragmentManager, containerId);
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.containerId = containerId;
    }

    @Nullable
    @Override
    public NavDestination navigate(@NonNull Destination destination, @Nullable Bundle args, @Nullable NavOptions navOptions, @Nullable Navigator.Extras navigatorExtras) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        // 获取当前显示的Fragment
        Fragment fragment = fragmentManager.getPrimaryNavigationFragment();
        if (fragment != null) {
            ft.hide(fragment);
        }
        final String tag = String.valueOf(destination.getId());
        fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment != null) {
            Log.i(TAG, "Fragment " + fragment.getTag() + " isn't null, show fragment.");
            ft.show(fragment);
        } else {
            Log.i(TAG, "Fragment is null, build fragment.");
            // fragment = instantiateFragment(context, fragmentManager, destination.getClassName(), args);
            fragment = fragmentManager.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), destination.getClassName());
            ft.add(containerId, fragment, tag);
        }
        ft.setPrimaryNavigationFragment(fragment);
        ft.setReorderingAllowed(true);
        ft.commit();
        return destination;
    }
}