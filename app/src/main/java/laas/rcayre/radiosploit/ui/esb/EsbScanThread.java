package laas.rcayre.radiosploit.ui.esb;

import android.widget.ProgressBar;

import laas.rcayre.radiosploit.HciInterface;
import laas.rcayre.radiosploit.MainActivity;
import laas.rcayre.radiosploit.PacketItemData;
import laas.rcayre.radiosploit.PacketListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EsbScanThread implements Runnable {
    /* This is the thread allowing to scan Enhanced ShockBurst devices */
    private HciInterface hciInterface;
    private MainActivity activity;
    private ArrayList<String> candidateAddressesList;
    private EsbDevicesList devicesList;
    private ProgressBar progressBar;

    private boolean running;

    public EsbScanThread(MainActivity activity, HciInterface hciInterface, PacketListAdapter adapter, ProgressBar progressBar) {
        this.hciInterface = hciInterface;
        this.activity = activity;
        this.devicesList = new EsbDevicesList(activity,adapter);
        this.candidateAddressesList = new ArrayList<String>();
        this.progressBar = progressBar;
    }

    public EsbDevicesList getDevicesList() {
        return this.devicesList;
    }
    public boolean isRunning() {
        return running;
    }

    public static String mostCommon(List<String> list) {
        /* This method returns the most common element in a list */
        Map<String, Integer> map = new HashMap<>();

        for (String t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }

        Map.Entry<String, Integer> max = null;

        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        return max.getKey();
    }
    @Override
    public void run() {
        running = true;
        try {
            /* We explore every channel during 750 ms*/
            int channel = 2;
            this.progressBar.setMax(82);
            while (running) {
                this.hciInterface.configureEsbScan(true, channel);
                Thread.sleep(750);
                PacketItemData currentPacket;
                this.progressBar.setProgress(channel-2);
                candidateAddressesList.clear();
                do {
                    currentPacket = this.hciInterface.nextPacket();
                    if (currentPacket != null && !currentPacket.getDescription().equals("00:00:00:00:00")) {
                        candidateAddressesList.add(currentPacket.getDescription());
                    }
                } while(currentPacket != null);
                // If we received something, we wait 10 secs
                if (candidateAddressesList.size() != 0) {
                    Thread.sleep(10000);
                    do {
                        currentPacket = this.hciInterface.nextPacket();
                        if (currentPacket != null && !currentPacket.getDescription().equals("00:00:00:00:00")) {
                            candidateAddressesList.add(currentPacket.getDescription());
                        }
                    } while(currentPacket != null);

                    // We only keep the most common device because we may have false positives
                    String winner = mostCommon(candidateAddressesList);
                    devicesList.addDevice(channel,winner);


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

