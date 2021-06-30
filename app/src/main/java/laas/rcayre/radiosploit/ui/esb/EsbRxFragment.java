package laas.rcayre.radiosploit.ui.esb;


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

public class EsbRxFragment extends Fragment {
    /* Enhanced Shockburst Rx fragment, allowing to receive Enhanced ShockBurst packets */
    private EsbRxThread esbRxThread;
    private ToggleButton startRxToggleButton;
    private TextView esbRxAddress;
    private TextView esbRxChannelLabel;
    private Slider esbRxChannelSlider;
    private HciInterface hciInterface;

    public EsbRxFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_esb_rx, container, false);

        esbRxChannelSlider = (Slider)(root.findViewById(R.id.esb_rx_channel_slider));
        esbRxChannelLabel = (TextView) (root.findViewById(R.id.esb_rx_channel_label));
        startRxToggleButton = (ToggleButton) root.findViewById(R.id.esb_rx_toggle_button);
        esbRxAddress = (TextView)root.findViewById(R.id.esb_rx_address_textentry);
        Button esbRxResetButton = (Button)(root.findViewById(R.id.esb_rx_reset_button));
        RecyclerView esbRxPacketView = (RecyclerView)root.findViewById(R.id.esb_rx_packet_view);

        hciInterface = ((MainActivity)getActivity()).getHciInterface();

        ArrayList<PacketItemData> packetList = new ArrayList<PacketItemData>();
        PacketListAdapter adapter = new EsbRxPacketListAdapter(packetList,this);
        esbRxPacketView.setHasFixedSize(true);
        esbRxPacketView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        esbRxPacketView.setAdapter(adapter);

        EsbDeviceBus.getInstance().listen().subscribe(getInputObserver());
        esbRxThread = new EsbRxThread((MainActivity)getActivity(),adapter,hciInterface,esbRxPacketView);

        esbRxResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.resetData();
            }
        });
        esbRxChannelSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                esbRxChannelLabel.setText("CH:" + String.valueOf((int) (value)));
                if (startRxToggleButton.isChecked() && esbRxAddress.getText().toString().length() == 14) {
                    hciInterface.configureEsbRx(true,(int)value, Dissector.addressToBytes(esbRxAddress.getText().toString()));
                }
            }
        });
        startRxToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int channel = (int)(esbRxChannelSlider.getValue());
                if(startRxToggleButton.isChecked()){
                    if (esbRxAddress.getText().toString().length() == 14) {
                        hciInterface.configureEsbRx(true, channel, Dissector.addressToBytes(esbRxAddress.getText().toString()));
                    }
                    else {
                        startRxToggleButton.setChecked(false);
                    }
                    Thread esbThread = new Thread(esbRxThread);
                    esbThread.start();
                }
                else {
                    byte[] address = {0x00,0x00,0x00,0x00,0x00};
                    hciInterface.configureEsbRx(false,channel, address);
                    esbRxThread.stop();
                }
            }});

        return root;
    }
    // Get input observer instance
    private Observer<PacketItemData> getInputObserver() {
        return new Observer<PacketItemData>() {
            @Override public void onSubscribe(Disposable d) {
            }
            @Override public void onNext(PacketItemData s) {
                int channel = Integer.parseInt(s.getStatus().substring(8,10));
                esbRxAddress.setText(s.getFormattedContent());
                esbRxChannelSlider.setValue(channel);
                esbRxChannelLabel.setText("CH: "+channel);
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
            /* If the fragment is hidden, stops the thread */
            if (esbRxThread != null && esbRxThread.isRunning()) {
                startRxToggleButton.setChecked(false);
                esbRxThread.stop();
            }
        }
    }

}