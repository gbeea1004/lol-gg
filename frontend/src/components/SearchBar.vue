<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const HISTORY_KEY = 'lolgg_search_history'
const MAX_HISTORY = 5

const props = defineProps({
  compact: { type: Boolean, default: false },
})

const router = useRouter()
const searchInput = ref('')
const showHistory = ref(false)
const history = ref([])
const wrapper = ref(null)

function loadHistory() {
  try {
    history.value = JSON.parse(localStorage.getItem(HISTORY_KEY) || '[]')
  } catch { history.value = [] }
}

function saveToHistory(gameName, tagLine) {
  const filtered = history.value.filter(
    h => !(h.gameName === gameName && h.tagLine === tagLine)
  )
  filtered.unshift({ gameName, tagLine })
  history.value = filtered.slice(0, MAX_HISTORY)
  localStorage.setItem(HISTORY_KEY, JSON.stringify(history.value))
}

function removeHistory(index) {
  history.value.splice(index, 1)
  localStorage.setItem(HISTORY_KEY, JSON.stringify(history.value))
}

function handleSearch() {
  const input = searchInput.value.trim()
  if (!input) return

  const hashIndex = input.lastIndexOf('#')
  if (hashIndex === -1) return

  const gameName = input.substring(0, hashIndex).trim()
  const tagLine = input.substring(hashIndex + 1).trim()
  if (!gameName || !tagLine) return

  saveToHistory(gameName, tagLine)
  showHistory.value = false
  router.push({ name: 'Summoner', params: { gameName, tagLine } })
  searchInput.value = ''
}

function selectHistory(item) {
  saveToHistory(item.gameName, item.tagLine)
  showHistory.value = false
  router.push({ name: 'Summoner', params: { gameName: item.gameName, tagLine: item.tagLine } })
  searchInput.value = ''
}

function onClickOutside(e) {
  if (wrapper.value && !wrapper.value.contains(e.target)) {
    showHistory.value = false
  }
}

onMounted(() => {
  loadHistory()
  document.addEventListener('mousedown', onClickOutside)
})
onUnmounted(() => {
  document.removeEventListener('mousedown', onClickOutside)
})
</script>

<template>
  <div ref="wrapper" class="relative w-full" :class="compact ? 'max-w-md' : 'max-w-xl'">
    <form @submit.prevent="handleSearch" class="flex w-full">
      <input
        v-model="searchInput"
        type="text"
        placeholder="소환사명#태그 (예: Hide on bush#KR1)"
        class="flex-1 bg-bg-secondary text-text-primary rounded-l-lg px-4 outline-none placeholder:text-text-muted border border-bg-tertiary border-r-0 focus:border-win transition-colors"
        :class="compact ? 'py-2 text-sm' : 'py-3 text-base'"
        @focus="showHistory = true"
      />
      <button
        type="submit"
        class="bg-win hover:bg-win/80 text-white font-semibold rounded-r-lg px-6 transition-colors cursor-pointer"
        :class="compact ? 'py-2 text-sm' : 'py-3 text-base'"
      >
        .GG
      </button>
    </form>

    <div
      v-if="showHistory && history.length > 0"
      class="absolute top-full left-0 right-0 mt-1 bg-bg-secondary border border-bg-tertiary rounded-lg overflow-hidden z-50 shadow-lg"
    >
      <div
        v-for="(item, index) in history"
        :key="`${item.gameName}#${item.tagLine}`"
        class="flex items-center justify-between px-4 py-2.5 hover:bg-bg-tertiary cursor-pointer transition-colors"
        @mousedown.prevent="selectHistory(item)"
      >
        <span class="text-sm text-text-secondary">
          <span class="text-text-primary font-medium">{{ item.gameName }}</span>
          <span class="text-text-muted">#{{ item.tagLine }}</span>
        </span>
        <button
          class="text-text-muted hover:text-lose text-xs p-1 cursor-pointer"
          @mousedown.prevent.stop="removeHistory(index)"
        >
          &times;
        </button>
      </div>
    </div>
  </div>
</template>
