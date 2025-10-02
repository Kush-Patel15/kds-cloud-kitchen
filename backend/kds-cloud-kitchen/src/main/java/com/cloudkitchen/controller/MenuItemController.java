// package com.cloudkitchen.controller;

// import com.cloudkitchen.model.MenuItem;
// import com.cloudkitchen.model.MenuItem.Category;
// import com.cloudkitchen.service.MenuItemService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;
// import java.util.Map;

// @RestController
// @RequestMapping("/api/menu")
// // @CrossOrigin(origins = "*")
// public class MenuItemController {

//     @Autowired
//     private MenuItemService menuItemService;

//     @GetMapping
//     public ResponseEntity<Map<String, Object>> getAllMenuItems() {
//         try {
//             List<MenuItem> menuItems = menuItemService.getAllMenuItems();
//             return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "menuItems", menuItems,
//                 "count", menuItems.size()
//             ));
//         } catch (Exception e) {
//             return ResponseEntity.internalServerError().body(Map.of(
//                 "success", false,
//                 "message", "Failed to fetch menu items: " + e.getMessage()
//             ));
//         }
//     }

//     @GetMapping("/available")
//     public ResponseEntity<Map<String, Object>> getAvailableMenuItems() {
//         try {
//             List<MenuItem> menuItems = menuItemService.getAvailableMenuItems();
//             return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "menuItems", menuItems,
//                 "count", menuItems.size()
//             ));
//         } catch (Exception e) {
//             return ResponseEntity.internalServerError().body(Map.of(
//                 "success", false,
//                 "message", "Failed to fetch available menu items: " + e.getMessage()
//             ));
//         }
//     }

//     @GetMapping("/popular")
//     public ResponseEntity<Map<String, Object>> getPopularMenuItems() {
//         try {
//             List<MenuItem> menuItems = menuItemService.getPopularMenuItems();
//             return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "menuItems", menuItems,
//                 "count", menuItems.size()
//             ));
//         } catch (Exception e) {
//             return ResponseEntity.internalServerError().body(Map.of(
//                 "success", false,
//                 "message", "Failed to fetch popular menu items: " + e.getMessage()
//             ));
//         }
//     }

//     @GetMapping("/category/{category}")
//     public ResponseEntity<Map<String, Object>> getMenuItemsByCategory(@PathVariable Category category) {
//         try {
//             List<MenuItem> menuItems = menuItemService.getMenuItemsByCategory(category);
//             return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "menuItems", menuItems,
//                 "count", menuItems.size(),
//                 "category", category
//             ));
//         } catch (Exception e) {
//             return ResponseEntity.internalServerError().body(Map.of(
//                 "success", false,
//                 "message", "Failed to fetch menu items by category: " + e.getMessage()
//             ));
//         }
//     }

//     @GetMapping("/search")
//     public ResponseEntity<Map<String, Object>> searchMenuItems(@RequestParam String query) {
//         try {
//             List<MenuItem> menuItems = menuItemService.searchMenuItems(query);
//             return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "menuItems", menuItems,
//                 "count", menuItems.size(),
//                 "query", query
//             ));
//         } catch (Exception e) {
//             return ResponseEntity.internalServerError().body(Map.of(
//                 "success", false,
//                 "message", "Failed to search menu items: " + e.getMessage()
//             ));
//         }
//     }

//     @GetMapping("/{id}")
//     public ResponseEntity<Map<String, Object>> getMenuItemById(@PathVariable Long id) {
//         try {
//             MenuItem menuItem = menuItemService.getMenuItemById(id);
//             return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "menuItem", menuItem
//             ));
//         } catch (Exception e) {
//             return ResponseEntity.notFound().build();
//         }
//     }

//     @PostMapping
//     public ResponseEntity<Map<String, Object>> createMenuItem(@RequestBody MenuItem menuItem) {
//         try {
//             MenuItem savedMenuItem = menuItemService.createMenuItem(menuItem);
//             return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "menuItem", savedMenuItem,
//                 "message", "Menu item created successfully"
//             ));
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().body(Map.of(
//                 "success", false,
//                 "message", "Failed to create menu item: " + e.getMessage()
//             ));
//         }
//     }

//     @PutMapping("/{id}")
//     public ResponseEntity<Map<String, Object>> updateMenuItem(@PathVariable Long id, @RequestBody MenuItem menuItem) {
//         try {
//             MenuItem updatedMenuItem = menuItemService.updateMenuItem(id, menuItem);
//             return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "menuItem", updatedMenuItem,
//                 "message", "Menu item updated successfully"
//             ));
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().body(Map.of(
//                 "success", false,
//                 "message", "Failed to update menu item: " + e.getMessage()
//             ));
//         }
//     }

//     @PutMapping("/{id}/availability")
//     public ResponseEntity<Map<String, Object>> updateMenuItemAvailability(@PathVariable Long id, 
//                                                                           @RequestBody Map<String, Boolean> availabilityUpdate) {
//         try {
//             Boolean isAvailable = availabilityUpdate.get("isAvailable");
//             MenuItem updatedMenuItem = menuItemService.updateMenuItemAvailability(id, isAvailable);
//             return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "menuItem", updatedMenuItem,
//                 "message", "Menu item availability updated"
//             ));
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().body(Map.of(
//                 "success", false,
//                 "message", "Failed to update menu item availability: " + e.getMessage()
//             ));
//         }
//     }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<Map<String, Object>> deleteMenuItem(@PathVariable Long id) {
//         try {
//             menuItemService.deleteMenuItem(id);
//             return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "message", "Menu item deleted successfully"
//             ));
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().body(Map.of(
//                 "success", false,
//                 "message", "Failed to delete menu item: " + e.getMessage()
//             ));
//         }
//     }

//     @GetMapping("/categories")
//     public ResponseEntity<Map<String, Object>> getCategories() {
//         try {
//             Category[] categories = Category.values();
//             return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "categories", categories
//             ));
//         } catch (Exception e) {
//             return ResponseEntity.internalServerError().body(Map.of(
//                 "success", false,
//                 "message", "Failed to fetch categories: " + e.getMessage()
//             ));
//         }
//     }
// }

package com.cloudkitchen.controller;

import com.cloudkitchen.model.MenuItem;
import com.cloudkitchen.repository.MenuItemRepository;
import com.cloudkitchen.service.BroadcastService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu-items")
public class MenuItemController {

  private final MenuItemRepository repo;
  private final BroadcastService ws;

  public MenuItemController(MenuItemRepository repo, BroadcastService ws) {
    this.repo = repo;
    this.ws = ws;
  }

  @GetMapping
  public ResponseEntity<List<MenuItem>> all() {
    try {
        List<MenuItem> items = repo.findAll();
        return ResponseEntity.ok(items);
    } catch (Exception e) {
        return ResponseEntity.status(500).body(new ArrayList<>());
    }
  }

  @PostMapping
  public MenuItem create(@RequestBody MenuItem m) {
    MenuItem saved = repo.save(m);
    ws.send("/topic/menu", Map.of("type","created","item", saved));
    return saved;
  }

  @PutMapping("/{id}")
  public MenuItem update(@PathVariable Long id, @RequestBody MenuItem m) {
    m.setId(id);
    MenuItem saved = repo.save(m);
    ws.send("/topic/menu", Map.of("type","updated","item", saved));
    return saved;
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    repo.deleteById(id);
    ws.send("/topic/menu", Map.of("type","deleted","id", id));
  }
}