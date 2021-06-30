package laas.rcayre.radiosploit.ui.mosart;

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
import laas.rcayre.radiosploit.dissectors.MosartDissector;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MosartTxFragment extends Fragment {

    private static final int REQUEST_CODE = 1;
    private View root;
    private ArrayList<PacketItemData> packetList = new ArrayList<PacketItemData>();
    private PacketListAdapter adapter;
    private MosartTxThread mosartTxThread;
    private ToggleButton startTxToggleButton;

    public MosartTxFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* This method is called when an editor dialog is validated */
        if (requestCode == 0) {
            String packetData = data.getStringExtra("packetData");
            byte[] mosartFrame = Dissector.hexToBytes(packetData);
            String description = mosartFrame.length > 2 ? MosartDissector.extractPacketTypeFromFrame(Arrays.copyOfRange(mosartFrame,2,mosartFrame.length)):"Unknown packet";
            if (resultCode == -1) { // the packet was new, add it !
                adapter.addNewData(new PacketItemData(mosartFrame, 0x04, description, ""));
            } else { // the packet was already present, modify it !
                adapter.updateData(resultCode, new PacketItemData(mosartFrame, 0x04, description, ""));
            }
        }
    }

    private Observer<PacketItemData> getInputObserver() {
        /*This method is called when a packet is received from RX Fragment*/
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
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_mosart_tx, container, false);

        HciInterface hciInterface = ((MainActivity)getActivity()).getHciInterface();



        RecyclerView packetView = (RecyclerView)root.findViewById(R.id.mosart_tx_packet_view);
        adapter = new MosartTxPacketListAdapter(packetList,this);
        packetView.setHasFixedSize(true);
        packetView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        packetView.setAdapter(adapter);
        MosartBus.getInstance().listen().subscribe(getInputObserver());

        ProgressBar txProgressBar = (ProgressBar)root.findViewById(R.id.mosart_tx_progressbar);
        startTxToggleButton = (ToggleButton)root.findViewById(R.id.mosart_tx_toggle_button);

        TextView mosartTxChannelLabel = (TextView)root.findViewById(R.id.mosart_tx_channel_label);
        Slider mosartTxChannelSlider = (Slider)root.findViewById(R.id.mosart_tx_channel_slider);
        mosartTxThread = new MosartTxThread((MainActivity)getActivity(),adapter,hciInterface,txProgressBar,startTxToggleButton);

        mosartTxChannelSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                mosartTxChannelLabel.setText("CH:" + String.valueOf((int) (value)));
                if (startTxToggleButton.isChecked()) {
                    mosartTxThread.updateChannel((int)(mosartTxChannelSlider.getValue()));
                }
            }

        });

        startTxToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(startTxToggleButton.isChecked()){
                    Thread mosartThread = new Thread(mosartTxThread);
                    mosartTxThread.updateChannel((int)(mosartTxChannelSlider.getValue()));
                    mosartThread.start();
                }
                else {
                    mosartTxThread.stop();
                }
            }});
        Button resetButton = root.findViewById(R.id.mosart_tx_reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.resetData();
            }
        });


        FloatingActionButton addPacketButton = root.findViewById(R.id.mosart_add_packet_floating_button);
        addPacketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                Fragment prev = getParentFragmentManager().findFragmentByTag("MosartEditDialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment dialogFragment = MosartEditDialogFragment.newInstance(-1,"");
                dialogFragment.setTargetFragment(MosartTxFragment.this,0);
                dialogFragment.show(ft, "MosartEditDialog");
            }
        });
        return root;
    }

    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (!visible) {
            /* If the fragment is hidden, we should stop the thread */
            if (mosartTxThread != null && mosartTxThread.isRunning()) {
                startTxToggleButton.setChecked(false);
                mosartTxThread.stop();
            }
        }
    }
}