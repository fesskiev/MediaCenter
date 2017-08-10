package com.fesskiev.mediacenterwear;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.drawer.WearableDrawerLayout;
import android.support.wearable.view.drawer.WearableNavigationDrawer;
import android.view.Gravity;
import android.view.ViewTreeObserver;


public class MainActivity extends WearableActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Enables Always-on
        setAmbientEnabled();

        final WearableDrawerLayout wearableDrawerLayout = (WearableDrawerLayout) findViewById(R.id.drawerLayout);
        WearableNavigationDrawer wearableNavigationDrawer = (WearableNavigationDrawer) findViewById(R.id.navigationDrawer);
        wearableNavigationDrawer.setAdapter(new NavigationAdapter());


        // Temporarily peeks the navigation and action drawers to ensure the user is aware of them.
        ViewTreeObserver observer = wearableDrawerLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                wearableDrawerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                wearableDrawerLayout.peekDrawer(Gravity.TOP);
                wearableDrawerLayout.peekDrawer(Gravity.BOTTOM);
            }
        });

        /* Action Drawer Tip: If you only have a single action for your Action Drawer, you can use a
         * (custom) View to peek on top of the content by calling
         * mWearableActionDrawer.setPeekContent(View). Make sure you set a click listener to handle
         * a user clicking on your View.
         */

    }

    private final class NavigationAdapter extends WearableNavigationDrawer.WearableNavigationDrawerAdapter {

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public void onItemSelected(int position) {

        }

        @Override
        public String getItemText(int pos) {
            switch (pos) {
                case 0:
                    return getString(R.string.drawer_item_control);
                case 1:
                    return getString(R.string.drawer_item_tracklist);
            }
            return "";
        }

        @Override
        public Drawable getItemDrawable(int pos) {
            switch (pos) {
                case 0:
                    return ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_control);
                case 1:
                    return ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_tracklist);
            }
            return null;
        }
    }


}
