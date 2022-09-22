<script setup lang="ts">
import {ref} from "vue";
const isGallery = ref(  window.location.pathname == "/gallery")
</script>

<template>
  <div v-bind:class=" isGallery? 'overflow-hidden': ''" id="app">
      <router-view />
  </div>
</template>

<style>
#app {
  text-align: center;
  font-family: Helvetica, Arial,  sans-serif;
  color: #2c3e50;
}

body{
  margin:0;
  background-color: #E9F2FB;
}

.overflow-hidden{
  overflow-x: hidden;
}

@keyframes appear-neu {
  From {
    box-shadow: unset;
  }
  To {
    box-shadow:
        inset 0 0 15px rgba(55, 84, 170,0),
        inset 0 0 20px rgba(255, 255, 255,0),
        7px 7px 15px rgba(55, 84, 170,.15),
        -7px -7px 20px rgba(255, 255, 255,1),
        inset 0 0 4px rgba(255, 255, 255,.2);
  }
}

.neumorphism{
  background: #E9F2FB;
  box-shadow:
      inset 0 0 15px rgba(55, 84, 170,0),
      inset 0 0 20px rgba(255, 255, 255,0),
      7px 7px 15px rgba(55, 84, 170,.15),
      -7px -7px 20px rgba(255, 255, 255,1),
      inset 0 0 4px rgba(255, 255, 255,.2);
  animation: appear-neu 650ms ease-in-out;
}

.neumorphism-push{
  transition: box-shadow 199ms ease-in-out;
}

.neumorphism-activate{
  box-shadow:
      inset 7px 7px 15px rgba(55, 84, 170,.15),
      inset -7px -7px 20px rgba(255, 255, 255,1),
      0 0 4px rgba(255, 255, 255,.2);
}

.neumorphism-push:hover{
  box-shadow:
      inset 7px 7px 15px rgba(55, 84, 170,.15),
      inset -7px -7px 20px rgba(255, 255, 255,1),
      0 0 4px rgba(255, 255, 255,.2);
  transition: box-shadow 199ms ease-in-out;
  color: #0777D9;
}

ul {
  list-style-type: none;
  margin: 0;
  padding: 0;
  overflow: hidden;
  background-color: #E9F2FB;
}
li {
  float: left;
}
li a {
  display: block;
  color: white;
  text-align: center;
  padding: 14px 16px;
  text-decoration: none;
}
.error {
  color: red;
  font-weight: bold;
  list-style-type: none;
  margin-top: 1em;
}
</style>
