package laas.rcayre.radiosploit.ui.esb;

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
import laas.rcayre.radiosploit.dissectors.EsbDissector;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class EsbEditDialogFragment extends DialogFragment {
    /* This fragment allows to modify a packet */


    public static EsbEditDialogFragment newInstance(int index, String packetData) {
        /* Creates a new dialog */
        EsbEditDialogFragment f = new EsbEditDialogFragment ();
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
        /* Adapt the dialog dimensions */
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

    }

    public void updateDissectorView(ChipGroup chipGroup, EsbDissector esbDissector, LayoutInflater inflater, HorizontalScrollView scrollView, CharSequence s) {
        /* Updates the fields list, based on the Enhanced ShockBurst Dissector's output */
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
        View v = inflater.inflate(R.layout.fragment_edit_packet, container, false);

        /* Get the parameters transmitted by the fragment */
        packetContent = getArguments().getString("PacketData");
        packetIndex = getArguments().getInt("PacketIndex");

        /* Get the views */
        ChipGroup chipGroup = v.findViewById(R.id.packet_editor_chipgroup);
        TextView packetData = v.findViewById(R.id.packet_editor_textentry);
        HorizontalScrollView scrollView = v.findViewById(R.id.packet_editor_fields_scrollview);
        Button closeButton = v.findViewById(R.id.packet_editor_close_button);
        EsbEditDialogFragment currentFragment = this;

        /* Configure the packet content */
        packetData.setText(packetContent);

        /* Configure the listeners */
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
                String currentText = packetData.getText().toString();
                if (currentText.length() % 2 == 0 && currentText.length() >= 1*2) {
                    byte[] data = Dissector.hexToBytes(packetData.getText().toString()+"00");
                    int checksum = EsbDissector.calculateChecksum(data);
                    byte[] crc = {(byte)(checksum & 0xFF)};
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
        /* Configure the dissectors */
        EsbDissector esbDissector = new EsbDissector(packetData.getText().toString());
        updateDissectorView(chipGroup,esbDissector, inflater, scrollView, packetData.getText());

        packetData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /* When the text is modified, we re-run the dissection process */
                if (s.toString().length() % 2 == 0) {
                    updateDissectorView(chipGroup,esbDissector, inflater, scrollView, s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        return v;
    }
}