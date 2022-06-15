package io.timpac.domain.board;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import io.timpac.domain.common.BaseTime;
import io.timpac.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
public class Comment extends BaseTime {
	@Id @GeneratedValue
	@Column(name = "COMMENT_ID")
	private Long commentId;
	
	private String detail;
	
	@ManyToOne
	@JoinColumn(name = "USER_ID")
	private User writer;
	
	@ManyToOne
	@JoinColumn(name = "BOARD_ID")
	private Board board;
}
