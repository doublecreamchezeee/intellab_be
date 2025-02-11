package com.example.courseservice.repository;

import com.example.courseservice.model.AssignmentDetail;
import com.example.courseservice.model.compositeKey.AssignmentDetailID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentDetailRepository extends JpaRepository<AssignmentDetail, AssignmentDetailID> {

}
