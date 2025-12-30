import request from './request'
import type { Task, TaskSummary, TaskReward, ProgressRequest } from '@/types/task'

export const getTaskSummary = () => {
  return request<TaskSummary>({
    url: '/api/tasks/summary',
    method: 'GET'
  })
}

export const updateTaskProgress = (userTaskId: string | number, progressDelta: number) => {
  return request<Task>({
    url: `/api/tasks/${userTaskId}/progress`,
    method: 'POST',
    data: {
      taskId: userTaskId,
      progressDelta
    }
  })
}

export const claimTaskReward = (userTaskId: string | number) => {
  return request<Task>({
    url: `/api/tasks/${userTaskId}/claim`,
    method: 'POST'
  })
}

export const getUserRewards = () => {
  return request<TaskReward[]>({
    url: '/api/tasks/rewards',
    method: 'GET'
  })
}
