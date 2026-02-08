<script setup>
const props = defineProps({
  summoner: { type: Object, required: true },
})

const DDRAGON = 'https://ddragon.leagueoflegends.com/cdn/16.3.1'
</script>

<template>
  <div class="flex items-center gap-4">
    <div class="relative">
      <img
        :src="`${DDRAGON}/img/profileicon/${summoner.profileIconId}.png`"
        :alt="summoner.gameName"
        class="w-20 h-20 rounded-2xl border-2 border-bg-tertiary"
      />
      <span class="absolute -bottom-1 left-1/2 -translate-x-1/2 bg-bg-tertiary text-xs px-2 py-0.5 rounded-full text-text-secondary font-semibold">
        {{ summoner.summonerLevel }}
      </span>
    </div>
    <div>
      <h2 class="text-2xl font-bold">
        {{ summoner.gameName }}
        <span class="text-text-muted text-lg font-normal">#{{ summoner.tagLine }}</span>
      </h2>
    </div>
  </div>
</template>
