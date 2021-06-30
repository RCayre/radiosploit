package laas.rcayre.radiosploit.ui.zigbee;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.example.radiosploit.R;
import laas.rcayre.radiosploit.dissectors.Dissector;
import laas.rcayre.radiosploit.dissectors.ZigbeeDissector;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;


public class ZigbeeEditDialogFragment extends DialogFragment {
    /* This fragment allows to modify a packet */
    public static ZigbeeEditDialogFragment newInstance(int index, String packetData) {
        /* Instantiate the corresponding dialog */
        ZigbeeEditDialogFragment f = new ZigbeeEditDialogFragment ();
        Bundle args = new Bundle();
        args.putString("PacketData", packetData);
        args.putInt("PacketIndex",index);
        f.setArguments(args);
        return f;
    }
    private int packetIndex = -1;
    private String packetContent = "";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        /* Adapt the dialog's height and width */
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

    }

    public void updateDissectorView(ChipGroup chipGroup, ZigbeeDissector zigbeeDissector, LayoutInflater inflater, HorizontalScrollView scrollView, CharSequence s) {
        /* Updates the fields list using the Zigbee Dissector output */
        chipGroup.removeAllViews();
        zigbeeDissector.update(s.toString());
        zigbeeDissector.dissect();
        ArrayList<String> fields = zigbeeDissector.getFields();
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
        View v = inflater.inflate(R.layout.fragment_edit_packet, container, false);
        /* We get the parameters transmitted to the fragment */
        packetContent = getArguments().getString("PacketData");
        packetIndex = getArguments().getInt("PacketIndex");

        /* Initializes the views */
        ChipGroup chipGroup = v.findViewById(R.id.packet_editor_chipgroup);
        TextView packetData = v.findViewById(R.id.packet_editor_textentry);
        HorizontalScrollView scrollView = v.findViewById(R.id.packet_editor_fields_scrollview);
        Button closeButton = v.findViewById(R.id.packet_editor_close_button);
        ZigbeeEditDialogFragment currentFragment = this;

        /* Update the content */
        packetData.setText(packetContent);

        /* Sets the event callbacks */
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentFragment.dismiss();
            }
        });
        Button resetButton = v.findViewById(R.id.packet_editor_reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                packetData.setText("");
            }
        });
        Button fcsButton = v.findViewById(R.id.packet_editor_insert_crc_button);
        fcsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Automatically append the FCS */
                String currentText = packetData.getText().toString();
                if (currentText.length() % 2 == 0 && currentText.length() >= 4) {
                    byte[] data = Dissector.hexToBytes(packetData.getText().toString());
                    byte[] crc = ZigbeeDissector.computeCRC(Arrays.copyOfRange(data,2,data.length));
                    packetData.setText(currentText+Dissector.bytesToHex(crc));
                }
            }
        });
        Button saveButton = v.findViewById(R.id.packet_editor_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentText = packetData.getText().toString();
                if (currentText.length() % 2 == 0) {
                    Intent intent = new Intent();
                    intent.putExtra("packetData",currentText);
                    getTargetFragment().onActivityResult(getTargetRequestCode(), packetIndex, intent);
                    currentFragment.dismiss();
                }
            }
        });
        ZigbeeDissector zigbeeDissector = new ZigbeeDissector(packetData.getText().toString());
        updateDissectorView(chipGroup,zigbeeDissector, inflater, scrollView, packetData.getText());

        packetData.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.toString().length() % 2 == 0) {
                            updateDissectorView(chipGroup,zigbeeDissector, inflater, scrollView, s);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
        return v;
    }
}