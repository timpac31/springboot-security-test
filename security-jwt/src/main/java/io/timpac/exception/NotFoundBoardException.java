package io.timpac.exception;

public class NotFoundBoardException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public NotFoundBoardException(Long boardId) {
		super(boardId + "가 존재하지 않습니다.");
	}
}
