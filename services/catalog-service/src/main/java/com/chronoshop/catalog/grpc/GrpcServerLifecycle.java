package com.chronoshop.catalog.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * Manuelno pokretanje gRPC servera (bez treceg-lica starter zavisnosti) uz Spring-ov lifecycle -
 * server kreze posle context refresh-a i gasi se uredno na shutdown.
 */
@Component
public class GrpcServerLifecycle implements SmartLifecycle {

  private static final Logger log = LoggerFactory.getLogger(GrpcServerLifecycle.class);

  private final int port;
  private final StockGrpcService stockGrpcService;
  private Server server;
  private volatile boolean running = false;

  public GrpcServerLifecycle(
      @Value("${grpc.server.port:9082}") int port, StockGrpcService stockGrpcService) {
    this.port = port;
    this.stockGrpcService = stockGrpcService;
  }

  @Override
  public void start() {
    try {
      server = ServerBuilder.forPort(port).addService(stockGrpcService).build().start();
      running = true;
      log.info("gRPC server (StockService) pokrenut na portu {}.", port);
    } catch (IOException e) {
      throw new IllegalStateException("Ne mogu da pokrenem gRPC server na portu " + port, e);
    }
  }

  @Override
  public void stop() {
    if (server != null) {
      try {
        server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        running = false;
        log.info("gRPC server zaustavljen.");
      }
    }
  }

  @Override
  public boolean isRunning() {
    return running;
  }
}
