package laas.rcayre.radiosploit.ui.esb;

import android.view.View;
import android.widget.Toast;

import laas.rcayre.radiosploit.PacketItemData;
import laas.rcayre.radiosploit.PacketListAdapter;

import java.util.ArrayList;

public class EsbDevicePacketListAdapter extends PacketListAdapter {
    /* Enhanced ShockBurst Device Packet List Adapter, represents a device PacketListData*/
    public EsbDevicePacketListAdapter(ArrayList<PacketItemData> list) {super(list);}
    @Override
    public void onItemClick(View view, PacketItemData itemData, int position) {

    }

    @Override
    public void onItemLongClick(View view, PacketItemData itemData, int position) {
        /* If a long click is detected on the device, automatically selects it in RX fragment */
        EsbDeviceBus.getInstance().publish(itemData);
        Toast.makeText(view.getContext(),"Address selected", Toast.LENGTH_LONG).show();
    }
}
