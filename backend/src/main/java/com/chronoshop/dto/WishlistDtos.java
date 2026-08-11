package com.chronoshop.dto;

import com.chronoshop.dto.WatchDtos.WatchResponse;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public final class WishlistDtos {

    private WishlistDtos() {
    }

    public record AddWishlistRequest(
            @NotNull Long watchId
    ) {
    }

    public record WishlistItemResponse(
            Long id,
            WatchResponse watch,
            LocalDateTime addedAt
    ) {
    }
}
