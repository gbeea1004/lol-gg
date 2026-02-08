<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  match: { type: Object, required: true },
  tierMap: { type: Object, default: () => ({}) },
})

const DDRAGON = 'https://ddragon.leagueoflegends.com/cdn/16.3.1'

const HISTORY_KEY = 'lolgg_search_history'
const MAX_HISTORY = 5

function saveToHistory(gameName, tagLine) {
  try {
    const history = JSON.parse(localStorage.getItem(HISTORY_KEY) || '[]')
    const filtered = history.filter(h => !(h.gameName === gameName && h.tagLine === tagLine))
    filtered.unshift({ gameName, tagLine })
    localStorage.setItem(HISTORY_KEY, JSON.stringify(filtered.slice(0, MAX_HISTORY)))
  } catch { /* ignore */ }
}

const QUEUE_NAMES = {
  420: '솔로 랭크',
  440: '자유 랭크',
  450: '칼바람 나락',
  480: '일반',
  490: '일반',
  400: '일반',
  900: 'URF',
  1700: '아레나',
  1900: 'Pick URF',
}

const expanded = ref(false)

const TIER_ORDER = {
  'Iron': 1, 'Bronze': 2, 'Silver': 3, 'Gold': 4,
  'Platinum': 5, 'Emerald': 6, 'Diamond': 7,
  'Master': 8, 'Grandmaster': 9, 'Challenger': 10,
}

const TIER_COLORS = {
  'Iron': '#8b7a6b',
  'Bronze': '#b08d57',
  'Silver': '#80989d',
  'Gold': '#cd8837',
  'Platinum': '#4e9996',
  'Emerald': '#0c9644',
  'Diamond': '#576ace',
  'Master': '#9d48e0',
  'Grandmaster': '#e44040',
  'Challenger': '#f4c874',
}

function tierColor(tierStr) {
  if (!tierStr) return null
  const name = tierStr.split(' ')[0]
  return TIER_COLORS[name] || null
}

const averageTier = computed(() => {
  let total = 0, count = 0
  for (const pp of props.match.participants) {
    const tier = props.tierMap[pp.puuid]
    if (!tier) continue
    const parts = tier.split(' ')
    const tierVal = TIER_ORDER[parts[0]]
    if (!tierVal) continue
    const rankVal = parts[1] ? (parseInt(parts[1]) - 1) : 3
    total += tierVal * 4 + rankVal
    count++
  }
  if (count === 0) return null
  const avg = Math.round(total / count)
  const tierIdx = Math.max(1, Math.min(Math.floor(avg / 4), 10))
  const rankIdx = avg % 4
  const tierName = Object.entries(TIER_ORDER).find(([, v]) => v === tierIdx)?.[0] || 'Gold'
  if (tierIdx >= 8) return tierName
  return `${tierName} ${rankIdx + 1}`
})

function formatDuration(seconds) {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}분 ${String(s).padStart(2, '0')}초`
}

function formatDurationShort(seconds) {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${String(s).padStart(2, '0')}`
}

function formatTimeAgo(timestamp) {
  const diff = Date.now() - timestamp
  const minutes = Math.floor(diff / 60000)
  if (minutes < 60) return `${minutes}분 전`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}시간 전`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}일 전`
  const months = Math.floor(days / 30)
  if (months < 12) return `${months}개월 전`
  const years = Math.floor(months / 12)
  return `${years}년 전`
}

function kdaRatio(k, d, a) {
  if (d === 0) return 'Perfect'
  return ((k + a) / d).toFixed(2)
}

function kdaClass(k, d, a) {
  if (d === 0) return 'text-gold'
  const ratio = (k + a) / d
  if (ratio >= 5) return 'text-gold'
  if (ratio >= 3) return 'text-teal'
  if (ratio >= 2) return 'text-text-primary'
  return 'text-lose'
}

function formatNumber(n) {
  return n.toLocaleString()
}

function csPerMin(cs, duration) {
  if (duration === 0) return '0.0'
  return (cs / (duration / 60)).toFixed(1)
}

function killParticipation(player, team) {
  const totalKills = team.reduce((sum, p) => sum + p.kills, 0)
  if (totalKills === 0) return 0
  return Math.round(((player.kills + player.assists) / totalKills) * 100)
}

function getItems(player) {
  return [player.item0, player.item1, player.item2, player.item3, player.item4, player.item5, player.item6]
}

const p = props.match.currentPlayer
const items = p ? getItems(p) : []

const blueTeam = computed(() => props.match.participants.filter(pp => pp.teamId === 100))
const redTeam = computed(() => props.match.participants.filter(pp => pp.teamId === 200))

const blueTeamKills = computed(() => blueTeam.value.reduce((s, p) => s + p.kills, 0))
const redTeamKills = computed(() => redTeam.value.reduce((s, p) => s + p.kills, 0))
const blueTeamGold = computed(() => blueTeam.value.reduce((s, p) => s + p.goldEarned, 0))
const redTeamGold = computed(() => redTeam.value.reduce((s, p) => s + p.goldEarned, 0))

const blueWin = computed(() => blueTeam.value[0]?.win)

const currentInParticipants = computed(() =>
  props.match.participants.find(pp => pp.puuid === p?.puuid)
)

const laneOpponent = computed(() => {
  const me = currentInParticipants.value
  if (!me || !me.teamPosition) return null
  return props.match.participants.find(
    pp => pp.teamId !== me.teamId && pp.teamPosition === me.teamPosition
  )
})

const laneScore = computed(() => {
  const me = currentInParticipants.value
  const opp = laneOpponent.value
  if (!me || !opp) return null
  const myCs = me.laneMinionsFirst10Minutes || 0
  const oppCs = opp.laneMinionsFirst10Minutes || 0
  const total = myCs + oppCs
  if (total === 0) return null
  const my = Math.round((myCs / total) * 100)
  return { my, opp: 100 - my }
})
</script>

<template>
  <div v-if="p" class="rounded-lg overflow-hidden">
    <!-- Summary Row -->
    <div
      class="flex items-center gap-3 py-3 pl-3 pr-8 border-l-4 cursor-pointer"
      :class="p.win ? 'bg-win-bg border-win' : 'bg-lose-bg border-lose'"
      @click="expanded = !expanded"
    >
      <!-- Game Info -->
      <div class="w-16 shrink-0 text-center">
        <div class="text-xs font-semibold" :class="p.win ? 'text-win' : 'text-lose'">
          {{ p.win ? '승리' : '패배' }}
        </div>
        <div class="text-text-muted text-xs mt-0.5">
          {{ QUEUE_NAMES[match.queueId] || match.gameMode }}
        </div>
        <div class="text-text-muted text-xs mt-0.5">
          {{ formatDurationShort(match.gameDuration) }}
        </div>
        <div class="text-text-muted text-xs mt-0.5">
          {{ formatTimeAgo(match.gameCreation) }}
        </div>
      </div>

      <!-- Champion -->
      <div class="shrink-0">
        <div class="relative">
          <img
            :src="`${DDRAGON}/img/champion/${p.championName}.png`"
            :alt="p.championName"
            class="w-12 h-12 rounded-full"
          />
          <span class="absolute -bottom-0.5 -right-0.5 bg-bg-tertiary text-[10px] w-5 h-5 flex items-center justify-center rounded-full text-text-secondary font-semibold">
            {{ p.champLevel }}
          </span>
        </div>
      </div>

      <!-- KDA -->
      <div class="w-24 shrink-0 text-center">
        <div class="text-sm font-semibold">
          <span class="text-text-primary">{{ p.kills }}</span>
          <span class="text-text-muted"> / </span>
          <span class="text-lose">{{ p.deaths }}</span>
          <span class="text-text-muted"> / </span>
          <span class="text-text-primary">{{ p.assists }}</span>
        </div>
        <div class="text-xs mt-0.5" :class="kdaClass(p.kills, p.deaths, p.assists)">
          {{ kdaRatio(p.kills, p.deaths, p.assists) }}
          <span v-if="p.deaths > 0"> KDA</span>
        </div>
      </div>

      <!-- Stats -->
      <div class="w-28 shrink-0 text-center text-xs text-text-secondary space-y-0.5">
        <div v-if="laneScore">
          라인전
          <span class="text-lose font-semibold">{{ laneScore.my }}</span>
          <span class="text-text-muted"> : </span>
          <span class="text-text-primary">{{ laneScore.opp }}</span>
        </div>
        <div>킬관여 {{ Math.round((p.killParticipation || 0) * 100) }}%</div>
        <div>CS {{ p.cs }} ({{ csPerMin(p.cs, match.gameDuration) }})</div>
        <div v-if="averageTier" class="font-semibold" :style="{ color: tierColor(averageTier) }">{{ averageTier }}</div>
      </div>

      <!-- Items -->
      <div class="flex gap-0.5 shrink-0">
        <template v-for="(itemId, idx) in items" :key="idx">
          <img
            v-if="itemId > 0"
            :src="`${DDRAGON}/img/item/${itemId}.png`"
            class="w-7 h-7 rounded"
            :class="idx === 6 ? 'rounded-full' : ''"
          />
          <div v-else class="w-7 h-7 rounded bg-bg-tertiary" :class="idx === 6 ? 'rounded-full' : ''" />
        </template>
      </div>

      <!-- Participants -->
      <div class="gap-2 ml-auto shrink-0 hidden lg:flex">
        <div class="space-y-0.5">
          <div
            v-for="participant in blueTeam.slice(0, 5)"
            :key="participant.puuid"
            class="flex items-center gap-1"
          >
            <img :src="`${DDRAGON}/img/champion/${participant.championName}.png`" class="w-4 h-4 rounded-sm" />
            <router-link
              :to="`/summoner/${participant.gameName}/${participant.tagLine}`"
              target="_blank"
              class="text-[10px] w-16 truncate no-underline hover:underline"
              :class="participant.puuid === p.puuid ? 'text-text-primary font-semibold' : 'text-text-muted'"
              @click.stop="saveToHistory(participant.gameName, participant.tagLine)"
            >
              {{ participant.gameName }}
            </router-link>
          </div>
        </div>
        <div class="space-y-0.5">
          <div
            v-for="participant in redTeam.slice(0, 5)"
            :key="participant.puuid"
            class="flex items-center gap-1"
          >
            <img :src="`${DDRAGON}/img/champion/${participant.championName}.png`" class="w-4 h-4 rounded-sm" />
            <router-link
              :to="`/summoner/${participant.gameName}/${participant.tagLine}`"
              target="_blank"
              class="text-[10px] w-16 truncate no-underline hover:underline"
              :class="participant.puuid === p.puuid ? 'text-text-primary font-semibold' : 'text-text-muted'"
              @click.stop="saveToHistory(participant.gameName, participant.tagLine)"
            >
              {{ participant.gameName }}
            </router-link>
          </div>
        </div>
      </div>

      <!-- Expand Arrow -->
      <div class="ml-auto shrink-0 text-text-muted xl:ml-2">
        <svg class="w-4 h-4 transition-transform" :class="expanded ? 'rotate-180' : ''" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z" clip-rule="evenodd" />
        </svg>
      </div>
    </div>

    <!-- Expanded Detail -->
    <div v-if="expanded" class="bg-bg-secondary">
      <!-- Team Tables -->
      <template v-for="(team, teamIdx) in [blueTeam, redTeam]" :key="teamIdx">
        <div class="px-3 py-2 text-xs font-semibold border-b border-bg-tertiary flex items-center gap-2"
          :class="teamIdx === 0
            ? (blueWin ? 'text-win' : 'text-lose')
            : (blueWin ? 'text-lose' : 'text-win')"
        >
          <span>{{ (teamIdx === 0 ? blueWin : !blueWin) ? '승리' : '패배' }}</span>
          <span class="text-text-muted">({{ teamIdx === 0 ? '블루팀' : '레드팀' }})</span>
        </div>

        <!-- Header -->
        <div class="grid grid-cols-[1fr_70px_90px_60px_60px_180px] gap-1 px-3 py-1.5 text-[10px] text-text-muted border-b border-bg-tertiary">
          <span>소환사</span>
          <span class="text-center">KDA</span>
          <span class="text-center">피해량</span>
          <span class="text-center">와드</span>
          <span class="text-center">CS</span>
          <span class="text-center">아이템</span>
        </div>

        <!-- Player Rows -->
        <div
          v-for="player in team"
          :key="player.puuid"
          class="grid grid-cols-[1fr_70px_90px_60px_60px_180px] gap-1 px-3 py-1.5 items-center border-b border-bg-tertiary text-xs"
          :class="player.puuid === p.puuid ? 'bg-bg-tertiary/50' : ''"
        >
          <!-- Summoner -->
          <div class="flex items-center gap-2 min-w-0">
            <div class="relative shrink-0">
              <img :src="`${DDRAGON}/img/champion/${player.championName}.png`" class="w-8 h-8 rounded-full" />
              <span class="absolute -bottom-0.5 -right-0.5 bg-bg-tertiary text-[9px] w-4 h-4 flex items-center justify-center rounded-full text-text-muted">
                {{ player.champLevel }}
              </span>
            </div>
            <div class="min-w-0">
              <router-link
                :to="`/summoner/${player.gameName}/${player.tagLine}`"
                target="_blank"
                class="block truncate font-semibold text-xs no-underline hover:underline"
                :class="player.puuid === p.puuid ? 'text-text-primary' : 'text-text-secondary'"
                @click.stop="saveToHistory(player.gameName, player.tagLine)"
              >
                {{ player.gameName }}
              </router-link>
              <div v-if="tierMap[player.puuid]" class="text-[10px] font-semibold" :style="{ color: tierColor(tierMap[player.puuid]) }">{{ tierMap[player.puuid] }}</div>
            </div>
          </div>

          <!-- KDA -->
          <div class="text-center">
            <div class="text-text-primary">{{ player.kills }}/{{ player.deaths }}/{{ player.assists }}</div>
            <div :class="kdaClass(player.kills, player.deaths, player.assists)">
              {{ kdaRatio(player.kills, player.deaths, player.assists) }}
            </div>
          </div>

          <!-- Damage -->
          <div class="text-center">
            <div class="text-text-secondary">{{ formatNumber(player.totalDamageDealtToChampions) }}</div>
            <div class="mt-0.5 h-1 bg-bg-tertiary rounded-full overflow-hidden mx-1">
              <div
                class="h-full rounded-full"
                :class="player.win ? 'bg-win' : 'bg-lose'"
                :style="{ width: Math.min(100, player.totalDamageDealtToChampions / 400) + '%' }"
              />
            </div>
          </div>

          <!-- Wards -->
          <div class="text-center text-text-secondary">
            {{ player.visionScore }}
          </div>

          <!-- CS -->
          <div class="text-center text-text-secondary">
            <div>{{ player.cs }}</div>
            <div class="text-text-muted">{{ csPerMin(player.cs, match.gameDuration) }}/m</div>
          </div>

          <!-- Items -->
          <div class="flex gap-0.5 justify-center">
            <template v-for="(itemId, idx) in getItems(player)" :key="idx">
              <img
                v-if="itemId > 0"
                :src="`${DDRAGON}/img/item/${itemId}.png`"
                class="w-6 h-6 rounded"
                :class="idx === 6 ? 'rounded-full' : ''"
              />
              <div v-else class="w-6 h-6 rounded bg-bg-tertiary" :class="idx === 6 ? 'rounded-full' : ''" />
            </template>
          </div>
        </div>
      </template>

      <!-- Team Totals -->
      <div class="grid grid-cols-2 text-xs font-semibold">
        <div class="px-3 py-2 text-center" :class="blueWin ? 'text-win' : 'text-lose'">
          Total Kill {{ blueTeamKills }} | Gold {{ formatNumber(blueTeamGold) }}
        </div>
        <div class="px-3 py-2 text-center" :class="blueWin ? 'text-lose' : 'text-win'">
          Total Kill {{ redTeamKills }} | Gold {{ formatNumber(redTeamGold) }}
        </div>
      </div>
    </div>
  </div>
</template>
