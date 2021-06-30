package laas.rcayre.radiosploit.ui.mosart;

import android.view.View;
import android.widget.Toast;

import laas.rcayre.radiosploit.PacketItemData;
import laas.rcayre.radiosploit.PacketListAdapter;

import java.util.ArrayList;

public class MosartDevicePacketListAdapter extends PacketListAdapter {
    /* Mosart Device Packet List Adapter, represents a device PacketListData*/
    public MosartDevicePacketListAdapter(ArrayList<PacketItemData> list) {super(list);}
    @Override
    public void onItemClick(View view, PacketItemData itemData, int position) {
    }

    @Override
    public void onItemLongClick(View view, PacketItemData itemData, int position) {
        /* If a long click is detected on the device, automatically selects it in Keylogger / RX fragment */
        MosartDeviceBus.getInstance().publish(itemData);
        Toast.makeText(view.getContext(),"Address selected", Toast.LENGTH_LONG).show();
    }
}
