package com.chronoshop.notification.stream;

import com.chronoshop.notification.model.NotificationEvent;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationRxAdapterTest {

    @Test
    void stream_bridgesReactorFluxToRxJava3Flowable() {
        NotificationBroadcaster broadcaster = new NotificationBroadcaster();
        NotificationRxAdapter adapter = new NotificationRxAdapter(broadcaster);
        NotificationEvent event = NotificationEvent.of("ORDER_CREATED", "Porudžbina ORD-9 je kreirana.", "ORD-9");

        TestSubscriber<NotificationEvent> subscriber = adapter.stream().test();
        broadcaster.publish(event);

        subscriber.awaitCount(1);
        subscriber.assertValue(event);
        assertThat(subscriber.values()).containsExactly(event);
    }
}
