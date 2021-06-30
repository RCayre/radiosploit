package laas.rcayre.radiosploit.ui.mosart;

import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import laas.rcayre.radiosploit.PacketItemData;
import laas.rcayre.radiosploit.PacketListAdapter;

import java.util.ArrayList;

public class MosartRxPacketListAdapter extends PacketListAdapter {
    /* Mosart Rx Packet List adapter, allowing to react to click events */
    private Fragment rxFragment;
    public MosartRxPacketListAdapter(ArrayList<PacketItemData> list, Fragment rxFragment) {
        super(list);
        this.rxFragment = rxFragment;
    }

    public void onItemClick(View view, PacketItemData item, int position) {
        /* When a short click is detected, opens the visualizer with the corresponding packet */
        FragmentTransaction ft = this.rxFragment.getParentFragmentManager().beginTransaction();
        Fragment prev = this.rxFragment.getParentFragmentManager().findFragmentByTag("MosartVisualizeDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment dialogFragment = MosartVisualizeDialogFragment.newInstance(item.getFormattedContent());
        dialogFragment.setTargetFragment(this.rxFragment,0);
        dialogFragment.show(ft, "MosartVisualizeDialog");
    }
    public void onItemLongClick(View view,PacketItemData item, int position) {
        /* When a long click is detected, transmits the packet to the TX fragment */
        MosartBus.getInstance().publish(new PacketItemData(item.getContent(),0x04, item.getDescription(),""));
        Toast.makeText(view.getContext(),"Packet added to TX list", Toast.LENGTH_LONG).show();
    }
}

