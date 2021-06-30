package laas.rcayre.radiosploit.ui.mosart;

import laas.rcayre.radiosploit.dissectors.MosartDissector;

public class MosartDevice {
    /* This class represents a Mosart Device */
    private int channel;
    private String address;
    private String description;
    public MosartDevice(int channel, String address, String description) {
        this.channel = channel;
        this.address = address;
        this.description = description;
    }

    public MosartDevice(int channel, byte[] mosartFrame) {
        this.channel = channel;
        this.address = MosartDissector.extractAddressFromFrame(mosartFrame);
        this.description = MosartDissector.extractDeviceTypeFromFrame(mosartFrame);
    }

    public int getChannel() {
        return this.channel;
    }
    public String getDescription() {
        return this.description;
    }

    public String getAddress() {
        return this.address;
    }
}

