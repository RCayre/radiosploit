package laas.rcayre.radiosploit.dissectors;

import java.util.Arrays;

public class MosartDissector extends Dissector {
    /* This class implements a Mosart dissector */
        public MosartDissector(byte[] content) {
            super(content);
        }
        public MosartDissector(String content) {
            super(content);
        }

        public static byte[] computeCRC(byte[] data) {
            /* This method calculates the CRC of a Mosart frame */
            int crc = 0;
            int j = 0;
            int i = 0;
            int count = data.length;
            while (--count >= 0) {
                crc = crc ^ (int)(data[j] << 8);
                j++;
                i=8;
                do {
                    if ((crc & 0x8000) != 0) {
                        crc = crc << 1 ^ 0x1021;
                    }
                    else {
                        crc = crc << 1;
                    }
                }while (--i > 0);
            }
            crc &= 0xffff;
            byte[] crcBytes = {(byte)(crc & 0xFF),(byte)((crc & 0xFF00) >> 8)};
            return crcBytes;
        }
        public static String extractDeviceTypeFromFrame(byte[] frame) {
            /* This method extracts the device type from the provided frame */
            String description = "Unknown device";
            if (frame.length >= 5) {
                if (((frame[4] & 0xF0) >> 4) == 0x1) {
                    description = "Dongle";
                } else if (((frame[4] & 0xF0) >> 4) == 0x4) {
                    description = "Mouse";
                } else if (((frame[4] & 0xF0) >> 4) == 0x7) {
                    description = "Keyboard";
                }
            }
            return description;
        }
        public static String extractPacketTypeFromFrame(byte[] frame) {
            /* This method extracts the packet type from the provided frame */
            String description = "Unknown packet";
            if (frame.length >= 5) {
                if (((frame[4] & 0xF0) >> 4) == 0x1) {
                    description = "Dongle Synchronization packet";
                } else if (((frame[4] & 0xF0) >> 4) == 0x4) {
                    description = "Mouse Movement packet";
                } else if (((frame[4] & 0xF0) >> 4) == 0x7) {
                    description = "Mouse or Keyboard Action packet";
                }
            }
            return description;
        }

        public static String extractAddressFromFrame(byte[] frame) {
            /* This method extracts the address from the provided frame */
            return bytesToAddress(Arrays.copyOfRange(frame,0,4));
        }

        public static String getKeyNameFromKeyCode(int keycode) {
            /* This method converts a keycode to its string representation */
            if (keycode == 8) return "[PAUSE]";
            else if (keycode == 14) return "[CTRL]";
            else if (keycode == 15) return "[F5]";
            else if (keycode == 16) return "a";
            else if (keycode == 17) return "[TAB]";
            else if (keycode == 18) return "q";
            else if (keycode == 19) return "[ESCAPE]";
            else if (keycode == 20) return "w";
            else if (keycode == 23) return "&";
            else if (keycode == 24) return "z";
            else if (keycode == 25) return "[CAPSLOCK]";
            else if (keycode == 26) return "s";
            else if (keycode == 27) return "<";
            else if (keycode == 28) return "x";
            else if (keycode == 30) return "[F1]";
            else if (keycode == 32) return "e";
            else if (keycode == 33) return "[F3]";
            else if (keycode == 34) return "d";
            else if (keycode == 35) return "[F4]";
            else if (keycode == 36) return "c";
            else if (keycode == 38) return "[F2]";
            else if (keycode == 39) return "\"";
            else if (keycode == 40) return "r";
            else if (keycode == 41) return "t";
            else if (keycode == 42) return "f";
            else if (keycode == 43) return "g";
            else if (keycode == 44) return "v";
            else if (keycode == 45) return "b";
            else if (keycode == 46) return "(";
            else if (keycode == 47) return "'";
            else if (keycode == 48) return "u";
            else if (keycode == 49) return "y";
            else if (keycode == 50) return "j";
            else if (keycode == 51) return "h";
            else if (keycode == 52) return ",";
            else if (keycode == 53) return "n";
            else if (keycode == 54) return "-";
            else if (keycode == 56) return "i";
            else if (keycode == 57) return "$";
            else if (keycode == 58) return "k";
            else if (keycode == 59) return "[F6]";
            else if (keycode == 60) return ";";
            else if (keycode == 62) return "=";
            else if (keycode == 63) return "_";
            else if (keycode == 64) return "o";
            else if (keycode == 65) return "[F7]";
            else if (keycode == 66) return "l";
            else if (keycode == 68) return ":";
            else if (keycode == 70) return "[F8]";
            else if (keycode == 72) return "p";
            else if (keycode == 74) return "m";
            else if (keycode == 77) return "!";
            else if (keycode == 78) return ")";
            else if (keycode == 80) return "[SCROLLLOCK]";
            else if (keycode == 83) return "[ALT]";
            else if (keycode == 87) return "[PRINTSCREEN]";
            else if (keycode == 89) return "[BACKSPACE]";
            else if (keycode == 90) return "*";
            else if (keycode == 91) return "[F11]";
            else if (keycode == 92) return "[ENTER]";
            else if (keycode == 93) return "[F12]";
            else if (keycode == 94) return "[F9]";
            else if (keycode == 95) return "[F10]";
            else if (keycode == 99) return " ";
            else if (keycode == 101) return "[DOWNARROW]";
            else if (keycode == 102) return "[DEL]";
            else if (keycode == 109) return "[RIGHT]";
            else if (keycode == 110) return "[INSERT]";
            else if (keycode == 118) return "[PAGEUP]";
            else if (keycode == 119) return "[PAGEDOWN]";
            else if (keycode == 123) return "[UPARROW]";
            else if (keycode == 125) return "[LEFTARROW]";
            else if (keycode == 126) return "[HOME]";
            else if (keycode == 127) return "[END]";
            else if (keycode == 129) return "[SHIFT]";
            else if (keycode == 137) return "[GUI]";
            else if (keycode == 155) return "[GUI]";
            else if (keycode == 157) return "[GUI]";
            else if (keycode == 158) return "[ALT]";
            else return "";
    }
    private boolean validCrc = false;
    private byte[] crc = {-1,-1};
    private int preamble = -1;
    private byte[] address = {-1,-1,-1,-1};
    private int frameType = -1;
    private int sequenceNumber = -1;

    private byte[] mouseCoordinates = {-1,-1,-1,-1};

    private byte[] sync = {-1,-1};

    private int actionState = -1;
    private int actionCode = -1;

    /* Fields dissectors */
    public byte[] parseField_preamble(byte[] data) {
        preamble = -1;
        if (data.length >= 2 && data[0] == (byte)0xF0 && data[1] == (byte)0xF0) {
            preamble = 0xF0F0;
            fieldList.add("Preamble: 0xF0F0");
            return Arrays.copyOfRange(data,2,data.length);
        }
        return STOP_DISSECTION;
    }
    public byte[] parseField_address(byte[] data) {

        if (data.length >= 4) {
            address[0] = data[0];
            address[1] = data[1];
            address[2] = data[2];
            address[3] = data[3];
            fieldList.add("Address: "+Dissector.bytesToAddress(address));
            return Arrays.copyOfRange(data,4,data.length);
        }
        return STOP_DISSECTION;
    }
    public byte[] parseField_frameTypeAndSequenceNumber(byte[] data) {

        if (data.length >= 1) {
            frameType = (data[0] & 0xF0) >> 4;
            sequenceNumber = data[0] & 0x0F;
            if (frameType == 0x4) {
                fieldList.add("Frame Type: Mouse Movement");
            }
            else if (frameType == 0x1) {
                fieldList.add("Frame Type: Dongle Sync");
            }
            else if (frameType == 0x7) {
                fieldList.add("Frame Type: Mouse or Keyboard Action");
            }
            else {
                fieldList.add("Frame Type: Unknown");
            }
            fieldList.add("Sequence Number: "+(sequenceNumber & 0xFF));
            return Arrays.copyOfRange(data,1,data.length);
        }
        return STOP_DISSECTION;
    }


    public byte[] parseField_mouseCoordinate(byte[] data,int number, String name) {

        if (data.length >= 1) {
            mouseCoordinates[number] = data[0];
            fieldList.add("Mouse coordinate ("+name+"): "+mouseCoordinates[number]);
            return Arrays.copyOfRange(data,1,data.length);
        }
        return STOP_DISSECTION;
    }

    public byte[] parseField_syncField(byte[] data) {
        if (data.length >= 2) {
            sync[0] = data[0];
            sync[1] = data[1];
            fieldList.add("Synchronization Field: "+Dissector.bytesToHex(sync));
            return Arrays.copyOfRange(data,2,data.length);
        }
        return STOP_DISSECTION;
    }

    public byte[] parseField_actionState(byte[] data) {

        if (data.length >= 1) {
            actionState = data[0] & 0xFF;
            if (actionState == 0x81) {
                fieldList.add("Action State: Pressed");
            }
            else if (actionState == 0x01) {
                fieldList.add("Action State: Released");
            }
            else {
                fieldList.add("Action State: Unknown");
            }

            return Arrays.copyOfRange(data,1,data.length);
        }
        return STOP_DISSECTION;
    }

    public byte[] parseField_actionCode(byte[] data) {

        if (data.length >= 1) {
            actionCode = data[0] & 0xFF;
            if (getKeyNameFromKeyCode(actionCode).equals("")) {
                fieldList.add("Action Code: "+actionCode);
            }
            else {
                fieldList.add("Action Code: "+actionCode+" ("+getKeyNameFromKeyCode(actionCode)+")");
            }
            return Arrays.copyOfRange(data,1,data.length);
        }
        return STOP_DISSECTION;
    }
    /* Payload dissectors */
    public void dissect_mouseMovement(byte[] updated) {
        String[] names = {"X1","Y1","X2","Y2"};
        for (int i=0;i<=3;i++) {
            updated = parseField_mouseCoordinate(updated,i,names[i]);
        }
    }

    public void dissect_dongleSync(byte[] updated) {
        updated = parseField_syncField(updated);
    }

    public void dissect_keyboardOrMouseAction(byte[] updated) {
        updated = parseField_actionState(updated);
        updated = parseField_actionCode(updated);
    }
    public void dissect() {
            fieldList.clear();
            validCrc = false;
            if (content.length >= 8) {
                byte[] calculatedCrc = computeCRC(Arrays.copyOfRange(content,6,content.length- 2));
                if (calculatedCrc[0] == content[content.length-2] && calculatedCrc[1] == content[content.length-1]) {
                    validCrc = true;
                    crc = Arrays.copyOfRange(content,content.length-2,content.length);
                    content = Arrays.copyOfRange(content,0,content.length - 2);
                }
            }

        byte[] updated = parseField_preamble(content);
            updated = parseField_address(updated);
            updated = parseField_frameTypeAndSequenceNumber(updated);
            if (frameType == 0x4) {
                dissect_mouseMovement(updated);
            }
            else if (frameType == 0x1) {
                dissect_dongleSync(updated);
            }
            else if (frameType == 0x7) {
                dissect_keyboardOrMouseAction(updated);
            }
            if (validCrc) {
                fieldList.add("CRC (valid): 0x"+bytesToHex(crc));
            }

        }
}
