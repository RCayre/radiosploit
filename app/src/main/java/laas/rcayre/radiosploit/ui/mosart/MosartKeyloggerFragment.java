package laas.rcayre.radiosploit.ui.mosart;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import laas.rcayre.radiosploit.HciInterface;
import laas.rcayre.radiosploit.MainActivity;
import laas.rcayre.radiosploit.PacketItemData;
import com.example.radiosploit.R;
import laas.rcayre.radiosploit.dissectors.Dissector;
import com.google.android.material.slider.Slider;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MosartKeyloggerFragment extends Fragment {
    /* This is the Mosart Keylogger fragment, allowing to write the key names */
    private MosartKeyloggerThread mosartKeyloggerThread;
    private ToggleButton startKeyloggerToggleButton;
    private TextView mosartKeyloggerAddress;
    private Slider mosartKeyloggerChannelSlider;
    private TextView mosartKeyloggerChannelLabel;
    private HciInterface hciInterface;

    public MosartKeyloggerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_mosart_keylogger, container, false);

        mosartKeyloggerChannelSlider = (Slider)(root.findViewById(R.id.mosart_keylogger_channel_slider));
        mosartKeyloggerChannelLabel = (TextView) (root.findViewById(R.id.mosart_keylogger_channel_label));
        startKeyloggerToggleButton = (ToggleButton) root.findViewById(R.id.mosart_keylogger_toggle_button);
        mosartKeyloggerAddress = (TextView)root.findViewById(R.id.mosart_keylogger_address_textentry);
        Button mosartKeyloggerResetButton = (Button)(root.findViewById(R.id.mosart_keylogger_reset_button));

        MosartDeviceBus.getInstance().listen().subscribe(getInputObserver());

        EditText mosartKeyloggerVisualizer = (EditText)root.findViewById(R.id.mosart_keylogger_visualizer);

        hciInterface = ((MainActivity)getActivity()).getHciInterface();
        mosartKeyloggerThread = new MosartKeyloggerThread((MainActivity)getActivity(),hciInterface,mosartKeyloggerVisualizer);

        mosartKeyloggerResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mosartKeyloggerVisualizer.setText("");
            }
        });
        mosartKeyloggerChannelSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                mosartKeyloggerChannelLabel.setText("CH:" + String.valueOf((int) (value)));
                if (startKeyloggerToggleButton.isChecked() && mosartKeyloggerAddress.getText().toString().length() == 11) {
                    hciInterface.configureMosartKeylogger(true,(int)value, Dissector.addressToBytes(mosartKeyloggerAddress.getText().toString()));
                }
            }
        });
        startKeyloggerToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int channel = (int)(mosartKeyloggerChannelSlider.getValue());
                if(startKeyloggerToggleButton.isChecked()){
                    if (mosartKeyloggerAddress.getText().toString().length() == 11) {
                        hciInterface.configureMosartKeylogger(true, channel, Dissector.addressToBytes(mosartKeyloggerAddress.getText().toString()));
                    }
                    else {
                        startKeyloggerToggleButton.setChecked(false);
                    }
                    Thread mosartThread = new Thread(mosartKeyloggerThread);
                    mosartThread.start();
                }
                else {
                        byte[] address = {0x00,0x00,0x00,0x00};
                        hciInterface.configureMosartKeylogger(false,channel, address);
                        mosartKeyloggerThread.stop();
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
                mosartKeyloggerAddress.setText(s.getFormattedContent());
                mosartKeyloggerChannelSlider.setValue(channel);
                mosartKeyloggerChannelLabel.setText("CH: "+channel);
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
            /* If the fragment is hidden, we have to stop the thread */
            if (mosartKeyloggerThread != null && mosartKeyloggerThread.isRunning()) {
                mosartKeyloggerThread.stop();
                byte[] address = {0x00, 0x00, 0x00, 0x00};
                hciInterface.configureMosartKeylogger(false, (int) (mosartKeyloggerChannelSlider.getValue()), address);
                startKeyloggerToggleButton.setChecked(false);
            }
        }
    }

}