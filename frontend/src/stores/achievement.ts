import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Achievement } from '@/types/achievement'
import * as achievementApi from '@/api/achievement'

export const useAchievementStore = defineStore('achievement', () => {
  const achievements = ref<Achievement[]>([])
  const loading = ref(false)

  async function fetchAchievements() {
    try {
      loading.value = true
      const res = await achievementApi.getUserAchievements()
      achievements.value = res || []
    } catch (error) {
      console.error('获取成就失败:', error)
      uni.showToast({
        title: '获取成就失败',
        icon: 'none'
      })
    } finally {
      loading.value = false
    }
  }

  return {
    achievements,
    loading,
    fetchAchievements
  }
})
