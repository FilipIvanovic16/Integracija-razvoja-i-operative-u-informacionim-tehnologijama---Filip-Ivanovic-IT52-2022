package com.chronoshop.catalog.grpc;

import com.chronoshop.catalog.domain.Brand;
import com.chronoshop.catalog.domain.Category;
import com.chronoshop.catalog.domain.Watch;
import com.chronoshop.catalog.service.WatchService;
import com.chronoshop.dto.WatchDtos.WatchResponse;
import com.chronoshop.exception.InsufficientStockException;
import com.chronoshop.exception.ResourceNotFoundException;
import com.chronoshop.grpc.stock.StockCheckRequest;
import com.chronoshop.grpc.stock.StockCheckResponse;
import com.chronoshop.grpc.stock.StockReserveRequest;
import com.chronoshop.grpc.stock.StockReserveResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockGrpcServiceTest {

    @Mock
    private WatchService watchService;
    @Mock
    private StreamObserver<StockCheckResponse> checkObserver;
    @Mock
    private StreamObserver<StockReserveResponse> reserveObserver;

    @InjectMocks
    private StockGrpcService service;

    @Test
    void checkStock_returnsFoundTrue_whenWatchExists() {
        Watch watch = watch(10L, "Submariner", "SUB-1", "12000.00", 5, true);
        when(watchService.findEntity(10L)).thenReturn(watch);

        service.checkStock(StockCheckRequest.newBuilder().setWatchId(10L).setRequestedQuantity(2).build(), checkObserver);

        ArgumentCaptor<StockCheckResponse> captor = ArgumentCaptor.forClass(StockCheckResponse.class);
        verify(checkObserver).onNext(captor.capture());
        verify(checkObserver).onCompleted();
        StockCheckResponse response = captor.getValue();
        assertThat(response.getFound()).isTrue();
        assertThat(response.getActive()).isTrue();
        assertThat(response.getAvailable()).isTrue();
        assertThat(response.getCurrentStock()).isEqualTo(5);
        assertThat(response.getWatchName()).isEqualTo("Submariner");
        assertThat(response.getPrice()).isEqualTo("12000.00");
    }

    @Test
    void checkStock_setsAvailableFalse_whenRequestedExceedsStock() {
        Watch watch = watch(10L, "Submariner", "SUB-1", "12000.00", 1, true);
        when(watchService.findEntity(10L)).thenReturn(watch);

        service.checkStock(StockCheckRequest.newBuilder().setWatchId(10L).setRequestedQuantity(5).build(), checkObserver);

        ArgumentCaptor<StockCheckResponse> captor = ArgumentCaptor.forClass(StockCheckResponse.class);
        verify(checkObserver).onNext(captor.capture());
        assertThat(captor.getValue().getAvailable()).isFalse();
    }

    @Test
    void checkStock_returnsFoundFalse_whenWatchMissing() {
        when(watchService.findEntity(99L)).thenThrow(new ResourceNotFoundException("Sat", 99L));

        service.checkStock(StockCheckRequest.newBuilder().setWatchId(99L).setRequestedQuantity(1).build(), checkObserver);

        ArgumentCaptor<StockCheckResponse> captor = ArgumentCaptor.forClass(StockCheckResponse.class);
        verify(checkObserver).onNext(captor.capture());
        assertThat(captor.getValue().getFound()).isFalse();
    }

    @Test
    void reserveStock_returnsSuccess_whenAdjustSucceeds() {
        WatchResponse updated = new WatchResponse(10L, "Submariner", "SUB-1", null, null, null,
                new BigDecimal("12000.00"), 3, true, null, null, null, null, null, java.util.List.of(),
                true, null, null, null);
        when(watchService.adjustStock(10L, -2)).thenReturn(updated);

        service.reserveStock(StockReserveRequest.newBuilder().setWatchId(10L).setDelta(-2).build(), reserveObserver);

        ArgumentCaptor<StockReserveResponse> captor = ArgumentCaptor.forClass(StockReserveResponse.class);
        verify(reserveObserver).onNext(captor.capture());
        verify(reserveObserver).onCompleted();
        assertThat(captor.getValue().getSuccess()).isTrue();
        assertThat(captor.getValue().getNewStock()).isEqualTo(3);
    }

    @Test
    void reserveStock_returnsFailure_whenInsufficientStock() {
        when(watchService.adjustStock(10L, -50)).thenThrow(new InsufficientStockException("Submariner", 50, 5));

        service.reserveStock(StockReserveRequest.newBuilder().setWatchId(10L).setDelta(-50).build(), reserveObserver);

        ArgumentCaptor<StockReserveResponse> captor = ArgumentCaptor.forClass(StockReserveResponse.class);
        verify(reserveObserver).onNext(captor.capture());
        assertThat(captor.getValue().getSuccess()).isFalse();
        assertThat(captor.getValue().getMessage()).contains("Submariner");
    }

    private Watch watch(Long id, String name, String ref, String price, int stock, boolean active) {
        Watch w = new Watch();
        w.setId(id);
        w.setName(name);
        w.setReferenceNumber(ref);
        w.setPrice(new BigDecimal(price));
        w.setStockQuantity(stock);
        w.setActive(active);
        w.setBrand(new Brand());
        w.setCategory(new Category());
        return w;
    }
}
