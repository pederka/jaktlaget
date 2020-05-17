package net.ddns.peder.jaktlaget.adapters;

import net.ddns.peder.jaktlaget.fragments.LandmarksFragment;
import net.ddns.peder.jaktlaget.fragments.TeamLandmarksFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * Created by peder on 4/1/17.
 */

public class LandmarksPagerAdapter extends FragmentStatePagerAdapter {


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
