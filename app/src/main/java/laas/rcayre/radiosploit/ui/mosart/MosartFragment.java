package laas.rcayre.radiosploit.ui.mosart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.radiosploit.R;
import com.google.android.material.tabs.TabLayout;

public class MosartFragment extends Fragment {
    /* Mosart fragment, it configures the tabs. Nothing to see here. */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mosart, container, false);
        MosartTabAdapter tabAdapter = new MosartTabAdapter(getChildFragmentManager());
        ViewPager viewPager = root.findViewById(R.id.mosart_view_pager);
        viewPager.setAdapter(tabAdapter);
        TabLayout tabs = root.findViewById(R.id.mosart_tabs);
        tabs.setupWithViewPager(viewPager);

        return root;
    }
}