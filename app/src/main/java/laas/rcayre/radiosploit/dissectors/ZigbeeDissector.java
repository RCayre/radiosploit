package laas.rcayre.radiosploit.dissectors;

import java.util.Arrays;

public class ZigbeeDissector extends Dissector {
    /* This class implements a Zigbee Dissectorf */
    public ZigbeeDissector(byte[] content) {
        super(content);
    }
    public ZigbeeDissector(String content) {
        super(content);
    }

    private int startOfFrameDelimiter = -1;
    private int packetLength = -1;
    private int fcfReserved1 = -1;
    private int panIdCompress = -1;
    private int ackRequest = -1;
    private int framePending = -1;
    private int securityEnabled = -1;
    private int frameType = -1;
    private int srcAddrMode = -1;
    private int frameVersion = -1;
    private int destAddrMode = -1;
    private int fcfReserved2 = -1;
    private int sequenceNumber = -1;
    private byte[] srcPanId = {(byte)-1,(byte)-1};
    private byte[] destPanId = {(byte)-1,(byte)-1};

    private byte[] shortSrcAddr = {(byte)-1,(byte)-1};
    private byte[] longSrcAddr = {(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1};


    private byte[] shortDestAddr = {(byte)-1,(byte)-1};
    private byte[] longDestAddr = {(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1};

    private int gtsSpecBit = -1;
    private int gtsSpecReserved = -1;
    private int gtsSpecDescCount = -1;
    private int gtsDirectionMask = -1;

    private int securityControlReserved = -1;
    private int securityControlKeyIdentifierMode = -1;
    private int securityControlSecurityLevel = -1;
    private byte[] securityFrameCounter = {(byte)-1,(byte)-1,(byte)-1,(byte)-1};
    private int keyIndex = -1;
    private byte[] shortKeySource = {(byte)-1,(byte)-1,(byte)-1,(byte)-1};
    private byte[] longKeySource = {(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1};

    private int superFrameOrder = -1;
    private int beaconOrder = -1;
    private int associationPermitted = -1;
    private int panCoordinator = -1;
    private int superFrameReserved = -1;
    private int batteryLifeExtend = -1;
    private int finalCapSlot = -1;

    private byte[] shortAddress = {(byte)-1,(byte)-1};
    private byte[] longAddress = {(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1,(byte)-1};

    private int longAddressesCount = -1;
    private int shortAddressesCount = -1;

    public static String getZigbeePacketDescription(byte[] data) {
        /* This method returns the description of the provided packet */
        String description = "Unknown packet";
        if (data.length > 3) {
            if ((data[2] & 0x03) == (byte)0x00) {
                description = "Beacon packet";
            }
            else if ((data[2] & 0x03) == (byte)0x01) {
                description = "Data packet";
            }
            else if ((data[2] & 0x03) == (byte)0x02) {
                description = "Acknowledgment packet";
            }
            else if ((data[2] & 0x03) == (byte)0x03) {
                description = "Command packet";
            }
        }
        return description;
    }
    public static boolean checkCrc(byte[] data) {
        /* This method checks if the frame contains a valid FCS */
        if (data.length > 4) {
            byte[] calculatedCrc = computeCRC(Arrays.copyOfRange(data, 2, data.length - 2));
            if (calculatedCrc[0] == data[data.length - 2] && calculatedCrc[1] == data[data.length - 1]) {
                return true;
            }
        }
        return false;
    }

    public static byte[] computeCRC(byte[] data) {
        /* This method calculates and returns the FCS of the provided frame */
        int crc = 0;
        for (byte c : data) {
            int q = (crc ^ c) & 0x0F;
            crc = (crc >> 4) ^ (q * 0x1081);
            q = (crc ^ (c >> 4)) & 0x0F;
            crc = (crc >> 4) ^ (q * 0x1081);
        }

        crc &= 0xffff;
        byte[] crcBytes = {(byte)(crc & 0xFF),(byte)((crc & 0xFF00) >> 8)};
        return crcBytes;
    }

    /* Fields dissectors */
    public byte[] parseField_startOfFrameDelimiter(byte[] data) {
        startOfFrameDelimiter = -1;
        if (data.length >= 1 && data[0] == (byte)0xA7) {
            startOfFrameDelimiter = 0xA7;
            fieldList.add("SFD: 0xA7");
            return Arrays.copyOfRange(data,1,data.length);
        }
        return STOP_DISSECTION;
    }

    public byte[] parseField_packetLength(byte[] data) {
        packetLength = -1;
        if (data.length >= 1) {
            packetLength = data[0] & 0xFF;
            fieldList.add("Length: "+String.valueOf(packetLength & 0xFF));
            return Arrays.copyOfRange(data,1,data.length);
        }
        return STOP_DISSECTION;
    }

    public byte[] parseField_frameControlField(byte[] data) {
        fcfReserved1 = -1;
        panIdCompress = -1;
        ackRequest = -1;
        framePending = -1;
        securityEnabled = -1;
        frameType = -1;
        srcAddrMode = -1;
        frameVersion = -1;
        destAddrMode = -1;
        fcfReserved2 = -1;
        if (data.length >= 1) {
            fcfReserved1 = ((data[0] & 0x80) >> 7);
            fieldList.add("FCF - Reserved(1): "+String.valueOf(fcfReserved1 & 0xFF));

            panIdCompress = ((data[0] & 0x40) >> 6);
            fieldList.add("FCF - PanID Compression: "+(panIdCompress == 0 ? "false" : "true"));

            ackRequest = ((data[0] & 0x20) >> 5);
            fieldList.add("FCF - ACK Request: "+(ackRequest == 0 ? "false" : "true"));

            framePending = ((data[0] & 0x10) >> 4);
            fieldList.add("FCF - Frame Pending: "+(framePending == 0 ? "false" : "true"));

            securityEnabled = ((data[0] & 0x8) >> 3);
            fieldList.add("FCF - Security Enabled: "+(securityEnabled == 0 ? "false" : "true"));

            frameType = ((data[0] & 0x7));
            if (frameType == 0) {
                fieldList.add("FCF - Frame Type: Beacon");
            }
            else if (frameType == 1) {
                fieldList.add("FCF - Frame Type: Data");
            }
            else if (frameType == 2) {
                fieldList.add("FCF - Frame Type: Ack");
            }
            else if (frameType == 3) {
                fieldList.add("FCF - Frame Type: Command");
            }
            else {
                fieldList.add("FCF - Frame Type: Unknown");
            }
        }
        if (data.length >= 2) {

            srcAddrMode = ((data[1] & 0xc0)>> 6);
            if (srcAddrMode == 0) {
                fieldList.add("FCF - Source Address Mode: None");
            }
            else if (srcAddrMode == 1) {
                fieldList.add("FCF - Source Address Mode: Reserved");
            }
            else if (srcAddrMode == 2) {
                fieldList.add("FCF - Source Address Mode: Short");
            }
            else if (srcAddrMode == 3) {
                fieldList.add("FCF - Source Address Mode: Long");
            }
            else {
                fieldList.add("FCF - Source Address Mode: Unknown");
            }

            frameVersion = ((data[1] & 0x30)>> 4);
            if (frameVersion == 0) {
                fieldList.add("FCF - Frame Version: 2003 compatibility");
            }
            else if (frameVersion == 1) {
                fieldList.add("FCF - Frame Version: 2006 compatibility");
            }
            else {
                fieldList.add("FCF - Frame Version: Unknown");
            }

            destAddrMode = ((data[1] & 0x0c)>> 2);
            if (destAddrMode == 0) {
                fieldList.add("FCF - Destination Address Mode: None");
            }
            else if (destAddrMode == 1) {
                fieldList.add("FCF - Destination Address Mode: Reserved");
            }
            else if (destAddrMode == 2) {
                fieldList.add("FCF - Destination Address Mode: Short");
            }
            else if (destAddrMode == 3) {
                fieldList.add("FCF - Destination Address Mode: Long");
            }
            else {
                fieldList.add("FCF - Destination Address Mode: Unknown");
            }

            fcfReserved2 = ((data[1] & 0x3));
            fieldList.add("FCF - Reserved(2): "+String.valueOf(fcfReserved2 & 0xFF));
            return Arrays.copyOfRange(data,2,data.length);
        }
        return STOP_DISSECTION;
    }
    public byte[] parseField_sequenceNumber(byte[] data) {
        sequenceNumber = -1;
        if (data.length >= 1) {
            sequenceNumber = data[0] & 0xFF;
            fieldList.add("Sequence Number: "+String.valueOf(sequenceNumber & 0xFF));
            return Arrays.copyOfRange(data,1,data.length);
        }
        return STOP_DISSECTION;
    }
    public byte[] parseField_srcPanId(byte[] data) {
        srcPanId[0] = -1;
        srcPanId[1] = -1;
        if (data.length >= 2) {
            srcPanId[0] = data[1];
            srcPanId[1] =data[0];
            fieldList.add("Source Pan ID: 0x"+bytesToHex(srcPanId));
            return Arrays.copyOfRange(data,2,data.length);
        }
        return STOP_DISSECTION;
    }
    public byte[] parseField_destPanId(byte[] data) {
        destPanId[0] = -1;
        destPanId[1] = -1;
        if (data.length >= 2) {
            destPanId[0] = data[1];
            destPanId[1] = data[0];
            fieldList.add("Destination Pan ID: 0x"+bytesToHex(destPanId));
            return Arrays.copyOfRange(data,2,data.length);
        }
        return STOP_DISSECTION;
    }

    public byte[] parseField_srcAddr(byte[] data) {
        for (int i=0;i<shortSrcAddr.length;i++) shortSrcAddr[i] = -1;
        for (int i=0;i<longSrcAddr.length;i++) longSrcAddr[i] = -1;

        if (srcAddrMode == 2 && data.length >= 2) {
            shortSrcAddr[0] = data[1];
            shortSrcAddr[1] =data[0];
            fieldList.add("Source Address: 0x"+bytesToHex(shortSrcAddr));
            return Arrays.copyOfRange(data,2,data.length);
        }
        else if (srcAddrMode == 3 && data.length >= 8) {
            longSrcAddr[0] = data[7];
            longSrcAddr[1] = data[6];
            longSrcAddr[2] = data[5];
            longSrcAddr[3] = data[4];
            longSrcAddr[4] = data[3];
            longSrcAddr[5] = data[2];
            longSrcAddr[6] = data[1];
            longSrcAddr[7] = data[0];
            fieldList.add("Destination Address: "+bytesToAddress(longSrcAddr));
            return Arrays.copyOfRange(data,8,data.length);
        }
        return STOP_DISSECTION;
    }
    public byte[] parseField_destAddr(byte[] data) {
        for (int i=0;i<shortDestAddr.length;i++) shortSrcAddr[i] = -1;
        for (int i=0;i<longDestAddr.length;i++) longDestAddr[i] = -1;

        if (destAddrMode == 2 && data.length >= 2) {
            shortDestAddr[0] = data[1];
            shortDestAddr[1] =data[0];
            fieldList.add("Destination Address: 0x"+bytesToHex(shortDestAddr));
            return Arrays.copyOfRange(data,2,data.length);
        }
        else if (destAddrMode == 3 && data.length >= 8) {
            longDestAddr[0] = data[7];
            longDestAddr[1] = data[6];
            longDestAddr[2] = data[5];
            longDestAddr[3] = data[4];
            longDestAddr[4] = data[3];
            longDestAddr[5] = data[2];
            longDestAddr[6] = data[1];
            longDestAddr[7] = data[0];
            fieldList.add("Destination Address: "+bytesToAddress(longDestAddr));
            return Arrays.copyOfRange(data,8,data.length);
        }
        return STOP_DISSECTION;
    }
    public byte[] parseField_auxSecurityHeader(byte[] data) {
        securityControlReserved = -1;
        securityControlKeyIdentifierMode = -1;
        securityControlSecurityLevel = -1;
        for (int i=0;i<securityFrameCounter.length;i++) securityFrameCounter[i] = -1;
        keyIndex = -1;
        for (int i=0;i<shortKeySource.length;i++) shortKeySource[i] = -1;
        for (int i=0;i<longKeySource.length;i++) longKeySource[i] = -1;

        if (data.length >= 1) {
            securityControlReserved = ((data[0] & 0xE0) >> 5);
            fieldList.add("Security Control - Reserved: "+String.valueOf(securityControlReserved & 0xFF));

            securityControlKeyIdentifierMode = ((data[0] & 0x18) >> 3);
            if (securityControlKeyIdentifierMode == 0) {
                fieldList.add("Security Control - Key Identifier Mode: Implicit");
            }
            else if (securityControlKeyIdentifierMode == 1) {
                fieldList.add("Security Control - Key Identifier Mode: 1oKeyIndex");
            }
            else if (securityControlKeyIdentifierMode == 2) {
                fieldList.add("Security Control - Key Identifier Mode: 4o-KeySource-1oKeyIndex");
            }
            else if (securityControlKeyIdentifierMode == 3) {
                fieldList.add("Security Control - Key Identifier Mode: 8o-KeySource-1oKeyIndex");
            }
            else {
                fieldList.add("Security Control - Key Identifier Mode: Unknown");
            }
            //0: "None", 1: "MIC-32", 2: "MIC-64", 3: "MIC-128", 4: "ENC", 5: "ENC-MIC-32", 6: "ENC-MIC-64", 7: "ENC-MIC-128"
            securityControlSecurityLevel = (data[0] & 0x07);
            if (securityControlSecurityLevel == 0) {
                fieldList.add("Security Control - Security Level: None");
            }
            else if (securityControlSecurityLevel == 1) {
                fieldList.add("Security Control - Security Level: MIC-32");
            }
            else if (securityControlSecurityLevel == 2) {
                fieldList.add("Security Control - Security Level: MIC-64");
            }
            else if (securityControlSecurityLevel == 3) {
                fieldList.add("Security Control - Security Level: MIC-128");
            }
            else if (securityControlSecurityLevel == 4) {
                fieldList.add("Security Control - Security Level: ENC");
            }
            else if (securityControlSecurityLevel == 5) {
                fieldList.add("Security Control - Security Level: ENC-MIC-32");
            }
            else if (securityControlSecurityLevel == 6) {
                fieldList.add("Security Control - Security Level: ENC-MIC-64");
            }
            else if (securityControlSecurityLevel == 7) {
                fieldList.add("Security Control - Security Level: ENC-MIC-128");
            }
            else {
                fieldList.add("Security Control - Security Level: Unknown");
            }
        }
        if (data.length >= 5) {
            securityFrameCounter[0] = data[4];
            securityFrameCounter[1] = data[3];
            securityFrameCounter[2] = data[2];
            securityFrameCounter[3] = data[1];
            fieldList.add("Security Frame Counter: 0x"+bytesToHex(securityFrameCounter));
            if (securityControlKeyIdentifierMode == 0) {
                return Arrays.copyOfRange(data,5,data.length);
            }
            else if (securityControlKeyIdentifierMode == 1 && data.length >= 6){
                keyIndex = data[5] & 0xFF;
                fieldList.add("Security Key Index: "+String.valueOf(keyIndex & 0xFF));
                return Arrays.copyOfRange(data,6,data.length);
            }
            else if (securityControlKeyIdentifierMode == 2 && data.length >= 10){
                shortKeySource[0] = data[8];
                shortKeySource[1] = data[7];
                shortKeySource[2] = data[6];
                shortKeySource[3] = data[5];
                fieldList.add("Security Key Source: "+bytesToAddress(shortKeySource));
                keyIndex = data[9] & 0xFF;
                fieldList.add("Security Key Index: "+String.valueOf(keyIndex & 0xFF));
                return Arrays.copyOfRange(data,10,data.length);
            }
            else if (securityControlKeyIdentifierMode == 3 && data.length >= 14){
                longKeySource[0] = data[12];
                longKeySource[1] = data[11];
                longKeySource[2] = data[10];
                longKeySource[3] = data[9];
                longKeySource[4] = data[8];
                longKeySource[5] = data[7];
                longKeySource[6] = data[6];
                longKeySource[7] = data[5];
                fieldList.add("Security Key Source: "+bytesToAddress(longKeySource));
                keyIndex = data[13] & 0xFF;
                fieldList.add("Security Key Index: "+String.valueOf(keyIndex & 0xFF));
                return Arrays.copyOfRange(data,14,data.length);
            }
        }

        return STOP_DISSECTION;
    }


    public byte[] parseField_superFrameSpec(byte[] data) {
        superFrameOrder = -1;
        beaconOrder = -1;
        associationPermitted = -1;
        panCoordinator = -1;
        superFrameReserved = -1;
        batteryLifeExtend = -1;
        finalCapSlot = -1;


        if (data.length >= 1) {
            superFrameOrder = (data[0] & 0xF0) >> 4;
            fieldList.add("SuperFrame - SuperFrame Interval: "+String.valueOf(superFrameOrder));
            beaconOrder = (data[0] & 0x0F);
            fieldList.add("SuperFrame - Beacon Interval: "+String.valueOf(beaconOrder));
        }
        if (data.length >= 2) {
            associationPermitted = ((data[1] & 0x80) >> 7);
            fieldList.add("SuperFrame - Association Permitted: "+(associationPermitted == 0 ? "false" : "true"));
            panCoordinator = ((data[1] & 0x40) >> 6);
            fieldList.add("SuperFrame - PAN Coordinator: "+(panCoordinator == 0 ? "false" : "true"));
            superFrameReserved = ((data[1] & 0x20) >> 5);
            fieldList.add("SuperFrame - Reserved: "+String.valueOf(superFrameReserved));
            batteryLifeExtend = ((data[1] & 0x10) >> 4);
            fieldList.add("SuperFrame - Battery Extension: "+(batteryLifeExtend == 0 ? "false" : "true"));
            finalCapSlot = (data[1] & 0x0F);
            fieldList.add("SuperFrame - Final Cap. Slot: "+String.valueOf(finalCapSlot));
            return Arrays.copyOfRange(data,2,data.length);
        }
        return STOP_DISSECTION;
    }
    public byte[] parseField_GTS(byte[] data) {
        gtsSpecReserved = -1;
        gtsSpecBit = -1;
        gtsSpecDescCount = -1;
        gtsDirectionMask = -1;

        if (data.length >= 1) {
            gtsSpecBit = ((data[0] & 0x80) >> 7);
            fieldList.add("GTS - Spec. bit: "+(gtsSpecBit == 0 ? "false" : "true"));
            gtsSpecReserved = ((data[0] & 0x78) >> 3);
            fieldList.add("GTS - Reserved: "+String.valueOf(gtsSpecReserved & 0xFF));
            gtsSpecDescCount = (data[0] & 0x07);
            fieldList.add("GTS - Count: "+String.valueOf(gtsSpecDescCount & 0xFF));
            if (gtsSpecDescCount == 0) {
                return Arrays.copyOfRange(data,1,data.length);
            }
            else if (data.length >= 2) {
                gtsDirectionMask = data[1] & 0x7F;
                fieldList.add("GTS - Direction Mask: "+String.valueOf(gtsDirectionMask & 0xFF));
                return Arrays.copyOfRange(data,2,data.length);
            }
        }
        return STOP_DISSECTION;
    }
    public byte[] parseField_pendingAddresses(byte[] data) {
        longAddressesCount = -1;
        shortAddressesCount = -1;
        if (data.length >= 1) {
            longAddressesCount=0;
            shortAddressesCount=0;
            longAddressesCount = (data[0] & 0x70) >> 4;
            fieldList.add("Pending Addresses - Long Addresses Count: "+String.valueOf(longAddressesCount & 0xFF));
            shortAddressesCount = (data[0] & 0x07);
            fieldList.add("Pending Addresses - Short Addresses Count: "+String.valueOf(shortAddressesCount & 0xFF));
            return Arrays.copyOfRange(data,1,data.length);

        }
        return STOP_DISSECTION;
    }

    public byte[] parseField_shortPendingAddresses(byte[] data,int count) {
        if (count > 0) {
            byte[] updated = data;
            int i = 0;
            while (i < count * 2 && updated.length >= 2) {
                shortAddress[0] = updated[1];
                shortAddress[1] = updated[0];
                fieldList.add("Pending Addresses - Short Address: 0x" + bytesToHex(shortAddress));
                updated = Arrays.copyOfRange(updated, 2, updated.length);
                i += 2;
            }
            if (i<count*2) return STOP_DISSECTION;
            return updated;
        }
        else return data;
    }
    public byte[] parseField_longPendingAddresses(byte[] data,int count) {
        if (count > 0) {
            byte[] updated = data;
            int i = 0;
            while (i < count * 8 && updated.length >= 8) {
                longAddress[0] = updated[7];
                longAddress[1] = updated[6];
                longAddress[2] = updated[5];
                longAddress[3] = updated[4];
                longAddress[4] = updated[3];
                longAddress[5] = updated[2];
                longAddress[6] = updated[1];
                longAddress[7] = updated[0];

                fieldList.add("Pending Addresses - Long Address: " + bytesToAddress(longAddress));
                updated = Arrays.copyOfRange(updated, 8, updated.length);
                i += 8;
            }
            if (i<count*8) return STOP_DISSECTION;
            return updated;
        }
        else return data;
    }
    private int cmdId = 0;
    public byte[] parseField_cmdId(byte[] data) {
        if (data.length >= 1) {
            cmdId = data[0] & 0xFF;
            if (cmdId == 1) {
                fieldList.add("Command ID: Association Request");
            }
            else if (cmdId == 2){
                fieldList.add("Command ID: Association Response");
            }
            else if (cmdId == 3){
                fieldList.add("Command ID: Disassociation Notification");
            }
            else if (cmdId == 4){
                fieldList.add("Command ID: Data Request");
            }
            else if (cmdId == 5){
                fieldList.add("Command ID: Pan ID Conflict Notification");
            }
            else if (cmdId == 6){
                fieldList.add("Command ID: Orphan Notification");
            }
            else if (cmdId == 7){
                fieldList.add("Command ID: Beacon Request");
            }
            else if (cmdId == 8){
                fieldList.add("Command ID: Coordinator Realignment");
            }
            else if (cmdId == 9){
                fieldList.add("Command ID: GTS Request");
            }
            else {
                fieldList.add("Command ID: Unknown");
            }
        }
        return STOP_DISSECTION;
    }

    /* Payload dissectors */
    public void dissect_beacon(byte[] updated) {
        updated = parseField_srcPanId(updated);
        updated = parseField_srcAddr(updated);
        if (securityEnabled != 0) {
            updated = parseField_auxSecurityHeader(updated);
        }
        updated = parseField_superFrameSpec(updated);
        updated = parseField_GTS(updated);
        updated = parseField_pendingAddresses(updated);
        updated = parseField_shortPendingAddresses(updated,shortAddressesCount);
        updated = parseField_longPendingAddresses(updated,longAddressesCount);

        if (updated.length > 0) {
            fieldList.add("Payload: " + bytesToHex(updated));
        }
    }
    public void dissect_data(byte[] updated) {
        updated = parseField_destPanId(updated);
        updated = parseField_destAddr(updated);
        if (srcAddrMode != 0 && panIdCompress == 0) {
            updated = parseField_destPanId(updated);
        }
        if (srcAddrMode != 0) {
            updated = parseField_srcAddr(updated);
        }
        if (securityEnabled != 0) {
            updated = parseField_auxSecurityHeader(updated);
        }
        if (updated.length > 0) {
            fieldList.add("Payload: " + bytesToHex(updated));
        }
    }
    public void dissect_ack(byte[] updated) {
        if (updated.length > 0) {
            fieldList.add("Payload: " + bytesToHex(updated));
        }
    }
    public void dissect_cmd(byte[] updated) {
        updated = parseField_destPanId(updated);
        updated = parseField_destAddr(updated);
        if (srcAddrMode != 0 && panIdCompress == 0) {
            updated = parseField_destPanId(updated);
        }
        if (srcAddrMode != 0) {
            updated = parseField_srcAddr(updated);
        }
        if (securityEnabled != 0) {
            updated = parseField_auxSecurityHeader(updated);
        }
        updated = parseField_cmdId(updated);
        if (updated.length > 0) {
            fieldList.add("Payload: " + bytesToHex(updated));
        }
    }
    private boolean validCrc = false;
    private byte[] crc = {(byte)-1,(byte)-1};

    public void dissect() {
        fieldList.clear();
        validCrc = false;
        if (content.length > 4) {
            byte[] calculatedCrc = computeCRC(Arrays.copyOfRange(content,2,content.length- 2));
            if (calculatedCrc[0] == content[content.length-2] && calculatedCrc[1] == content[content.length-1]) {
                validCrc = true;
                crc = Arrays.copyOfRange(content,content.length-2,content.length);
                content = Arrays.copyOfRange(content,0,content.length - 2);
            }
        }
        byte[] updated = parseField_startOfFrameDelimiter(content);
        updated = parseField_packetLength(updated);
        updated = parseField_frameControlField(updated);
        updated = parseField_sequenceNumber(updated);
        if (frameType == 0) {
            dissect_beacon(updated);
        }
        else if (frameType == 1) {
            dissect_data(updated);
        }
        else if (frameType == 2) {
            dissect_ack(updated);
        }
        else if (frameType == 3) {
            dissect_cmd(updated);
        }
        if (validCrc) {
            fieldList.add("CRC (valid): 0x"+bytesToHex(crc));
        }
    }
}
