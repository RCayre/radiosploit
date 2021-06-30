package laas.rcayre.radiosploit.ui.zigbee;

import androidx.recyclerview.widget.RecyclerView;

import laas.rcayre.radiosploit.HciInterface;
import laas.rcayre.radiosploit.MainActivity;
import laas.rcayre.radiosploit.PacketListAdapter;
import laas.rcayre.radiosploit.PacketItemData;
import laas.rcayre.radiosploit.dissectors.ZigbeeDissector;

public class ZigbeeRxThread implements Runnable {
    /* Zigbee RX thread, allowing to get the packets from the HCI Interface and add it to the list */
    private PacketListAdapter zigbeePacketListAdapter;
    private HciInterface hciInterface;
    private MainActivity activity;
    private RecyclerView packetView;
    private boolean checkFCS;
    private boolean running;

    public ZigbeeRxThread(MainActivity activity, PacketListAdapter zigbeePacketListAdapter, HciInterface hciInterface,boolean checkFCS,RecyclerView packetView) {
        this.zigbeePacketListAdapter = zigbeePacketListAdapter;
        this.hciInterface = hciInterface;
        this.activity = activity;
        this.checkFCS = checkFCS;
        this.packetView = packetView;
    }

    public boolean isRunning() {
        return running;
    }

    public void updateCheckFCS(boolean checkFCS) {
        this.checkFCS = checkFCS;
    }
    @Override
    public void run() {
        running = true;
        while (running) {
            /* While the thread is running, get packets, check the FCS if needed and add it to the packet list */
            PacketItemData packet = this.hciInterface.nextPacket();

            if (packet != null && (!this.checkFCS || (this.checkFCS && ZigbeeDissector.checkCrc(packet.getContent())))) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Add the packet
                        zigbeePacketListAdapter.addNewData(packet);
                        // Scroll to the last packet
                        packetView.scrollToPosition(zigbeePacketListAdapter.getItemCount()-1);
                    }
                });


            }
        }
    }

    public void stop() {
        running = false;
    }
}
