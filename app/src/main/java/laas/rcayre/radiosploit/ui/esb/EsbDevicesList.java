package laas.rcayre.radiosploit.ui.esb;

import laas.rcayre.radiosploit.MainActivity;
import laas.rcayre.radiosploit.PacketItemData;
import laas.rcayre.radiosploit.PacketListAdapter;
import laas.rcayre.radiosploit.dissectors.Dissector;

import java.util.ArrayList;

public class EsbDevicesList {
    /* This class is a representation of a Enhanced ShockBurst Devices List*/

    private PacketListAdapter esbDeviceListAdapter ;
    private MainActivity activity;
    ArrayList<EsbDevice> deviceList = new ArrayList<EsbDevice>();
    public EsbDevicesList(MainActivity activity, PacketListAdapter adapter) {
        this.esbDeviceListAdapter = adapter;
        this.activity = activity;
    }
    public void clear() {
        /* This method clears the list */
        this.deviceList.clear();
        this.esbDeviceListAdapter.resetData();
    }
    public void  addDevice(EsbDevice device) {
        /* This method allows to add a device if it is not already known */
        boolean found = false;
        for (int i=0;i<deviceList.size();i++) {
            if (deviceList.get(i).getAddress().equals(device.getAddress())) {
                found = true;
                break;
            }
        }
        if (!found) {
            deviceList.add(device);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    esbDeviceListAdapter.addNewData(new PacketItemData(Dissector.addressToBytes(device.getAddress()),0x02,device.getAddress(),"FREQ: "+String.valueOf(2400+device.getChannel())+"MHz"));
                }
            });

            }
        }
        public void addDevice(int channel,String address) {
            addDevice(new EsbDevice(channel,address));
        }
}
