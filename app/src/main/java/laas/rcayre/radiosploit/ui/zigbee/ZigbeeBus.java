package laas.rcayre.radiosploit.ui.zigbee;

import laas.rcayre.radiosploit.PacketItemData;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.Observable;

public class ZigbeeBus {
    /* This class is used to exchange information between two Zigbee fragments (RX > TX, used to add a received packet to the TX list) */

    /* Singleton implementation */
    private static ZigbeeBus mInstance;
    public static ZigbeeBus getInstance() {
        if (mInstance == null) {
            mInstance = new ZigbeeBus();
        }
        return mInstance;
    }
    private ZigbeeBus() {
    }


    private PublishSubject<PacketItemData> publisher = PublishSubject.create();

    void publish(PacketItemData packet) {
        /* Publish a packet */
        publisher.onNext(packet);
    }


    Observable<PacketItemData> listen() {
        /* Returns an Observable */
        return publisher;
    }
}
