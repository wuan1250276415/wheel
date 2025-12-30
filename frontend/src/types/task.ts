export interface Task {
  id: string | number
  taskCode: string
  taskName: string
  taskType: number
  taskTypeDesc: string
  description: string
  difficulty: number
  difficultyDesc: string
  periodType: number
  periodTypeDesc: string
  targetCount: number
  rewardType: number
  rewardTypeDesc: string
  rewardValue: number
  userTaskId: string | number
  currentCount: number
  status: number
  statusDesc: string
  progress: number
  periodStart: string
  periodEnd: string
}

export interface TaskSummary {
  dailyTasks: Task[]
  weeklyTasks: Task[]
  totalCompleted: number
  totalRewards: number
}

export interface TaskReward {
  id: string | number
  userId: number
  taskId: string | number
  taskName: string
  rewardType: number
  rewardTypeDesc: string
  rewardValue: number
  claimedAt: string
}

export interface ProgressRequest {
  taskId: string | number
  progressDelta: number
}
