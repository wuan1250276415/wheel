import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Task, TaskSummary, TaskReward } from '@/types/task'
import * as taskApi from '@/api/task'

export const useTaskStore = defineStore('task', () => {
  const dailyTasks = ref<Task[]>([])
  const weeklyTasks = ref<Task[]>([])
  const rewards = ref<TaskReward[]>([])
  const totalCompleted = ref(0)
  const totalRewards = ref(0)
  const loading = ref(false)

  const allTasks = computed(() => [...dailyTasks.value, ...weeklyTasks.value])

  const completedTasks = computed(() =>
    allTasks.value.filter(t => t.status >= 1)
  )

  const pendingTasks = computed(() =>
    allTasks.value.filter(t => t.status === 0)
  )

  const updateTaskInList = (tasks: Task[], userTaskId: string | number, updatedTask: Task) => {
    const index = tasks.findIndex(t => t.userTaskId === userTaskId)
    if (index !== -1) {
      tasks[index] = updatedTask
    }
  }

  async function fetchTasks() {
    try {
      loading.value = true
      const res = await taskApi.getTaskSummary()
      dailyTasks.value = res.dailyTasks || []
      weeklyTasks.value = res.weeklyTasks || []
      totalCompleted.value = res.totalCompleted || 0
      totalRewards.value = res.totalRewards || 0
    } catch (error) {
      console.error('获取任务失败:', error)
      uni.showToast({
        title: '获取任务失败',
        icon: 'none'
      })
    } finally {
      loading.value = false
    }
  }

  async function updateProgress(userTaskId: string | number, delta: number) {
    try {
      loading.value = true
      const res = await taskApi.updateTaskProgress(userTaskId, delta)

      updateTaskInList(dailyTasks.value, userTaskId, res)
      updateTaskInList(weeklyTasks.value, userTaskId, res)

      uni.showToast({
        title: '进度已更新',
        icon: 'success'
      })
    } catch (error) {
      console.error('更新进度失败:', error)
      uni.showToast({
        title: '更新进度失败',
        icon: 'none'
      })
    } finally {
      loading.value = false
    }
  }

  async function claimReward(userTaskId: string | number) {
    try {
      loading.value = true
      const res = await taskApi.claimTaskReward(userTaskId)

      updateTaskInList(dailyTasks.value, userTaskId, res)
      updateTaskInList(weeklyTasks.value, userTaskId, res)
      totalRewards.value += 1

      uni.showToast({
        title: '奖励领取成功',
        icon: 'success'
      })

      await fetchRewards()
    } catch (error) {
      console.error('领取奖励失败:', error)
      uni.showToast({
        title: '领取奖励失败',
        icon: 'none'
      })
    } finally {
      loading.value = false
    }
  }

  async function fetchRewards() {
    try {
      const res = await taskApi.getUserRewards()
      rewards.value = res || []
    } catch (error) {
      console.error('获取奖励记录失败:', error)
    }
  }

  return {
    dailyTasks,
    weeklyTasks,
    rewards,
    totalCompleted,
    totalRewards,
    loading,
    allTasks,
    completedTasks,
    pendingTasks,
    fetchTasks,
    updateProgress,
    claimReward,
    fetchRewards
  }
})
