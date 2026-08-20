package com.chronoshop.catalog.grpc;

import com.chronoshop.catalog.domain.Watch;
import com.chronoshop.catalog.service.WatchService;
import com.chronoshop.grpc.stock.StockCheckRequest;
import com.chronoshop.grpc.stock.StockCheckResponse;
import com.chronoshop.grpc.stock.StockServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Za razliku od StockGrpcServiceTest (poziva metode direktno, mimo mreze), ovde se
 * StockGrpcService stvarno izlaze preko gRPC in-process servera i pogadja pravim
 * generisanim blocking stub-om - proverava da protobuf serijalizacija/deserijalizacija
 * i StockServiceGrpc kod generisan iz stock.proto zaista rade zajedno, bez potrebe
 * za mrežnim portom ili bazom.
 */
@ExtendWith(MockitoExtension.class)
class StockGrpcServiceInProcessTest {

    @Mock
    private WatchService watchService;

    private Server server;
    private ManagedChannel channel;
    private StockServiceGrpc.StockServiceBlockingStub stub;

    @BeforeEach
    void setUp() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(new StockGrpcService(watchService))
                .build().start();
        channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
        stub = StockServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    void checkStock_realWireRoundTrip_returnsExpectedValues() {
        Watch watch = new Watch();
        watch.setId(1L);
        watch.setName("Submariner");
        watch.setReferenceNumber("SUB-1");
        watch.setPrice(new BigDecimal("12000.00"));
        watch.setStockQuantity(5);
        watch.setActive(true);
        when(watchService.findEntity(1L)).thenReturn(watch);

        StockCheckResponse response = stub.checkStock(StockCheckRequest.newBuilder()
                .setWatchId(1L).setRequestedQuantity(2).build());

        assertThat(response.getFound()).isTrue();
        assertThat(response.getActive()).isTrue();
        assertThat(response.getAvailable()).isTrue();
        assertThat(response.getCurrentStock()).isEqualTo(5);
        assertThat(response.getWatchName()).isEqualTo("Submariner");
        assertThat(response.getReferenceNumber()).isEqualTo("SUB-1");
        assertThat(response.getPrice()).isEqualTo("12000.00");
    }
}
