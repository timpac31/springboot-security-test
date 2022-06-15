package io.timpac.domain.board.dto;

import java.time.LocalDateTime;

import io.timpac.domain.board.Board;
import io.timpac.domain.user.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BoardListDto {
	private long boardId;
	private String title;
	private User writer;
	private int commentCount;
	private LocalDateTime created;
	private LocalDateTime updated;
	
	public static BoardListDto from(Board board) {
		return BoardListDto.builder()
			.boardId(board.getBoardId())
			.title(board.getTitle())
			.writer(board.getWriter())
			.commentCount(board.getComments() == null ? 0 : board.getComments().size())
			.created(board.getCreated())
			.updated(board.getUpdated())
			.build();
	}
}
