package laas.rcayre.radiosploit.dissectors;

import java.util.ArrayList;

public abstract class Dissector {
    /* This class implements a Protocol Dissector */

    public static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    protected static final byte[] STOP_DISSECTION = {};

    public static String bytesToHex(byte[] bytes) {
        /* Converts bytes to an Hex string */
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
    public static byte[] addressToBytes(String address) {
        /* Converts an address string ("11:22:33:44") to the correspond bytes */
        String addr = address.replace(":","");
        return hexToBytes(addr);
    }

    public static String bytesToAddress(byte[] bytes) {
        /* Converts bytes to an address string ("11:22:33:44") */
        String hex = bytesToHex(bytes);
        StringBuilder builder = new StringBuilder();

        int index = 0;
        while (index < hex.length())
        {
            builder.append(hex.substring(index,index+2));

            index += 2;
            if (index < hex.length()) builder.append(":");
        }
        return builder.toString();
    }

    public static byte[] hexToBytes(String s) {
        /*Converts an hex string to the corresponding bytes */
        int len = s.length();

        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    protected ArrayList<String> fieldList;
    protected byte[] content;

    public Dissector(byte[] content) {
        this.content = content;
        this.fieldList = new ArrayList<String>();
    }
    public Dissector(String content) {
        this.content = hexToBytes(content);
        this.fieldList = new ArrayList<String>();
    }
    public void update(byte[] content) {
        this.content = content;
    }

    public void update(String content) {
        this.content = hexToBytes(content);
    }
    public ArrayList<String> getFields() {
        return this.fieldList;
    }
    public abstract void dissect();


}
