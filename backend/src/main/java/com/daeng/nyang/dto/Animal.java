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

@Data	// @Data에 속해있는 @ToString @Getter @Setter 이 세개 쓸 것임 
@Entity	// 이 클래스 이름으로 연결된 DB에서 같은 이름의 table이랑 매핑시킴
@Builder // 생성자 만듬
@AllArgsConstructor	// 변수를 다 가져가는 생성자 생성
@NoArgsConstructor // 디폴트 생성자 생성
public class Animal { // 동물 정보

	@Id	// PK 로 쓰겠음
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 얘가 auto_increment
	private Long desertion_no;

	@Column(name = "filename")	// table에서 column명은 filename
	private String filename; // 백엔드에서 쓸 칼럼명은 filename	-- 다르게 줄 수 있음

	@Column(name = "happen_dt")
	private String happen_dt;
	
	@Column(name = "happen_place")
	private String happen_place;

	@Column(name = "kind_p")
	private String kind_p;
	
	@Column(name = "kind_c")
	private String kind_c;

	@Column(name = "color_cd")
	private String color_cd;

	@Column(name = "age")
	private int age;

	@Column(name = "weight")
	private float weight;

	@Column(name = "notice_no")
	private String notice_no;

	@Column(name = "notice_sdt")
	private String notice_sdt;

	@Column(name = "notice_edt")
	private String notice_edt;

	@Column(name = "popfile")
	private String popfile;

	@Column(name = "process_state")
	private String process_state;

	@Column(name = "sex_cd")
	private Character sex_cd;

	@Column(name = "neuter_yn")
	private Character neuter_yn;

	@Column(name = "special_mark")
	private String special_mark;

	@Column(name = "care_nm")
	private String care_nm;

	@Column(name = "care_tel")
	private String care_tel;

	@Column(name = "care_addr")
	private String care_addr;

	@Column(name = "org_nm")
	private String org_nm;

	@Column(name = "charge_nm")
	private String charge_nm;

	@Column(name = "officetel")
	private String officetel;

	@Column(name = "mbti")
	private String mbti;
}
