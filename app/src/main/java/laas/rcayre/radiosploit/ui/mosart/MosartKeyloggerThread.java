package laas.rcayre.radiosploit.ui.mosart;

import android.widget.EditText;

import laas.rcayre.radiosploit.HciInterface;
import laas.rcayre.radiosploit.MainActivity;
import laas.rcayre.radiosploit.PacketItemData;

public class MosartKeyloggerThread implements Runnable {
    /* This thread handles the keylogger packets and modify the Keystroke Visualizer */
    private HciInterface hciInterface;
    private MainActivity activity;
    private EditText visualizer;
    private boolean running;

    public MosartKeyloggerThread(MainActivity activity, HciInterface hciInterface,EditText visualizer) {
        this.hciInterface = hciInterface;
        this.activity = activity;
        this.visualizer = visualizer;
    }

    public boolean isRunning() {
        return running;
    }
    @Override
    public void run() {
        running = true;
        PacketItemData currentPacket;

        while (running) {
            currentPacket = this.hciInterface.nextPacket();
            if (currentPacket != null) {
                PacketItemData finalCurrentPacket = currentPacket;
                // When a keylogger packet is received, adds the corresponding key to the Keystroke Visualizer
                activity.runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (finalCurrentPacket.getDescription() != "*") { // Ignore key release
                                    visualizer.setText(visualizer.getText() + finalCurrentPacket.getDescription());
                                }
                            }
                        }
                );

            }
        }
    }

    public void stop() {
        running = false;
    }
}
