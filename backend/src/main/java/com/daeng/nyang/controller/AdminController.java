package com.daeng.nyang.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.daeng.nyang.dto.TotToken;
import com.daeng.nyang.service.user.AdminService;

@RestController // ResponseBody+@Controller -- json형태로 객체 반환
@CrossOrigin("*")
public class AdminController {

	@Autowired
	RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private AdminService adminService; // AdminService = new AdminService();

	@GetMapping(path = "/admin/upload/checkNO") // server port + /admin/upload/checkNo 의 url로 GET 요청 시 아래 메소드 접근
	// 동물 유기번호가 DB에 있는 정보인지 조회
	public ResponseEntity<HashMap<String, Object>> checkNO(@RequestParam Long desertion_no) { // params의 키값이
																								// desertion_no인 value값
																								// 받아옴
		HashMap<String, Object> map = adminService.findNO(desertion_no); // AdminService내의 findNO(Long) 메소드 호출
		if ((boolean) map.get("success")) // map에 저장된 success의 값이 true이면
			return new ResponseEntity<>(map, HttpStatus.OK); // 동물이 존재한다는 의미 -> 상태값과 데이터(동물이미지정보) 전달
		// 위에서 리턴되지 않았으면 DB에 없는 유기번호이므로
		return new ResponseEntity<>(map, HttpStatus.ACCEPTED); // 상태값과 데이터(null) 전달
	}

	@GetMapping(path = "/admin/upload/checkFile") // server port + /admin/upload/checkFile 의 url로 GET 요청 시 아래 메소드 접근
	public ResponseEntity<HashMap<String, Object>> checkFile(@RequestParam String fileName) { // params에서 key값이
																								// fileName인 value값을 데려옴
		HashMap<String, Object> map = adminService.findFile(fileName); // AdminService내의 findFile(String)메소드 호출
		if ((boolean) map.get("success")) // DB에서 같은 이름의 파일을 찾으면 덮어쓰기 되므로
			return new ResponseEntity<>(map, HttpStatus.ACCEPTED); // 있으면 true를 담아 상태값과 함께 전달
		return new ResponseEntity<>(map, HttpStatus.OK); // 없으면 false를 담아 상태값과 함께 전달
	}

	@PostMapping(path = "/admin/uploadVideo") // server port + /admin/uploadVideo 의 url로 POST 요청시 아래 메소드 접근
	// 동물영상 저장
	public ResponseEntity<HashMap<String, Object>> uploadDBVideo(@RequestBody Map<String, Object> video, // body에 담긴
																											// data를
																											// Map<K,V>의
																											// 형태로 받아옴
			HttpServletRequest request) { // accessToken 조회를 위해 HttpServletRequest도 받아옴
		TotToken user = (TotToken) redisTemplate.opsForValue().get(request.getHeader("Authorization"));
		// request의 header에 저장된 accessToken으로 redis에서 사용자 정보 조회
		HashMap<String, Object> map = adminService.uploadVideo(video, user.getAccount().getUser_id());
		// AdminService 내의 uploadVideo(Map<String, Object>, String)메소드 호출
		if ((boolean) map.get("success")) // success 가 true이면 db에 저장 완료 false 이면 db에 저장 안됨
			return new ResponseEntity<>(map, HttpStatus.OK);
		return new ResponseEntity<>(map, HttpStatus.ACCEPTED);
	}

	@PostMapping(path = "/admin/upload") // server port + /admin/upload 의 url로 POST 요청 시 아래 메소드 접근
	public ResponseEntity<HashMap<String, Object>> upload(@RequestParam MultipartFile mfile,	// body로 받은 formData를 MultipartFile 형태로 전달받음
			HttpServletRequest request) {	// 유저정보 조회를 위해 HttpServletRequest도 받아옴
		HashMap<String, Object> map = new HashMap<>();
		if (mfile == null) {	// params로 전달된 정보가 없다면 요청이 잘못된것
			map.put("msg", "영상정보가 없습니다.");	//메시지를 담아서
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);	// 상태값과 데이터를 반환
		}
		map = adminService.uploadVideo(request.getHeader("Authorization"), mfile);	// AdminService 내의 uploadVideo(String, MultipartFile) 메소드 호출
		return new ResponseEntity<>(map, HttpStatus.OK); // 저장이 잘 됐을거야.
	}

}
