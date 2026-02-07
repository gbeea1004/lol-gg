<script setup>
import SearchBar from '../components/SearchBar.vue'
</script>

<template>
  <div class="min-h-screen flex flex-col items-center justify-center px-4">
    <h1 class="text-5xl font-bold mb-2 tracking-tight">
      <span class="text-win">LoL</span><span class="text-text-secondary">.GG</span>
    </h1>
    <p class="text-text-secondary mb-8 text-lg">리그오브레전드 전적 검색</p>
    <SearchBar />
  </div>
</template>
