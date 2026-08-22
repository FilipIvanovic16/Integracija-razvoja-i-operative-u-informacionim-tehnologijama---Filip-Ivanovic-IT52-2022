package com.chronoshop.catalog.grpc;

import com.chronoshop.catalog.domain.Watch;
import com.chronoshop.catalog.service.WatchService;
import com.chronoshop.dto.WatchDtos.WatchResponse;
import com.chronoshop.exception.InsufficientStockException;
import com.chronoshop.exception.ResourceNotFoundException;
import com.chronoshop.grpc.stock.StockCheckRequest;
import com.chronoshop.grpc.stock.StockCheckResponse;
import com.chronoshop.grpc.stock.StockReserveRequest;
import com.chronoshop.grpc.stock.StockReserveResponse;
import com.chronoshop.grpc.stock.StockServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

/**
 * gRPC ekvivalent REST CheckStock/ReserveStock putanje (WatchController.adjustStock,
 * feat/order-service). Poslovni ishodi (nije pronadjen, nedovoljno na stanju) se vracaju u samom
 * odgovoru (found=false / success=false), ne kao gRPC status greske - vidi napomenu u stock.proto.
 */
@Component
public class StockGrpcService extends StockServiceGrpc.StockServiceImplBase {

  private final WatchService watchService;

  public StockGrpcService(WatchService watchService) {
    this.watchService = watchService;
  }

  @Override
  public void checkStock(
      StockCheckRequest request, StreamObserver<StockCheckResponse> responseObserver) {
    StockCheckResponse response;
    try {
      Watch watch = watchService.findEntity(request.getWatchId());
      response =
          StockCheckResponse.newBuilder()
              .setFound(true)
              .setActive(watch.isActive())
              .setAvailable(watch.getStockQuantity() >= request.getRequestedQuantity())
              .setCurrentStock(watch.getStockQuantity())
              .setWatchName(watch.getName())
              .setReferenceNumber(watch.getReferenceNumber())
              .setPrice(watch.getPrice().toPlainString())
              .build();
    } catch (ResourceNotFoundException e) {
      response = StockCheckResponse.newBuilder().setFound(false).build();
    }
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void reserveStock(
      StockReserveRequest request, StreamObserver<StockReserveResponse> responseObserver) {
    StockReserveResponse response;
    try {
      WatchResponse updated = watchService.adjustStock(request.getWatchId(), request.getDelta());
      response =
          StockReserveResponse.newBuilder()
              .setSuccess(true)
              .setNewStock(updated.stockQuantity())
              .setMessage("OK")
              .build();
    } catch (InsufficientStockException | ResourceNotFoundException e) {
      response =
          StockReserveResponse.newBuilder()
              .setSuccess(false)
              .setNewStock(0)
              .setMessage(e.getMessage())
              .build();
    }
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
