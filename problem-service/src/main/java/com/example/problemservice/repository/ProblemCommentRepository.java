package com.example.problemservice.repository;

import com.example.problemservice.model.ProblemComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProblemCommentRepository extends JpaRepository<ProblemComment, UUID> {
    List<ProblemComment> findAllByProblem_ProblemId(UUID problemId);
    List<ProblemComment> findAllByProblem_ProblemIdAndParentCommentIsNull(UUID problemId);
    Page<ProblemComment> findAllByProblem_ProblemIdAndParentCommentIsNull(UUID problemId, Pageable pageable);
    Page<ProblemComment> findByProblem_ProblemIdAndParentCommentIsNull(UUID problemId, Pageable pageable);
    Page<ProblemComment> findAllByParentComment_CommentId(UUID commentId, Pageable pageable);
    void deleteAllByParentComment_CommentId(UUID commentId);


    @Modifying
    @Transactional
    @Query("UPDATE ProblemComment pc SET pc.numberOfLikes = pc.numberOfLikes + 1 WHERE pc.commentId = :commentId")
    void incrementNumberOfLikes(UUID commentId);

    @Modifying
    @Transactional
    @Query("UPDATE ProblemComment pc SET pc.numberOfLikes = pc.numberOfLikes - 1 WHERE pc.commentId = :commentId")
    void decrementNumberOfLikes(UUID commentId);
}
