package com.daeng.nyang.service.user;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.daeng.nyang.dto.Animal;
import com.daeng.nyang.dto.AnimalVideo;
import com.daeng.nyang.repo.AnimalRepo;
import com.daeng.nyang.repo.AnimalVideoRepo;

@Service
public class AdminService {
	
	@Autowired
	private AnimalVideoRepo animalVideoRepo;
	
	@Autowired
	private AnimalRepo animalRepo;
	
	@Value("${ubuntu.profile.upload.directory}")
	private String filePath;
	
	public HashMap<String, Object> findNO(Long desertion_no){
		HashMap<String, Object> map = new HashMap<>();
		Animal a = animalRepo.findAnimalByDesertionNo(desertion_no);
		if(a==null)
			map.put("success", false);
		else {
			map.put("success", true);
			map.put("image", a.getPopfile());
		}
		return map;
			
	}
	
	public HashMap<String, Object> findFile(String fileName){
		HashMap<String, Object> map = new HashMap<String, Object>();
		Optional<AnimalVideo> test = animalVideoRepo.findByFilepath(fileName);
		if(test.isPresent())
			map.put("success", true);
		else map.put("success", false);
		return map;
	}

	
	public HashMap<String, Object> uploadVideo(Map<String, Object> video, String user_id) {	// 얘는 DB에 파일경로와 영상정보 저장
		HashMap<String, Object> map = new HashMap<>();
		String title = (String)video.get("title");
		String content = (String)video.get("content");
		String filepath = (String)video.get("filepath");
		Long desertion_no = Long.parseLong((String)video.get("desertion_no"));
		AnimalVideo av = AnimalVideo.builder().content(content).desertion_no(desertion_no)
				.title(title).writer(user_id).filepath(filepath).build();
		AnimalVideo result = animalVideoRepo.save(av);
		if (animalVideoRepo.findByDesertionNoAndTitle(desertion_no, title).isPresent()) {
			map.put("success", true);
			map.put("uid", result.getUid());
		}
		else
			map.put("success", false);
		return map;
	}

	public HashMap<String, Object> uploadVideo(String accessToken, MultipartFile mfile) {	// 얘는 서버에 영상 저장
		StringTokenizer originName = new StringTokenizer(mfile.getOriginalFilename(),"_");
		Long uid = Long.parseLong(originName.nextToken());
		String filename = originName.nextToken();
		HashMap<String, Object> map = new HashMap<String, Object>();
		AnimalVideo av = animalVideoRepo.findByUid(uid);
		try {
			String dest = filePath + av.getDesertion_no()+"_"+filename;	// 파일명은 미리 지정해둔 filePath + 동물유기번호 + "_" + 파일명 으로 저장
			mfile.transferTo(new File(dest));	// 이렇게 하면 dest의 위치에 파일이 저장됨
			if(animalVideoRepo.findByUid(uid).getFilepath()==null) {
				map.put("success", false);
				map.put("msg", "영상저장 실패");
				animalVideoRepo.delete(av);
			}
			else {
				map.put("success", true);
			}
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
			map.put("success", false);
			map.put("msg", "영상 저장 실패");
		}
		return map;
	}

}
