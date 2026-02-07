import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import SummonerView from '../views/SummonerView.vue'

const routes = [
  { path: '/', name: 'Home', component: HomeView },
  { path: '/summoner/:gameName/:tagLine', name: 'Summoner', component: SummonerView },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
