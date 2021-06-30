package laas.rcayre.radiosploit.ui.mosart;

import android.widget.ProgressBar;

import laas.rcayre.radiosploit.HciInterface;
import laas.rcayre.radiosploit.MainActivity;
import laas.rcayre.radiosploit.PacketItemData;
import laas.rcayre.radiosploit.PacketListAdapter;
import laas.rcayre.radiosploit.dissectors.MosartDissector;

public class MosartScanThread implements Runnable {
    /* This class implements the Mosart Scan thread */
    private HciInterface hciInterface;
    private MainActivity activity;
    private MosartDevicesList devicesList;
    private ProgressBar progressBar;

    private boolean running;

    public MosartScanThread(MainActivity activity, HciInterface hciInterface, PacketListAdapter adapter, ProgressBar progressBar) {
        this.hciInterface = hciInterface;
        this.activity = activity;
        this.devicesList = new MosartDevicesList(activity, adapter);
        this.progressBar = progressBar;
    }

    public boolean isRunning() {
        return running;
    }

    public MosartDevicesList getDevicesList() {
        return this.devicesList;
    }
    @Override
    public void run() {
        running = true;
        try {
            // Ok, ugly algorithm incoming.

            int channel = 2;
            this.progressBar.setMax(82);
            while (running) {
                // We explore every channel during 250ms, and we allow dongle frames (they are frequently transmitted)
                this.hciInterface.configureMosartScan(true, channel, true);
                Thread.sleep(250);
                PacketItemData currentPacket;
                this.progressBar.setProgress(channel-2);
                boolean dongleFound = false;
                do {
                    currentPacket = this.hciInterface.nextPacket();
                    if (currentPacket != null && MosartDissector.extractDeviceTypeFromFrame(currentPacket.getContent()).equals("Dongle")) {
                        dongleFound = true;
                        devicesList.addDevice(channel,currentPacket.getContent());
                    }
                } while(currentPacket != null);

                // If we found dongle frames, it's probably an used channel :
                if (dongleFound) {
                    // We stay on the same channel during 5 more seconds, but we ignore dongle frames
                    this.hciInterface.configureMosartScan(true, channel, false);
                    Thread.sleep(5000);
                    do {
                        currentPacket = this.hciInterface.nextPacket();
                        if (currentPacket != null) {
                            // If we found other devices here, we add them to the list
                            devicesList.addDevice(channel,currentPacket.getContent());
                        }
                    } while(currentPacket != null);

                }

                if (channel < 80) channel++;
                else channel = 2;
            }
        }
        catch (InterruptedException e) {
            running = false;
        }
    }

    public void stop() {
        running = false;
    }
}
