package com.example.tcprototype2.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.tcprototype2.R;
import com.example.tcprototype2.ReadyFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {
    private ViewPager viewPager;
    private TabLayout tabs;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        // Set ViewPager for tabs
        viewPager = root.findViewById(R.id.view_pager_history);
        setupViewPager(viewPager);
        // Set tabs
        tabs = root.findViewById(R.id.tabs_history);
        tabs.setupWithViewPager(viewPager);

        return root;
    }

    private void setupViewPager(ViewPager viewPager){
        HistoryTabsAdapter adapter = new HistoryTabsAdapter(getChildFragmentManager());
        adapter.addFragment(new ReceivedFragment(), "Received");
        adapter.addFragment(new SentFragment(), "Sent");
        viewPager.setAdapter(adapter);
    }

    public class HistoryTabsAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public HistoryTabsAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        public void addFragment(Fragment fragment, String title){
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
    }
}