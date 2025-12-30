// 主题配置类型定义
export interface ThemeStyles {
  // 转盘背景
  wheelBgGradient: string
  wheelBorderColor: string
  wheelBoxShadow: string

  // 背景光晕
  glowBgIdle: string
  glowBgActive: string

  // 装饰环
  ringOuterColor: string
  ringInnerColor: string
  ringInnerStyle?: string
  ringInnerWidth?: string

  // 装饰元素
  decoratorBg: string
  decoratorShadow: string

  // 中心按钮
  btnBg: string
  btnShadow: string
  btnIconColor: string
  btnIconContent: string
  btnIconFontFamily?: string

  // 中心装饰文字
  centerDecoTextColor: string
  centerDecoFontFamily: string

  // 结果弹窗
  popupBg: string
  popupCardBg: string
  popupTitleColor: string
  popupResultTextColor: string
  popupResultBorderColor: string
  popupCloseBtnBg: string
  popupCloseBtnShadow: string
}

export interface Theme {
  id: string
  name: string
  previewImage: string
  price: number | string
  styles: ThemeStyles
}

// 浪漫粉主题（默认）
export const romanticTheme: Theme = {
  id: 'romantic',
  name: '浪漫粉',
  previewImage: '/static/themes/preview_romantic.png',
  price: 'free',
  styles: {
    wheelBgGradient: 'linear-gradient(135deg, #ffd1ff 0%, #fad0c4 100%)',
    wheelBorderColor: 'rgba(255, 255, 255, 0.6)',
    wheelBoxShadow: '0 10rpx 30rpx rgba(255, 182, 193, 0.4), inset 0 0 40rpx rgba(255, 255, 255, 0.8)',
    glowBgIdle: 'radial-gradient(circle, rgba(255, 182, 193, 0.4) 0%, rgba(255, 105, 180, 0) 70%)',
    glowBgActive: 'radial-gradient(circle, rgba(255, 20, 147, 0.6) 0%, rgba(255, 105, 180, 0.2) 80%)',
    ringOuterColor: 'rgba(255, 255, 255, 0.6)',
    ringInnerColor: 'rgba(255, 255, 255, 0.8)',
    ringInnerStyle: 'dotted',
    ringInnerWidth: '4rpx',
    decoratorBg: 'white',
    decoratorShadow: '0 0 10rpx white',
    btnBg: 'white',
    btnShadow: '0 8rpx 20rpx rgba(255, 105, 180, 0.3)',
    btnIconColor: '#ff69b4',
    btnIconContent: '❤',
    centerDecoTextColor: '#fff',
    centerDecoFontFamily: "'Courier New', Courier, monospace",
    popupBg: 'rgba(0, 0, 0, 0.4)',
    popupCardBg: 'rgba(255, 255, 255, 0.95)',
    popupTitleColor: '#888',
    popupResultTextColor: '#d63384',
    popupResultBorderColor: '#ffe4e1',
    popupCloseBtnBg: 'linear-gradient(135deg, #ff9a9e 0%, #ff6a88 100%)',
    popupCloseBtnShadow: '0 10rpx 20rpx rgba(255, 106, 136, 0.3)'
  }
}

// 赛博科技主题
export const techTheme: Theme = {
  id: 'tech',
  name: '赛博科技',
  previewImage: '/static/themes/preview_tech.png',
  price: 100,
  styles: {
    wheelBgGradient: 'linear-gradient(135deg, #0d1a26 0%, #030a10 100%)',
    wheelBorderColor: 'rgba(0, 255, 255, 0.5)',
    wheelBoxShadow: '0 0 40rpx rgba(0, 255, 255, 0.3), inset 0 0 20rpx rgba(0, 191, 255, 0.5)',
    glowBgIdle: 'radial-gradient(circle, rgba(0, 255, 255, 0.3) 0%, rgba(0, 255, 255, 0) 70%)',
    glowBgActive: 'radial-gradient(circle, rgba(0, 255, 255, 0.7) 0%, rgba(0, 255, 255, 0.1) 80%)',
    ringOuterColor: 'rgba(0, 255, 255, 0.4)',
    ringInnerColor: 'rgba(0, 255, 255, 0.6)',
    ringInnerStyle: 'solid',
    ringInnerWidth: '1rpx',
    decoratorBg: 'cyan',
    decoratorShadow: '0 0 12rpx cyan',
    btnBg: '#0a192f',
    btnShadow: '0 0 25rpx rgba(0, 255, 255, 0.4)',
    btnIconColor: '#64ffda',
    btnIconContent: '⏻',
    centerDecoTextColor: '#64ffda',
    centerDecoFontFamily: "'Roboto Mono', monospace",
    popupBg: 'rgba(10, 25, 47, 0.6)',
    popupCardBg: 'linear-gradient(150deg, #0a192f, #133b5c)',
    popupTitleColor: '#8892b0',
    popupResultTextColor: '#64ffda',
    popupResultBorderColor: 'rgba(0, 255, 255, 0.5)',
    popupCloseBtnBg: '#64ffda',
    popupCloseBtnShadow: '0 0 20rpx rgba(100, 255, 218, 0.3)'
  }
}

// 复古街机主题
export const retroTheme: Theme = {
  id: 'retro',
  name: '复古街机',
  previewImage: '/static/themes/preview_retro.png',
  price: 150,
  styles: {
    wheelBgGradient: 'conic-gradient(from 90deg at 50% 50%, #ff00ff, #ffff00, #00ffff, #ff00ff)',
    wheelBorderColor: '#000',
    wheelBoxShadow: '8rpx 8rpx 0 #000, inset 0 0 10rpx #000',
    glowBgIdle: 'none',
    glowBgActive: 'none',
    ringOuterColor: '#000',
    ringInnerColor: '#FFF',
    ringInnerStyle: 'dashed',
    ringInnerWidth: '4rpx',
    decoratorBg: 'yellow',
    decoratorShadow: 'none',
    btnBg: '#ff00ff',
    btnShadow: '6rpx 6rpx 0 #000',
    btnIconColor: 'yellow',
    btnIconContent: '★',
    centerDecoTextColor: '#fff',
    centerDecoFontFamily: "'Press Start 2P', cursive",
    popupBg: 'rgba(0, 0, 0, 0.5)',
    popupCardBg: '#fff',
    popupTitleColor: '#555',
    popupResultTextColor: '#ff00ff',
    popupResultBorderColor: '#000',
    popupCloseBtnBg: 'yellow',
    popupCloseBtnShadow: '4rpx 4rpx 0 #000'
  }
}

// 新春贺喜主题
export const festivalTheme: Theme = {
  id: 'festival',
  name: '新春贺喜',
  previewImage: '/static/themes/preview_festival.png',
  price: 200,
  styles: {
    wheelBgGradient: 'radial-gradient(circle, #ffd700, #c00)',
    wheelBorderColor: 'rgba(255, 215, 0, 0.8)',
    wheelBoxShadow: '0 0 50rpx rgba(255, 0, 0, 0.5), inset 0 0 30rpx #ffeb3b',
    glowBgIdle: 'radial-gradient(circle, rgba(255, 215, 0, 0.4) 0%, rgba(255, 0, 0, 0) 70%)',
    glowBgActive: 'radial-gradient(circle, rgba(255, 223, 0, 0.7) 0%, rgba(255, 0, 0, 0.2) 80%)',
    ringOuterColor: 'rgba(255, 215, 0, 0.7)',
    ringInnerColor: 'rgba(255, 215, 0, 0.9)',
    ringInnerStyle: 'solid',
    ringInnerWidth: '2rpx',
    decoratorBg: 'gold',
    decoratorShadow: '0 0 10rpx gold',
    btnBg: '#c00',
    btnShadow: '0 5rpx 15rpx rgba(139, 0, 0, 0.4)',
    btnIconColor: '#ffd700',
    btnIconContent: '福',
    btnIconFontFamily: "'Ma Shan Zheng', cursive",
    centerDecoTextColor: '#ffd700',
    centerDecoFontFamily: "'Ma Shan Zheng', cursive",
    popupBg: 'rgba(100, 0, 0, 0.3)',
    popupCardBg: 'radial-gradient(circle at top, #fffbf0, #fff2d0)',
    popupTitleColor: '#a80000',
    popupResultTextColor: '#c00',
    popupResultBorderColor: '#ffd700',
    popupCloseBtnBg: 'linear-gradient(135deg, #ff5f5f, #c00)',
    popupCloseBtnShadow: '0 10rpx 20rpx rgba(192, 0, 0, 0.3)'
  }
}

// 导出所有主题
export const themes: Record<string, Theme> = {
  romantic: romanticTheme,
  tech: techTheme,
  retro: retroTheme,
  festival: festivalTheme
}

// 默认主题
export const defaultTheme = romanticTheme
