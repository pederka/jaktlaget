package net.ddns.peder.drevet.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import net.ddns.peder.drevet.fragments.TeamFragment;
import net.ddns.peder.drevet.fragments.TeamManagementFragment;

/**
 * Created by peder on 4/1/17.
 */

public class TeamPagerAdapter extends FragmentStatePagerAdapter {


    public TeamPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment;
        switch (i) {
            case (0):
                fragment = new TeamManagementFragment();
                break;
            case (1):
                fragment = new TeamFragment();
                break;
            default:
                fragment = new TeamManagementFragment();
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
                return "Lagvalg";
            case (1):
                return "Jegere";
            default:
                return "Feil";
        }
    }
}
