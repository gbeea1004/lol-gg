import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
})

export function searchSummoner(gameName, tagLine) {
  return api.get('/summoner', { params: { gameName, tagLine } })
}

export function getMatches(puuid, start = 0, count = 20, filter = {}) {
  return api.get(`/matches/${puuid}`, { params: { start, count, ...filter } })
}

export function getTiers(puuids) {
  return api.post('/tiers', puuids)
}
