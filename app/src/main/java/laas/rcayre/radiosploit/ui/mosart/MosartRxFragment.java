package laas.rcayre.radiosploit.ui.mosart;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import laas.rcayre.radiosploit.HciInterface;
import laas.rcayre.radiosploit.MainActivity;
import laas.rcayre.radiosploit.PacketItemData;
import laas.rcayre.radiosploit.PacketListAdapter;
import com.example.radiosploit.R;
import laas.rcayre.radiosploit.dissectors.Dissector;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MosartRxFragment extends Fragment {
    /* This is the Mosart RX Fragment, allowing to receive Mosart packets */
    private MosartRxThread mosartRxThread;
    private ToggleButton startRxToggleButton;
    private TextView mosartRxAddress;
    private TextView mosartRxChannelLabel;
    private Slider mosartRxChannelSlider;
    private HciInterface hciInterface;

    public MosartRxFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View root = inflater.inflate(R.layout.fragment_mosart_rx, container, false);

        mosartRxChannelSlider = (Slider)(root.findViewById(R.id.mosart_rx_channel_slider));
        mosartRxChannelLabel = (TextView) (root.findViewById(R.id.mosart_rx_channel_label));
        startRxToggleButton = (ToggleButton) root.findViewById(R.id.mosart_rx_toggle_button);
        mosartRxAddress = (TextView)root.findViewById(R.id.mosart_rx_address_textentry);
        Button mosartRxResetButton = (Button)(root.findViewById(R.id.mosart_rx_reset_button));
        RecyclerView mosartRxPacketView = (RecyclerView)root.findViewById(R.id.mosart_rx_packet_view);

        hciInterface = ((MainActivity)getActivity()).getHciInterface();

        ArrayList<PacketItemData> packetList = new ArrayList<PacketItemData>();
        PacketListAdapter adapter = new MosartRxPacketListAdapter(packetList,this);
        mosartRxPacketView.setHasFixedSize(true);
        mosartRxPacketView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        mosartRxPacketView.setAdapter(adapter);

        MosartDeviceBus.getInstance().listen().subscribe(getInputObserver());
        mosartRxThread = new MosartRxThread((MainActivity)getActivity(),adapter,hciInterface,mosartRxPacketView);

        mosartRxResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.resetData();
            }
        });
        mosartRxChannelSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                mosartRxChannelLabel.setText("CH:" + String.valueOf((int) (value)));
                if (startRxToggleButton.isChecked() && mosartRxAddress.getText().toString().length() == 11) {
                    hciInterface.configureMosartRx(true,(int)value, Dissector.addressToBytes(mosartRxAddress.getText().toString()));
                }
            }
        });
        startRxToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int channel = (int)(mosartRxChannelSlider.getValue());
                if(startRxToggleButton.isChecked()){
                    if (mosartRxAddress.getText().toString().length() == 11) {
                        hciInterface.configureMosartRx(true, channel, Dissector.addressToBytes(mosartRxAddress.getText().toString()));
                    }
                    else {
                        startRxToggleButton.setChecked(false);
                    }
                    Thread mosartThread = new Thread(mosartRxThread);
                    mosartThread.start();
                }
                else {
                    byte[] address = {0x00,0x00,0x00,0x00};
                    hciInterface.configureMosartRx(false,channel, address);
                    mosartRxThread.stop();
                }
            }});

        return root;
    }

    /* Launched when a packet is received from Mosart Scan, and configures automatically the address and channel */
    private Observer<PacketItemData> getInputObserver() {
        return new Observer<PacketItemData>() {
            @Override public void onSubscribe(Disposable d) {
            }
            @Override public void onNext(PacketItemData s) {
                int channel = Integer.parseInt(s.getStatus().substring(8,10));
                mosartRxAddress.setText(s.getFormattedContent());
                mosartRxChannelSlider.setValue(channel);
                mosartRxChannelLabel.setText("CH: "+channel);
            }
            @Override public void onError(Throwable e) {
            }
            @Override public void onComplete() {
            }
        };
    }

    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (!visible) {
            /* If the fragment is hidden, we have to stop the RX thread */
            if (mosartRxThread != null && mosartRxThread.isRunning()) {
                startRxToggleButton.setChecked(false);
                mosartRxThread.stop();
            }
        }
    }
}