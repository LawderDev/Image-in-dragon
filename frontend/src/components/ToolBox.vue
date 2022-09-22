<script setup lang="ts">
  import ToolBoxButton from "@/components/buttons/ToolBoxButton.vue";
  import {reactive, UnwrapRef, watch} from "vue";
  import {EffectTypes, Cursors, DropBox, Effect} from "@/composables/Effects";
  // Initialisation du store
  import {useImageStore} from "@/store";
  import {storeToRefs} from "pinia";
  const store = useImageStore()

  // Récupération des attributs nécéssaires du store
  let { selectedImage, appliedEffects } = storeToRefs(store)

  const state = reactive({
    selectedEffect: new Effect(""),
    listEffect: [
        new Effect(EffectTypes.GaussianBlur),
        new Effect(EffectTypes.MeanBlur),
        new Effect(EffectTypes.Sobel),
        new Effect(EffectTypes.Threshold),
        new Effect(EffectTypes.Luminosity),
        new Effect(EffectTypes.EgalisationS),
        new Effect(EffectTypes.EgalisationV),
        new Effect(EffectTypes.EgalisationRGB),
        new Effect(EffectTypes.DynContrast),
        new Effect(EffectTypes.Filter),
        new Effect(EffectTypes.Rainbow),
        new Effect(EffectTypes.ColorToGray),
        new Effect(EffectTypes.Negative),
        new Effect(EffectTypes.WaterColor),
        new Effect(EffectTypes.Draw),
        new Effect(EffectTypes.Rotation),
        new Effect(EffectTypes.Perspective),
        new Effect(EffectTypes.Vortex)
      ] as Effect[],
    timer: undefined as unknown as ReturnType<typeof setTimeout>,
    waitTime: 500,
  })

  watch(() => selectedImage.value.id, () =>{
    closeSlider()
    appliedEffects.value.length = 0
    state.selectedEffect = new Effect("")
  })

  const hasParam = (e: UnwrapRef<Effect>) => e.params.dropBoxes.length !== 0 || e.params.cursors.length !== 0

  // Open the menu of parameters
  const openSlider = () => {
    let element = document.getElementById('container-options') as HTMLElement
    if(element) {
      if(window.matchMedia("(min-width: 360px) and (max-width:640px)").matches)
        element.style.width = '60vw';
      else
        element.style.width = '30vw'
    }
  }

  // Close the menu of parameters
  const closeSlider = () => {
    let element = document.getElementById('container-options') as HTMLElement
    if(element) {
        element.style.width = '0'
    }
  }

  const activeButton = () => "neumorphism-activate"
  const selectEffect = (e: UnwrapRef<Effect>) => state.selectedEffect = e
  const addEffects = (effect: UnwrapRef<Effect>) => appliedEffects.value.push(effect)
  const findEffect = (type:string) => state.listEffect.find((e) => e.type === type)
  const removeEffect = (type: string) => appliedEffects.value = appliedEffects.value.filter((e) => e.type !== type )
  const isAppliedEffect = (type:string) => appliedEffects.value.find((e) => e.type === type)
  const removeEffectAndRefresh = (type: string) => removeEffect(type)

  /**
   * Action to perform when we want to apply an effect
   * @param effect
   */
  const handleEffect = (effect: UnwrapRef<Effect>) =>{
    if(selectedImage.value.id === -1) return
    openSlider()
    selectEffect(effect)
    if(!isAppliedEffect(effect.type)) performEffect(effect, null, null)
  }

  /**
   * Action to perform when we want to apply an effect without parameters
   * @param effect
   */
  const handleEffectNoParam = (effect: UnwrapRef<Effect>) => {
    if(selectedImage.value.id === -1) return
    closeSlider()
    selectEffect(effect)
    if(isAppliedEffect(effect.type)) removeEffect(effect.type)
    else addEffects(effect)
  }

  /**
   * Avoid spam of left and right arrow
   * @param effect the effect
   * @param e the event
   * @param param the parameters
   */
  const handleKeyUpCursors = (effect: UnwrapRef<Effect>, e: any, param: UnwrapRef<Cursors> | UnwrapRef<DropBox> | null) => {
    clearTimeout(state.timer);
    state.timer = setTimeout(() => performEffect(effect, e, param), state.waitTime);
  }

  /**
   * Perform an effect
   * @param effect the effect
   * @param e the event
   * @param param the parameters
   */
  const performEffect = (effect: UnwrapRef<Effect>, e: any, param: UnwrapRef<Cursors> | UnwrapRef<DropBox> | null) => {
    if (param) param.value = e.target.value
    removeEffect(effect.type)
    addEffects(effect)
  }

  /**
   * Cursor management min less than max
   * @param effect the effect
   * @param indMin min cursor index
   * @param indMax max cursor index
   */
  const minInfToMax = (effect: UnwrapRef<Effect> | undefined, indMin: number, indMax: number) => {
    if (!effect) return
    let minCursors = effect.params.cursors[indMin]
    let maxCursors = effect.params.cursors[indMax]
    if (minCursors.param[1])
      minCursors.param[1] = maxCursors.value + ""
    if (Number(minCursors.value) > Number(maxCursors.value)){
      minCursors.value = maxCursors.value
    }
  }

  //Perform specific operation of specific effects
  watch(() => findEffect(EffectTypes.Filter), (newEffect) => {
    minInfToMax(newEffect,1,2);
  }, {deep:true})

  watch(() => findEffect(EffectTypes.Rainbow), (newEffect) => {
    minInfToMax(newEffect, 0, 1);
  }, {deep:true})

  watch(() => findEffect(EffectTypes.DynContrast), (newEffect) => {
    minInfToMax(newEffect, 0, 1);
  }, {deep:true})

</script>

<template>
  <div id="container-tool-box">
    <ul id="tool-box" class="neumorphism">
      <li v-for="effect in state.listEffect" :key="effect.type">
        <tool-box-button
            class="tool-box-item-appear item"
            :class="isAppliedEffect(effect.type) && activeButton()"
            @click="hasParam(effect) ? handleEffect(effect): handleEffectNoParam(effect)"
        >
          <span class="effect-icon">{{ effect.text }}</span>
        </tool-box-button>
      </li>
    </ul>
    <div id="container-options">
      <ul id="options" class="neumorphism">
        <li class="title-option" v-if="hasParam(state.selectedEffect)">{{ state.selectedEffect.text }}</li>
          <li class="option-cursor" v-for="c in state.selectedEffect.params.cursors" :key="state.selectedEffect.type + c.name">
              <span class="options-cursor-title">{{ c.text }}</span>
              <input type="range"
                     :min="c.param[0]"
                     :max="c.param[1]"
                     :value="c.value"
                     :step="c.step"
                     @mouseup="performEffect(state.selectedEffect,$event,c)"
                     @keyup.left="handleKeyUpCursors(state.selectedEffect, $event, c)"
                     @keyup.right="handleKeyUpCursors(state.selectedEffect, $event, c)"
              />
              <span>{{ c.value }}</span>
          </li>
        <li v-for="dB in state.selectedEffect.params.dropBoxes" :key="state.selectedEffect.type + dB.name">
          <select class="select-box neumorphism neumorphism-push" :name="state.selectedEffect.type" v-model="dB.value" @change="performEffect(state.selectedEffect,$event,dB)">
            <option value="">Choisir un {{ dB.text }}</option>
            <option  v-for="v in dB.param" :value="v">{{ v }}</option>
          </select>
        </li>
        <li v-if="hasParam(state.selectedEffect)">
          <tool-box-button class="apply-filter" v-if="isAppliedEffect(state.selectedEffect.type)" @click="removeEffectAndRefresh(state.selectedEffect.type)">Enlever</tool-box-button>
          <tool-box-button class="apply-filter" v-else @click="performEffect(state.selectedEffect,null,null)">Appliquer</tool-box-button>
        </li>
      </ul>
      <a id="arrow-left" class="neumorphism neumorphism-push" @click="closeSlider()" >&lt;</a>
    </div>
  </div>


</template>

<style scoped>
.item{
  margin-top: 20px;
  width: 25px;
  height: 25px;
}

#container-tool-box{
  display: flex;
}

#tool-box{
  border-radius: 20px;
  width: 80px;
  height: 71vh;
  display: flex;
  align-items: center;
  flex-direction: column;
  overflow-y: scroll ;
  z-index: 2;
  scrollbar-width: none;
}

.select-box{
  margin-top: 1vh;
  width: 150px;
  height: 30px;
  border-radius: 20px;
}

#tool-box::-webkit-scrollbar {
  display: none;
}

.options-cursor-title{
margin-right: 5px;
}

.option-cursor{
  display: flex;
  flex-wrap: wrap;
  margin-left: 5vw;
}

.apply-filter{
  margin-left: auto;
  margin-right: auto;
  margin-top: 1vw;
  width: 80px;
  height: 5px;
}

.title-option{
  margin-top:2vh;
  margin-bottom: 2vh;
}

#options{
  display: flex;
  flex-direction: column;
  width: 20vw;
  height: 71vh;
  border-radius: 20px;
  position: relative;
  z-index: 0;
}

#container-options{
  display: flex;
  align-items: center;
  position: relative;
  width: 0;
  transition: 550ms width ease-in-out, 550ms height ease-in-out;
}

#arrow-left{
  font-size: 1.5em;
  margin-right: 2vw;
  position: relative;
  right: 3vw;
  margin-top: auto;
  margin-bottom: auto;
  border-radius: 15px;
  width: 45px;
  height: 45px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  z-index: 1;
}

.neumorphism-activate .effect-icon{
  color: #0777D9;
}
@media (min-width: 360px) and (max-width:640px){
  #options{
    width: 100vw;
  }
}

</style>