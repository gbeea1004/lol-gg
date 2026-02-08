import { ref } from 'vue'

const HISTORY_KEY = 'lolgg_search_history'
const MAX_HISTORY = 10

export function useSearchHistory() {
  const history = ref([])

  function load() {
    try {
      history.value = JSON.parse(localStorage.getItem(HISTORY_KEY) || '[]')
    } catch { history.value = [] }
  }

  function save(gameName, tagLine) {
    try {
      const list = JSON.parse(localStorage.getItem(HISTORY_KEY) || '[]')
      const filtered = list.filter(h => !(h.gameName === gameName && h.tagLine === tagLine))
      filtered.unshift({ gameName, tagLine })
      const result = filtered.slice(0, MAX_HISTORY)
      localStorage.setItem(HISTORY_KEY, JSON.stringify(result))
      history.value = result
    } catch { /* ignore */ }
  }

  function remove(index) {
    history.value.splice(index, 1)
    localStorage.setItem(HISTORY_KEY, JSON.stringify(history.value))
  }

  return { history, load, save, remove }
}
