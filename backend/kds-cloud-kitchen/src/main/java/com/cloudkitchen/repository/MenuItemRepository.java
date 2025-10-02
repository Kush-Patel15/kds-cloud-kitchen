// package com.cloudkitchen.repository;

// import com.cloudkitchen.model.MenuItem;
// import com.cloudkitchen.model.MenuItem.Category;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository;

// import java.util.List;

// @Repository
// public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
//     List<MenuItem> findByCategoryOrderByName(Category category);
//     List<MenuItem> findByIsAvailableTrueOrderByName();
//     List<MenuItem> findByIsPopularTrueAndIsAvailableTrueOrderByRatingDesc();
//     List<MenuItem> findByCategoryAndIsAvailableTrueOrderByName(Category category);
//     List<MenuItem> findByNameContainingIgnoreCaseAndIsAvailableTrue(String name);
//     List<MenuItem> findTop10ByIsAvailableTrueOrderByRatingDescRatingCountDesc();
// }

package com.cloudkitchen.repository;

import com.cloudkitchen.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
}