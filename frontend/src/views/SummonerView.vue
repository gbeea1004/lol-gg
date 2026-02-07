<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { searchSummoner } from '../api'
import SearchBar from '../components/SearchBar.vue'
import SummonerProfile from '../components/SummonerProfile.vue'
import RankInfo from '../components/RankInfo.vue'
import MatchList from '../components/MatchList.vue'

const route = useRoute()
const summoner = ref(null)
const loading = ref(false)
const error = ref(null)

async function fetchSummoner() {
  const { gameName, tagLine } = route.params
  loading.value = true
  error.value = null
  summoner.value = null
  try {
    const res = await searchSummoner(gameName, tagLine)
    summoner.value = res.data
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

onMounted(fetchSummoner)
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
          <RankInfo :leagues="summoner.leagues" />
        </div>

        <!-- Main Content -->
        <div class="flex-1 min-w-0">
          <MatchList :puuid="summoner.puuid" />
        </div>
      </div>
    </div>
  </div>
</template>
