package laas.rcayre.radiosploit.ui.esb;

import laas.rcayre.radiosploit.PacketItemData;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.Observable;

public class EsbDeviceBus {
    /* This class is used to exchange information between two ESB fragments (Scan  > RX, used to automatically configure RX parameters based on the transmitted device) */

    /* Singleton implementation */
    private static EsbDeviceBus mInstance;
    public static EsbDeviceBus getInstance() {
        if (mInstance == null) {
            mInstance = new EsbDeviceBus();
        }
        return mInstance;
    }
    private EsbDeviceBus() {
    }
    private PublishSubject<PacketItemData> publisher = PublishSubject.create();
    void publish(PacketItemData packet) {
        publisher.onNext(packet);
    }
    // Listen should return an Observable
    Observable<PacketItemData> listen() {
        return publisher;
    }
}
