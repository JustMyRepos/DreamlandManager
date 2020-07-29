package top.canyie.dreamland.manager.ui.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import top.canyie.dreamland.manager.ui.fragments.PageFragment;

/**
 * @author canyie
 */
public class MainPagerAdapter extends FragmentPagerAdapter {
    private final PageFragment[] mFragments;
    public MainPagerAdapter(@NonNull FragmentManager fm, @NonNull PageFragment... fragments) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.mFragments = fragments;
    }

    @NonNull @Override public Fragment getItem(int position) {
        return mFragments[position];
    }

    @Override public int getCount() {
        return mFragments.length;
    }

    @Nullable @Override public CharSequence getPageTitle(int position) {
        return mFragments[position].getTitle();
    }
}
