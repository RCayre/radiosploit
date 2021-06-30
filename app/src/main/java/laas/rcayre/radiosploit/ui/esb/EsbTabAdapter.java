package laas.rcayre.radiosploit.ui.esb;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


public class EsbTabAdapter extends FragmentPagerAdapter {
    /* This class implements the EsbTabAdapter. We have three tabs: Scan (EsbScanFragment), RX (EsbRxFragment) and TX (EsbTxFragment)*/

    private String[] tabs_name = { "Scan", "RX", "TX" };
    public EsbTabAdapter(FragmentManager fm) {
        super(fm);
    }
    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                return new EsbScanFragment();
            case 1:
                return new EsbRxFragment();
            case 2:
                return new EsbTxFragment();
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
        return 3;
    }
}
