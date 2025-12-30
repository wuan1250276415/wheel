export interface Achievement {
  id: string | number
  achievementCode: string
  achievementName: string
  description: string
  iconUrl: string
  rarity: number
  rarityDesc: string
  category: number
  categoryDesc: string
  isHidden: boolean
  progress: number
  targetValue: number
  isUnlocked: boolean
  unlockedAt: string
  progressPercent: number
}
