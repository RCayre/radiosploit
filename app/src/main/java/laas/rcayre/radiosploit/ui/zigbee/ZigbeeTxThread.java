package laas.rcayre.radiosploit.ui.zigbee;


import android.widget.ProgressBar;
import android.widget.ToggleButton;

import laas.rcayre.radiosploit.HciInterface;
import laas.rcayre.radiosploit.MainActivity;
import laas.rcayre.radiosploit.PacketListAdapter;

public class ZigbeeTxThread implements Runnable {
    /* Zigbee TX thread, used to transmit Zigbee packets */
    private PacketListAdapter zigbeePacketListAdapter;
    private HciInterface hciInterface;
    private ProgressBar txProgressBar;
    private ToggleButton txToggleButton;
    private MainActivity mainActivity;
    private int channel;

    private boolean running;

    public ZigbeeTxThread(MainActivity mainActivity,PacketListAdapter  zigbeePacketListAdapter, HciInterface hciInterface, ProgressBar txProgressBar, ToggleButton txToggleButton) {
        this.mainActivity = mainActivity;
        this.zigbeePacketListAdapter = zigbeePacketListAdapter;
        this.hciInterface = hciInterface;
        this.txProgressBar = txProgressBar;
        this.txToggleButton = txToggleButton;
        this.channel = 11;
    }

    public boolean isRunning() {
        return running;
    }

    public void updateChannel(int channel) {
        this.channel = channel;
    }
    @Override
    public void run() {
        running = true;
        for (int i=0;i<this.zigbeePacketListAdapter.getItemCount();i++) {
            this.zigbeePacketListAdapter.updateStatus(i,"NOT SENT");
        }
        txProgressBar.setMax(this.zigbeePacketListAdapter.getItemCount());
        for (int i=0;i<this.zigbeePacketListAdapter.getItemCount() && running;i++) {
            this.hciInterface.sendZigbeePacket(this.channel,this.zigbeePacketListAdapter.getData(i).getContent());
            this.zigbeePacketListAdapter.updateStatus(i,"SENT");
            txProgressBar.setProgress(i+1);
        }
        byte[] stop = {};
        // We are forced to send an empty packet to avoid a double packet transmission...
        this.hciInterface.sendZigbeePacket(this.channel,stop);
        txProgressBar.setProgress(0);
        for (int i=0;i<this.zigbeePacketListAdapter.getItemCount();i++) {
            this.zigbeePacketListAdapter.updateStatus(i,"");
        }
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txToggleButton.setChecked(false);
            }
        });

        running = false;
    }

    public void stop() {
        running = false;
    }
}
