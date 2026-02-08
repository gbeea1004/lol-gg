<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { searchSummoner } from '../api'
import SearchBar from '../components/SearchBar.vue'
import SummonerProfile from '../components/SummonerProfile.vue'
import RankInfo from '../components/RankInfo.vue'
import MatchList from '../components/MatchList.vue'

const COOLDOWN_SECONDS = 120

const route = useRoute()
const summoner = ref(null)
const loading = ref(false)
const error = ref(null)
const refreshing = ref(false)
const refreshKey = ref(0)
const cooldown = ref(0)
let cooldownTimer = null

const SUMMONER_CACHE_KEY = 'lolgg_summoner'

function getCachedSummoner() {
  try {
    const cached = sessionStorage.getItem(SUMMONER_CACHE_KEY)
    if (!cached) return null
    const data = JSON.parse(cached)
    const { gameName, tagLine } = route.params
    if (data.gameName === gameName && data.tagLine === tagLine) {
      return data.summoner
    }
  } catch { /* ignore */ }
  return null
}

function cacheSummoner(data) {
  const { gameName, tagLine } = route.params
  sessionStorage.setItem(SUMMONER_CACHE_KEY, JSON.stringify({ gameName, tagLine, summoner: data }))
}

function clearMatchCaches(puuid) {
  const keysToRemove = []
  for (let i = 0; i < sessionStorage.length; i++) {
    const key = sessionStorage.key(i)
    if (key?.startsWith(`lolgg_matches_${puuid}_`)) {
      keysToRemove.push(key)
    }
  }
  keysToRemove.forEach(k => sessionStorage.removeItem(k))
}

async function fetchSummoner() {
  const { gameName, tagLine } = route.params
  error.value = null

  const cached = getCachedSummoner()
  if (cached) {
    summoner.value = cached
    return
  }

  loading.value = true
  summoner.value = null
  try {
    const res = await searchSummoner(gameName, tagLine)
    summoner.value = res.data
    cacheSummoner(res.data)
  } catch (e) {
    if (e.response?.status === 404) {
      error.value = '소환사를 찾을 수 없습니다.'
    } else if (e.response?.status === 429) {
      error.value = 'API 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.'
    } else {
      error.value = e.response?.data?.error || '오류가 발생했습니다.'
    }
  } finally {
    loading.value = false
  }
}

async function refresh() {
  if (refreshing.value || cooldown.value > 0) return
  refreshing.value = true
  try {
    const res = await searchSummoner(route.params.gameName, route.params.tagLine)
    summoner.value = res.data
    cacheSummoner(res.data)
    if (summoner.value?.puuid) {
      clearMatchCaches(summoner.value.puuid)
    }
    refreshKey.value++
  } catch {
    // silently fail
  } finally {
    refreshing.value = false
    startCooldown()
  }
}

const COOLDOWN_KEY = 'lolgg_refresh_until'

function startCooldown(remaining = COOLDOWN_SECONDS) {
  clearInterval(cooldownTimer)
  if (remaining === COOLDOWN_SECONDS) {
    localStorage.setItem(COOLDOWN_KEY, Date.now() + COOLDOWN_SECONDS * 1000)
  }
  cooldown.value = remaining
  cooldownTimer = setInterval(() => {
    cooldown.value--
    if (cooldown.value <= 0) {
      cooldown.value = 0
      localStorage.removeItem(COOLDOWN_KEY)
      clearInterval(cooldownTimer)
    }
  }, 1000)
}

function restoreCooldown() {
  const until = parseInt(localStorage.getItem(COOLDOWN_KEY))
  if (!until) return
  const remaining = Math.ceil((until - Date.now()) / 1000)
  if (remaining > 0) {
    startCooldown(remaining)
  } else {
    localStorage.removeItem(COOLDOWN_KEY)
  }
}

function formatCooldown(seconds) {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  if (m > 0) return `${m}분 ${s}초 후`
  return `${s}초 후`
}

onMounted(() => {
  fetchSummoner()
  restoreCooldown()
})
onUnmounted(() => clearInterval(cooldownTimer))
watch(() => route.params, fetchSummoner)
</script>

<template>
  <div class="min-h-screen">
    <!-- Header -->
    <header class="bg-bg-secondary border-b border-bg-tertiary py-3 px-6">
      <div class="max-w-6xl mx-auto flex items-center gap-6">
        <router-link to="/" class="text-xl font-bold shrink-0 no-underline">
          <span class="text-win">LoL</span><span class="text-text-secondary">.GG</span>
        </router-link>
        <SearchBar compact />
      </div>
    </header>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="text-center text-text-muted">
        <div class="inline-block w-8 h-8 border-2 border-win border-t-transparent rounded-full animate-spin" />
        <p class="mt-3">소환사 정보를 불러오는 중...</p>
      </div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="max-w-xl mx-auto mt-20 text-center">
      <div class="text-6xl mb-4">:(</div>
      <p class="text-xl text-lose mb-2">{{ error }}</p>
      <router-link to="/" class="text-win hover:underline">돌아가기</router-link>
    </div>

    <!-- Content -->
    <div v-else-if="summoner" class="max-w-6xl mx-auto px-4 py-6">
      <div class="flex flex-col lg:flex-row gap-6">
        <!-- Left Sidebar -->
        <div class="lg:w-80 shrink-0 space-y-4">
          <SummonerProfile :summoner="summoner" />
          <button
            @click="refresh"
            :disabled="refreshing || cooldown > 0"
            class="w-full py-2.5 rounded-lg text-sm font-semibold transition-colors cursor-pointer disabled:cursor-default"
            :class="refreshing || cooldown > 0
              ? 'bg-bg-tertiary text-text-muted'
              : 'bg-win text-white hover:bg-win/90'"
          >
            <template v-if="refreshing">
              <span class="inline-block w-4 h-4 border-2 border-white/50 border-t-white rounded-full animate-spin align-middle mr-1.5" />
              갱신 중
            </template>
            <template v-else>전적 갱신</template>
          </button>
          <p v-if="cooldown > 0" class="text-xs text-text-muted text-center -mt-2">
            갱신 가능 시간: {{ formatCooldown(cooldown) }}
          </p>
          <RankInfo :leagues="summoner.leagues" />
        </div>

        <!-- Main Content -->
        <div class="flex-1 min-w-0">
          <MatchList :puuid="summoner.puuid" :refresh-key="refreshKey" />
        </div>
      </div>
    </div>
  </div>
</template>
