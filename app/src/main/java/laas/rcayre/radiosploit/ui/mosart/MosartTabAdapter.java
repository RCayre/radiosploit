package laas.rcayre.radiosploit.ui.mosart;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


public class MosartTabAdapter extends FragmentPagerAdapter {
    /* This class implements the MosartTabAdapter. We have four tabs: Scan (MosartScanFragment), KeyLogger (MosartKeyloggerFragment), RX (MosartRxFragment) and TX (MosartTxFragment)*/
    private String[] tabs_name = { "Scan", "Key logger", "RX", "TX" };
    public MosartTabAdapter(FragmentManager fm) {
        super(fm);
    }
    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                return new MosartScanFragment();
            case 1:
                return new MosartKeyloggerFragment();
            case 2:
                return new MosartRxFragment();
            case 3:
                return new MosartTxFragment();
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
        return 4;
    }
}
