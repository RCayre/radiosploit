package laas.rcayre.radiosploit.ui.esb;

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
import laas.rcayre.radiosploit.dissectors.EsbDissector;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class EsbTxFragment extends Fragment {
    /* This is the Enhanced ShockBurst TX Fragment, allowing to transmit Enhanced ShockBurst packets */
    private static final int REQUEST_CODE = 1;
    private View root;
    private ArrayList<PacketItemData> packetList = new ArrayList<PacketItemData>();
    private PacketListAdapter adapter;
    private EsbTxThread esbTxThread;
    private ToggleButton startTxToggleButton;
    private TextView esbTxAddressEntry;
    public EsbTxFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* Called when the edit dialog is validated */
        if (requestCode == 0) {
            String packetData = data.getStringExtra("packetData");
            byte[] esbFrame = Dissector.hexToBytes(packetData);
            String description = EsbDissector.extractPacketTypeFromFrame(esbFrame);
            if (description.equals("")) {
                description = "Unknown packet";
            }
            if (resultCode == -1) { // new data
                adapter.addNewData(new PacketItemData(esbFrame, 0x06, description, "")); // adds it
            } else { // existing data
                adapter.updateData(resultCode, new PacketItemData(esbFrame, 0x06, description, "")); // updates it
            }
        }
    }

    /* This method is called when a Packet is received from RX Fragment, it adds it to the packets list*/
    private Observer<PacketItemData> getInputObserver() {
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
        root = inflater.inflate(R.layout.fragment_esb_tx, container, false);

        HciInterface hciInterface = ((MainActivity)getActivity()).getHciInterface();
        EsbDeviceBus.getInstance().listen().subscribe(getInputObserver());


        RecyclerView packetView = (RecyclerView)root.findViewById(R.id.esb_tx_packet_view);
        adapter = new EsbTxPacketListAdapter(packetList,this);
        packetView.setHasFixedSize(true);
        packetView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        packetView.setAdapter(adapter);
        EsbBus.getInstance().listen().subscribe(getInputObserver());

        ProgressBar txProgressBar = (ProgressBar)root.findViewById(R.id.esb_tx_progressbar);
        startTxToggleButton = (ToggleButton)root.findViewById(R.id.esb_tx_toggle_button);
        esbTxAddressEntry = (TextView)root.findViewById(R.id.esb_tx_address_textentry);
        TextView esbTxChannelLabel = (TextView)root.findViewById(R.id.esb_tx_channel_label);
        Slider esbTxChannelSlider = (Slider)root.findViewById(R.id.esb_tx_channel_slider);
        esbTxThread = new EsbTxThread((MainActivity)getActivity(),adapter,hciInterface,txProgressBar,startTxToggleButton,esbTxAddressEntry);

        esbTxChannelSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                esbTxChannelLabel.setText("CH:" + String.valueOf((int) (value)));
                if (startTxToggleButton.isChecked()) {
                    esbTxThread.updateChannel((int)(esbTxChannelSlider.getValue()));
                }
            }

        });

        startTxToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(startTxToggleButton.isChecked()){
                    Thread esbThread = new Thread(esbTxThread);
                    esbTxThread.updateChannel((int)(esbTxChannelSlider.getValue()));
                    esbThread.start();
                }
                else {
                    esbTxThread.stop();
                }
            }});
        Button resetButton = root.findViewById(R.id.esb_tx_reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.resetData();
            }
        });

        /* When the add button is clicked  */
        FloatingActionButton addPacketButton = root.findViewById(R.id.esb_add_packet_floating_button);
        addPacketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* Opens the Edit Dialog */
                FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                Fragment prev = getParentFragmentManager().findFragmentByTag("EsbEditDialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment dialogFragment = EsbEditDialogFragment.newInstance(-1,"");
                dialogFragment.setTargetFragment(EsbTxFragment.this,0);
                dialogFragment.show(ft, "EsbEditDialog");
            }
        });
        return root;
    }
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (!visible) {
            /* if the fragment is hidden, we have to stop the thread */
            if (esbTxThread != null && esbTxThread.isRunning()) {
                startTxToggleButton.setChecked(false);
                esbTxThread.stop();
            }
        }
    }
}