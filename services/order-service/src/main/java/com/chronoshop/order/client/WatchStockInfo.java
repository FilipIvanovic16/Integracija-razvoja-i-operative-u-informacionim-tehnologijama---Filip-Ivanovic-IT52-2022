package com.chronoshop.order.client;

import java.math.BigDecimal;

/**
 * Zajednicki oblik podataka o satu za StockClient - isti bez obzira da li je stigao
 * preko gRPC-a (primarno) ili REST fallback-a, da OrderService ne mora da zna kojim
 * putem je odgovor stigao.
 */
public record WatchStockInfo(
        Long watchId,
        String name,
        String referenceNumber,
        BigDecimal price,
        int stockQuantity,
        boolean active
) {
}
