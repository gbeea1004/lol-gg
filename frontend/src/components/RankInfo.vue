<script setup>
const props = defineProps({
  leagues: { type: Array, required: true },
})

const QUEUE_NAMES = {
  RANKED_SOLO_5x5: '솔로 랭크',
  RANKED_FLEX_SR: '자유 랭크',
}

function tierEmblem(tier) {
  return `https://raw.communitydragon.org/latest/plugins/rcp-fe-lol-static-assets/global/default/images/ranked-emblem/emblem-${tier.toLowerCase()}.png`
}

function winRate(wins, losses) {
  const total = wins + losses
  if (total === 0) return 0
  return Math.round((wins / total) * 100)
}

const RANK_NUMBER = { I: '1', II: '2', III: '3', IV: '4' }

function tierDisplay(tier) {
  return tier.charAt(0) + tier.slice(1).toLowerCase()
}

function rankDisplay(rank) {
  return RANK_NUMBER[rank] || rank
}
</script>

<template>
  <div class="space-y-3">
    <div
      v-for="league in leagues"
      :key="league.queueType"
      class="bg-bg-secondary rounded-lg p-4"
    >
      <div class="text-text-secondary text-xs mb-3 font-semibold uppercase tracking-wide">
        {{ QUEUE_NAMES[league.queueType] || league.queueType }}
      </div>
      <div class="flex items-center gap-2 overflow-hidden">
        <div class="w-24 h-24 shrink-0 overflow-hidden flex items-center justify-center">
          <img
            :src="tierEmblem(league.tier)"
            :alt="league.tier"
            class="w-44 max-w-none scale-125"
          />
        </div>
        <div class="min-w-0">
          <div class="flex items-start gap-4">
            <div>
              <div class="font-bold text-xl">
                {{ tierDisplay(league.tier) }} {{ rankDisplay(league.rank) }}
              </div>
              <div class="text-text-secondary text-sm">
                {{ league.leaguePoints }} LP
              </div>
            </div>
            <div class="text-text-secondary text-sm">
              <div>{{ league.wins }}승 {{ league.losses }}패</div>
              <div
                class="font-semibold"
                :class="winRate(league.wins, league.losses) >= 50 ? 'text-win' : 'text-lose'"
              >
                승률 {{ winRate(league.wins, league.losses) }}%
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="leagues.length === 0" class="bg-bg-secondary rounded-lg p-4 text-center text-text-muted">
      랭크 정보 없음
    </div>
  </div>
</template>
