package com.daeng.nyang.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daeng.nyang.dto.AnimalVideo;
import com.daeng.nyang.service.video.VideoService;

@RestController
@CrossOrigin("*")
public class VideoController {

	@Autowired
	private VideoService videoService;

	// 모든 비디오
	@GetMapping("/newuser/video/allvideo")	// server port + /newuser/video/allvideo 의 url로 GET 요청 시 아래 메소드 진입
	public ResponseEntity<HashMap<String, Object>> allvideo() { // 비회원도 접근 가능한 메소드
		HashMap<String, Object> map = new HashMap<String, Object>();
		try {
			List<AnimalVideo> videoList = videoService.allVideo();	// VideoService 에 정의된 allVideo() 메소드를 호출하여 db에 저장된 영상정보리스트를 가져옴
			map.put("VideoList", videoList); // 조회한 데이터를 프론트에 전달할 map에 저장
			return new ResponseEntity<HashMap<String, Object>>(map, HttpStatus.OK);	//데이터와 상태값 전달
		} catch (Exception e) {	
			e.printStackTrace();
			return new ResponseEntity<>(null, HttpStatus.ACCEPTED);	// 여기까지 왔는데 데이터 조회가 되이 않았으면 백엔드 문제이므로 프론트에 상태값 전달
		}
	}

	@GetMapping("/newuser/video/detailvideo")	// server port + /newuser/video/detailVideo 의 url로 GET 요청 시 아래 메소드 접근
	// 특정 비디오 조회
	public ResponseEntity<HashMap<String, Object>> detailvideo(@RequestParam Long uid) {	// 특정 비디오 조회이므로 db에 PK로 가지고 있는 uid 필요
		HashMap<String, Object> map = new HashMap<String, Object>();
		try {
			AnimalVideo av = videoService.detailvideo(uid);	// VideoService에 정의된 detailvideo(Long) 메소드를 호출하여 비디오 정보 조회
			map.put("VideoDetail", av);	// 조회한 정보를 담아서
			return new ResponseEntity<HashMap<String, Object>>(map, HttpStatus.OK);	// 프론트에 상태값과 함께 전달
		} catch (Exception e) {	// try 안에서 에러가생겼다면
			e.printStackTrace();
			return new ResponseEntity<>(null, HttpStatus.ACCEPTED);	// 백엔드 문제이므로 상태값 전달
		}
	}
	
	@GetMapping("/newuser/video/search")	// server port + /newuser/video/search 의 url로 GET 요청 시 아래 메소드 진입
	// 특정동물의 영상조회
	public ResponseEntity<HashMap<String, Object>> searchVideo(@RequestParam Long desertion_no){	// params로 desertion_no를 받아옴
		List<AnimalVideo> videoList = videoService.searchVideo(desertion_no);	// VideoService에 정의된 searchVideo(Long) 메소드를 호출하여 영상리스트를 받아옴
		HashMap<String, Object> map = new HashMap<>();
		map.put("videoList", videoList);	//데이터에 조회한 영상정보를 담아서
		return new ResponseEntity<>(map, HttpStatus.OK); // 상태값과 함께 전달
	}
}
