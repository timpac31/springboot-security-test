package io.timpac.repository.board;

import org.springframework.data.jpa.repository.JpaRepository;

import io.timpac.domain.board.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
