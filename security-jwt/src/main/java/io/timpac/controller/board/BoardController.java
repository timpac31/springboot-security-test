package io.timpac.controller.board;

import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.timpac.domain.board.Board;
import io.timpac.domain.board.Comment;
import io.timpac.domain.board.dto.BoardListDto;
import io.timpac.domain.common.RestResponsePage;
import io.timpac.domain.user.Authority;
import io.timpac.domain.user.User;
import io.timpac.service.board.BoardService;
import io.timpac.service.user.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {
	private final BoardService boardService;
	private final UserService userService;
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/save")
	public Board save(@RequestBody Board board, Authentication auth) {
		User user = (User)auth.getPrincipal();
		
		if(ObjectUtils.isEmpty(board.getBoardId())) {
			board.setWriter(userService.getUser(user.getUsername()));
		} else {
			boardService.find(board.getBoardId()).map(b -> {
				if(!b.getWriter().getUsername().equals(user.getUsername()) && !user.hasAuthority(Authority.ADMIN)) {
					throw new AccessDeniedException("게시자만 수정할 수 있습니다.");
				}
				return b;
			});
		}
		
		return boardService.writeBoard(board);
	}
	
	@GetMapping("/list")
	public RestResponsePage<BoardListDto> list(
			@RequestParam(defaultValue = "1") Integer pageNum,
			@RequestParam(defaultValue = "1") Integer pageSize) {
		return RestResponsePage.from(boardService.list(pageNum, pageSize));
	}
	
	@GetMapping("/{boardId}")
	public Optional<Board> detail(@PathVariable Long boardId) {
		return boardService.detail(boardId);
	}
	
	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/{boardId}")
	public Optional<Board> remove(@PathVariable Long boardId, Authentication auth) {
		User user = (User) auth.getPrincipal();
		return boardService.find(boardId).map(board -> {
			if(user.getUsername().equals(board.getWriter().getUsername()) || user.hasAuthority(Authority.ADMIN)) {
				boardService.delete(boardId);
			} else {
				throw new AccessDeniedException("게시자만 삭제할 수 있습니다.");
			}
			return board;
		});
	}
	
	@PutMapping("/{boardId}/comment")
	public Comment addComment(
			@PathVariable Long boardId, @RequestBody Comment comment, 
			Authentication auth) {
		comment.setWriter((User)auth.getPrincipal());
		return boardService.addComment(boardId, comment);
	}
	
	@DeleteMapping("/{boardId}/comment/{commentId}")
	public Boolean removeComment(
			@PathVariable Long commentId, @PathVariable Long boardId,
			@AuthenticationPrincipal User user) {
		
		Optional<Comment> commentOp = boardService.getComment(commentId);
		if(commentOp.isEmpty()) {
			return false;
		}
		
		Comment comment = commentOp.get();
		if(!user.getId().equals(comment.getWriter().getId())) {
			throw new AccessDeniedException("게시자만 삭제할 수 있습니다.");
		}
		boardService.removeComment(comment.getCommentId());
		
		return true;
	}
	
}
