package laas.rcayre.radiosploit.ui.zigbee;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import laas.rcayre.radiosploit.HciInterface;
import laas.rcayre.radiosploit.MainActivity;
import laas.rcayre.radiosploit.PacketItemData;
import laas.rcayre.radiosploit.PacketListAdapter;
import com.example.radiosploit.R;
import laas.rcayre.radiosploit.dissectors.Dissector;
import laas.rcayre.radiosploit.dissectors.ZigbeeDissector;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class ZigbeeTxFragment extends Fragment {
    /* Zigbee TX fragment, allowing to send a list of packets */
    private static final int REQUEST_CODE = 1;
    private View root;
    private ArrayList<PacketItemData> packetList = new ArrayList<PacketItemData>();
    private ZigbeeTxThread zigbeeTxThread;
    private ToggleButton startTxToggleButton;

    private PacketListAdapter adapter;

    public ZigbeeTxFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* This method is called when an Editor dialog is validated */
        if (requestCode == 0) {
            String packetData = data.getStringExtra("packetData");
            byte[] zigbeeFrame = Dissector.hexToBytes(packetData);
            String description = ZigbeeDissector.getZigbeePacketDescription(zigbeeFrame);
            /* It was a new packet, add it */
            if (resultCode == -1) {
                adapter.addNewData(new PacketItemData(zigbeeFrame, 0x01, description, ""));
            }
            /* It was an existing packet, modify it */
            else {
                adapter.updateData(resultCode, new PacketItemData(zigbeeFrame, 0x01, description, ""));
            }
        }
    }


    private Observer<PacketItemData> getInputObserver() {
        /* Called when a packet is transmitted from RX fragment */
        return new Observer<PacketItemData>() {
            @Override public void onSubscribe(Disposable d) {
            }
            @Override public void onNext(PacketItemData s) {
                adapter.addNewData(s);
            }
            @Override public void onError(Throwable e) {
            }
            @Override public void onComplete() {
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_zigbee_tx, container, false);

        HciInterface hciInterface = ((MainActivity)getActivity()).getHciInterface();



        RecyclerView packetView = (RecyclerView)root.findViewById(R.id.zigbee_tx_packet_view);
        adapter = new ZigbeeTxPacketListAdapter(packetList,this);
        packetView.setHasFixedSize(true);
        packetView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        packetView.setAdapter(adapter);
        ZigbeeBus.getInstance().listen().subscribe(getInputObserver());

        ProgressBar txProgressBar = (ProgressBar)root.findViewById(R.id.zigbee_tx_progressbar);
        startTxToggleButton = (ToggleButton)root.findViewById(R.id.zigbee_tx_toggle_button);

        TextView zigbeeTxChannelLabel = (TextView)root.findViewById(R.id.zigbee_tx_channel_label);
        Slider zigbeeTxChannelSlider = (Slider)root.findViewById(R.id.zigbee_tx_channel_slider);
        zigbeeTxThread = new ZigbeeTxThread((MainActivity)getActivity(),adapter,hciInterface,txProgressBar,startTxToggleButton);

        zigbeeTxChannelSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                zigbeeTxChannelLabel.setText("CH:" + String.valueOf((int) (value)));
                if (startTxToggleButton.isChecked()) {
                    zigbeeTxThread.updateChannel((int)(zigbeeTxChannelSlider.getValue()));
                }
            }

        });

        startTxToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //int channel = (int)(zigbeeeChannelSlider.getValue());
                if(startTxToggleButton.isChecked()){
                    Thread zigbeeThread = new Thread(zigbeeTxThread);
                    zigbeeTxThread.updateChannel((int)(zigbeeTxChannelSlider.getValue()));
                    zigbeeThread.start();
                }
                else {
                    zigbeeTxThread.stop();
                }
            }});
        Button resetButton = root.findViewById(R.id.zigbee_tx_reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.resetData();
            }
        });


        FloatingActionButton addPacketButton = root.findViewById(R.id.zigbee_add_packet_floating_button);
        addPacketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                Fragment prev = getParentFragmentManager().findFragmentByTag("ZigbeeEditDialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment dialogFragment = ZigbeeEditDialogFragment.newInstance(-1,"");
                dialogFragment.setTargetFragment(ZigbeeTxFragment.this,0);
                dialogFragment.show(ft, "ZigbeeEditDialog");
            }
        });
        return root;
    }
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (!visible) {
            /* We should stop the thread if the fragment is hidden */
            if (zigbeeTxThread != null && zigbeeTxThread.isRunning()) {
                startTxToggleButton.setChecked(false);
                zigbeeTxThread.stop();
            }
        }
    }
}