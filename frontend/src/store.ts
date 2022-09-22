import { defineStore } from 'pinia'
import {Effect} from "@/composables/Effects";
import {ImageType} from '@/types/ImageType'

export const useImageStore = defineStore('main', {
    state: () => ({
        selectedImage: {
            id: -1,
            source: '',
            name: '',
            type: '',
            size: '',
            url:''
        } as ImageType,
        appliedEffects: Array<Effect>(),
        uploaded: false,
        deleted: false,
    }),
})