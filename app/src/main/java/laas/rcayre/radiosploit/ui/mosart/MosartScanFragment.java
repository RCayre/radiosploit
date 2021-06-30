package laas.rcayre.radiosploit.ui.mosart;


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

public class MosartScanFragment extends Fragment {
    /* This fragment allows to scan Mosart devices */
    private MosartScanThread mosartScanThread;
    private ToggleButton mosartScanToggleButton;
    public MosartScanFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Gets the views */
        View root = inflater.inflate(R.layout.fragment_mosart_scan, container, false);
        mosartScanToggleButton = root.findViewById(R.id.mosart_scan_toggle_button);
        RecyclerView deviceView = root.findViewById(R.id.mosart_scan_device_view);
        ProgressBar progressBar = root.findViewById(R.id.mosart_scan_progressbar);
        ArrayList<PacketItemData> packetList = new ArrayList<PacketItemData>();
        PacketListAdapter adapter = new MosartDevicePacketListAdapter(packetList);
        deviceView.setHasFixedSize(true);
        deviceView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        deviceView.setAdapter(adapter);

        HciInterface hciInterface = ((MainActivity)getActivity()).getHciInterface();
        mosartScanThread = new MosartScanThread((MainActivity)getActivity(),hciInterface,adapter, progressBar);
        Button mosartScanResetButton = root.findViewById(R.id.mosart_scan_reset_button);

        /* Configures the listeners */
        mosartScanResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mosartScanThread.getDevicesList().clear();
            }
        });

        mosartScanToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //int channel = (int)(zigbeeeChannelSlider.getValue());
                if(mosartScanToggleButton.isChecked()){
                    Thread mosartThread = new Thread(mosartScanThread);
                    mosartThread.start();
                }
                else {
                    mosartScanThread.stop();
                }
            }});

        return root;
    }
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (!visible) {
            /* When the fragment is hidden, stops the thread ! */
            if (mosartScanThread != null && mosartScanThread.isRunning()) {
                mosartScanToggleButton.setChecked(false);
                mosartScanThread.stop();
            }
        }
    }

}