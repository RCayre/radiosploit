package laas.rcayre.radiosploit.dissectors;

import java.util.Arrays;

public class EsbDissector extends Dissector {
    /* This class implements an Enhanced ShockBurst dissector */
    public EsbDissector(byte[] content) {
        super(content);
    }
    public EsbDissector(String content) {
        super(content);
    }

    public static int calculateChecksum(byte[] frame) {
        /* Calculates a logitech checksum */
        int cksum = 0xFF;
        for (int i=0;i<frame.length-1;i++) {
            cksum = (cksum - frame[i]) & 0xFF;
        }
        cksum = (cksum + 1) & 0xFF;
        return cksum;
    }
    public static boolean checkChecksum(byte[] frame) {
        /* Check a logitech checksum */
        int cksum = 0xFF;
        for (int i=0;i<frame.length-1;i++) {
            cksum = (cksum - frame[i]) & 0xFF;
        }
        cksum = (cksum + 1) & 0xFF;
        return cksum == frame[frame.length-1];
    }

    public static int updateCrc(int crc, int byt, int size) {
        /* Helper method to calculate an Enhanced ShockBurst CRC*/
        crc = crc ^ ((byt & 0xFF) << 8);
        int counter = size;
        while (size-- > 0) {
            if ((crc & 0x8000) == 0x8000) {
                crc = (crc << 1) ^ 0x1021;
            }
            else crc = (crc<<1);
        }
        crc = crc & 0xFFFF;
        return crc;
    }
    public static byte[] calculateCrc(byte[] frame,int size) {
        /* Method allowing to calculate an Enhanced Shockburst CRC */
        int crc = 0xFFFF;
        for (int i=1;i<size;i++) {
            crc = updateCrc(crc,frame[i], 8);
        }
        crc = updateCrc(crc,frame[size], 1);
        byte[] rcrc = {(byte)((crc & 0xFF00) >> 8),(byte)(crc & 0xFF)};
        return rcrc;
    }
    public static byte[] buildEsbFrame(byte address[],byte[] frame) {
        /* Method allowing to build a complete frame from an address and a payload */
        byte[] data = new byte[1+5+2+frame.length+2];
        data[0] = (byte)0xAA;
        data[1] = address[0];
        data[2] = address[1];
        data[3] = address[2];
        data[4] = address[3];
        data[5] = address[4];
        data[6] = (byte)((frame.length & 0xFF) << 2);
        data[7] = (byte)((frame[0] >> 1) & 0xFF);
        for (int i=1;i<frame.length;i++) {
            data[7+i] = (byte)((((frame[i-1]  & 0xFF) << 7) | ((frame[i] & 0xFF) >> 1)) & 0xFF);
        }
        data[7+frame.length] = (byte)((frame[frame.length-1] & 0xFF) << 7);
        byte[] crc = calculateCrc(data,2+5+frame.length);
        data[7+frame.length] = (byte)(data[7+frame.length] | ((crc[0] & 0xFF) >> 1));
        data[7+frame.length+1] = (byte)(((crc[0] & 0xFF) << 7) | ((crc[1] & 0xFF) >> 1));
        data[7+frame.length+2] = (byte)(((crc[1] & 0xFF) << 7));
        // WTF ? THIS PROTOCOL IS HORRIBLE
        return data;
    }

    public static String extractPacketTypeFromFrame(byte[] frame) {
        /* Method returning the packet type of a given payload */
        String value = "";
        if (frame.length > 2) {
            if (frame[1] == (byte)0x51 && checkChecksum(frame)) {
                value = "Wake Up packet ";
            }
            else if (frame[1] == (byte)0xC2 && checkChecksum(frame)) {
                value = "Mouse packet";
            }
            else if (frame[1] == (byte)0x40 && checkChecksum(frame)) {
                value = "Keep Alive packet";
            }
            else if (frame[1] == (byte)0x4F && checkChecksum(frame)) {
                value = "Set Keep Alive packet";
            }
            else if (frame[1] == (byte)0xD3 && checkChecksum(frame)) {
                value = "Encrypted Keystroke packet";
            }
            else if (frame[1] == (byte)0xC1 && checkChecksum(frame)) {
                value = "Unencrypted Keystroke packet";
            }
            else if (frame[1] == (byte)0xC3 && checkChecksum(frame)) {
                value = "Multimedia Keystroke packet";
            }
            else if (frame[1] == (byte)0x0F && frame.length == 4 && frame[0] == (byte)0x0F && frame[2] == (byte)0x0F && frame[3] == (byte)0x0F) {
                value = "Ping Request packet";
            }
        }
        return value;
    }

    private int deviceIndex = -1;
    private int frameType = -1;
    private int deviceIndexToWakeUp = -1;
    private int unknownFieldOne = -1;
    private int unknownFieldTwo = -1;
    private byte[] unknownFieldThree  = {(byte)-1,(byte)-1,(byte)-1};
    private byte[] unused  = {(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1};

    private int x = -1;
    private int y = -1;

    private int wheelX = -1;
    private int wheelY = -1;

    private int buttonMask = -1;
    private int timeout = -1;

    private int unusedSetKeepAlive  = -1;
    private byte[] unused2SetKeepAlive = {(byte)-1,(byte)-1,(byte)-1,(byte)-1};
    private byte[] hidData = {(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1};

    private byte[] aesCounter = {(byte)-1,(byte)-1,(byte)-1,(byte)-1};
    private int unknownEncrypted = -1;
    private byte[] unusedEncrypted = {(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1};

    /* Fields Dissectors methods */
    public byte[] parseField_deviceIndex(byte[] data) {

        if (data.length >= 1) {
            deviceIndex = data[0] & 0xFF;
            fieldList.add("Device Index: "+deviceIndex);
            return Arrays.copyOfRange(data,1,data.length);
        }
        return STOP_DISSECTION;
    }

    public byte[] parseField_deviceIndexToWakeUp(byte[] data) {

        if (data.length >= 1) {
            deviceIndexToWakeUp = data[0] & 0xFF;
            fieldList.add("Device Index (to Wake Up): "+deviceIndexToWakeUp);
            return Arrays.copyOfRange(data,1,data.length);
        }
        return STOP_DISSECTION;
    }


    public byte[] parseField_unknownWakeUpFieldOne(byte[] data) {

        if (data.length >= 1) {
            unknownFieldOne = data[0] & 0xFF;
            fieldList.add("Unknown field (1): "+unknownFieldOne);
            return Arrays.copyOfRange(data,1,data.length);
        }
        return STOP_DISSECTION;
    }

    public byte[] parseField_unknownWakeUpFieldTwo(byte[] data) {

        if (data.length >= 1) {
            unknownFieldTwo = data[0] & 0xFF;
            fieldList.add("Unknown field (2): "+unknownFieldTwo);
            return Arrays.copyOfRange(data,1,data.length);
        }
        return STOP_DISSECTION;
    }
    public byte[] parseField_unknownWakeUpFieldThree(byte[] data) {

        if (data.length >= 3) {
            unknownFieldThree[0] = (byte)(data[0] & 0xFF);
            unknownFieldThree[1] = (byte)(data[1] & 0xFF);
            unknownFieldThree[2] = (byte)(data[2] & 0xFF);
            fieldList.add("Unknown field (3): 0x"+bytesToHex(unknownFieldThree));
            return Arrays.copyOfRange(data,3,data.length);
        }
        return STOP_DISSECTION;
    }

    public byte[] parseField_unusedField(byte[] data) {

        if (data.length >= 13) {
            for (int i=0;i<13;i++) {
                unused[i] = (byte) (data[i] & 0xFF);
            }
            fieldList.add("Unused: 0x"+bytesToHex(unused));
            return Arrays.copyOfRange(data,13,data.length);
        }
        return STOP_DISSECTION;
    }

    public byte[] parseField_hidData(byte[] data) {

        if (data.length >= 7) {
            for (int i=0;i<7;i++) {
                hidData[i] = (byte) (data[i] & 0xFF);
            }
            fieldList.add("HID Data: 0x"+bytesToHex(hidData));
            return Arrays.copyOfRange(data,7,data.length);
        }
        return STOP_DISSECTION;
    }

    public byte[] parseField_buttonMask(byte[] data) {
        if (data.length >= 1) {
            buttonMask = data[0] & 0xFF;
            String key = "unknown";
            if (buttonMask == (byte)0x00) {
                key = "none";
            }
            else if (buttonMask == (byte)0x01) {
                key = "left";
            }
            else if (buttonMask == (byte)0x02) {
                key = "right";
            }
            else if (buttonMask == (byte)0x04) {
                key = "center";
            }
            fieldList.add("Button Mask: "+buttonMask+"("+key+")");
            return Arrays.copyOfRange(data,1,data.length);
        }
        return STOP_DISSECTION;
    }
    public byte[] parseField_mouseMovement(byte[] data) {
        if (data.length >= 3) {
            x = (data[0] & 0xFF) | ((data[1] & 0x0F) << 8);
            if ((x & 2048) != 0) {
                x -= 4096;
            }
            y = ((data[2] & 0xFF) << 4) | (data[1] & 0xF0) >> 4;
            if ((y & 2048) != 0) {
                y -= 4096;
            }

            fieldList.add("X: "+x);
            fieldList.add("Y: "+y);
            return Arrays.copyOfRange(data,3,data.length);
        }
        return STOP_DISSECTION;
    }
    public byte[] parseField_mouseWheel(byte[] data) {
        if (data.length >= 2) {
            wheelX = data[0] & 0xFF;
            wheelY = data[1] & 0xFF;

            fieldList.add("Wheel X: "+wheelX);
            fieldList.add("Wheel Y: "+wheelY);
            return Arrays.copyOfRange(data,2,data.length);
        }
        return STOP_DISSECTION;
    }

    public byte[] parseField_unknownEncryptedKeystroke(byte[] data) {
        if (data.length >= 1) {
            unknownEncrypted = data[0] & 0xFF;

            fieldList.add("Unknown: "+unknownEncrypted);
            return Arrays.copyOfRange(data,1,data.length);
        }
        return STOP_DISSECTION;
    }

    public byte[] parseField_aesCounter(byte[] data) {
        if (data.length >= 4) {
            aesCounter[0] = (byte)(data[0] & 0xFF);
            aesCounter[1] = (byte)(data[1] & 0xFF);
            aesCounter[2] = (byte)(data[2] & 0xFF);
            aesCounter[3] = (byte)(data[3] & 0xFF);

            fieldList.add("AES Counter: 0x"+bytesToHex(aesCounter));
            return Arrays.copyOfRange(data,4,data.length);
        }
        return STOP_DISSECTION;
    }
    public byte[] parseField_timeout(byte[] data) {
        if (data.length >= 2) {
            timeout = (data[0] & 0xFF) << 8 | (data[1] & 0xFF);
            fieldList.add("Timeout: "+timeout);
            return Arrays.copyOfRange(data,2,data.length);
        }
        return STOP_DISSECTION;
    }
    public byte[] parseField_unusedSetKeepAlive(byte[] data) {
        if (data.length >= 1) {
            unusedSetKeepAlive = data[0] & 0xFF;
            fieldList.add("Unused (1): "+unusedSetKeepAlive);
            return Arrays.copyOfRange(data,1,data.length);
        }
        return STOP_DISSECTION;
    }

    public byte[] parseField_unusedEncrypted(byte[] data) {
        if (data.length >= 7) {
            unusedEncrypted[0] = (byte)(data[0] & 0xFF);
            unusedEncrypted[1] = (byte)(data[1] & 0xFF);
            unusedEncrypted[2] = (byte)(data[2] & 0xFF);
            unusedEncrypted[3] = (byte)(data[3] & 0xFF);
            unusedEncrypted[4] = (byte)(data[4] & 0xFF);
            unusedEncrypted[5] = (byte)(data[5] & 0xFF);
            unusedEncrypted[6] = (byte)(data[6] & 0xFF);
            fieldList.add("Unused : 0x"+bytesToHex(unusedEncrypted));
            return Arrays.copyOfRange(data,7,data.length);
        }
        return STOP_DISSECTION;
    }

    public byte[] parseField_unusedSetKeepAliveTwo(byte[] data) {
        if (data.length >= 4) {
            unused2SetKeepAlive[0] = (byte)(data[0] & 0xFF);
            unused2SetKeepAlive[1] = (byte)(data[1] & 0xFF);
            unused2SetKeepAlive[2] = (byte)(data[2] & 0xFF);
            unused2SetKeepAlive[3] = (byte)(data[3] & 0xFF);
            fieldList.add("Unused (2): 0x"+bytesToHex(unused2SetKeepAlive));
            return Arrays.copyOfRange(data,4,data.length);
        }
        return STOP_DISSECTION;
    }
    public byte[] parseField_frameType(byte[] data) {

        if (data.length >= 1) {
            frameType = data[0] & 0xFF;
            if (frameType == 0x51) {
                fieldList.add("Frame Type: Wake Up");
            }
            else if (frameType == 0xC2) {
                fieldList.add("Frame Type: Mouse");
            }
            else if (frameType == 0x40) {
                fieldList.add("Frame Type: Keep Alive");
            }
            else if (frameType == 0x4F) {
                fieldList.add("Frame Type: Set Keep Alive");
            }
            else if (frameType == 0xD3) {
                fieldList.add("Frame Type: Encrypted Keystroke");
            }
            else if (frameType == 0xC1) {
                fieldList.add("Frame Type: Unencrypted Keystroke");
            }
            else if (frameType == 0xC3) {
                fieldList.add("Frame Type: Multimedia Keystroke");
            }
            else {
                fieldList.add("Frame Type: Unknown");
            }
            return Arrays.copyOfRange(data,1,data.length);
        }
        return STOP_DISSECTION;
    }

    /* Payloads dissectors */
    public void dissect_wakeUp(byte[] updated) {
        updated = parseField_deviceIndexToWakeUp(updated);
        updated = parseField_unknownWakeUpFieldOne(updated);
        updated = parseField_unknownWakeUpFieldTwo(updated);
        updated = parseField_unknownWakeUpFieldThree(updated);
        updated = parseField_unusedField(updated);
    }

    public void dissect_mouse(byte[] updated) {
        updated = parseField_buttonMask(updated);
        updated = parseField_unusedSetKeepAlive(updated);
        updated = parseField_mouseMovement(updated);
        updated = parseField_mouseWheel(updated);
    }

    public void dissect_keepAlive(byte[] updated) {
        updated = parseField_timeout(updated);
    }

    public void dissect_setKeepAlive(byte[] updated) {
        updated = parseField_unusedSetKeepAlive(updated);
        updated = parseField_timeout(updated);
        updated = parseField_unusedSetKeepAliveTwo(updated);
    }

    public void dissect_unencryptedKeystroke(byte[] updated) {
        updated = parseField_hidData(updated);
    }

    public void dissect_multimediaKeystroke(byte[] updated) {
        updated = parseField_hidData(updated);
    }
    public void dissect_encryptedKeystroke(byte[] updated) {
        updated = parseField_hidData(updated);
        updated = parseField_unknownEncryptedKeystroke(updated);
        updated = parseField_aesCounter(updated);
        updated = parseField_unusedEncrypted(updated);

    }
    private byte[] checkSum = {-1};
    public void dissect() {
        if (content.length == 4 && content[0] == (byte)0x0F && content[1] == (byte)0x0F && content[2] == (byte)0x0F && content[3] == (byte)0x0F) {
            fieldList.add("Ping Request payload");
        }
        else {
            fieldList.clear();
            boolean validCrc = content.length != 0 ? checkChecksum(content) : false;
            if (validCrc) {
                checkSum = Arrays.copyOfRange(content, content.length - 1, content.length);
                content = Arrays.copyOfRange(content, 0, content.length - 1);
            }
            byte[] updated = parseField_deviceIndex(content);
            updated = parseField_frameType(updated);
            if (frameType == 0x51) {
                dissect_wakeUp(updated);
            } else if (frameType == 0xC2) {
                dissect_mouse(updated);
            } else if (frameType == 0x40) {
                dissect_keepAlive(updated);
            } else if (frameType == 0x4F) {
                dissect_setKeepAlive(updated);
            } else if (frameType == 0xD3) {
                dissect_encryptedKeystroke(updated);
            } else if (frameType == 0xC1) {
                dissect_unencryptedKeystroke(updated);
            } else if (frameType == 0xC3) {
                dissect_multimediaKeystroke(updated);
            }
            if (validCrc) {
                fieldList.add("Checksum (valid): 0x" + bytesToHex(checkSum));
            }
        }
    }
}