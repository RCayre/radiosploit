package laas.rcayre.radiosploit.ui.zigbee;

import android.view.View;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import laas.rcayre.radiosploit.PacketItemData;
import laas.rcayre.radiosploit.PacketListAdapter;

import java.util.ArrayList;

public class ZigbeeTxPacketListAdapter extends PacketListAdapter {
    /* Zigbee TX Packet List Adapter */
    private ZigbeeTxFragment txFragment;
    public ZigbeeTxPacketListAdapter(ArrayList<PacketItemData> list, ZigbeeTxFragment txFragment) {
        super(list);
        this.txFragment = txFragment;
    }

    public void onItemClick(View view, PacketItemData item, int position) {
        /* If there's a short click, open the packet in the editor view */
        FragmentTransaction ft = this.txFragment.getParentFragmentManager().beginTransaction();
        Fragment prev =  this.txFragment.getParentFragmentManager().findFragmentByTag("ZigbeeEditDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment dialogFragment = ZigbeeEditDialogFragment.newInstance(position,item.getFormattedContent());
        dialogFragment.setTargetFragment( this.txFragment,0);
        dialogFragment.show(ft, "ZigbeeEditDialog");

    }
    public void onItemLongClick(View view,PacketItemData item, int position) {

    }
}

