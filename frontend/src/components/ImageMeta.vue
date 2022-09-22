<script setup lang="ts">
  // Initialisation du store
  import { useImageStore } from '@/store'
  import {storeToRefs} from "pinia";
  const store = useImageStore()
  // Récuparation des attributs nécéssaires du store
  let { selectedImage } = storeToRefs(store)
</script>

<template>
  <ul id="meta-img" v-if="selectedImage.id !== -1">
    <li class="meta" :key="selectedImage.id"><span class="meta-name">Nom: </span> {{ selectedImage.name }}</li>
    <li class="meta" :key="selectedImage.id"><span class="meta-name">Type: </span> {{ selectedImage.type }}</li>
    <li class="meta" :key="selectedImage.id"><span class="meta-name">Taille: </span>{{ selectedImage.size }}</li>
  </ul>
</template>

<style scoped>
#meta-img{
  display: flex;
  flex-direction: column;
  text-align: left;
  border-radius: 20px;
  background-color:rgba(44, 62, 80, 0.1);
  color: black;
  width: 9vw;
  position: absolute;
  justify-content: center;
  padding: 2vh 2vw;
  right: 2vw;
  top: 10vh;
  animation: appear-opacity 650ms ease-in-out;
}


@media (min-width: 360px) and (max-width:640px){
  #meta-img{
    top:10vh;
    width: 50vw;
    right: 18vw;
  }
}

.meta{
  margin-bottom: 0.5vh;
  margin-top: 0.5vh;
  animation: appear-opacity 650ms ease-in-out;
}

.meta-name{
  font-weight: bold;
}

@keyframes appear-opacity {
  From {
    opacity: 0;
  }
  To {
    opacity: 100%;
  }
}
</style>
