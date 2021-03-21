package com.daeng.nyang.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daeng.nyang.dto.Account;
import com.daeng.nyang.dto.AnimalListFE;
import com.daeng.nyang.dto.Apply;
import com.daeng.nyang.dto.TotToken;
import com.daeng.nyang.jwt.JwtTokenUtil;
import com.daeng.nyang.service.animal.AnimalService;
import com.daeng.nyang.service.email.EmailService;
import com.daeng.nyang.service.user.AccountService;

import io.swagger.annotations.ApiOperation;

@RestController	// responseBody + @Controller
@CrossOrigin("*") // CORS 방지
public class AccountController {

	@Value("${jwt.secret}")
	private String SALT_VALUE;	// application.properties의 jwt.secret 값을 SALT_VALUE 의 값으로 초기화함

	private static String salt = BCrypt.gensalt();

	@Autowired
	private AccountService accountService;	// AccountService accountService = new AccountService();

	@Autowired
	private EmailService emailService;	// EmailService emailService = new EmailServiceImpl();
	
	@Autowired
	private AnimalService animalService;	// AnimalService animalService = new AnimalService();

	@Autowired
	private JwtTokenUtil jwtTokenUtil;	// JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
	@Autowired
	RedisTemplate<String, Object> redisTemplate;	// RedisTemplate<String, Object> redisTemplate = new redisTemplate();

	@PostMapping(path = "/newuser/signup")	// server port + /newuser/signup 의 url로 POST 요청 시 아래 메소드 진입
	@ApiOperation("회원가입")
	public ResponseEntity<HashMap<String, Object>> signup(@RequestBody Account account) {	// 요청의 body에 적히는 key - value 형태를 Account class의 형태로 바꿔서 가져옴
		HashMap<String, Object> result;
		result = accountService.signup(account);
		if ((boolean) result.get("success"))	// result의 key가 success인 값(value)이 true이면 db에 문제 없이 저장완료 
			return new ResponseEntity<HashMap<String, Object>>(result, HttpStatus.OK);
		else	// 그게 아니면 db에 저장할 때 문제 생김
			return new ResponseEntity<HashMap<String, Object>>(result, HttpStatus.ACCEPTED);	// 요청은 받아들여졌으나 결과는 나쁨
	}

	@GetMapping(path = "/newuser/signup/{user_id}")	// server port + /newuser/signup/{user_id} 의 url로 GET 요청 시 아래 메소드 진입
	@ApiOperation("아이디 중복 검사")
	public ResponseEntity<HashMap<String, Object>> checkID(@PathVariable String user_id) { // {user_id}의 형태로 들어오는 값을 user_id의 변수에 담아 가져옴
		if (accountService.checkID(user_id)) // db에 중복되는 아이디가 없으면 true일 것
			return new ResponseEntity<>(HttpStatus.OK); /// 그럼 사용 가능한 아이디
		HashMap<String, Object> map = new HashMap<>();
		map.put("msg", "duplicated");	// 중복되는 아이디가 있으면 중복되었다고 메시지 담아서
		return new ResponseEntity<>(map, HttpStatus.ACCEPTED); // 프론트에 상태값과 함께 전달
	}

	@GetMapping(path = "/newuser/signup")	// server port + /newuser/signup 의 url로 GET 요청 시 아래의 메소드 진입
	@ApiOperation("이메일 유효성 검사")
	public ResponseEntity<?> checkEmail(@RequestParam String email) {	// params의 key-value 값 중 key값이 email인 변수 가져옴
		Map<String, Object> resultMap = new HashMap<String, Object>();
		boolean isAvailabe = accountService.checkEmail(email); // 사용가능한 email인지 확인
		if (isAvailabe) { // 사용가능하면
			String auth_number = emailService.sendAuthEmail(email);// 메일로 인증번호 전송
//			System.out.println(auth_number);
			String hash_number = BCrypt.hashpw(auth_number, salt); //BCrypt의 hashpw를 이용하여 salt값으로 인증번호를 암호화함
			resultMap.put("origin_hash", hash_number);	// 프론트에 전달할 해시값 ( 보안용 )
			return ResponseEntity.status(HttpStatus.OK).body(resultMap);	// 상태값과 데이터 전달
		} else {	// 사용가능한 email이 아니라면
			resultMap.put("origin_hash", null);	
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(resultMap); // 상태값과 null값을 넣은 데이터 전달
		}
	}

	@GetMapping(path = "/newuser/signup/hashcheck")	// server port + /newuser/signup/hashcheck 의 url로 GET 요청시 아래의 메소드 진입
	@ApiOperation("인증번호 유효성검사")
	public ResponseEntity<?> checkAuthNumber(@RequestParam String auth_number, @RequestParam String hash_number) { // body에 담은 key - value 값을 auth_number와 hash_number로 나누어 받음
		Map<String, Object> resultMap = new HashMap<String, Object>();
		boolean result = BCrypt.checkpw(auth_number, hash_number);	// 프론트에서 받은 auth_number와 hash_number로 값이 일치하는 지 확인
		if (result) {	// 맞으면
			resultMap.put("result", result); // true값 담아서
			return ResponseEntity.status(HttpStatus.OK).body(resultMap); // 상태값과 데이터 전달
		} else {	// 틀리면
			resultMap.put("result", result); // false값 담아서
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(resultMap); // 상태값과 데이터 전달 
		}
	}

	@PostMapping(path = "/newuser/login")	// server port + /newuser/login 의 url 로 POST 요청 시 아래 메소드 진입
	@ApiOperation("로그인")
	public ResponseEntity<HashMap<String, Object>> login(@RequestBody Map<String, String> m) {	// body의  key - value 값을 Map 형태로 받음
		String user_id = m.get("user_id"); // parameter로 받은 Map에서 key값이 user_id 인 값(value)을 가져옴
		String user_password = m.get("user_password"); // parameter로 받은 Map에서 key값이 user_password 인 값(value)을 가져옴
		HashMap<String, Object> result = accountService.login(user_id, user_password); // accountService에 정의되어 있는 login() 호출
		if ((boolean) result.get("success")) // result의 키값이 success인 값(value)가 true이면 로그인 성공
			return new ResponseEntity<HashMap<String, Object>>(result, HttpStatus.OK); // 상태값과 데이터(access token 과 refresh token) 전달
		else // false이면 로그인 실패
			return new ResponseEntity<HashMap<String, Object>>(result, HttpStatus.UNAUTHORIZED); // 상태값과 데이터 전달
	}

	@PostMapping(path = "/newuser/refresh")	// server port + /newuser/refresh 의 url로 POST 요청 시 아래 메소드 진입
	@ApiOperation("accessTOKEN 갱신")	
	public ResponseEntity<HashMap<String, Object>> requestForNewAccessToken(HttpServletRequest request) {	// 프론트에서 headers로 전달되는 값을 받기 위해 parameter로 HttpServletRequest값을 받음
		String accessToken = request.getHeader("accessToken");	// Header에 저장된 key값이 accessToken인 value(값)를 가져옴
		String refreshToken = request.getHeader("refreshToken"); // Header에 저장된 key값이 refreshToken인 value(값)를 가져옴
		HashMap<String, Object> response;
		response = accountService.refreshToken(accessToken, refreshToken);	// AccountService에 정의된 refreshToken() 메소드 호출
		if ((boolean) response.get("success"))	//response의 key값이 success인 value가 true이면 accessToken 발급 성공했으므로
			return new ResponseEntity<>(response, HttpStatus.OK); // 상태값과 데이터 전달(새로 발급한 accessToken)
		else	// accessToken 발급 실패시
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);	// 상태값과 데이터 전달
	}

	// 회원
//	@PostMapping(path = "/user/changepw")
//	@ApiOperation("비밀번호 변경")
//	public ResponseEntity<HashMap<String, Object>> findUserId(@RequestBody Account account) {
//		HashMap<String, Object> map = accountService.changPW(account);
//		return new ResponseEntity<>(map, HttpStatus.OK);
//	}

	@PostMapping(path = "/user/logout")	// server port + /user/logout 의 url로 POST 요청 시 아래 메소드 진입
	@ApiOperation("로그아웃") // redis에 저장된 토큰이 만료되면 사용자는 가지고 있던 token으로 인증 및 인가받을 수 없다.
	public ResponseEntity<?> logout(HttpServletRequest request) {	// headers로 전달되는 값을 받기 위해 parameter로 HttpServletRequest 값을 받음
		String accessToken = request.getHeader("Authorization");	// Header에 저장된 key값이 Authorization인 value(값)를 가져옴
		String user_id = null;
		try {
			TotToken user = (TotToken) redisTemplate.opsForValue().get(accessToken);	// redis에 저장된 key값이 accessToken인 value값을 가져옴
			user_id = user.getAccount().getUser_id();	//사용자 계정의 id를 가져옴
		} catch (Exception e) {	// redis에 user정보(accessToken)가 존재하지 않는다면 
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);	// 인증되지 않은 유저이기 때문에 UNAUTHORIZED의 상태값을 반환한다.
		}

		try {
			ValueOperations<String, Object> vo = redisTemplate.opsForValue();
			if (vo.get(user_id) != null) {
				redisTemplate.expire(user_id, 1, TimeUnit.SECONDS);	// 위에서 꺼낸 user_id로 redis에서 key값이 user_id인 데이터(refresh token)를 1초의 여유시간을 두고 만료시킨다.
				if (vo.get(accessToken) != null) {
					redisTemplate.expire(accessToken, 1, TimeUnit.SECONDS);	// redis에서 key값이 accessToken인 데이터(access token)를 1초의 여유시간을 두고 만료시킨다.
				}
			}
		} catch (Exception e) {	// redis에 refreshToken이나 accessToken이 없다면 인증받지 않은 유저이다.
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // UNAUTHORIZED의 상태값 반환
		}
		return new ResponseEntity<>(HttpStatus.OK);	// redis에서 사용자 계정 관련 정보를 제대로 만료시켰다면 OK의 상태값 반환
	}

	@GetMapping(path = "/user/adopt/create") // server port + /user/adopt/create 의 url로 GET 요청 시 아래 메소드 접근
	@ApiOperation("문자인증")
	public ResponseEntity<HashMap<String, Object>> checkPhone(@RequestParam String phone) {	// 사용자의 전화번호를 params로 전달받는다 (ex. 010-xxxx-xxxx / 010XXXXXXXX)
		int rand = (int) (Math.random() * 899999) + 100000; // 인증할 번호 6자리를 만든다.
		HashMap<String, Object> result = accountService.checkPhone(phone, rand);	// 프론트에서 받아온 번호로 인증번호를 전송한다.
		if (result == null)	// 결과값이 null이라면
			return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);	// 인증번호 전송이 제대로 이루어지지 않은 것 : BAD_REQUEST의 상태값 반환
		else	//결과값이 존재한다면 
			return new ResponseEntity<>(result, HttpStatus.OK);	// 상태값과 데이터(전달된 인증번호) 반환
	}

	@PostMapping(path = "/user/adopt/create")	// server port + /user/adopt/create 의 url로 POST 요청 시 아래 메소드 접근
	@ApiOperation("입양신청서 저장")
	public ResponseEntity<HashMap<String, Object>> createAdopt(@RequestBody Apply apply, HttpServletRequest request) {	// body의 값을 Apply 객체의 형태에 맞게 바꿔서 받아옴, header에 담긴 accessToken을 사용하기 위해 HttpServletRequest를 받음.
		String accessToken = request.getHeader("Authorization");	// Headers에서 Authorization이라는 키를 가진 value를 가져옴
		try {
			TotToken user = (TotToken) redisTemplate.opsForValue().get(accessToken);	// redis에서 accessToken을 키로 갖고 있는 값(value)을 가져옴 
			String user_id = user.getAccount().getUser_id();	// redis에서 가져온 유저 정보에서 user_id를 가져옴
			HashMap<String, Object> result = accountService.createApply(user_id, apply);	// AccountService의 createApply(String, Apply) 메소드 호출
			if ((boolean) result.get("success"))	// 결과값이 true이면
				return new ResponseEntity<>(result, HttpStatus.OK);	// 저장 잘 됨
			else
				return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);	// createApply() 메소드 내에서 어떤 문제로 저장 안됨
		} catch (Exception e) {	// redis에 저장된 토큰 정보가 없다면
			e.printStackTrace();
			return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);	// 인증받지 않은 사용자이므로 이 기능을 사용할 수 없음 -> UNAUTHORIZED의 상태값 반환
		}
	}

	@GetMapping(path = "/user/adopt/read") // server port + /user/adopt/read 의 url로 GET 요청 시 아래 메소드 접근
	// 입양신청리스트 조회
	public ResponseEntity<HashMap<String, Object>> readAdopt(HttpServletRequest request) {	// accessToken을 받아옹기 위해 HttpServletRequest 받음
		String accessToken = request.getHeader("Authorization");	// request의 header에 저장된 Authorization 값을 가져옴
		TotToken user = (TotToken)redisTemplate.opsForValue().get(accessToken); // accessToken으로 redis에 저장된 user 정보 가져옴
		String user_id = user.getAccount().getUser_id();	// user 정보에 저장된 user_id 가져옴 
		HashMap<String, Object> map = accountService.readAdopt();	// AccountService 내의 readAdopt()를 호출하여 입양신청리스트 가져옴
		map.put("user_id", user_id);	// 프론트에 전달해줄 데이터에 내 아이디도 저장 ( = 프론트에서 내 글 보기에 활용 )
		return new ResponseEntity<HashMap<String, Object>>(map, HttpStatus.OK);	// 상태값과 데이터 전달
	}

	@GetMapping(path = "/user/adopt/read/{uid}")	// server port + /user/adopt/read/{uid} 의 url로 GET 요청 시 아래 메소드 접근
	// 해당 입양신청서 조회
	public ResponseEntity<HashMap<String, Object>> readone(@PathVariable long uid, HttpServletRequest request) {	// url에 같이 담겨온 /{uid} 에서 uid 값을 빼오고, accessToken을 가져오기 위해 HttpServletRequest 가져옴
		HashMap<String, Object> result = new HashMap<String, Object>();
		Apply apply = accountService.readAdopt(uid);	// AccountService 내의 readAdopt(Long) 메소드를 호출하여 입양신청정보를 가져옴
		AnimalListFE animal = animalService.animalDetail(apply.getAni_num());	// AnimalService 내의 animaldetail(Long) 메소드를 호출하여 입양신청한 동물 정보를 가져옴
		
		if(apply==null || animal==null)	// 입양정보가 null 이거나 동물정보가 null이라면 (업다면)
			return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);	// 정보조회가 불가능한 것이므로 권한문제로 넘김 -> FORBIDDEN(인가문제) 전달
		else {	// db에 입양정보와 동물정보가 존재한다면
			result.put("apply", apply);	// 프론트가 받을 데이터에 입양정보와 
			result.put("animal", animal); // 동물정보를 저장해서
		}
		return new ResponseEntity<>(result, HttpStatus.OK); // 상태값과 데이터 전달
	}

	@GetMapping(path = "/user/userId") // server port + /user/userId 의 url로 GET 요청 시 아래 메소드 접근
	// 활동중인 유저가 관리자계정인지 조회
	public ResponseEntity<HashMap<String, Object>> userID(HttpServletRequest request) {	// accessToken을 사용하기 위해 HttpServletRequest 받아옴
		TotToken user = (TotToken) redisTemplate.opsForValue().get(request.getHeader("Authorization"));	// 헤더에서 꺼낸 accessToken을 키값으로 가진 사용자정보를 redis에서 꺼냄
		HashMap<String, Object> map = new HashMap<>();
		if (user.getAccount().getRole().equals("ROLE_ADMIN")) {	// 사용자 정보에서 그 권한이 관리자 계정이라면	
			map.put("success", true);	// 데이터에 true값 저장해서
			return new ResponseEntity<>(map, HttpStatus.OK); // 상태값과 데이터 전달
		}
		// 관리자 계정이 아니라면
		map.put("success", false);	// 데이터에 false값 저장해서
		return new ResponseEntity<>(map, HttpStatus.OK);	// 상태값과 데이터 전달
	}

}
