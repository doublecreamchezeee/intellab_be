package com.example.courseservice.repository;

import com.example.courseservice.model.AssignmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.courseservice.model.compositeKey.assignmentDetailID;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentDetailRepository extends JpaRepository<AssignmentDetail, assignmentDetailID> {

}
