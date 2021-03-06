package net.ddns.peder.jaktlaget.adapters;

import android.util.SparseArray;
import android.view.ViewGroup;

import net.ddns.peder.jaktlaget.fragments.TeamFragment;
import net.ddns.peder.jaktlaget.fragments.TeamInfoFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * Created by peder on 4/1/17.
 */

public class TeamPagerAdapter extends FragmentStatePagerAdapter {
    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

    public TeamPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment;
        switch (i) {
            case (0):
                fragment = new TeamInfoFragment();
                break;
            case (1):
                fragment = new TeamFragment();
                break;
            default:
                fragment = new TeamInfoFragment();
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
                return "Laginfo";
            case (1):
                return "Jegere";
            default:
                return "Feil";
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

}
