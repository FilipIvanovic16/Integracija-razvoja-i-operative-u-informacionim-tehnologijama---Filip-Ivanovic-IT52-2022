package com.chronoshop.order.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.chronoshop.dto.WatchDtos.WatchResponse;
import com.chronoshop.exception.BadRequestException;
import com.chronoshop.exception.ResourceNotFoundException;
import com.chronoshop.grpc.stock.StockCheckResponse;
import com.chronoshop.grpc.stock.StockReserveResponse;
import com.chronoshop.order.grpc.StockGrpcClient;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockClientTest {

  @Mock private StockGrpcClient grpcClient;
  @Mock private CatalogClient restClient;

  @InjectMocks private StockClient stockClient;

  @Test
  void checkStock_usesGrpcResult_whenGrpcSucceeds() {
    when(grpcClient.checkStock(10L, 2))
        .thenReturn(
            StockCheckResponse.newBuilder()
                .setFound(true)
                .setActive(true)
                .setAvailable(true)
                .setCurrentStock(5)
                .setWatchName("Submariner")
                .setReferenceNumber("SUB-1")
                .setPrice("12000.00")
                .build());

    WatchStockInfo info = stockClient.checkStock(10L, 2);

    assertThat(info.name()).isEqualTo("Submariner");
    assertThat(info.price()).isEqualByComparingTo("12000.00");
    verifyNoInteractions(restClient);
  }

  @Test
  void checkStock_throwsResourceNotFound_whenGrpcReportsNotFound() {
    when(grpcClient.checkStock(99L, 1))
        .thenReturn(StockCheckResponse.newBuilder().setFound(false).build());

    assertThatThrownBy(() -> stockClient.checkStock(99L, 1))
        .isInstanceOf(ResourceNotFoundException.class);
    verifyNoInteractions(restClient);
  }

  @Test
  void checkStock_fallsBackToRest_whenGrpcUnavailable() {
    when(grpcClient.checkStock(10L, 2)).thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));
    WatchResponse restWatch =
        new WatchResponse(
            10L,
            "Submariner",
            "SUB-1",
            null,
            null,
            null,
            new BigDecimal("12000.00"),
            5,
            true,
            null,
            null,
            null,
            null,
            null,
            List.of(),
            true,
            null,
            null,
            null);
    when(restClient.getWatch(10L)).thenReturn(restWatch);

    WatchStockInfo info = stockClient.checkStock(10L, 2);

    assertThat(info.name()).isEqualTo("Submariner");
    verify(restClient).getWatch(10L);
  }

  @Test
  void reserveStock_fallsBackToRest_whenGrpcUnavailable() {
    when(grpcClient.reserveStock(10L, -2))
        .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

    stockClient.reserveStock(10L, -2);

    verify(restClient).adjustStock(10L, -2);
  }

  @Test
  void reserveStock_throwsBadRequest_whenGrpcReportsFailure() {
    when(grpcClient.reserveStock(10L, -50))
        .thenReturn(
            StockReserveResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Nedovoljno na stanju.")
                .build());

    assertThatThrownBy(() -> stockClient.reserveStock(10L, -50))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Nedovoljno");
    verifyNoInteractions(restClient);
  }
}
