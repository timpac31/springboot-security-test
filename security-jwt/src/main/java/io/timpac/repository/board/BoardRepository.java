package io.timpac.repository.board;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import io.timpac.domain.board.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {
	Page<Board> findAllByOrderByCreatedDesc(Pageable pageable);
	
	@EntityGraph(attributePaths = {"comments", "writer"})
	Optional<Board> findOneWithJoinByBoardId(Long BoardId);
}
