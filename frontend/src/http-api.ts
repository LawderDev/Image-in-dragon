import axios, { AxiosResponse, AxiosError } from 'axios';
import {PropType, UnwrapRef} from "vue";
import { useImageStore } from '@/store'
import {storeToRefs} from "pinia";
import {Cursors, DropBox, Effect} from "@/composables/Effects";
const store = useImageStore()
let { selectedImage, appliedEffects } = storeToRefs(store)


const instance = axios.create({
  baseURL: "/",
  timeout: 15000,
});

const responseBody = (response: AxiosResponse) => response.data;

const requests = {
  get: (url: string, param: {}) => instance.get(url, param).then(responseBody),
  post: (url: string, body: {}) => instance.post(url, body, { headers: { "Content-Type": "multipart/form-data" }, }).then(responseBody),
  put: (url: string, body: {}) => instance.put(url, body).then(responseBody),
  delete: (url: string) => instance.delete(url).then(responseBody)
};

export const api = {
  getImageList: (): Promise<AxiosResponse<any>> => requests.get('images', {}),
  getImageListByNumber: (index: number, size: number): Promise<AxiosResponse<any>> => requests.get('images', {params: { index: index, size:size }}),
  getImageListWithFilters: (type: String, nameImg: String): Promise<AxiosResponse<any>> => requests.get('images', {params: { type: type, nameImg:nameImg }}),
  getImage: (id: PropType<{ type: NumberConstructor; required: boolean }> | number | undefined): Promise<AxiosResponse<any>> => requests.get(`images/${id}`, { responseType: "blob" }),

  getImageEffect: (): Promise<AxiosResponse<any>> => {
    let params = new Map<string, string>();
    let algorithm : string = ""
    let separator = "_"
    let hasAllParams = true

    appliedEffects.value.forEach((e: UnwrapRef<Effect>, index: number) => {
      if (index !== 0) algorithm += separator
      algorithm += e.type
      e.params.dropBoxes.forEach((dB: UnwrapRef<DropBox>) =>{
          if(dB.value === "") {
            hasAllParams = false
            return
          }
          if (params.has(dB.name) && dB.value !== "") params.set(dB.name, params.get(dB.name) + separator + dB.value)
          else params.set(dB.name, dB.value)
      })
      if(!hasAllParams) return
      e.params.cursors.forEach((c: UnwrapRef<Cursors>) =>{
        if (params.has(c.name)) params.set(c.name, params.get(c.name) + separator + c.value)
        else params.set(c.name, c.value.toString())
      })
    })
    if(!hasAllParams) return Promise.reject()

    let objParams = Object.fromEntries(params)
    return requests.get(`images/${selectedImage.value.id}`, {
      responseType: "blob",
      params : { algorithm,  ...objParams}
    })
  },

  createImage: (form: FormData): Promise<AxiosResponse<any>> => requests.post('images', form),
  deleteImage: (id: number): Promise<AxiosResponse<any>> => requests.delete(`images/${id}`),
};