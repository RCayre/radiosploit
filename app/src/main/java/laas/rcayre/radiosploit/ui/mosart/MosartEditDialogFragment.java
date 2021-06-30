package laas.rcayre.radiosploit.ui.mosart;

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
import laas.rcayre.radiosploit.dissectors.MosartDissector;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;

public class MosartEditDialogFragment extends DialogFragment {
    /* This fragment allows to modify a packet */

    public static MosartEditDialogFragment newInstance(int index, String packetData) {
        /* Creates a new dialog */
        MosartEditDialogFragment f = new MosartEditDialogFragment ();
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
        /* Adapt the dimensions */
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

    }

    public void updateDissectorView(ChipGroup chipGroup, MosartDissector mosartDissector, LayoutInflater inflater, HorizontalScrollView scrollView, CharSequence s) {
        /* Updates the fields list, based on the Mosart Dissector's output */
        chipGroup.removeAllViews();
        mosartDissector.update(s.toString());
        mosartDissector.dissect();
        ArrayList<String> fields = mosartDissector.getFields();
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

        /* Initializes the Views */
        ChipGroup chipGroup = v.findViewById(R.id.packet_editor_chipgroup);
        TextView packetData = v.findViewById(R.id.packet_editor_textentry);
        HorizontalScrollView scrollView = v.findViewById(R.id.packet_editor_fields_scrollview);
        Button closeButton = v.findViewById(R.id.packet_editor_close_button);
        MosartEditDialogFragment currentFragment = this;

        /* Configure the listeners */
        packetData.setText(packetContent);
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
                if (currentText.length() % 2 == 0 && currentText.length() >= 6*2) {
                    byte[] data = Dissector.hexToBytes(packetData.getText().toString());
                    byte[] crc = MosartDissector.computeCRC(Arrays.copyOfRange(data,6,data.length));
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
        MosartDissector mosartDissector = new MosartDissector(packetData.getText().toString());
        updateDissectorView(chipGroup,mosartDissector, inflater, scrollView, packetData.getText());

        /* Apply the dissector every time the text is changed */
        packetData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() % 2 == 0) { /* We want to be sure that we have an hexadecimal string (even number of characters) */
                    updateDissectorView(chipGroup,mosartDissector, inflater, scrollView, s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        return v;
    }
}