<script setup lang="ts">
import NavBar from '@/components/NavBar.vue'
import {onMounted, PropType, reactive, UnwrapRef} from "vue";
import {api} from "@/http-api";
import {ImageType} from "@/types/ImageType";

//Initialize the store
import { useImageStore } from '@/store'
import {storeToRefs} from "pinia"
import Image from '@/components/ImageGetter.vue'
const store = useImageStore()
// Get required attributes of the store
let { selectedImage} = storeToRefs(store)

// Select an image
const imageClick = (image: ImageType) => selectedImage.value = image

const state = reactive({
  images: Array<ImageType>(),
  limit: 0,
  nameImg: "",
  type: "all"
})

/**
 * Get a list of images depending on specific filters ( image type, image name)
 */
const getImages = async () => {
  let pngCheckbox = (<HTMLInputElement>document.getElementById("type-png")).checked;
  let jpgCheckbox = (<HTMLInputElement>document.getElementById("type-jpg")).checked;

  if( pngCheckbox && !jpgCheckbox){
    state.type = "png";
  }
  else if(jpgCheckbox && !pngCheckbox){
    state.type = "jpeg"
  }else{
    state.type = "all";
  }

  state.images = await loadImages()
}

/**
 * Return list images
 */
const loadImages = async () => {
  return api.getImageListWithFilters(state.type, state.nameImg).then((data) => {
      let dataArray = data as unknown as [{}]
      dataArray.shift()
      return data as unknown as Array<UnwrapRef<ImageType>>;
    }).catch(e => {
      console.log(e.message)
      return state.images
    })
}

/**
 * Display information of an image
 * @param id String
 */
const mouseOver =  (id: Number) => {
  (<HTMLDivElement>document.getElementById('imgInfo'+id)).style.display = "block";
}

/**
 * Hide information of an image
 * @param id String
 */
const mouseOut =  (id: Number) => {
  (<HTMLDivElement>document.getElementById('imgInfo'+id)).style.display = "none";
}

/**
 * Get proper form of size image without number of channels
 * @param size String
 */
const getSize =  (size: String) => {
  let s = size.split('x', 3)
  let tmp = s[1]
  s[1] = "x"
  s[2] = tmp
  return s[0].concat(s[1]).concat(s[2])
}

onMounted(async () => {
  state.images = await loadImages()
})

</script>

<template>
  <nav-bar></nav-bar>
  <div>
    <div>
        <h2 class="" for="search"> Chercher une image:</h2>
        <div class="">
          <form>
             <input type="search" v-model="state.nameImg" placeholder="Entrez le nom de l'image" class="items"/>
             <div id="type-check">
                <input type="checkbox" name="png" value="" class="items" id="type-png"/><label for="PNG">PNG</label>
                <input type="checkbox" name="jpg" value="" class="items" id="type-jpg"/><label for="JPG">JPEG</label>
             </div>
              <input v-on:click="getImages()" type="button" name="search" value="Chercher" class="button neumorphism neumorphism-push items"/>
          </form>
        </div>
    </div>
    <div class="gallery">
      <div v-for="image in state.images" :key="image.id" :id="image.id.toString()"
           @click="imageClick(image)" @mouseover="mouseOver(image.id)" @mouseout="mouseOut(image.id)"
           class="gallery-img">
        <Image class="img neumorphism appear"
               :alt="image.name" :id="image.id"
        />
        <router-link to="/">
        <div class="gallery-img-info"
              :id="'imgInfo'+image.id">
          <div>
            <p><b>nom:</b> {{image.name}}</p>
            <p><b>type:</b> {{image.type}}</p>
            <p><b>taille:</b> {{getSize(image.size)}}</p>
          </div>
        </div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>

.gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(20rem, 1fr));
  grid-gap: 1rem;
  max-width: 80rem;
  margin: 5rem auto;
  padding: 0 5rem;
}

.gallery-img img {
  width: 100%;
  height: 22vw;
  object-fit: cover;
  border-radius: 0.75rem;
}

.appear{
  animation: appear 650ms ease-in-out;
}

.gallery-img {
  cursor: pointer;
  position: relative;
}

.gallery-img-info{
  opacity: 90%;
  animation: appear-opacity 400ms ease-in-out;
  background: white;
  z-index: 9999;
  position: absolute;
  top: 0;
  right: 0;
  display: none;
  width: 100%;
  height: 22vw;
  object-fit: cover;
  border-radius: 0.75rem;
  font-size: 20px;
  vertical-align: middle;
}

.gallery-img-info > div{
  top: 30%;
  position: relative;
}

.button{
  font-family: Helvetica, Arial,  sans-serif;
  font-size: 16px;
  border-radius: 50px;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 35px;
  cursor: pointer;
  text-decoration: none;
  color: #2c3e50;
  border:none;
}

.button:hover{
  color: #0777D9;
}

.items{
  margin: 10px;
}

@media (max-width: 825px){
  .gallery-img img {
    height: 50vw;
  }

  .gallery-img-info{
    height: 50vw;
  }
}

@media (min-width: 825px) and (max-width: 1160px){
  .gallery-img img {
    height: 40vw;
  }

  .gallery-img-info{
    height: 40vw;
  }
}


@keyframes appear-opacity {
  From {
    opacity: 0;
  }
  To {
    opacity: 90%;
  }
}

@keyframes appear {
  From {
    opacity: 0;
    box-shadow: unset;
  }
  To {
    opacity: 100%;
    box-shadow:
        inset 0 0 15px rgba(55, 84, 170,0),
        inset 0 0 20px rgba(255, 255, 255,0),
        7px 7px 15px rgba(55, 84, 170,.15),
        -7px -7px 20px rgba(255, 255, 255,1),
        inset 0 0 4px rgba(255, 255, 255,.2);
  }
}

@media (min-width: 628px){

  #type-check{
    display: contents;
  }
}

@media (min-width: 1910px){
  .gallery {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(30rem, 1fr));
    grid-gap: 1rem;
    max-width: 100rem;
    margin: 5rem auto;
    padding: 0 5rem;
  }
}

</style>