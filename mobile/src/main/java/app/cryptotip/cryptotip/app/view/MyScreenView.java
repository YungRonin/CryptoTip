package app.cryptotip.cryptotip.app.view;

import android.support.v7.app.ActionBar;

import com.gani.lib.screen.GActivity;
import com.gani.lib.screen.GScreenView;

public class MyScreenView extends GScreenView {
    public MyScreenView(GActivity activity) {
        super(activity);

        //getDrawer().setFitsSystemWindows(false);
    }

    @Override
    protected void initNavigation(boolean topNavigation, ActionBar actionBar) {
        super.initNavigation(topNavigation, actionBar);

        // Use our custom button instead.
        actionBar.setDisplayHomeAsUpEnabled(false);
    }
}
