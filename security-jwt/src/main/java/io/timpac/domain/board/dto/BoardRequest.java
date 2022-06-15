package io.timpac.domain.board.dto;

import io.timpac.domain.user.User;
import lombok.Data;

@Data
public class BoardRequest {
	private Long boardId;
	private String title;
	private String content;
	private User writer;
	
	public static BoardRequest of(Long boardId, String title, String content, User writer) {
		BoardRequest boardRequest = new BoardRequest();
		boardRequest.setBoardId(boardId);
		boardRequest.setTitle(title);
		boardRequest.setContent(content);
		boardRequest.setWriter(writer);
		return boardRequest;
	}
}
