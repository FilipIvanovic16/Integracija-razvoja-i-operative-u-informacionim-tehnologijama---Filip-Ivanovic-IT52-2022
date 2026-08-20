package com.chronoshop.order.client;

import com.chronoshop.dto.WatchDtos.WatchResponse;
import com.chronoshop.exception.BadRequestException;
import com.chronoshop.exception.ResourceNotFoundException;
import com.chronoshop.grpc.stock.StockCheckResponse;
import com.chronoshop.grpc.stock.StockReserveResponse;
import com.chronoshop.order.grpc.StockGrpcClient;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * CheckStock/ReserveStock preko gRPC-a, sa REST fallback-om pri nedostupnosti - tacno
 * po specifikaciji. gRPC (StockGrpcClient) je primarni put; StatusRuntimeException
 * (server nedostupan, timeout, ...) je jedini signal koji prebacuje na postojeci REST
 * CatalogClient iz feat/order-service PR-a. Poslovna pravila (sat ne postoji,
 * nedovoljno na stanju) NISU "nedostupnost" - vracaju se direktno kao izuzeci, bez
 * REST poziva, jer bi REST vratio istu poslovnu grešku.
 */
@Component
public class StockClient {

    private static final Logger log = LoggerFactory.getLogger(StockClient.class);

    private final StockGrpcClient grpcClient;
    private final CatalogClient restClient;

    public StockClient(StockGrpcClient grpcClient, CatalogClient restClient) {
        this.grpcClient = grpcClient;
        this.restClient = restClient;
    }

    public WatchStockInfo checkStock(Long watchId, int requestedQuantity) {
        try {
            StockCheckResponse response = grpcClient.checkStock(watchId, requestedQuantity);
            if (!response.getFound()) {
                throw new ResourceNotFoundException("Sat", watchId);
            }
            return new WatchStockInfo(watchId, response.getWatchName(), response.getReferenceNumber(),
                    new BigDecimal(response.getPrice()), response.getCurrentStock(), response.getActive());
        } catch (StatusRuntimeException e) {
            log.warn("gRPC CheckStock nedostupan ({}) - prelazim na REST fallback.", e.getStatus());
            return toStockInfo(restClient.getWatch(watchId));
        }
    }

    public void reserveStock(Long watchId, int delta) {
        try {
            StockReserveResponse response = grpcClient.reserveStock(watchId, delta);
            if (!response.getSuccess()) {
                throw new BadRequestException(response.getMessage());
            }
        } catch (StatusRuntimeException e) {
            log.warn("gRPC ReserveStock nedostupan ({}) - prelazim na REST fallback.", e.getStatus());
            restClient.adjustStock(watchId, delta);
        }
    }

    private WatchStockInfo toStockInfo(WatchResponse watch) {
        return new WatchStockInfo(watch.id(), watch.name(), watch.referenceNumber(),
                watch.price(), watch.stockQuantity(), watch.active());
    }
}
