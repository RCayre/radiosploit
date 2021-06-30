package laas.rcayre.radiosploit.ui.esb;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import laas.rcayre.radiosploit.HciInterface;
import laas.rcayre.radiosploit.MainActivity;
import laas.rcayre.radiosploit.PacketItemData;
import laas.rcayre.radiosploit.PacketListAdapter;
import com.example.radiosploit.R;

import java.util.ArrayList;

public class EsbScanFragment extends Fragment {
    /* This is the Enhanced Shockburst Scan fragment, allowing to detect ESB devices */
    private EsbScanThread esbScanThread;
    private ToggleButton esbScanToggleButton;
    public EsbScanFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_esb_scan, container, false);
        esbScanToggleButton = root.findViewById(R.id.esb_scan_toggle_button);
        RecyclerView deviceView = root.findViewById(R.id.esb_scan_device_view);
        ProgressBar progressBar = root.findViewById(R.id.esb_scan_progressbar);
        ArrayList<PacketItemData> packetList = new ArrayList<PacketItemData>();
        PacketListAdapter adapter = new EsbDevicePacketListAdapter(packetList);
        deviceView.setHasFixedSize(true);
        deviceView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        deviceView.setAdapter(adapter);

        HciInterface hciInterface = ((MainActivity)getActivity()).getHciInterface();
        esbScanThread = new EsbScanThread((MainActivity)getActivity(),hciInterface,adapter, progressBar);
        Button esbScanResetButton = root.findViewById(R.id.esb_scan_reset_button);
        esbScanResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                esbScanThread.getDevicesList().clear();
            }
        });

        esbScanToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(esbScanToggleButton.isChecked()){
                    Thread esbThread = new Thread(esbScanThread);
                    esbThread.start();
                }
                else {
                    esbScanThread.stop();
                }
            }});

        return root;
    }
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (!visible) {
            /* If the fragment is hidden, stops the thread */
            if (esbScanThread != null && esbScanThread.isRunning()) {
                esbScanToggleButton.setChecked(false);
                esbScanThread.stop();
            }
        }
    }

}