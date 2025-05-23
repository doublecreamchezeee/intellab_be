package com.example.courseservice.repository;

import com.example.courseservice.model.Option;
import com.example.courseservice.model.compositeKey.OptionID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.UUID;


@Repository
public interface OptionRepository extends JpaRepository<Option, OptionID> {
}
