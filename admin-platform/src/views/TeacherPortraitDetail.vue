<template>
  <div class="teacher-portrait-detail">
    <div v-if="portraitData" class="portrait-content">
      <!-- 用户信息横幅 -->
      <div class="user-info">
        <el-avatar :src="userAvatar" :size="80" @error="handleAvatarError">
          <el-icon><User /></el-icon>
        </el-avatar>
        <div class="user-details">
          <h3>{{ portraitData.username }}</h3>
          <p>已使用 {{ portraitData.historyCount }} 条历史记录进行分析</p>
        </div>
      </div>

      <!-- 主要内容区域：左右分栏 -->
      <div class="main-content-layout has-hexagram">
        <!-- 左侧：文字描述 -->
        <div class="left-content">
          <el-card class="description-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="card-title">教师特点分析</span>
              </div>
            </template>
            <div class="description-content" v-if="portraitData.description" v-html="formatDescription(portraitData.description)"></div>
            <div class="description-content" v-else>
              <p class="empty-description">暂无教师特点分析内容</p>
            </div>
          </el-card>
        </div>

        <!-- 右侧：六芒星矩阵图 -->
        <div class="right-content">
          <el-card class="hexagram-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="card-title">六芒星能力矩阵</span>
              </div>
            </template>
            <div class="hexagram-container">
              <div ref="hexagramChart" class="hexagram-chart"></div>
            </div>
            <!-- 综合得分显示 -->
            <div class="average-score">
              <span class="score-label">综合得分：</span>
              <span class="score-value">{{ calculateAverageScore() }}</span>
              <span class="score-unit">分</span>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 打分规则说明 -->
      <div class="rule-text">
        <div class="rule-title">打分规则说明</div>
        <div class="rule-content-text">{{ portraitData.scoringRule }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch, computed } from 'vue'
import { User } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const props = defineProps({
  portraitData: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close'])

const userAvatar = ref('')
const hexagramChart = ref(null)
let chartInstance = null
let resizeHandler = null

// 计算头像URL
const userAvatarComputed = computed(() => {
  if (props.portraitData?.avatarUrl) {
    return props.portraitData.avatarUrl
  } else if (props.portraitData?.username) {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(props.portraitData.username)}&size=200`
  }
  return ''
})

watch(() => props.portraitData, (newData) => {
  if (newData) {
    userAvatar.value = userAvatarComputed.value
    nextTick(() => {
      setTimeout(() => {
        drawHexagramChart()
      }, 200)
    })
  }
}, { immediate: true })

// 绘制六芒星矩阵图
const drawHexagramChart = () => {
  if (!hexagramChart.value || !props.portraitData) {
    return
  }
  
  if (!props.portraitData.hexagramScore) {
    return
  }
  
  // 销毁旧图表
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  
  // 创建新图表
  try {
    chartInstance = echarts.init(hexagramChart.value)
  } catch (error) {
    console.error('创建图表实例失败:', error)
    return
  }
  
  const scores = props.portraitData.hexagramScore || {}
  const dimensions = [
    '教学基本功',
    '教学过程设计',
    '教态',
    '多媒体与板书运用',
    '课堂气氛',
    '时间节奏掌控'
  ]
  
  const values = [
    scores.teachingFoundation || 0,
    scores.teachingProcessDesign || 0,
    scores.teachingManner || 0,
    scores.multimediaAndBlackboard || 0,
    scores.classroomAtmosphere || 0,
    scores.timeRhythmControl || 0
  ]
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      borderColor: '#5B8DEF',
      borderWidth: 1,
      textStyle: {
        color: '#fff',
        fontSize: 14
      },
      formatter: (params) => {
        let index = 0
        if (chartInstance && chartInstance._currentDimensionIndex !== undefined) {
          index = chartInstance._currentDimensionIndex
        } else if (params.dataIndex !== undefined) {
          index = params.dataIndex % dimensions.length
        }
        if (index < 0 || index >= dimensions.length) {
          index = 0
        }
        const value = values[index] || 0
        const dimensionName = dimensions[index] || '未知维度'
        const percentage = ((value / 100) * 100).toFixed(1)
        return `
          <div style="padding: 10px;">
            <div style="font-weight: bold; margin-bottom: 6px; font-size: 15px;">${dimensionName}</div>
            <div style="margin-bottom: 4px;">得分: <span style="color: #5B8DEF; font-weight: bold; font-size: 16px;">${value.toFixed(1)}</span> 分</div>
            <div>占比: <span style="color: #5B8DEF; font-weight: bold;">${percentage}%</span></div>
          </div>
        `
      }
    },
    radar: {
      indicator: dimensions.map((name) => ({
        name: name,
        max: 100,
        nameGap: 8,
        nameLocation: 'end'
      })),
      center: ['50%', '52%'],
      radius: '70%',
      startAngle: 90,
      splitNumber: 5,
      shape: 'polygon',
      axisName: {
        color: '#1A202C',
        fontSize: 14,
        fontWeight: 600,
        fontFamily: 'PingFang SC, Microsoft YaHei, sans-serif',
        formatter: (name) => {
          return name
        }
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: [
            'rgba(91, 141, 239, 0.05)',
            'rgba(91, 141, 239, 0.1)',
            'rgba(91, 141, 239, 0.15)',
            'rgba(91, 141, 239, 0.2)',
            'rgba(91, 141, 239, 0.25)'
          ]
        }
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: '#E2E8F0',
          width: 1,
          type: 'solid'
        }
      },
      axisLine: {
        show: true,
        lineStyle: {
          color: '#CBD5E0',
          width: 1
        }
      }
    },
    series: [
      {
        name: '教师能力评估',
        type: 'radar',
        emphasis: {
          areaStyle: {
            color: 'rgba(91, 141, 239, 0.4)'
          },
          lineStyle: {
            width: 3
          }
        },
        data: [
          {
            value: values,
            name: '综合评分',
            symbol: 'circle',
            symbolSize: 8,
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  {
                    offset: 0,
                    color: 'rgba(91, 141, 239, 0.4)'
                  },
                  {
                    offset: 1,
                    color: 'rgba(91, 141, 239, 0.1)'
                  }
                ]
              }
            },
            lineStyle: {
              color: '#5B8DEF',
              width: 3,
              type: 'solid'
            },
            itemStyle: {
              color: '#5B8DEF',
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: true,
              position: 'top',
              formatter: (params) => {
                return params.value.toFixed(0)
              },
              color: '#1A202C',
              fontSize: 12,
              fontWeight: 700,
              fontFamily: 'PingFang SC, Microsoft YaHei, sans-serif'
            }
          }
        ]
      }
    ]
  }
  
  try {
    chartInstance.setOption(option)
    
    // 响应式调整
    if (resizeHandler) {
      window.removeEventListener('resize', resizeHandler)
    }
    resizeHandler = () => {
      if (chartInstance) {
        chartInstance.resize()
      }
    }
    window.addEventListener('resize', resizeHandler)
  } catch (error) {
    console.error('设置图表配置失败:', error)
  }
}

// 格式化描述内容
const formatDescription = (text) => {
  if (!text || typeof text !== 'string') {
    return '<p class="empty-description">暂无教师特点分析内容</p>'
  }
  
  let formatted = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
  
  formatted = formatted.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  formatted = formatted.replace(/\n{3,}/g, '\n\n')
  
  const lines = formatted.split('\n')
  const result = []
  let lastWasEmpty = false
  let lastWasTitle = false
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim()
    
    if (!line) {
      if (!lastWasEmpty && lastWasTitle && i < lines.length - 1) {
        result.push('<div class="paragraph-spacer"></div>')
        lastWasEmpty = true
      }
      continue
    }
    
    lastWasEmpty = false
    
    if (/^(\d+[\.、]|[\u4e00-\u9fa5]+[、：:]|【|##|一、|二、|三、|四、|五、|六、)/.test(line)) {
      result.push(`<p class="section-title">${line}</p>`)
      lastWasTitle = true
    } else if (/^[-•\d]/.test(line)) {
      result.push(`<p class="list-item">${line}</p>`)
      lastWasTitle = false
    } else {
      result.push(`<p class="paragraph">${line}</p>`)
      lastWasTitle = false
    }
  }
  
  return result.join('')
}

// 处理头像加载失败
const handleAvatarError = () => {
  if (props.portraitData?.username) {
    userAvatar.value = `https://ui-avatars.com/api/?name=${encodeURIComponent(props.portraitData.username)}&size=200`
  }
}

// 计算综合得分
const calculateAverageScore = () => {
  if (!props.portraitData || !props.portraitData.hexagramScore) {
    return '0.0'
  }
  
  const scores = props.portraitData.hexagramScore
  const values = [
    scores.teachingFoundation || 0,
    scores.teachingProcessDesign || 0,
    scores.teachingManner || 0,
    scores.multimediaAndBlackboard || 0,
    scores.classroomAtmosphere || 0,
    scores.timeRhythmControl || 0
  ]
  
  const average = values.reduce((sum, val) => sum + val, 0) / values.length
  return average.toFixed(1)
}

onMounted(() => {
  if (props.portraitData) {
    userAvatar.value = userAvatarComputed.value
    nextTick(() => {
      setTimeout(() => {
        drawHexagramChart()
      }, 200)
    })
  }
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
    resizeHandler = null
  }
})
</script>

<style scoped lang="scss">
.teacher-portrait-detail {
  width: 100%;
  
  .portrait-content {
    display: flex;
    flex-direction: column;
    gap: 24px;
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 24px 28px;
    background: linear-gradient(135deg, #5B8DEF 0%, #4A7AD9 100%);
    border-radius: 12px;
    color: white;
    box-shadow: 0 4px 16px rgba(91, 141, 239, 0.2);

    .user-details {
      flex: 1;
      
      h3 {
        margin: 0 0 8px 0;
        font-size: 22px;
        font-weight: 600;
        letter-spacing: 0.3px;
      }

      p {
        margin: 0;
        font-size: 14px;
        opacity: 0.95;
        font-weight: 400;
      }
    }
  }

  .main-content-layout {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 24px;
    
    @media (max-width: 1200px) {
      grid-template-columns: 1fr;
    }
  }

  .left-content,
  .right-content {
    display: flex;
    flex-direction: column;
    min-height: 0;
  }

  .description-card,
  .hexagram-card {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    
    :deep(.el-card__body) {
      flex: 1;
      min-height: 0;
      padding: 0;
      display: flex;
      flex-direction: column;
    }
    
    .card-header {
      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #1A202C;
      }
    }
  }

  .description-content {
    padding: 16px 32px 16px 20px;
    background-color: #F8FAFC;
    border-radius: 8px;
    flex: 1;
    min-height: 0;
    max-height: 600px;
    overflow-y: auto;
    overflow-x: hidden;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    font-size: 14px;
    line-height: 1.6;
    color: #1A202C;
    box-sizing: border-box;
    scrollbar-width: thin;
    scrollbar-color: #CBD5E0 #F8FAFC;
    
    &::-webkit-scrollbar {
      width: 8px;
      display: block !important;
    }
    
    &::-webkit-scrollbar-track {
      background: #F8FAFC;
      border-radius: 4px;
      margin: 8px 0;
    }
    
    &::-webkit-scrollbar-thumb {
      background: #CBD5E0;
      border-radius: 4px;
      
      &:hover {
        background: #A0AEC0;
      }
    }
    
    :deep(.paragraph-spacer) {
      height: 8px;
      margin: 0;
    }
    
    :deep(p) {
      margin: 0;
      color: #1A202C;
      line-height: 1.6;
      
      &.section-title {
        font-size: 15px;
        font-weight: 700;
        color: #1A202C;
        margin: 12px 0 8px 0;
        padding-bottom: 6px;
        border-bottom: 2px solid #E2E8F0;
        
        &:first-child {
          margin-top: 0;
        }
      }
      
      &.list-item {
        padding-left: 18px;
        position: relative;
        margin: 4px 0;
        
        &::before {
          content: '•';
          position: absolute;
          left: 4px;
          color: #5B8DEF;
          font-weight: bold;
          font-size: 14px;
        }
      }
      
      &.paragraph {
        margin: 4px 0;
        text-align: justify;
        word-break: break-word;
      }
      
      &.empty-description {
        color: #718096;
        font-style: italic;
        text-align: center;
        padding: 30px 20px;
      }
    }
  }

  .hexagram-container {
    padding: 24px;
    display: flex;
    justify-content: center;
    align-items: center;
    flex: 1;
    min-height: 500px;
    background: linear-gradient(135deg, #F8FAFC 0%, #EDF2F7 100%);
    border-radius: 8px;
  }

  .hexagram-chart {
    width: 100% !important;
    height: 100% !important;
    min-height: 500px;
    background: transparent;
  }

  .average-score {
    padding: 20px 24px;
    text-align: center;
    border-top: 1px solid #E2E8F0;
    background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
    
    .score-label {
      font-size: 18px;
      font-weight: 600;
      color: #4A5568;
      margin-right: 8px;
    }
    
    .score-value {
      font-size: 32px;
      font-weight: 700;
      color: #5B8DEF;
      letter-spacing: 1px;
    }
    
    .score-unit {
      font-size: 18px;
      font-weight: 500;
      color: #718096;
      margin-left: 4px;
    }
  }

  .rule-text {
    margin-top: 24px;
    padding: 20px 0;
    border-top: 1px solid #E2E8F0;
  }

  .rule-title {
    font-size: 13px;
    font-weight: 600;
    color: #4A5568;
    margin-bottom: 12px;
    letter-spacing: 0.2px;
  }

  .rule-content-text {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    font-size: 12px;
    line-height: 1.8;
    color: #718096;
    white-space: pre-wrap;
    word-wrap: break-word;
  }
}
</style>

