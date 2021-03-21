package com.daeng.nyang.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnimalListFE {	// 이 클래스는 백엔드에서 DB의 테이블 여러개를 사용할 거라 @Entity나 @Table 선언을 하지 않음
	
	private Long desertion_no;
	private String kind_c;
	private String color_cd;
	private int age;
	private float weight;
	private String popfile;
	private String process_state;
	private Character sex_cd;
	private Character neuter_yn;
	private String special_mark;
	private String charge_nm;
	private String officetel;
	private String mbti;

	private String[] personality;	// 얘는 ptag table에서 가져올 데이터
	private boolean like;	// 얘는 AnimalLike table에서 가져올 데이터 
	

}
