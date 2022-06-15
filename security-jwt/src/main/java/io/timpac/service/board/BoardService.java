package io.timpac.service.board;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.timpac.domain.board.Board;
import io.timpac.domain.board.Comment;
import io.timpac.domain.board.dto.BoardListDto;
import io.timpac.domain.board.dto.BoardRequest;
import io.timpac.exception.NotFoundBoardException;
import io.timpac.repository.board.BoardRepository;
import io.timpac.repository.board.CommentRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class BoardService {
	private final BoardRepository boardRepository;
	private final CommentRepository commentRepository;
	
	public Board writeBoard(Board board) {
		return boardRepository.save(board);
	}
	
	public Page<BoardListDto> list(int pageNum, int pageSize) {
		return boardRepository.findAllByOrderByCreatedDesc(PageRequest.of(pageNum-1, pageSize))
				.map(board -> BoardListDto.from(board));
	}
	
	public Optional<Board> find(Long boardId) {
		return boardRepository.findById(boardId);
	}
	
	public List<Board> findAll() {
		return boardRepository.findAll();
	}
		
	public Optional<Board> detail(Long boardId) {
		return boardRepository.findOneWithJoinByBoardId(boardId);
	}
	
	public boolean update(BoardRequest boardRequest) {
		Board board;
		try {
			board = boardRepository.findById(boardRequest.getBoardId()).get();
		} catch(NoSuchElementException e) {
			return false;
		}
		
		board.setTitle(boardRequest.getTitle());
		board.setContent(boardRequest.getContent());
		
		return true;
	}
	
	public Optional<Board> delete(Long boardId) {
		return boardRepository.findById(boardId).map(board -> { 
			boardRepository.delete(board);
			return board;
		});
	}

	public void deleteAll() {
		boardRepository.deleteAll();
	}
	
	public Comment addComment(Long boardId, Comment comment) {
		Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundBoardException(boardId));
		comment.setBoard(board);
		return commentRepository.save(comment);
	}
	
	public Optional<Comment> getComment(Long CommentId) {
		return commentRepository.findById(CommentId);
	}
	
	public void removeComment(Long CommentId) {
		commentRepository.deleteById(CommentId);
	}
	
}
