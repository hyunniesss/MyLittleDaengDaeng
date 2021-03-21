<template> <!-- 이 페이지는 상단의 AdoptionContact.vue와 하단의 AdoptionForm.vue 로 이루어짐 -->
  <div>
    <Header />
    <div class="container" style="padding-top: 80px; width: 70%">
      <div v-if="pageTrigger">
        <AdoptionContact />
      </div>
      <div>
        <AdoptionForm />
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapMutations } from "vuex";
import Header from "../components/Header.vue";
import AdoptionContact from "../components/adoption/AdoptionContact.vue";
import AdoptionForm from "../components/adoption/AdoptionForm.vue";
import axios from "axios";
import SERVER from "@/api/url";

export default {
  name: "Adoption",
  components: {
    Header,
    AdoptionContact,
    AdoptionForm,
  },
  data() {
    return {
      pageTrigger: 0,
    };
  },
};
</script>
