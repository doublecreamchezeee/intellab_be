package com.example.problemservice.repository;

import com.example.problemservice.model.ProblemCommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProblemCommentReactionRepository extends JpaRepository<ProblemCommentReaction, Long> {
    List<ProblemCommentReaction> findAllByProblemComment_CommentIdAndReactionId_UserUuid(UUID problemComment_commentId, UUID reactionId_userUuid);
    List<ProblemCommentReaction> findByProblemComment_CommentId(UUID problemComment_commentId);
    List<ProblemCommentReaction> findByReactionId_UserUuid(UUID reactionId_userUuid);
    void deleteByProblemComment_CommentIdAndReactionId_UserUuid(UUID problemComment_commentId, UUID reactionId_userUuid);
    void deleteByProblemComment_CommentId(UUID problemComment_commentId);
    void deleteByReactionId_UserUuid(UUID reactionId_userUuid);
    Optional<ProblemCommentReaction> findByProblemComment_CommentIdAndReactionId_UserUuid(UUID problemComment_commentId, UUID reactionId_userUuid);
    Boolean existsByProblemComment_CommentIdAndReactionId_UserUuid(UUID problemComment_commentId, UUID reactionId_userUuid);

    @Query("SELECT pcr FROM ProblemCommentReaction pcr WHERE pcr.problemComment.commentId IN :commentIds AND pcr.reactionId.userUuid = :userUuid")
    List<ProblemCommentReaction> findAllByCommentIdsAndUserUuid(@Param("commentIds") List<UUID> commentIds, @Param("userUuid") UUID userUuid);
}
