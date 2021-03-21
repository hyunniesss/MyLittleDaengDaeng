package com.daeng.nyang.dto;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Builder @NoArgsConstructor @AllArgsConstructor
@Getter @Setter @ToString
public class AnimalVideo {
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private long uid;
	
	@Column
	private long desertion_no;	// 영상의 주인공인 동물의 PK
	@Column
	private String title;
	@Column
	private String filepath;	// 영상이 저장된 위치
	@Column
	private String content;
	@Column
	private String writer;	// 작성자는 유저들의 user_id
	@CreationTimestamp
	private Date regtime;	// DB에 저장할 때의 시간으로 regtime 지정 = default current_time_stamp
	

}
