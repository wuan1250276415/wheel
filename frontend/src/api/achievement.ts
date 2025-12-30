import request from './request'
import type { Achievement } from '@/types/achievement'

export const getUserAchievements = () => {
  return request<Achievement[]>({
    url: '/api/achievements/list',
    method: 'GET'
  })
}
