package laas.rcayre.radiosploit;

import com.example.radiosploit.R;
import laas.rcayre.radiosploit.dissectors.Dissector;

public class PacketItemData {
    /* This crappy class is used to represent a Packet or a Device */
    private byte[] content;
    private int imgId; // icon
    private int type;
    private String description;
    private String status;

    public PacketItemData(byte[] content, int type, String description,String status) {
        this.content = content;
        this.description = description;
        this.type = type;
        this.status = status;
        if (type == 0x01) {
            this.imgId = R.drawable.ic_zigbee;
        }
        else if (type == 0x02) {
            if (description.equals("Mouse")) {
                this.imgId = R.drawable.ic_mouse_dev;
            }
            else if (description.equals("Keyboard")) {
                this.imgId = R.drawable.ic_keyboard_dev;
            }
            else {
                this.imgId = R.drawable.ic_unknown_dev;
            }
        }
        else if (type == 0x04) {
            this.imgId = R.drawable.ic_packet;
        }
        else if (type == 0x05) {
            this.imgId = R.drawable.ic_unknown_dev;
        }
        else if (type == 0x06) {
            this.imgId = R.drawable.ic_packet;
        }

        else {
            this.imgId = R.drawable.ic_unknown_dev;
        }
    }
    public String getFormattedContent() {
        /* Getter allowing to get a Human readable version of the content */
        if (this.type != 0x02) {
            return Dissector.bytesToHex(content);
        }
        else return Dissector.bytesToAddress(content);
    }

    public String getDescription() {
        /* Getter allowing to get the description */
        return description;
    }

    public String getStatus() {
        /* Getter allowing to get the status */
        return status;
    }

    public int getType() {
        /* Getter allowing to get the type */
        return this.type;
    }

    public void setContent(byte[] content) {
        /* Setter allowing to modify the content */
        this.content = content;
    }
    public byte[] getContent() {
        /* Getter allowing to get the content (byte array) */
        return this.content;
    }
    public int getImgId() {
        /* Getter allowing to get the image identifier (used to choose the icon)*/
        return imgId;
    }
    public void update(PacketItemData newContent) {
        /* Method allowing to update the content of the current instance */
        this.content = newContent.getContent();
        this.description = newContent.getDescription();
        this.type = newContent.getType();
        this.imgId = newContent.getImgId();
        this.status = newContent.getStatus();

    }
    public void setStatus(String status) {
        /* Setter allowing to modify the status */
        this.status = status;
    }
}
