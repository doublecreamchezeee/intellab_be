package com.example.courseservice.repository;


import com.example.courseservice.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findAllByName(String name);
    List<Category> findAllByIdIn(List<Integer> ids);

}
