<template>
  <a id="button" class="neumorphism neumorphism-push">
    <slot></slot>
  </a>
</template>

<style scoped>
.selected {
  color: #0777D9;
}

.unselected {
  color: black;
}

#button{
  border-radius: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  color: #2c3e50;
}

#button:hover{
  color: #0777D9;
}
</style>