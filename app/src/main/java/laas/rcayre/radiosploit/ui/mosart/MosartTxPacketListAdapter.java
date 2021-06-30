package laas.rcayre.radiosploit.ui.mosart;

import android.view.View;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import laas.rcayre.radiosploit.PacketItemData;
import laas.rcayre.radiosploit.PacketListAdapter;

import java.util.ArrayList;

public class MosartTxPacketListAdapter extends PacketListAdapter {
    /* This class implements Mosart Tx Packet List Adapter, allowing to react to click events */
    private MosartTxFragment txFragment;
    public MosartTxPacketListAdapter(ArrayList<PacketItemData> list, MosartTxFragment txFragment) {
        super(list);
        this.txFragment = txFragment;
    }

    public void onItemClick(View view, PacketItemData item, int position) {
        /* A short click is detected, opens an editor dialog */
        FragmentTransaction ft = this.txFragment.getParentFragmentManager().beginTransaction();
        Fragment prev =  this.txFragment.getParentFragmentManager().findFragmentByTag("MosartEditDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment dialogFragment = MosartEditDialogFragment.newInstance(position,item.getFormattedContent());
        dialogFragment.setTargetFragment( this.txFragment,0);
        dialogFragment.show(ft, "MosartEditDialog");

    }
    public void onItemLongClick(View view,PacketItemData item, int position) {

    }
}

