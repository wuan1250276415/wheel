import type { Directive, DirectiveBinding } from 'vue'
import { hasPermission } from '@/utils/permission'

export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding

    if (!value) {
      return
    }

    const hasAuth = hasPermission(value)

    if (!hasAuth) {
      el.style.display = 'none'
      // 或者直接移除元素
      // el.parentNode?.removeChild(el)
    }
  }
}
