package laas.rcayre.radiosploit.ui.esb;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.radiosploit.R;
import com.google.android.material.tabs.TabLayout;

public class EsbFragment extends Fragment {
    /* Enhanced ShockBurst fragment, mainly configure the tabs */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_esb, container, false);
        EsbTabAdapter tabAdapter = new EsbTabAdapter(getChildFragmentManager());
        ViewPager viewPager = root.findViewById(R.id.esb_view_pager);
        viewPager.setAdapter(tabAdapter);
        TabLayout tabs = root.findViewById(R.id.esb_tabs);
        tabs.setupWithViewPager(viewPager);

        return root;
    }
}