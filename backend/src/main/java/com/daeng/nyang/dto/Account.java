package com.daeng.nyang.dto;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {	// 유저 정보
	
	@Id // PK로 사용할 column
    @GeneratedValue(strategy = GenerationType.IDENTITY)	// auto_increment로 쓸 것
    private Long id;
	
	@Column(nullable=false, unique=true, length=100)	// NOT NULL이고 중복 안되고 최대 길이는 100자
	private String user_id;
	
	@Column(nullable=false)	// NOT NULL
	private String user_email;
	
	@Column(nullable=false) // NOT NULL
	private String user_password;
	
	@Column(nullable=false, length=50)	//NOT NULL 이고 최대 길이는 50자
	private String user_name;
	
	@CreationTimestamp	// 최초 저장 시에 default current_time_stamp
    private Date regdate;	

    @UpdateTimestamp	// update 시 마다 current_time_stamp로 다시 받음
    private Date updatedate;

    private String role;	//유저 권한으로 쓸 예정

}
