<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  compact: { type: Boolean, default: false },
})

const router = useRouter()
const searchInput = ref('')

function handleSearch() {
  const input = searchInput.value.trim()
  if (!input) return

  const hashIndex = input.lastIndexOf('#')
  if (hashIndex === -1) return

  const gameName = input.substring(0, hashIndex).trim()
  const tagLine = input.substring(hashIndex + 1).trim()
  if (!gameName || !tagLine) return

  router.push({ name: 'Summoner', params: { gameName, tagLine } })
  searchInput.value = ''
}
</script>

<template>
  <form @submit.prevent="handleSearch" class="flex w-full" :class="compact ? 'max-w-md' : 'max-w-xl'">
    <input
      v-model="searchInput"
      type="text"
      placeholder="소환사명#태그 (예: Hide on bush#KR1)"
      class="flex-1 bg-bg-secondary text-text-primary rounded-l-lg px-4 outline-none placeholder:text-text-muted border border-bg-tertiary border-r-0 focus:border-win transition-colors"
      :class="compact ? 'py-2 text-sm' : 'py-3 text-base'"
    />
    <button
      type="submit"
      class="bg-win hover:bg-win/80 text-white font-semibold rounded-r-lg px-6 transition-colors cursor-pointer"
      :class="compact ? 'py-2 text-sm' : 'py-3 text-base'"
    >
      .GG
    </button>
  </form>
</template>
