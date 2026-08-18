package com.chronoshop.catalog.repository.spec;

import com.chronoshop.catalog.domain.Watch;
import com.chronoshop.domain.enums.Documentation;
import com.chronoshop.domain.enums.Gender;
import com.chronoshop.domain.enums.MovementType;
import com.chronoshop.domain.enums.WatchCondition;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class WatchSpecifications {

    private WatchSpecifications() {
    }

    public static Specification<Watch> build(
            String search,
            List<Long> brandIds,
            List<Long> categoryIds,
            List<MovementType> movements,
            List<Gender> genders,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean activeOnly,
            List<WatchCondition> conditions,
            Boolean preOwned,
            List<String> materials,
            List<Documentation> documentations) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("referenceNumber")), like),
                        cb.like(cb.lower(root.get("brand").get("name")), like)));
            }
            if (brandIds != null && !brandIds.isEmpty()) {
                predicates.add(root.get("brand").get("id").in(brandIds));
            }
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categoryIds));
            }
            if (movements != null && !movements.isEmpty()) {
                predicates.add(root.get("movement").in(movements));
            }
            if (genders != null && !genders.isEmpty()) {
                predicates.add(root.get("gender").in(genders));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (Boolean.TRUE.equals(activeOnly)) {
                predicates.add(cb.isTrue(root.get("active")));
            }
            if (conditions != null && !conditions.isEmpty()) {
                predicates.add(root.get("condition").in(conditions));
            } else if (preOwned != null) {
                // preOwned se primenjuje samo kad nema eksplicitnih condition filtera
                if (preOwned) {
                    predicates.add(cb.notEqual(root.get("condition"), WatchCondition.NEW));
                    predicates.add(cb.isNotNull(root.get("condition")));
                } else {
                    predicates.add(cb.equal(root.get("condition"), WatchCondition.NEW));
                }
            }
            if (materials != null && !materials.isEmpty()) {
                predicates.add(root.get("material").in(materials));
            }
            if (documentations != null && !documentations.isEmpty()) {
                predicates.add(root.get("documentation").in(documentations));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
