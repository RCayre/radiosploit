package laas.rcayre.radiosploit.ui.esb;

import laas.rcayre.radiosploit.dissectors.Dissector;

public class EsbDevice {
    /* This class represents an Enhanced ShockBurst Device */
    private int channel;
    private String address;

    public EsbDevice(int channel, String address) {
        this.channel = channel;
        this.address = address;
    }

    public EsbDevice(int channel, byte[] address) {
        this.channel = channel;
        this.address = Dissector.bytesToAddress(address);
    }

    public int getChannel() {
        return this.channel;
    }
    public String getAddress() {
        return this.address;
    }
}

