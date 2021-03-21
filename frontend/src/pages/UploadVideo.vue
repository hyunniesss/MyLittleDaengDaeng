<template>
  <div style="padding-top: 75px"> <!-- header를 가리기 때문에 padding-top으로 조정 -->
    <Header />  <!-- header 컴포넌트를 최상단에 -->
    <v-container style="padding-top: 30px; width: 50%">
      <h2 style="text-align: center; padding-bottom: 12px">동영상 업로드</h2>
      <div style="display: flex; height: 60px">
        <img
          :src="image"
          alt="이미지"
          style="
            height: 60px;
            margin: 0 1% 1% 0;
            border-radius: 20px;
            width: 60px;
          "
        />
        <v-text-field
          placeholder="일련번호"
          v-model="desertionNo"
          outlined
        ></v-text-field>
        <v-icon
          style="color: green; padding: 0"
          elevation="0"
          :disabled="desertionNoCheck != 0"
        >
          mdi-checkbox-marked-circle
        </v-icon>
      </div>
      <p v-if="desertionNoCheck == 1" style="color: orange">
        일련번호는 15자입니다.
      </p>
      <p v-else-if="desertionNoCheck == 2" style="color: red">
        존재하지 않는 일련번호입니다.
      </p>

      <div style="display: flex; margin-top: 25px; height: 60px">
        <v-text-field
          placeholder="제목"
          v-model="title"
          outlined
          style="padding: 0"
        ></v-text-field>
        <v-icon
          style="color: green; padding: 0"
          elevation="0"
          :disabled="title.length < 1"
        >
          mdi-checkbox-marked-circle
        </v-icon>
      </div>

      <div style="display: flex; margin-top: 25px; padding-bottom: 12px">
        <input
          type="file"
          ref="file"
          @change="selectFile"
          :disabled="desertionNoCheck != 0"
        />
        <v-icon style="color: green" :disabled="selectedFileCheck != 3">
          mdi-checkbox-marked-circle
        </v-icon>
        <!-- </v-btn> -->
      </div>
      <label v-if="selectedFileCheck == 1" style="color: red"
        >이미 업로드된 파일입니다</label
      >
      <label v-else-if="selectedFileCheck == 2" style="color: red"
        >지원하지 않는 파일 형식입니다.</label
      >
      <div style="display: flex; padding-top: 12px">
        <v-textarea
          :counter="500"
          outlined
          name="input-7-4"
          v-model="content"
          placeholder="내용"
          style="padding-top: 24px"
        ></v-textarea>
        <v-icon
          style="color: green"
          :disabled="content.length < 1 || contentCheck === 1"
        >
          mdi-checkbox-marked-circle
        </v-icon>
        <!-- </v-btn> -->
      </div>
      <label v-if="contentCheck" style="color: orange"
        >500자 이하로 입력해주세요</label
      >
      <div style="padding-top: 20px">
        <v-btn
          outlined
          rounded
          :disabled="
            selectedFileCheck != 3 ||
            desertionNoCheck != 0 ||
            title.length < 1 ||
            content.length < 1 ||
            contentCheck === 1
          "
          @click="upload"
          >등록하기</v-btn
        >
      </div>
    </v-container>
  </div>
</template>

<script>
import Header from "../components/Header.vue";
import SERVER from "@/api/url";
import axios from "axios";

export default {
  components: {
    Header,
  },

  data() {
    return {
      desertionNo: "",
      title: "",
      file: undefined,
      content: "",
      selectedFiles: false,
      desertionNoCheck: 3,  // trigger로 이용
      selectedFileCheck: 0, // trigger로 이용
      image: require(`@/assets/image/merong1.png`), // 페이지 로딩 시 기본 이미지
      error: {
        message: "일련번호를 확인해주세요",
      },
      contentCheck: 0,
    };
  },
  watch: {
    desertionNo() {
      this.image = require(`@/assets/image/merong1.png`); // deserionNo값이 바뀔 때마다 우선 default로 이미지 바꿔놓고
      console.log(this.desertionNo.length);
      if (this.desertionNo.length < 15) this.desertionNoCheck = 1; // desertionNo 길이가 14자이하이면 에러메시지 띄울 것
      if (this.desertionNo.length == 15) {  // desertionNo 길이가 15자이면 백엔드와 통신해서 존재하는 번호인지 확인
        SERVER.tokenCheck(() => { // 우선 accesstoken이 만료됐으면 재발급 받고
          axios
            .get(SERVER.URL + "/admin/upload/checkNO", {  // 이 url로 백엔드 호출
              params: {
                desertion_no: this.desertionNo, // params에 desertion_no 값을 key - value 형태로 담아서
              },
              headers: {
                Authorization: this.$cookies.get("accessToken"),  // header에는 accessToken을 담아서
              },
            })
            .then((res) => {  // 상태값이 200~299 라면 이쪽으로 진입
              if (res.status == 202) {  // 상태값이 ACCEPTED면 DB에 존재하지 않는 일련번호
                this.desertionNoCheck = 2; // 존재하지 않는 일련번호입니다.
              } else {  // 202가 아니면 200(OK)
                this.image = res.data.image;  // DB에서 받아온 이미지로 페이지의 이미지 바꿈
                this.desertionNoCheck = 0;  // 에러메시지 없애기
                this.error.message = "";
              }
            })
            .catch((err) => { // 에러나면
              console.log(err); // 에러가 뭔지 콘솔로 확인
            });
        });
      }
    },

    file() {
      if (this.file[0].name.slice(-3) == "mp4") { // 마지막 세 자가 mp4일때만 데이터 저장할 수 있도록
        SERVER.tokenCheck(() => { // accesstoken이 만료됐으면 재발급받고 refreshtoken도 만료됐으면 로그아웃
          axios
            .get(SERVER.URL + "/admin/upload/checkFile", {  // 이 url로 백엔드에 접근
              params: {
                fileName: this.desertionNo + "_" + this.file[0].name, // params에 filename으로 사용자가 입력한 파일이름과 유기번호를 조합하여 전달
              },
              headers: {
                Authorization: this.$cookies.get("accessToken"),  // 쿠키에서 accessToken 빼서 header에 담아서 같이 -> admin계정만 영상업로드 가능
              },
            })
            .then((res) => {
              console.log(res);
              if (res.status == 202) {  // 결과가 ACCEPTED이면 뭐가 잘 안된 것 : DB에 중복된 이름 존재
                this.selectedFileCheck = 1; // 1이면 중복된 영상이 있다는 에러 메시지
              } else {  // 202가 아니면 200 : DB에 중복된 이름 없음 -> 올린 적 없는 영상임
                this.selectedFileCheck = 3; // 3이면 괜찮
              }
            })
            .catch((err) => {
              console.log(err);
            });
        });
      } else {  // mp4 파일이 아니면
        this.selectedFileCheck = 2; // 파일형식 에러메시지
      }
    },

    content() {
      if (this.content.length <= 500) {
        this.contentCheck = 0;
      } else {
        this.contentCheck = 1;
      }
    },
  },

  methods: {
    selectFile() {
      this.file = this.$refs.file.files;  // 파일을 선택하면 references로 찾아서 files를 받아옴
      this.selectedFiles = true; 
    },

    upload() {  // upload의 axios 호출을 둘로 나눈 이유 : 시간 내에 formData와 다른 정보를 함께 가져가는 방법을 찾지 못했다.
    // 그래서 생각해낸 대안책이 axios를 두번 호출하는 것
      var formData = new FormData();
      SERVER.tokenCheck(() => {
        axios
          .post(
            SERVER.URL + "/admin/uploadVideo",
            {
              desertion_no: this.desertionNo,
              title: this.title,
              content: this.content,
              filepath: this.desertionNo + "_" + this.file[0].name,
            },
            {
              headers: {
                Authorization: this.$cookies.get("accessToken"),
                contentType: "application/json",
              },
            }
          ) // db에 영상에 관련된 내용을 우선 저장 후 
          .then((res) => {  // db에 잘 업로드 됐으면 이제 영상 저장할 차례
            formData.append(  // formData에 
              "mfile", //백엔드에서 받아줄 변수명과
              this.file[0], // 업로드할 파일과
              res.data.uid + "_" + this.file[0].name // 파일명을 담아서
            );
            if (res.data.success == true) { // db에 영상 외 정보가 잘 저장됐다면 영상을 서버에 올리면 됨
              axios
                .post(SERVER.URL + "/admin/upload", formData, { // formData를 그대로 body에 담아 header에 유저 정보와 content-Type을 함께 전달한다
                  headers: {  // content-Type을 적지 않으면 formData를 백엔드가 읽지 못한다.
                    Authorization: this.$cookies.get("accessToken"),
                    "content-Type":
                      "multipart/form-data; charset=utf-8; boundary='calculated when request is sent';",
                  },
                })
                .then((res) => {
                  console.log("RES : ", res);
                  alert("등록되었습니다");
                  this.$router.push("/videos");
                })
                .catch((err) => {
                  console.log("ERROR : ", err);
                });
            }
          })
          .catch((err) => {
            console.log("ERRORERROR : ", err);
          });
      });
    },
  },
};
</script>

<style scoped>
</style>