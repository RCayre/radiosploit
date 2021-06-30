package laas.rcayre.radiosploit.ui.esb;

import androidx.recyclerview.widget.RecyclerView;

import laas.rcayre.radiosploit.HciInterface;
import laas.rcayre.radiosploit.MainActivity;
import laas.rcayre.radiosploit.PacketListAdapter;
import laas.rcayre.radiosploit.PacketItemData;

public class EsbRxThread implements Runnable {
    /* This class implements the Enhanced ShockBurst RX Thread, allowing to populates the RX packet list */
    private PacketListAdapter esbPacketListAdapter;
    private HciInterface hciInterface;
    private MainActivity activity;
    private RecyclerView packetView;
    private boolean running;

    public EsbRxThread(MainActivity activity, PacketListAdapter esbPacketListAdapter, HciInterface hciInterface,RecyclerView packetView) {
        this.esbPacketListAdapter = esbPacketListAdapter;
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

            if (packet != null) {
                /* if we receive a packet ...*/
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // adds it to the list
                        esbPacketListAdapter.addNewData(packet);
                        // scrolls to the end of the list
                        packetView.scrollToPosition(esbPacketListAdapter.getItemCount()-1);
                    }
                });


            }
        }
    }

    public void stop() {
        running = false;
    }
}
