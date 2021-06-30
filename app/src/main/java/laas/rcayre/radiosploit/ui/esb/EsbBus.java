package laas.rcayre.radiosploit.ui.esb;

import laas.rcayre.radiosploit.PacketItemData;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.Observable;

public class EsbBus {
    /* This class is used to exchange information between two ESB fragments (RX > TX, used to add a received packet to the TX list) */

    /* Singleton implementation */
    private static EsbBus mInstance;
    public static EsbBus getInstance() {
        if (mInstance == null) {
            mInstance = new EsbBus();
        }
        return mInstance;
    }
    private EsbBus() {
    }
    private PublishSubject<PacketItemData> publisher = PublishSubject.create();
    void publish(PacketItemData packet) {
        publisher.onNext(packet);
    }
    Observable<PacketItemData> listen() {
        return publisher;
    }
}
