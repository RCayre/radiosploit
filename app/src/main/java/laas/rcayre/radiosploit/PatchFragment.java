package laas.rcayre.radiosploit;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.DialogFragment;

import com.example.radiosploit.R;

public class PatchFragment extends DialogFragment {
    /* This Fragment is used to inform the user that he should install controller patches, nothing to see here */
    public static PatchFragment newInstance(String title) {

        PatchFragment frag = new PatchFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);

        return frag;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = android.view.WindowManager.LayoutParams.MATCH_PARENT;
        params.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;

        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_patch, container, false);
        Button okButton = v.findViewById(R.id.patch_ok_button);

        // If we detect a click on the button, close the window
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return v;
    }
}