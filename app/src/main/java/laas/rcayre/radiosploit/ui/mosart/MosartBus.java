package laas.rcayre.radiosploit.ui.mosart;

import laas.rcayre.radiosploit.PacketItemData;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.Observable;

public class MosartBus {
    /* This class is used to exchange information between two Mosart fragments (RX > TX, used to add a received packet to the TX list) */

    /* Singleton implementation */
    private static MosartBus mInstance;
    public static MosartBus getInstance() {
        if (mInstance == null) {
            mInstance = new MosartBus();
        }
        return mInstance;
    }
    private MosartBus() {
    }
    private PublishSubject<PacketItemData> publisher = PublishSubject.create();

    void publish(PacketItemData packet) {
        publisher.onNext(packet);
    }
    Observable<PacketItemData> listen() {
        return publisher;
    }
}
