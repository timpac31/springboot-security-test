package io.timpac.board;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import io.timpac.domain.board.Board;
import io.timpac.domain.board.Comment;
import io.timpac.domain.board.dto.BoardListDto;
import io.timpac.domain.board.dto.BoardRequest;
import io.timpac.domain.user.User;
import io.timpac.domain.user.dto.UserDto;
import io.timpac.service.board.BoardService;
import io.timpac.service.user.UserService;

@SpringBootTest
public class BoardServiceTest {

	@Autowired private BoardService boardService;
	@Autowired private UserService userService;
	private User writer;
	
	@BeforeEach
	public void setup() {
		writer = userService.signUp(UserDto.builder().username("jyd").password("1234").build());
	}
	
	@AfterEach
	public void clear() {
		boardService.deleteAll();
		userService.deleteAll();
	}

	@DisplayName("게시글 저장")
	@Test
	public void test1() {
		boardService.writeBoard(createBoard("제목1", "안녕하세요"));
		List<Board> boardList = boardService.findAll();
		
		assertEquals(1, boardList.size());
		assertEquals("제목1", boardList.get(0).getTitle());
		assertEquals("안녕하세요", boardList.get(0).getContent());
		assertEquals("jyd", boardList.get(0).getWriter().getUsername());
	}
	
	@DisplayName("리스트 가져오기")
	@Test
	public void test2() {
		for(int i=0; i<6; i++) {
			boardService.writeBoard(createBoard("제목" + i, "test"));
		}
		
		Page<BoardListDto> list = boardService.list(2, 5);
		
		assertEquals(6, list.getTotalElements());
		assertEquals(2, list.getTotalPages());
		assertEquals("제목0", list.getContent().get(0).getTitle());
	}
	
	@DisplayName("상세 가져오기")
	@Test
	public void test3() {
		boardService.writeBoard(createBoard("제목1", "안녕하세요"));
		Board writeBoard = boardService.writeBoard(createBoard("제목2", "하이"));
		
		Optional<Board> detail = boardService.detail(writeBoard.getBoardId());
		
		assertTrue(detail.isPresent());
		Board board = detail.get();
		assertEquals("제목2", board.getTitle());
		assertEquals("하이", board.getContent());
		assertEquals("jyd", board.getWriter().getUsername());
	}
	
	@DisplayName("업데이트")
	@Test
	public void test4() {
		Board writeBoard = boardService.writeBoard(createBoard("제목1", "안녕하세요"));
		BoardRequest boardRequest = BoardRequest.of(writeBoard.getBoardId(), "제목2", "하이", writer);
		boolean result = boardService.update(boardRequest);
		Board detail = boardService.detail(writeBoard.getBoardId()).get();
		
		assertTrue(result);
		assertEquals("제목2", detail.getTitle());
		assertEquals("하이", detail.getContent());
	}
	
	@DisplayName("삭제")
	@Test
	public void test5() {
		Board writeBoard = boardService.writeBoard(createBoard("제목1", "안녕하세요"));
		boardService.delete(writeBoard.getBoardId());
		
		assertTrue(boardService.detail(writeBoard.getBoardId()).isEmpty());
	}
	
	@DisplayName("댓글 등록")
	@Test
	public void test6() {
		Board writeBoard = boardService.writeBoard(createBoard("제목1", "안녕하세요"));
		Comment comment1 = boardService.addComment(writeBoard.getBoardId(), Comment.builder().writer(writer).detail("반갑습니다.").build());
		Comment comment2 = boardService.addComment(writeBoard.getBoardId(), Comment.builder().writer(writer).detail("222222").build());
		Board board = boardService.detail(writeBoard.getBoardId()).get();
		List<Comment> comments = board.getComments();
		
		assertEquals(2, comments.size());
		assertEquals(comment1.getDetail(), comments.get(0).getDetail());
		assertEquals(comment1.getWriter().getUsername(), comments.get(0).getWriter().getUsername());
		assertEquals(comment2.getDetail(), comments.get(1).getDetail());
		assertEquals(comment2.getWriter().getUsername(), comments.get(1).getWriter().getUsername());
	}
	
	@DisplayName("댓글 삭제: 3개의 댓글중 2번째 댓글을 삭제한다.")
	@Test
	public void test7() {
		Board writeBoard = boardService.writeBoard(createBoard("제목1", "안녕하세요"));
		Long boardId = writeBoard.getBoardId();
		Comment comment1 = boardService.addComment(boardId, Comment.builder().writer(writer).detail("11").build());
		Comment comment2 = boardService.addComment(boardId, Comment.builder().writer(writer).detail("22").build());
		Comment comment3 = boardService.addComment(boardId, Comment.builder().writer(writer).detail("33").build());
		boardService.removeComment(comment2.getCommentId());
		
		Board board = boardService.detail(boardId).get();
		List<Comment> comments = board.getComments();
		
		assertEquals(2, comments.size());
		assertEquals(comment1.getDetail(), comments.get(0).getDetail());
		assertEquals(comment1.getWriter().getUsername(), comments.get(0).getWriter().getUsername());
		assertEquals(comment3.getDetail(), comments.get(1).getDetail());
		assertEquals(comment3.getWriter().getUsername(), comments.get(1).getWriter().getUsername());
	}

	private Board createBoard(String title, String content) {
		return Board.builder()
			.title(title)
			.content(content)
			.writer(writer)
			.build();
	}
}
