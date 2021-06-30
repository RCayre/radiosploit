package laas.rcayre.radiosploit.ui.zigbee;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ZigbeeTabAdapter extends FragmentPagerAdapter {
    /* Zigbee Tab Adapter, it exposes two tabs, Receive (ZigbeeRxFragment) and Transmit (ZigbeeTxFragment) */
    private String[] tabs_name = { "Receive", "Transmit" };
    public ZigbeeTabAdapter(FragmentManager fm) {
        super(fm);
    }
    @Override
    public Fragment getItem(int index) {
        switch (index) {
            case 0:
                return new ZigbeeRxFragment();
            case 1:
                return new ZigbeeTxFragment();
        }

        return null;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabs_name[position];
    }

    @Override
    public int getCount() {
        return 2;
    }
}
