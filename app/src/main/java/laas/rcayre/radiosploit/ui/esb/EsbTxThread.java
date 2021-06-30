package laas.rcayre.radiosploit.ui.esb;


import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import laas.rcayre.radiosploit.HciInterface;
import laas.rcayre.radiosploit.MainActivity;
import laas.rcayre.radiosploit.PacketListAdapter;
import laas.rcayre.radiosploit.dissectors.Dissector;

public class EsbTxThread implements Runnable {
    /* This class implements the Enhanced ShockBurst TX Thread, allowing to transmit packets */
    private PacketListAdapter esbPacketListAdapter;
    private HciInterface hciInterface;
    private ProgressBar txProgressBar;
    private ToggleButton txToggleButton;
    private MainActivity mainActivity;
    private TextView addressEntry;
    private int channel;

    private boolean running;

    public EsbTxThread(MainActivity mainActivity,PacketListAdapter  esbPacketListAdapter, HciInterface hciInterface, ProgressBar txProgressBar, ToggleButton txToggleButton, TextView addressEntry) {
        this.mainActivity = mainActivity;
        this.esbPacketListAdapter = esbPacketListAdapter;
        this.hciInterface = hciInterface;
        this.txProgressBar = txProgressBar;
        this.addressEntry = addressEntry;
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
        /* For every packet in the list, we transmit it, then the thread is automatically stopped */
        running = true;
        for (int i=0;i<this.esbPacketListAdapter.getItemCount();i++) {
            this.esbPacketListAdapter.updateStatus(i,"NOT SENT");
        }


        txProgressBar.setMax(this.esbPacketListAdapter.getItemCount());
        for (int i=0;i<this.esbPacketListAdapter.getItemCount() && running;i++) {
            this.hciInterface.sendEsbPacket(this.channel,Dissector.addressToBytes(addressEntry.getText().toString()),this.esbPacketListAdapter.getData(i).getContent());
            this.esbPacketListAdapter.updateStatus(i,"SENT");
            txProgressBar.setProgress(i+1);
        }
        byte[] stop = {0x00,0x00,0x00,0x00,0x00,0x00};
        byte[] nullAddress = {0x00,0x00,0x00,0x00,0x00};
        this.hciInterface.sendEsbPacket(this.channel,nullAddress,stop);
        txProgressBar.setProgress(0);
        for (int i=0;i<this.esbPacketListAdapter.getItemCount();i++) {
            this.esbPacketListAdapter.updateStatus(i,"");
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
