package laas.rcayre.radiosploit.ui.zigbee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.radiosploit.R;
import com.google.android.material.tabs.TabLayout;

public class ZigbeeFragment extends Fragment {
    /* Zigbee Fragment, it mainly instantiates the tabs */
    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_zigbee, container, false);
        ZigbeeTabAdapter tabAdapter = new ZigbeeTabAdapter(getChildFragmentManager());
        ViewPager viewPager = root.findViewById(R.id.zigbee_view_pager);
        viewPager.setAdapter(tabAdapter);
        TabLayout tabs = root.findViewById(R.id.zigbee_tabs);
        tabs.setupWithViewPager(viewPager);
        return root;
    }
}