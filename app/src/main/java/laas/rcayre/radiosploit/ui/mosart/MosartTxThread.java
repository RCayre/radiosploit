package laas.rcayre.radiosploit.ui.mosart;


import android.widget.ProgressBar;
import android.widget.ToggleButton;

import laas.rcayre.radiosploit.HciInterface;
import laas.rcayre.radiosploit.MainActivity;
import laas.rcayre.radiosploit.PacketListAdapter;

public class MosartTxThread implements Runnable {
    /* This class implements the Mosart TX Thread*/
    private PacketListAdapter mosartPacketListAdapter;
    private HciInterface hciInterface;
    private ProgressBar txProgressBar;
    private ToggleButton txToggleButton;
    private MainActivity mainActivity;
    private int channel;

    private boolean running;

    public MosartTxThread(MainActivity mainActivity,PacketListAdapter  mosartPacketListAdapter, HciInterface hciInterface, ProgressBar txProgressBar, ToggleButton txToggleButton) {
        this.mainActivity = mainActivity;
        this.mosartPacketListAdapter = mosartPacketListAdapter;
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
        /* Transmits every packets present in the list, then stops the thread */
        running = true;
        for (int i=0;i<this.mosartPacketListAdapter.getItemCount();i++) {
            this.mosartPacketListAdapter.updateStatus(i,"NOT SENT");
        }
        txProgressBar.setMax(this.mosartPacketListAdapter.getItemCount());
        for (int i=0;i<this.mosartPacketListAdapter.getItemCount() && running;i++) {
            this.hciInterface.sendMosartPacket(this.channel,this.mosartPacketListAdapter.getData(i).getContent());
            this.mosartPacketListAdapter.updateStatus(i,"SENT");
            txProgressBar.setProgress(i+1);
        }
        byte[] stop = {};
        // We are forced to send this ugly command to avoid last packet double transmission
        this.hciInterface.sendMosartPacket(this.channel,stop);
        txProgressBar.setProgress(0);
        for (int i=0;i<this.mosartPacketListAdapter.getItemCount();i++) {
            this.mosartPacketListAdapter.updateStatus(i,"");
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
