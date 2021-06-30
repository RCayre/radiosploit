package laas.rcayre.radiosploit.ui.zigbee;

import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import laas.rcayre.radiosploit.PacketItemData;
import laas.rcayre.radiosploit.PacketListAdapter;

import java.util.ArrayList;

public class ZigbeeRxPacketListAdapter extends PacketListAdapter {
    /* Zigbee RX packet list adapter */
    private Fragment rxFragment;
    public ZigbeeRxPacketListAdapter(ArrayList<PacketItemData> list, Fragment rxFragment) {
        super(list);
        this.rxFragment = rxFragment;
    }

    public void onItemClick(View view, PacketItemData item, int position) {
        /* If a short click is detected, opens the packet in the Visualizer */
        FragmentTransaction ft = this.rxFragment.getParentFragmentManager().beginTransaction();
        Fragment prev = this.rxFragment.getParentFragmentManager().findFragmentByTag("ZigbeeVisualizeDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment dialogFragment = ZigbeeVisualizeDialogFragment.newInstance(item.getFormattedContent());
        dialogFragment.setTargetFragment(this.rxFragment,0);
        dialogFragment.show(ft, "ZigbeeVisualizeDialog");
    }
    public void onItemLongClick(View view,PacketItemData item, int position) {
        /* If a long click is detected, add the packet to the TX list */
        ZigbeeBus.getInstance().publish(new PacketItemData(item.getContent(),0x01, item.getDescription(),""));
        Toast.makeText(view.getContext(),"Packet added to TX list", Toast.LENGTH_LONG).show();
    }
}

