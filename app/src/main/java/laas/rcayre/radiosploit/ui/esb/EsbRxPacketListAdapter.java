package laas.rcayre.radiosploit.ui.esb;

import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import laas.rcayre.radiosploit.PacketItemData;
import laas.rcayre.radiosploit.PacketListAdapter;

import java.util.ArrayList;

public class EsbRxPacketListAdapter extends PacketListAdapter {
    /* This class represents the packet list adapter for Enhanced Shockburst RX packets*/
    private Fragment rxFragment;
    public EsbRxPacketListAdapter(ArrayList<PacketItemData> list, Fragment rxFragment) {
        super(list);
        this.rxFragment = rxFragment;
    }

    public void onItemClick(View view, PacketItemData item, int position) {
         /* Short click : visualize the packet using EsbVisualizeDialogFragment */
        FragmentTransaction ft = this.rxFragment.getParentFragmentManager().beginTransaction();
        Fragment prev = this.rxFragment.getParentFragmentManager().findFragmentByTag("EsbVisualizeDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment dialogFragment = EsbVisualizeDialogFragment.newInstance(item.getFormattedContent());
        dialogFragment.setTargetFragment(this.rxFragment,0);
        dialogFragment.show(ft, "EsbVisualizeDialog");
    }
    public void onItemLongClick(View view,PacketItemData item, int position) {
        /* Long click: transmit the packet to TX fragment using EsbBus */
        EsbBus.getInstance().publish(new PacketItemData(item.getContent(),0x06, item.getDescription(),""));
        Toast.makeText(view.getContext(),"Packet added to TX list", Toast.LENGTH_LONG).show();
    }
}

