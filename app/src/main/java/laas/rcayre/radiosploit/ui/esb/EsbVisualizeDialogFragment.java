package laas.rcayre.radiosploit.ui.esb;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import laas.rcayre.radiosploit.PacketItemData;
import com.example.radiosploit.R;
import laas.rcayre.radiosploit.dissectors.Dissector;
import laas.rcayre.radiosploit.dissectors.EsbDissector;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class EsbVisualizeDialogFragment extends DialogFragment {
    /* This fragment allows to visualize a packet */

    public static EsbVisualizeDialogFragment newInstance(String packetData) {
        /* Instantiate the corresponding dialog */
        EsbVisualizeDialogFragment f = new EsbVisualizeDialogFragment ();
        Bundle args = new Bundle();
        args.putString("PacketData", packetData);
        f.setArguments(args);
        return f;
    }
    private String packetContent = "";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        /*Adapt the dimensions */
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

    }

    public void updateDissectorView(ChipGroup chipGroup, EsbDissector esbDissector, LayoutInflater inflater, HorizontalScrollView scrollView, CharSequence s) {
        /* Updates the fields list using the ESB Dissector output */
        chipGroup.removeAllViews();
        esbDissector.update(s.toString());
        esbDissector.dissect();
        ArrayList<String> fields = esbDissector.getFields();
        for (int i = 0; i < fields.size(); i++) {
            Chip newChip = (Chip) inflater.inflate(R.layout.chip_field_entry, chipGroup, false);
            newChip.setText(fields.get(i));
            newChip.setCloseIconVisible(false);
            newChip.setCheckedIconVisible(false);
            newChip.setCheckable(false);
            chipGroup.addView(newChip);
            scrollView.post(new Runnable() {
                public void run() {
                    scrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                }
            });
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_visualize_packet, container, false);
        packetContent = getArguments().getString("PacketData");

        ChipGroup chipGroup = v.findViewById(R.id.packet_visualizer_chipgroup);
        TextView packetData = v.findViewById(R.id.packet_visualizer_textentry);
        HorizontalScrollView scrollView = v.findViewById(R.id.packet_visualizer_fields_scrollview);
        Button closeButton = v.findViewById(R.id.packet_visualizer_close_button);
        EsbVisualizeDialogFragment currentFragment = this;

        packetData.setText(packetContent);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentFragment.dismiss();
            }
        });

        Button addButton = v.findViewById(R.id.packet_visualizer_add_to_tx_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentText = packetData.getText().toString();
                if (currentText.length() % 2 == 0) {
                    /* Adds the packet to the TX list */
                    byte[] esbFrame = Dissector.hexToBytes(currentText);
                    String description  = EsbDissector.extractPacketTypeFromFrame(esbFrame);
                    if (description.equals("")) description = "Unknown packet";
                    EsbBus.getInstance().publish(new PacketItemData(esbFrame,0x06, description,""));
                    Toast.makeText(v.getContext(),"Packet added to TX list", Toast.LENGTH_LONG).show();
                    currentFragment.dismiss();
                }
            }
        });
        EsbDissector esbDissector = new EsbDissector(packetData.getText().toString());
        updateDissectorView(chipGroup,esbDissector, inflater, scrollView, packetData.getText());

        return v;
    }
}