package com.chronoshop.order.grpc;

import com.chronoshop.grpc.stock.StockCheckRequest;
import com.chronoshop.grpc.stock.StockCheckResponse;
import com.chronoshop.grpc.stock.StockReserveRequest;
import com.chronoshop.grpc.stock.StockReserveResponse;
import com.chronoshop.grpc.stock.StockServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Tanak omotac oko generisanog gRPC blocking stub-a. StockClient je taj koji odlucuje kada preci na
 * REST fallback - ova klasa samo propagira StatusRuntimeException dalje.
 */
@Component
public class StockGrpcClient {

  private final ManagedChannel channel;
  private final StockServiceGrpc.StockServiceBlockingStub stub;

  public StockGrpcClient(@Value("${app.catalog-service.grpc-target}") String target) {
    this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    this.stub = StockServiceGrpc.newBlockingStub(channel);
  }

  public StockCheckResponse checkStock(Long watchId, int requestedQuantity)
      throws StatusRuntimeException {
    return stub.withDeadlineAfter(2, TimeUnit.SECONDS)
        .checkStock(
            StockCheckRequest.newBuilder()
                .setWatchId(watchId)
                .setRequestedQuantity(requestedQuantity)
                .build());
  }

  public StockReserveResponse reserveStock(Long watchId, int delta) throws StatusRuntimeException {
    return stub.withDeadlineAfter(2, TimeUnit.SECONDS)
        .reserveStock(StockReserveRequest.newBuilder().setWatchId(watchId).setDelta(delta).build());
  }

  @PreDestroy
  public void shutdown() {
    channel.shutdown();
  }
}
