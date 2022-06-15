package io.timpac.domain.board;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

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
public class Board extends BaseTime {
	@Id @GeneratedValue
	@Column(name = "BOARD_ID")
	private Long boardId;
	
	private String title;
	private String content;
	
	@ManyToOne
	@JoinColumn(name = "USER_ID")
	private User writer;
	
	@OneToMany(mappedBy = "board", cascade = {CascadeType.REMOVE, CascadeType.REFRESH})
	private List<Comment> comments;
}
