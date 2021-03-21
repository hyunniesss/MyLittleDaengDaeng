package com.daeng.nyang.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnimalLike {	// 동물을 좋아요한 정보

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 얘가 auto_increment
	private int no;

	@Column
	private String user_id;	// user_id

	@Column
	private Long desertion_no;	// desertion_no
}

// Account table의 UNIQUE한 값인 user_id와 Animal table의 PK인 desertion_no로 둘을 이어줌