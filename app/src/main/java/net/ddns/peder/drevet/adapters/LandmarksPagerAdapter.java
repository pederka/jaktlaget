package net.ddns.peder.drevet.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import net.ddns.peder.drevet.fragments.LandmarksFragment;
import net.ddns.peder.drevet.fragments.TeamLandmarksFragment;
import net.ddns.peder.drevet.fragments.TeamManagementFragment;

/**
 * Created by peder on 4/1/17.
 */

public class LandmarksPagerAdapter extends FragmentPagerAdapter {


    public LandmarksPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment;
        switch (i) {
            case (0):
                fragment = new LandmarksFragment();
                break;
            case (1):
                fragment = new TeamLandmarksFragment();
                break;
            default:
                fragment = new LandmarksFragment();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case (0):
                return "Egne";
            case (1):
                return "Lagets";
            default:
                return "Feil";
        }
    }
}
