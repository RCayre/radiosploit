package laas.rcayre.radiosploit.ui.mosart;

import laas.rcayre.radiosploit.MainActivity;
import laas.rcayre.radiosploit.PacketItemData;
import laas.rcayre.radiosploit.PacketListAdapter;
import laas.rcayre.radiosploit.dissectors.Dissector;

import java.util.ArrayList;

public class MosartDevicesList {
    /* This class is a representation of a Mosart Devices List*/
    private PacketListAdapter mosartDeviceListAdapter;
    private MainActivity activity;
    ArrayList<MosartDevice> deviceList = new ArrayList<MosartDevice>();
    public MosartDevicesList(MainActivity activity, PacketListAdapter adapter) {
        this.mosartDeviceListAdapter = adapter;
        this.activity = activity;
    }
    public void clear() {
        /* This method clears the list */
        this.deviceList.clear();
        this.mosartDeviceListAdapter.resetData();
    }
    public void  addDevice(MosartDevice device) {
        /* This method allows to add a device if it is not already known */
        boolean found = false;
        for (int i=0;i<deviceList.size();i++) {
            if (deviceList.get(i).getAddress().equals(device.getAddress()) && deviceList.get(i).getChannel() == device.getChannel() && deviceList.get(i).getDescription().equals(device.getDescription())) {
                found = true;
                break;
            }
        }
        if (!found) {
            deviceList.add(device);
            /* We ignore dongle devices */
            if (!device.getDescription().equals("Dongle")) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mosartDeviceListAdapter.addNewData(new PacketItemData(Dissector.addressToBytes(device.getAddress()),0x02,device.getDescription(),"FREQ: "+String.valueOf(2400+device.getChannel())+"MHz"));
                    }
                });

            }
        }
    }
    public void addDevice(int channel, byte[] mosartFrame) {
        this.addDevice(new MosartDevice(channel,mosartFrame));
    }

}
