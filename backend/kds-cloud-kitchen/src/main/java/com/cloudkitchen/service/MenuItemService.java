package com.cloudkitchen.service;

import com.cloudkitchen.model.MenuItem;
import com.cloudkitchen.model.MenuItem.Category;
import com.cloudkitchen.repository.MenuItemRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;

    public MenuItemService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    /* ---------- CRUD ---------- */

    public MenuItem create(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }

    public MenuItem get(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found: " + id));
    }

    public MenuItem findById(Long id) {
        return menuItemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));
    }

    public List<MenuItem> getAll() {
        return menuItemRepository.findAll();
    }

    public MenuItem update(Long id, MenuItem input) {
        MenuItem m = get(id);
        m.setName(input.getName());
        m.setDescription(input.getDescription());
        m.setPrice(input.getPrice());
        m.setCategory(input.getCategory());
        m.setPrepTimeMinutes(input.getPrepTimeMinutes());
        m.setIsAvailable(input.getIsAvailable());
        m.setIsPopular(input.getIsPopular());
        m.setImageUrl(input.getImageUrl());
        m.setAllergenInfo(input.getAllergenInfo());
        m.setNutritionalInfo(input.getNutritionalInfo());
        // Skip rating changes unless intentional
        return menuItemRepository.save(m);
    }

    public void delete(Long id) {
        menuItemRepository.deleteById(id);
    }

    /* ---------- Query helpers (replacing removed repository methods) ---------- */

    public List<MenuItem> findByIsAvailableTrueOrderByName() {
        return menuItemRepository.findAll().stream()
                .filter(mi -> Boolean.TRUE.equals(mi.getIsAvailable()))
                .sorted(Comparator.comparing(this::safeName))
                .collect(Collectors.toList());
    }

    public List<MenuItem> findByIsPopularTrueAndIsAvailableTrueOrderByRatingDesc() {
        return menuItemRepository.findAll().stream()
                .filter(mi -> Boolean.TRUE.equals(mi.getIsAvailable()))
                .filter(mi -> Boolean.TRUE.equals(mi.getIsPopular()))
                .sorted(Comparator
                        .comparingDouble(this::safeRating).reversed()
                        .thenComparingInt(this::safeRatingCount).reversed()
                        .thenComparing(this::safeName))
                .collect(Collectors.toList());
    }

    public List<MenuItem> findByCategoryAndIsAvailableTrueOrderByName(Category category) {
        if (category == null) return List.of();
        return menuItemRepository.findAll().stream()
                .filter(mi -> Boolean.TRUE.equals(mi.getIsAvailable()))
                .filter(mi -> category.equals(mi.getCategory()))
                .sorted(Comparator.comparing(this::safeName))
                .collect(Collectors.toList());
    }

    public List<MenuItem> findByNameContainingIgnoreCaseAndIsAvailableTrue(String query) {
        String q = query == null ? "" : query.toLowerCase().trim();
        return menuItemRepository.findAll().stream()
                .filter(mi -> Boolean.TRUE.equals(mi.getIsAvailable()))
                .filter(mi -> safeName(mi).toLowerCase().contains(q))
                .sorted(Comparator.comparing(this::safeName))
                .collect(Collectors.toList());
    }

    public List<MenuItem> findByCategoryOrderByName(Category category) {
        if (category == null) return List.of();
        return menuItemRepository.findAll().stream()
                .filter(mi -> category.equals(mi.getCategory()))
                .sorted(Comparator.comparing(this::safeName))
                .collect(Collectors.toList());
    }

    public Map<String,Object> getMenuInsights() {
        List<MenuItem> all = menuItemRepository.findAll();
        List<MenuItem> available = all.stream().filter(mi -> Boolean.TRUE.equals(mi.getIsAvailable())).toList();
        Map<String, Long> categoryCounts = new LinkedHashMap<>();
        for (Category c : Category.values()) {
            long cnt = available.stream().filter(mi -> c.equals(mi.getCategory())).count();
            categoryCounts.put(c.name(), cnt);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("totalItems", all.size());
        map.put("availableItems", available.size());
        map.put("popularItems", available.stream().filter(mi -> Boolean.TRUE.equals(mi.getIsPopular())).count());
        map.put("categoryCounts", categoryCounts);
        return map;
    }

    /* ---------- Added to satisfy AnalyticsController ---------- */

    // Return popular & available items ordered by rating desc (limit optional)
    public List<MenuItem> getPopularMenuItems() {
        return findByIsPopularTrueAndIsAvailableTrueOrderByRatingDesc();
    }

    // Overloaded variant used by AnalyticsController; date filters ignored (no createdAt field on MenuItem)
    public Map<String,Object> getMenuInsights(String startDate, String endDate) {
        // If later you add a createdAt field, parse dates and filter before computing insights.
        return getMenuInsights(); // delegate to existing no-arg method
    }

    /* ---------- Utility ---------- */

    private String safeName(MenuItem m) {
        return m.getName() == null ? "" : m.getName();
    }
    private double safeRating(MenuItem m) {
        return m.getRating() == null ? 0.0 : m.getRating().doubleValue();
    }
    private int safeRatingCount(MenuItem m) {
        return m.getRatingCount() == null ? 0 : m.getRatingCount();
    }
}