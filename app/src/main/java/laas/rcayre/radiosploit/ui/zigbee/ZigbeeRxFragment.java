package laas.rcayre.radiosploit.ui.zigbee;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import laas.rcayre.radiosploit.HciInterface;
import laas.rcayre.radiosploit.MainActivity;
import laas.rcayre.radiosploit.PacketListAdapter;
import com.example.radiosploit.R;
import laas.rcayre.radiosploit.PacketItemData;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;

public class ZigbeeRxFragment extends Fragment {
    /* Zigbee RX Fragment, used to receive Zigbee packets */
    private ZigbeeRxThread zigbeeRxThread;
    private Slider zigbeeChannelSlider;
    private HciInterface hciInterface;
    private ToggleButton startRxToggleButton;
    public ZigbeeRxFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_zigbee_rx, container, false);
        zigbeeChannelSlider = (Slider)(root.findViewById(R.id.zigbee_channel_slider));
        TextView zigbeeChannelLabel = (TextView) (root.findViewById(R.id.zigbee_channel_label));
        startRxToggleButton = (ToggleButton) root.findViewById(R.id.zigbee_rx_toggle_button);
        Button zigbeeResetButton = root.findViewById(R.id.zigbee_reset_button);
        Switch zigbeeFcsSwitch = root.findViewById(R.id.zigbee_fcs_check);
        RecyclerView packetView = root.findViewById(R.id.zigbee_rx_packet_view);

        hciInterface = ((MainActivity)getActivity()).getHciInterface();

        zigbeeChannelSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                zigbeeChannelLabel.setText("CH:" + String.valueOf((int) (value)));
                if (startRxToggleButton.isChecked()) {
                    hciInterface.configureZigbeeRx(true,(int)value);
                }
            }

        });
        ArrayList<PacketItemData> packetList = new ArrayList<PacketItemData>();
        PacketListAdapter adapter = new ZigbeeRxPacketListAdapter(packetList,this);
        packetView.setHasFixedSize(true);
        packetView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        packetView.setAdapter(adapter);


        zigbeeRxThread = new ZigbeeRxThread((MainActivity)getActivity(),adapter,hciInterface,zigbeeFcsSwitch.isChecked(),packetView);
        startRxToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int channel = (int)(zigbeeChannelSlider.getValue());
                if(startRxToggleButton.isChecked()){
                    hciInterface.configureZigbeeRx(true,channel);
                    Thread zigbeeThread = new Thread(zigbeeRxThread);
                    zigbeeThread.start();
                }
                else {
                    hciInterface.configureZigbeeRx(false,channel);
                    zigbeeRxThread.stop();
                }
            }});
        zigbeeResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                adapter.resetData();
            }
        });

        zigbeeFcsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                zigbeeRxThread.updateCheckFCS(isChecked);
            }
        });

        return root;

    }
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (!visible) {
            /* If the fragment is hidden, we have to stop the thread if it is running */
            if (zigbeeRxThread != null && zigbeeRxThread.isRunning()) {
                int channel = (int)(zigbeeChannelSlider.getValue());
                hciInterface.configureZigbeeRx(false,channel);
                startRxToggleButton.setChecked(false);
                zigbeeRxThread.stop();
            }
        }
    }
}