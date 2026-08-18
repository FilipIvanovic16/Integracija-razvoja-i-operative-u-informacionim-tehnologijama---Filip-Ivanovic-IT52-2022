package com.chronoshop.catalog.repository.spec;

import com.chronoshop.catalog.domain.Brand;
import com.chronoshop.catalog.domain.Category;
import com.chronoshop.catalog.domain.Watch;
import com.chronoshop.catalog.repository.BrandRepository;
import com.chronoshop.catalog.repository.CategoryRepository;
import com.chronoshop.catalog.repository.WatchRepository;
import com.chronoshop.domain.enums.Gender;
import com.chronoshop.domain.enums.MovementType;
import com.chronoshop.domain.enums.WatchCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class WatchSpecificationsTest {

    @Autowired
    private WatchRepository watchRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private Brand rolex;
    private Brand seiko;
    private Category diver;
    private Category dress;

    @BeforeEach
    void seed() {
        rolex = brandRepository.save(brand("Rolex"));
        seiko = brandRepository.save(brand("Seiko"));
        diver = categoryRepository.save(category("Diver"));
        dress = categoryRepository.save(category("Dress"));

        watchRepository.save(watch("Submariner", "SUB-1", rolex, diver,
                new BigDecimal("12000.00"), MovementType.AUTOMATIC, Gender.MENS, WatchCondition.NEW, true));
        watchRepository.save(watch("Presage", "PRE-1", seiko, dress,
                new BigDecimal("450.00"), MovementType.AUTOMATIC, Gender.UNISEX, WatchCondition.NEW, true));
        watchRepository.save(watch("Prospex", "PRO-1", seiko, diver,
                new BigDecimal("520.00"), MovementType.QUARTZ, Gender.MENS, WatchCondition.GOOD, true));
        watchRepository.save(watch("Archive Piece", "ARC-1", rolex, diver,
                new BigDecimal("9000.00"), MovementType.AUTOMATIC, Gender.MENS, WatchCondition.NEW, false));
    }

    @Test
    void filtersByBrand() {
        Specification<Watch> spec = WatchSpecifications.build(
                null, List.of(seiko.getId()), null, null, null, null, null, true, null, null, null, null);

        List<Watch> results = watchRepository.findAll(spec, PageRequest.of(0, 10)).getContent();

        assertThat(results).extracting(Watch::getReferenceNumber)
                .containsExactlyInAnyOrder("PRE-1", "PRO-1");
    }

    @Test
    void filtersByPriceRange() {
        Specification<Watch> spec = WatchSpecifications.build(
                null, null, null, null, null,
                new BigDecimal("400.00"), new BigDecimal("1000.00"), true, null, null, null, null);

        List<Watch> results = watchRepository.findAll(spec, PageRequest.of(0, 10)).getContent();

        assertThat(results).extracting(Watch::getReferenceNumber)
                .containsExactlyInAnyOrder("PRE-1", "PRO-1");
    }

    @Test
    void filtersByMovementAndCategory() {
        Specification<Watch> spec = WatchSpecifications.build(
                null, null, List.of(diver.getId()), List.of(MovementType.QUARTZ), null,
                null, null, true, null, null, null, null);

        List<Watch> results = watchRepository.findAll(spec, PageRequest.of(0, 10)).getContent();

        assertThat(results).extracting(Watch::getReferenceNumber).containsExactly("PRO-1");
    }

    @Test
    void activeOnlyExcludesInactiveWatches() {
        Specification<Watch> spec = WatchSpecifications.build(
                null, null, null, null, null, null, null, true, null, null, null, null);

        List<Watch> results = watchRepository.findAll(spec, PageRequest.of(0, 10)).getContent();

        assertThat(results).extracting(Watch::getReferenceNumber).doesNotContain("ARC-1");
    }

    @Test
    void searchMatchesNameCaseInsensitive() {
        Specification<Watch> spec = WatchSpecifications.build(
                "submariner", null, null, null, null, null, null, true, null, null, null, null);

        List<Watch> results = watchRepository.findAll(spec, PageRequest.of(0, 10)).getContent();

        assertThat(results).extracting(Watch::getReferenceNumber).containsExactly("SUB-1");
    }

    @Test
    void preOwnedTrueExcludesNewCondition() {
        Specification<Watch> spec = WatchSpecifications.build(
                null, null, null, null, null, null, null, true, null, true, null, null);

        List<Watch> results = watchRepository.findAll(spec, PageRequest.of(0, 10)).getContent();

        assertThat(results).extracting(Watch::getReferenceNumber).containsExactly("PRO-1");
    }

    private Brand brand(String name) {
        Brand b = new Brand();
        b.setName(name);
        return b;
    }

    private Category category(String name) {
        Category c = new Category();
        c.setName(name);
        return c;
    }

    private Watch watch(String name, String ref, Brand brand, Category category, BigDecimal price,
                        MovementType movement, Gender gender, WatchCondition condition, boolean active) {
        Watch w = new Watch();
        w.setName(name);
        w.setReferenceNumber(ref);
        w.setBrand(brand);
        w.setCategory(category);
        w.setPrice(price);
        w.setStockQuantity(5);
        w.setMovement(movement);
        w.setGender(gender);
        w.setCondition(condition);
        w.setActive(active);
        return w;
    }
}
