package com.chronoshop.config;

import com.chronoshop.domain.Watch;
import com.chronoshop.domain.WatchImage;
import com.chronoshop.repository.WatchImageRepository;
import com.chronoshop.repository.WatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(10)
public class WatchImageMigrator implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(WatchImageMigrator.class);

    private final WatchRepository watchRepository;
    private final WatchImageRepository watchImageRepository;

    public WatchImageMigrator(WatchRepository watchRepository, WatchImageRepository watchImageRepository) {
        this.watchRepository = watchRepository;
        this.watchImageRepository = watchImageRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<Watch> watches = watchRepository.findAll();
        int migrated = 0;
        for (Watch w : watches) {
            if (w.getImages().isEmpty() && w.getImageUrl() != null && !w.getImageUrl().isBlank()) {
                WatchImage img = new WatchImage();
                img.setWatch(w);
                img.setUrl(w.getImageUrl());
                img.setSortOrder(0);
                watchImageRepository.save(img);
                migrated++;
            }
        }
        if (migrated > 0) {
            log.info("WatchImageMigrator: migrated {} watches with legacy imageUrl", migrated);
        }
    }
}
