<script setup lang="ts">
import ToolBox from '@/components/ToolBox.vue'
import Carrousel from '@/components/HomeCaroussel.vue'
import Image from "@/components/ImageGetter.vue"
import NavBar from '@/components/NavBar.vue'
import ImageMeta from "@/components/ImageMeta.vue";

// Initialisation du store
import { useImageStore } from '@/store'
import {storeToRefs} from "pinia";
const store = useImageStore()
// Récupération des attributs nécéssaires du store
let { selectedImage } = storeToRefs(store)

</script>

<template>
  <nav-bar></nav-bar>
  <div id="main-content">
    <div id="toolBox">
      <tool-box></tool-box>
    </div>
    <div id="img-box-selected">
      <div class="img-box" :key="selectedImage.id">
        <Image v-if="selectedImage.id !== -1" :id="selectedImage.id" :authorize-effect="true"></Image>
      </div>
    </div>
    <image-meta></image-meta>
  </div>
  <div id="carrousel-box">
    <carrousel></carrousel>
  </div>
</template>

<style scoped>
#img-box-selected{
  margin: auto;
  height: 70vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.img-box img{
  max-height: 70vh;
  animation: appear-opacity 650ms ease-in-out;
}

.img-box{
  animation: appear-opacity 500ms ease-in-out;
}

@media (min-width: 360px) and (max-width:640px){
  #img-box-selected{
    position: fixed;
    left: 26vw;
    z-index: -1;
  }

  .img-box img {
    max-width: 70vw;
  }
}

@keyframes appear-opacity {
  From {
    opacity: 0;
  }
  To {
    opacity: 100%;
  }
}


#main-content{
  display: flex;
  width: 100vw;
}

#toolBox{
  margin-top: 1.5vh;
  margin-left: 1vw;
}

#carrousel-box{
  margin-top: 1.5vh;
  margin-left: auto;
  margin-right: auto;
  width: 98vw;
  height: 15vh;
}
</style>
