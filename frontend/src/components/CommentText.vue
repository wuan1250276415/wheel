<template>
  <view class="comment-text">
    <text
      v-for="(part, index) in parsedContent"
      :key="index"
      :class="part.type === 'mention' ? 'mention' : ''"
    >
      {{ part.text }}
    </text>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Comment } from '@/types/moment'

interface Props {
  comment: Comment
}

const props = defineProps<Props>()

interface ContentPart {
  text: string
  type: 'text' | 'mention'
  userId?: number
}

const parsedContent = computed(() => {
  const parts: ContentPart[] = []
  const content = props.comment.content
  const mentionedUsers = props.comment.mentionedUsers || []

  if (mentionedUsers.length === 0) {
    // 没有提及用户，直接返回原文本
    parts.push({ text: content, type: 'text' })
    return parts
  }

  // 使用正则提取所有 @用户名 模式
  const mentionRegex = /@([^\s@]+)/g
  let lastIndex = 0
  let match

  while ((match = mentionRegex.exec(content)) !== null) {
    // 添加前面的普通文本
    if (match.index > lastIndex) {
      parts.push({
        text: content.substring(lastIndex, match.index),
        type: 'text'
      })
    }

    // 检查这个提及是否在 mentionedUsers 中
    const mentionedNickname = match[1]
    const user = mentionedUsers.find(u => u.nickname === mentionedNickname)

    if (user) {
      // 这是有效的提及
      parts.push({
        text: match[0],
        type: 'mention',
        userId: user.id
      })
    } else {
      // 不是有效的提及，当作普通文本
      parts.push({
        text: match[0],
        type: 'text'
      })
    }

    lastIndex = match.index + match[0].length
  }

  // 添加剩余的文本
  if (lastIndex < content.length) {
    parts.push({
      text: content.substring(lastIndex),
      type: 'text'
    })
  }

  return parts
})
</script>

<style scoped lang="scss">
.comment-text {
  font-size: 28rpx;
  line-height: 1.6;
  color: #333;
  word-wrap: break-word;

  .mention {
    color: #007aff;
    font-weight: 500;
  }
}
</style>
