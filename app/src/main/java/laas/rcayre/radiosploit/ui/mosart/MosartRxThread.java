package laas.rcayre.radiosploit.ui.mosart;

import androidx.recyclerview.widget.RecyclerView;

import laas.rcayre.radiosploit.HciInterface;
import laas.rcayre.radiosploit.MainActivity;
import laas.rcayre.radiosploit.PacketListAdapter;
import laas.rcayre.radiosploit.PacketItemData;

public class MosartRxThread implements Runnable {
    /* Mosart RX Thread, receives Mosart packets and populates the packet's list */
    private PacketListAdapter mosartPacketListAdapter;
    private HciInterface hciInterface;
    private MainActivity activity;
    private RecyclerView packetView;
    private boolean running;

    public MosartRxThread(MainActivity activity, PacketListAdapter mosartPacketListAdapter, HciInterface hciInterface,RecyclerView packetView) {
        this.mosartPacketListAdapter = mosartPacketListAdapter;
        this.hciInterface = hciInterface;
        this.activity = activity;
        this.packetView = packetView;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            PacketItemData packet = this.hciInterface.nextPacket();
            /* When we receive a packet ... */
            if (packet != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Adds it to the list
                        mosartPacketListAdapter.addNewData(packet);
                        // Scrolls to the last packet
                        packetView.scrollToPosition(mosartPacketListAdapter.getItemCount()-1);
                    }
                });


            }
        }
    }

    public void stop() {
        running = false;
    }
}
