package io.timpac.exception;

public class DupulicateUsernameException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DupulicateUsernameException() {
		super("이미 존재하는 유저입니다.");
	}
	
	public DupulicateUsernameException(String username) {
		super(username + " 는 이미 존재하는 유저입니다.");
	}
}
