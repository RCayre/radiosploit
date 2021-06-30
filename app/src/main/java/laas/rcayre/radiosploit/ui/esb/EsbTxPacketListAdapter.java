package laas.rcayre.radiosploit.ui.esb;

import android.view.View;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import laas.rcayre.radiosploit.PacketItemData;
import laas.rcayre.radiosploit.PacketListAdapter;

import java.util.ArrayList;

public class EsbTxPacketListAdapter extends PacketListAdapter {
    /* This is the Enhanced Shockburst packet List Adapter, allowing to react to click events*/
    private EsbTxFragment txFragment;
    public EsbTxPacketListAdapter(ArrayList<PacketItemData> list, EsbTxFragment txFragment) {
        super(list);
        this.txFragment = txFragment;
    }

    public void onItemClick(View view, PacketItemData item, int position) {
        /* Short click, opens the edit dialog with the corresponding packet */
        FragmentTransaction ft = this.txFragment.getParentFragmentManager().beginTransaction();
        Fragment prev =  this.txFragment.getParentFragmentManager().findFragmentByTag("EsbEditDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment dialogFragment = EsbEditDialogFragment.newInstance(position,item.getFormattedContent());
        dialogFragment.setTargetFragment( this.txFragment,0);
        dialogFragment.show(ft, "EsbEditDialog");

    }
    public void onItemLongClick(View view,PacketItemData item, int position) {
    /* Long click, do nothing */
    }
}

