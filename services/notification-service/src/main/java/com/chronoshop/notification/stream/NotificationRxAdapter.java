package com.chronoshop.notification.stream;

import com.chronoshop.notification.model.NotificationEvent;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava3Adapter;

/**
 * RxJava 3 operatorski sloj nad Reactor tokom, po specifikaciji ("RxJava adapter sloj
 * nad tokom dogadjaja"). Sinks.Many/Flux iz NotificationBroadcaster-a je izvor istine;
 * ovde se tok premosti u RxJava3 Flowable (RxJava3Adapter), primeni bar jedan realan
 * RxJava3 operator (observeOn na sopstvenom Scheduler-u, umesto prostog passthrough-a),
 * i vraca nazad kao Flowable koji WebFlux kontroler moze direktno da vrati (Spring-ov
 * ReactiveAdapterRegistry zna da serijalizuje Flowable kao SSE).
 */
@Component
public class NotificationRxAdapter {

    private static final Logger log = LoggerFactory.getLogger(NotificationRxAdapter.class);

    private final NotificationBroadcaster broadcaster;

    public NotificationRxAdapter(NotificationBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    public Flowable<NotificationEvent> stream() {
        return RxJava3Adapter.fluxToFlowable(broadcaster.stream())
                .observeOn(Schedulers.single())
                .doOnNext(e -> log.debug("SSE push preko RxJava3 sloja: {}", e.type()));
    }
}
