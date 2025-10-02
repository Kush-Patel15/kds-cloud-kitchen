package com.cloudkitchen.controller.dto;

import java.util.List;

public record OrderCreateRequest(
        String customerName,
        String customerPhone,
        String orderType,
        List<Item> items
) {
    public record Item(Long menuItemId, Integer quantity) {}
}