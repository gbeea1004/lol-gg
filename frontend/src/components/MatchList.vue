<script setup>
import { ref, onMounted, watch } from 'vue'
import { getMatches, getTiers } from '../api'
import MatchCard from './MatchCard.vue'

const PAGE_SIZE = 20

const tabs = [
  { label: '전체', filter: {} },
  { label: '솔로랭크', filter: { queue: 420 } },
  { label: '자유랭크', filter: { queue: 440 } },
  { label: '일반', filter: { type: 'normal' } },
]

const props = defineProps({
  puuid: { type: String, required: true },
  refreshKey: { type: Number, default: 0 },
})

const matches = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const error = ref(null)
const hasMore = ref(true)
const tierMap = ref({})
const activeTab = ref(0)
let fetchId = 0

function cacheKey() {
  const filterStr = JSON.stringify(tabs[activeTab.value].filter)
  return `lolgg_matches_${props.puuid}_${filterStr}`
}

function saveToCache() {
  try {
    sessionStorage.setItem(cacheKey(), JSON.stringify({
      matches: matches.value,
      tierMap: tierMap.value,
      hasMore: hasMore.value,
    }))
  } catch { /* sessionStorage full, ignore */ }
}

function loadFromCache() {
  try {
    const cached = sessionStorage.getItem(cacheKey())
    if (!cached) return false
    const data = JSON.parse(cached)
    matches.value = data.matches
    tierMap.value = data.tierMap || {}
    hasMore.value = data.hasMore
    return true
  } catch { return false }
}

function collectPuuids(matchList) {
  const set = new Set()
  for (const m of matchList) {
    for (const p of m.participants) {
      set.add(p.puuid)
    }
  }
  return [...set]
}

async function prefetchTiers(matchList) {
  const puuids = collectPuuids(matchList).filter(id => !tierMap.value[id])
  if (puuids.length === 0) return
  try {
    const res = await getTiers(puuids)
    tierMap.value = { ...tierMap.value, ...res.data.tiers }
    saveToCache()
  } catch (e) {
    // silently fail
  }
}

async function fetchMatches(force = false) {
  if (!force && loadFromCache()) return

  const currentFetchId = ++fetchId
  loading.value = true
  error.value = null
  matches.value = []
  tierMap.value = {}
  hasMore.value = true
  try {
    const res = await getMatches(props.puuid, 0, PAGE_SIZE, tabs[activeTab.value].filter)
    if (currentFetchId !== fetchId) return // stale response
    matches.value = res.data
    if (res.data.length < PAGE_SIZE) hasMore.value = false
    saveToCache()
    // Start prefetching tiers in background
    prefetchTiers(res.data)
  } catch (e) {
    if (currentFetchId !== fetchId) return
    error.value = e.response?.data?.error || '매치 정보를 불러올 수 없습니다.'
  } finally {
    if (currentFetchId === fetchId) loading.value = false
  }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  const currentFetchId = fetchId
  loadingMore.value = true
  try {
    const res = await getMatches(props.puuid, matches.value.length, PAGE_SIZE, tabs[activeTab.value].filter)
    if (currentFetchId !== fetchId) return // tab changed during load
    matches.value.push(...res.data)
    if (res.data.length < PAGE_SIZE) hasMore.value = false
    saveToCache()
    // Prefetch tiers for new matches
    prefetchTiers(res.data)
  } catch (e) {
    // silently fail, user can retry
  } finally {
    if (currentFetchId === fetchId) loadingMore.value = false
  }
}

function selectTab(index) {
  if (activeTab.value === index) return
  activeTab.value = index
  fetchMatches(false)
}

onMounted(() => fetchMatches(false))
watch(() => props.puuid, () => fetchMatches(false))
watch(() => props.refreshKey, (newVal, oldVal) => {
  if (newVal > oldVal) fetchMatches(true)
})
</script>

<template>
  <div>
    <h3 class="text-lg font-semibold mb-3">최근 전적</h3>

    <div class="flex gap-1 mb-3">
      <button
        v-for="(tab, index) in tabs"
        :key="tab.label"
        @click="selectTab(index)"
        class="px-3 py-1.5 rounded text-sm font-medium transition-colors cursor-pointer"
        :class="activeTab === index
          ? 'bg-[#5383e8] text-white'
          : 'bg-bg-secondary text-text-secondary hover:bg-bg-tertiary'"
      >
        {{ tab.label }}
      </button>
    </div>

    <div v-if="loading" class="text-center py-10 text-text-muted">
      <div class="inline-block w-6 h-6 border-2 border-win border-t-transparent rounded-full animate-spin" />
      <p class="mt-2">매치 정보를 불러오는 중...</p>
    </div>

    <div v-else-if="error" class="bg-lose-bg rounded-lg p-4 text-center text-lose">
      {{ error }}
    </div>

    <div v-else-if="matches.length === 0" class="bg-bg-secondary rounded-lg p-8 text-center text-text-muted">
      최근 매치 기록이 없습니다.
    </div>

    <template v-else>
      <div class="space-y-2">
        <MatchCard v-for="match in matches" :key="match.matchId" :match="match" :tier-map="tierMap" />
      </div>

      <div v-if="hasMore" class="mt-3">
        <button
          @click="loadMore"
          :disabled="loadingMore"
          class="w-full py-3 bg-bg-secondary hover:bg-bg-tertiary rounded-lg text-text-secondary text-sm font-semibold transition-colors cursor-pointer disabled:opacity-50"
        >
          <template v-if="loadingMore">
            <span class="inline-block w-4 h-4 border-2 border-win border-t-transparent rounded-full animate-spin align-middle mr-2" />
            불러오는 중...
          </template>
          <template v-else>
            더보기
          </template>
        </button>
      </div>
    </template>
  </div>
</template>
