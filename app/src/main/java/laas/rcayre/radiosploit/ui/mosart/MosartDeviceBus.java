package laas.rcayre.radiosploit.ui.mosart;

import laas.rcayre.radiosploit.PacketItemData;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.Observable;

public class MosartDeviceBus {
    /* This class is used to exchange information between two Zigbee fragments (Scan > RX / Keylogger, used to select a scanned device) */

    /* Singleton implementation */
    private static MosartDeviceBus mInstance;
    public static MosartDeviceBus getInstance() {
        if (mInstance == null) {
            mInstance = new MosartDeviceBus();
        }
        return mInstance;
    }
    private MosartDeviceBus() {
    }
    private PublishSubject<PacketItemData> publisher = PublishSubject.create();

    void publish(PacketItemData packet) {
        publisher.onNext(packet);
    }
    Observable<PacketItemData> listen() {
        return publisher;
    }
}
