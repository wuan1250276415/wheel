import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Moment, MomentCreateRequest, Comment, CommentCreateRequest, PageResponse } from '@/types/moment'
import * as momentApi from '@/api/moment'

export const useMomentStore = defineStore('moment', () => {
  // 状态
  const moments = ref<Moment[]>([])
  const currentMoment = ref<Moment | null>(null)
  const comments = ref<Comment[]>([])
  const isLoadingTimeline = ref(false)
  const isCreating = ref(false)
  const isUpdating = ref(false)
  const isDeleting = ref(false)
  const hasMore = ref(true)
  const currentPage = ref(1)
  const pageSize = ref(10)

  // 计算属性
  const momentCount = computed(() => moments.value.length)

  /**
   * 加载动态时间线
   */
  async function loadTimeline(refresh = false) {
    if (isLoadingTimeline.value) return

    if (refresh) {
      currentPage.value = 1
      moments.value = []
      hasMore.value = true
    }

    if (!hasMore.value) return

    try {
      isLoadingTimeline.value = true
      const response = await momentApi.getMomentTimeline(currentPage.value, pageSize.value)

      if (response) {
        if (refresh) {
          moments.value = response.records
        } else {
          moments.value.push(...response.records)
        }

        hasMore.value = currentPage.value < response.pages
        if (hasMore.value) {
          currentPage.value++
        }
      }
    } catch (error) {
      console.error('加载动态时间线失败:', error)
      uni.showToast({
        title: '加载失败',
        icon: 'none'
      })
    } finally {
      isLoadingTimeline.value = false
    }
  }

  /**
   * 创建动态
   */
  async function createMoment(data: MomentCreateRequest) {
    try {
      isCreating.value = true
      await momentApi.createMoment(data)

      uni.showToast({
        title: '发布成功',
        icon: 'success'
      })
      // 刷新时间线
      await loadTimeline(true)
      return true
    } catch (error) {
      console.error('创建动态失败:', error)
      uni.showToast({
        title: '发布失败',
        icon: 'none'
      })
      return false
    } finally {
      isCreating.value = false
    }
  }

  /**
   * 获取动态详情
   */
  async function getMomentDetail(momentId: string | number) {
    try {
      const response = await momentApi.getMomentDetail(momentId)

      if (response) {
        currentMoment.value = response
        return response
      }
      return null
    } catch (error) {
      console.error('获取动态详情失败:', error)
      uni.showToast({
        title: '加载失败',
        icon: 'none'
      })
      return null
    }
  }

  /**
   * 更新动态
   */
  async function updateMoment(momentId: string | number, data: MomentCreateRequest) {
    try {
      isUpdating.value = true
      await momentApi.updateMoment(momentId, data)

      uni.showToast({
        title: '更新成功',
        icon: 'success'
      })

      // 更新列表中的动态
      const moment = moments.value.find(m => m.id === momentId)
      if (moment) {
        if (data.content !== undefined) moment.content = data.content
        if (data.images !== undefined) moment.images = data.images
        if (data.visibility !== undefined) moment.visibility = data.visibility
      }

      // 更新当前动态详情
      if (currentMoment.value && currentMoment.value.id === momentId) {
        if (data.content !== undefined) currentMoment.value.content = data.content
        if (data.images !== undefined) currentMoment.value.images = data.images
        if (data.visibility !== undefined) currentMoment.value.visibility = data.visibility
      }

      return true
    } catch (error) {
      console.error('更新动态失败:', error)
      uni.showToast({
        title: '更新失败',
        icon: 'none'
      })
      return false
    } finally {
      isUpdating.value = false
    }
  }

  /**
   * 删除动态
   */
  async function deleteMoment(momentId: string | number) {
    try {
      isDeleting.value = true
      await momentApi.deleteMoment(momentId)

      // 从列表中移除
      moments.value = moments.value.filter(m => m.id !== momentId)
      uni.showToast({
        title: '删除成功',
        icon: 'success'
      })
      return true
    } catch (error) {
      console.error('删除动态失败:', error)
      uni.showToast({
        title: '删除失败',
        icon: 'none'
      })
      return false
    } finally {
      isDeleting.value = false
    }
  }

  /**
   * 点赞/取消点赞动态（乐观更新）
   */
  async function toggleLike(momentId: string | number, isLiked: boolean) {
    // 乐观更新本地状态
    const moment = moments.value.find(m => m.id === momentId)
    if (moment) {
      moment.isLiked = !isLiked
      moment.likeCount += isLiked ? -1 : 1
    }

    if (currentMoment.value && currentMoment.value.id === momentId) {
      currentMoment.value.isLiked = !isLiked
      currentMoment.value.likeCount += isLiked ? -1 : 1
    }

    try {
      isLiked
        ? await momentApi.unlikeMoment(momentId)
        : await momentApi.likeMoment(momentId)

      return true
    } catch (error) {
      console.error('点赞操作失败:', error)
      // 回滚更新
      if (moment) {
        moment.isLiked = isLiked
        moment.likeCount += isLiked ? 1 : -1
      }
      if (currentMoment.value && currentMoment.value.id === momentId) {
        currentMoment.value.isLiked = isLiked
        currentMoment.value.likeCount += isLiked ? 1 : -1
      }
      uni.showToast({
        title: '操作失败',
        icon: 'none'
      })
      return false
    }
  }

  /**
   * 加载评论列表
   */
  async function loadComments(momentId: string | number, page = 1, size = 20) {
    try {
      const response = await momentApi.getComments(momentId, page, size)

      if (response) {
        if (page === 1) {
          comments.value = response.records
        } else {
          comments.value.push(...response.records)
        }
        return response
      }
      return null
    } catch (error) {
      console.error('加载评论失败:', error)
      uni.showToast({
        title: '加载评论失败',
        icon: 'none'
      })
      return null
    }
  }

  /**
   * 添加评论
   */
  async function addComment(momentId: string | number, data: CommentCreateRequest) {
    try {
      await momentApi.addComment(momentId, data)

      uni.showToast({
        title: '评论成功',
        icon: 'success'
      })

      // 更新评论数
      const moment = moments.value.find(m => m.id === momentId)
      if (moment) {
        moment.commentCount++
      }

      if (currentMoment.value && currentMoment.value.id === momentId) {
        currentMoment.value.commentCount++
      }

      // 重新加载评论列表
      await loadComments(momentId, 1)
      return true
    } catch (error) {
      console.error('添加评论失败:', error)
      uni.showToast({
        title: '评论失败',
        icon: 'none'
      })
      return false
    }
  }

  /**
   * 删除评论
   */
  async function deleteComment(commentId: string | number, momentId: string | number) {
    try {
      await momentApi.deleteComment(commentId)

      // 从列表中移除
      comments.value = comments.value.filter(c => c.id !== commentId)

      // 更新评论数
      const moment = moments.value.find(m => m.id === momentId)
      if (moment && moment.commentCount > 0) {
        moment.commentCount--
      }

      if (currentMoment.value && currentMoment.value.id === momentId && currentMoment.value.commentCount > 0) {
        currentMoment.value.commentCount--
      }

      uni.showToast({
        title: '删除成功',
        icon: 'success'
      })
      return true
    } catch (error) {
      console.error('删除评论失败:', error)
      uni.showToast({
        title: '删除失败',
        icon: 'none'
      })
      return false
    }
  }

  /**
   * 重置状态
   */
  function reset() {
    moments.value = []
    currentMoment.value = null
    comments.value = []
    isLoadingTimeline.value = false
    isCreating.value = false
    isUpdating.value = false
    isDeleting.value = false
    hasMore.value = true
    currentPage.value = 1
  }

  return {
    // 状态
    moments,
    currentMoment,
    comments,
    isLoadingTimeline,
    isCreating,
    isUpdating,
    isDeleting,
    hasMore,
    momentCount,

    // 方法
    loadTimeline,
    createMoment,
    getMomentDetail,
    updateMoment,
    deleteMoment,
    toggleLike,
    loadComments,
    addComment,
    deleteComment,
    reset
  }
})
