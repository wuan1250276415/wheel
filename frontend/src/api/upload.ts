import request from './request'

/**
 * 上传单个图片
 */
export const uploadImage = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)

  return request<{ url: string; size: number; fileName: string; fileType: string }>({
    url: '/api/upload/image',
    method: 'POST',
    data: formData,
    header: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量上传图片
 */
export const uploadImages = (files: File[]) => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })

  return request<Array<{ url: string; size: number; fileName: string; fileType: string }>>({
    url: '/api/upload/images',
    method: 'POST',
    data: formData,
    header: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
