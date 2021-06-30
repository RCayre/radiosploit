package laas.rcayre.radiosploit;

import laas.rcayre.radiosploit.dissectors.EsbDissector;
import laas.rcayre.radiosploit.dissectors.MosartDissector;
import laas.rcayre.radiosploit.dissectors.ZigbeeDissector;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import static laas.rcayre.radiosploit.dissectors.Dissector.HEX_ARRAY;
import static laas.rcayre.radiosploit.dissectors.Dissector.bytesToAddress;

public class HciInterface implements Runnable {
    /* This class allows to communicate with the Host Controller Interface. */
    LinkedList<PacketItemData> packetList = new LinkedList<PacketItemData>();


    private boolean waitingRxResponse = false;
    private boolean waitingTxResponse = false;
    private boolean waitingReadRamResponse = false;
    private byte[] readRamResponse;

    public void decode(byte[] hciEvent) {
        /* This method is called when a hciEvent is received to decode it and if it is a packet transmitted by our patches, format it and add it to the packet queue  */
        if (hciEvent.length > 1) {
            // We received a custom event, it's probably one of our patch event :)
            if (hciEvent[1] == (byte) 0xFF) {

                // We received an event indicating that the RX mode is enabled
                if (hciEvent[3] == (byte) (0x52) && hciEvent[4] == (byte) (0x58)) {
                    waitingRxResponse = false;
                // We received an event indicating that the TX operation has been performed
                }
                else if (hciEvent[3] == (byte) (0x54) && hciEvent[4] == (byte) (0x58)) {
                    waitingTxResponse = false;
                }
                // We received a Zigbee packet
                else if (hciEvent[3] == 0x01) {
                    int frequency = hciEvent[4] + 2402;
                    byte[] zigbeeFrame = Arrays.copyOfRange(hciEvent, 5, hciEvent.length);
                    String description = ZigbeeDissector.getZigbeePacketDescription(zigbeeFrame);
                    packetList.add(new PacketItemData(zigbeeFrame, 0x01, description, "FREQ:" + frequency + " MHz"));
                }
                // We received a Mosart scan packet
                else if (hciEvent[3] == 0x02) {
                    int frequency = hciEvent[4] + 2402;
                    byte[] mosartFrame = Arrays.copyOfRange(hciEvent, 5, hciEvent.length);
                    packetList.add(new PacketItemData(mosartFrame, 0x02, MosartDissector.extractDeviceTypeFromFrame(mosartFrame), "FREQ:" + frequency + " MHz"));

                }
                // We received a Mosart keylogger packet
                else if (hciEvent[3] == 0x03) {
                    int frequency = hciEvent[4] + 2402;
                    byte[] mosartFrame = Arrays.copyOfRange(hciEvent, 5, hciEvent.length);
                    packetList.add(new PacketItemData(mosartFrame, 0x03, MosartDissector.getKeyNameFromKeyCode(mosartFrame[0]), "FREQ:" + frequency + " MHz"));
                }
                // We received a Mosart normal packet
                else if (hciEvent[3] == 0x04) {
                    int frequency = hciEvent[4] + 2402;
                    byte[] mosartFrame = Arrays.copyOfRange(hciEvent, 5, hciEvent.length);

                    // Append the preamble
                    byte[] preamble = {(byte)0xF0,(byte)0xF0};
                    byte[] completeFrame = new byte[preamble.length + mosartFrame.length];
                    System.arraycopy(preamble, 0, completeFrame, 0, preamble.length);
                    System.arraycopy(mosartFrame, 0, completeFrame, preamble.length, mosartFrame.length);
                    packetList.add(new PacketItemData(completeFrame,0x04, MosartDissector.extractPacketTypeFromFrame(mosartFrame), "FREQ:"+frequency+" Mhz"));
                }
                // We received a Enhanced ShockBurst scan packet (!!! false positive !!!)
                else if (hciEvent[3] == 0x05) {
                    int frequency = hciEvent[4] + 2402;
                    byte[] esbFrame = Arrays.copyOfRange(hciEvent, 5, hciEvent.length);
                    packetList.add(new PacketItemData(esbFrame,0x05,bytesToAddress(esbFrame), "FREQ:"+frequency+" Mhz"));

                }
                // We received a Enhanced ShockBurst normal packet
                else if (hciEvent[3] == 0x06) {
                    int frequency = hciEvent[4] + 2402;
                    byte[] esbFrame = Arrays.copyOfRange(hciEvent, 5, hciEvent.length);
                    String description = EsbDissector.extractPacketTypeFromFrame(esbFrame);
                    if (!description.equals("")) {
                        packetList.add(new PacketItemData(esbFrame, 0x06, description, "FREQ:" + frequency + " Mhz"));
                    }
                }
            }
            // We received a Read Ram response
            else if (hciEvent[1] == (byte) 0x0e) {
                if (waitingReadRamResponse) {
                    waitingReadRamResponse = false;
                    readRamResponse = Arrays.copyOfRange(hciEvent,7,hciEvent.length);
                }
            }
        }
    }


    public PacketItemData nextPacket() {
        /* This method returns the next packet in the packet queue (if available), if the queue is empty it returns null */
        try {
            return packetList.removeFirst();
        }
        catch(NoSuchElementException e) {
            return null;
        }
    }

    public boolean sendCommand(byte[] command) {
        /* This method allows to send an HCI command to the controller */
        StringBuilder formattedCommand = new StringBuilder();
        formattedCommand.append("su -c echo -n \"");
        for (int i=0;i<command.length;i++) {
            formattedCommand.append("\\x");
            int v = command[i] & 0xFF;
            formattedCommand.append(HEX_ARRAY[v >>> 4]);
            formattedCommand.append(HEX_ARRAY[v & 0x0F]);
        }
        formattedCommand.append("\" >> /dev/ttySAC1");


        try {
            Process txProcess = Runtime.getRuntime().exec(formattedCommand.toString());
            int returnCode = txProcess.waitFor();
            if (255 != returnCode)
            {
                // We enabled a RX mode, we are waiting for an event indicating that the command succeeded
                if (command[1] == (byte)0x60 && command[2] == (byte)0x20) {
                    waitingRxResponse = true;
                }
                // We performed a TX operation, we are waiting for an event indicating that the command succeeded
                else if (command[1] == (byte)0x61 && command[2] == (byte)0x20) {
                    waitingTxResponse = true;
                    while (waitingTxResponse) {
                        txProcess = Runtime.getRuntime().exec(formattedCommand.toString()); // try again while it is empty ...
                    }
                }
                // We sended a ReadRam command, we are waiting for an event including the data
                else if (command[1] == (byte)0x4D && command[2] == (byte)0xFC) {
                    waitingReadRamResponse = true;
                    while (waitingReadRamResponse) {
                        txProcess = Runtime.getRuntime().exec(formattedCommand.toString());
                    }
                }
                return true;
            }
            else
            {
                return false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public byte[] readRam(int address,int size) {
        /* This method transmit a ReadRam command (vendor specific command allowing to read data in controller's RAM) */
        byte finalSize = (byte)(0xFF & size);
        byte[] command = {0x01,(byte)0x4d,(byte)0xfc,(byte)0x05,(byte)(address & 0xFF),(byte)((address & 0xFF00) >> 8),(byte)((address & 0xFF0000) >> 16),(byte)((address & 0xFF000000) >> 24),finalSize};
        waitingReadRamResponse = true;
        sendCommand(command);
        return readRamResponse;
    }

    public void configureZigbeeRx(boolean enable,int channel) {
        /* This method allows to enable Zigbee RX mode */
        byte formattedChannel = (byte)(channel & 0xFF);
        byte[] command = {0x01,0x60,0x20,0x02,(byte)(enable ? 0x01 : 0x00),formattedChannel};
        sendCommand(command);
    }
    public void configureMosartScan(boolean enable,int channel, boolean allowDongleFrames) {
        /* This method allows to enable Mosart Scan mode */
        byte formattedChannel = (byte)(channel & 0xFF);
        byte[] command = {0x01,0x60,0x20,0x03,(byte)(enable ? 0x02 : 0x00),formattedChannel,(byte)(allowDongleFrames ? 0x01 : 0x00)};
        sendCommand(command);
    }

    public void configureMosartKeylogger(boolean enable,int channel, byte[] address) {
        /* This method allows to enable Mosart Keylogger mode */
        byte formattedChannel = (byte)(channel & 0xFF);
        byte[] command = {0x01,0x60,0x20,0x06,(byte)(enable ? 0x03 : 0x00),formattedChannel,address[0],address[1],address[2],address[3]};
        sendCommand(command);
    }
    public void configureMosartRx(boolean enable,int channel, byte[] address) {
        /* This method allows to enable Mosart RX mode */
        byte formattedChannel = (byte)(channel & 0xFF);
        byte[] command = {0x01,0x60,0x20,0x06,(byte)(enable ? 0x04 : 0x00),formattedChannel,address[0],address[1],address[2],address[3]};
        sendCommand(command);
    }

    public void configureEsbScan(boolean enable,int channel) {
        /* This method allows to configure Enhanced ShockBurst Scan mode */
        byte formattedChannel = (byte)(channel & 0xFF);
        byte[] pattern = {0x00,0x00,0x00};
        byte[] command = {0x01,0x60,0x20,0x02,(byte)(enable ? 0x05 : 0x00),formattedChannel};
        sendCommand(command);
    }
    public void configureEsbRx(boolean enable,int channel, byte[] address) {
        /* This method allows to enable Enhanced ShockBurst RX mode */
        byte formattedChannel = (byte)(channel & 0xFF);
        byte[] command = {0x01,0x60,0x20,0x07,(byte)(enable ? 0x06 : 0x00),formattedChannel,address[0],address[1],address[2],address[3],address[4]};
        sendCommand(command);
    }

    public void sendZigbeePacket(int channel, byte[] packet) {
        /* This method allows to transmit a Zigbee packet */
        byte formattedChannel = (byte)(channel & 0xFF);
        byte[] command = new byte[7+packet.length];
        command[0] = 0x01;
        command[1] = 0x61;
        command[2] = 0x20;
        command[3] = (byte)((3+packet.length) & 0xFF);
        command[4] = 0x01; // Zigbee mode
        command[5] = formattedChannel;
        command[6] = (byte)(packet.length);
        for (int i=0;i<packet.length;i++) {
            command[7+i] = packet[i];
        }
        sendCommand(command);
    }

    public void sendMosartPacket(int channel, byte[] packet) {
        /* This method allows to transmit a Mosart packet */
        byte formattedChannel = (byte)(channel & 0xFF);
        byte[] command = new byte[7+packet.length];
        command[0] = 0x01;
        command[1] = 0x61;
        command[2] = 0x20;
        command[3] = (byte)((3+packet.length) & 0xFF);
        command[4] = 0x02; // Mosart mode
        command[5] = formattedChannel;
        command[6] = (byte)(packet.length);
        for (int i=0;i<packet.length;i++) {
            command[7+i] = packet[i];
        }
        sendCommand(command);

    }


    public void sendEsbPacket(int channel,byte[] address, byte[] payload) {
        /* This method allows to transmit a Enhanced ShockBurst packet */
        byte formattedChannel = (byte)(channel & 0xFF);
        byte[] packet = EsbDissector.buildEsbFrame(address,payload);
        byte[] command = new byte[7+packet.length];
        command[0] = 0x01;
        command[1] = 0x61;
        command[2] = 0x20;
        command[3] = (byte)((3+packet.length) & 0xFF);
        command[4] = 0x06; // ESB mode
        command[5] = formattedChannel;
        command[6] = (byte)(packet.length);
        for (int i=0;i<packet.length;i++) {
            command[7+i] = packet[i];
        }
        sendCommand(command);

    }
    @Override
    public void run() {
        /* This method parses the btsnoop_hci.log in real time to extract HCI event */
        try {
            boolean running = true;

            Process rxProcess = Runtime.getRuntime().exec("su -c tail -f -c +$(stat -c %s /data/log/bt/btsnoop_hci.log) /data/log/bt/btsnoop_hci.log");
            DataInputStream os = new DataInputStream(rxProcess.getInputStream());
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            int bytesSkippedAfterSync = 13;

            int frameLength =  0;
            boolean inFrame = false;

            while (running) {
                output.write(os.readByte());
                if (!inFrame) {
                    if (output.size() == 8) {
                        byte[] syncPattern = output.toByteArray();
                        int originalLength = (syncPattern[0] | (syncPattern[1] << 8) | (syncPattern[2] << 16) | (syncPattern[3] << 24));
                        int includedLength = (syncPattern[4] | (syncPattern[5] << 8) | (syncPattern[6] << 16) | (syncPattern[7] << 24));

                        if (originalLength == includedLength && originalLength > 0 && originalLength < 255) {
                            inFrame = true;
                            frameLength = originalLength;
                            output.reset();
                        }
                        else {
                            output.reset();
                            for (int i = 1; i <= 7; i++) output.write(syncPattern[i]);
                        }
                    }
                }
                else {
                    if (output.size() >= bytesSkippedAfterSync+frameLength || output.size() > 255)  {
                        byte[] finalFrame = new byte[frameLength];
                        for (int i=0;i<frameLength;i++) finalFrame[i] = output.toByteArray()[i+13];
                        decode(finalFrame);
                        output.reset();
                        inFrame = false;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
