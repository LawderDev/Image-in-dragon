<script setup lang="ts">
import {defineProps, onMounted, PropType, ref, UnwrapRef, watchEffect} from 'vue';
import { api } from '@/http-api';
import {Effect} from "@/composables/Effects";
// Initialisation du store
import { useImageStore } from '@/store'
import {storeToRefs} from "pinia";
const store = useImageStore()
// Récupération des attributs nécessaires du store
let { selectedImage, appliedEffects } = storeToRefs(store)

const props = defineProps({
  id: {
    type: Number,
    required: true,
  },
  authorizeEffect: {
    type: Boolean,
    default: false,
  }
})

let source = ref("")

/**
 * Update the url source of the image
 * @param data the data
 */
const updateSource = (data: Blob) => {
  const reader = new FileReader()
  reader.readAsDataURL(data)
  reader.onload = () => {
    let result: string = reader.result as string
    if (result) {
      source.value = result
      if(selectedImage.value.id === props.id)
        selectedImage.value.source = result
    }
  };
}

/**
 * Get and set the url source of the image
 * @param id the id of the image
 */
const getImage = (id: PropType<{ type: NumberConstructor; required: boolean }> | undefined | number) => {
  api.getImage(id)
      .then((data) => updateSource(data as unknown as Blob))
      .catch(e => console.log(e.message))
}

/**
 * Get and set the url source of the image with effects specified
 * @param effects the effects
 */
const getImageEffect = (effects: UnwrapRef<Effect[]>) =>{
  if(!props.authorizeEffect || !selectedImage.value || selectedImage.value.id === -1) return
  if(!effects || effects.length === 0) return getImage(selectedImage.value.id)
  api.getImageEffect()
      .then((data) => updateSource(data as unknown as Blob))
      .catch((e) =>{
        if(e) console.log(e.message)
      })
}

onMounted(() => getImage(props.id))
watchEffect(() => getImageEffect(appliedEffects.value))
</script>

<template>
  <img :src="source" :alt="props.id + ''">
</template>

<style scoped>
</style>