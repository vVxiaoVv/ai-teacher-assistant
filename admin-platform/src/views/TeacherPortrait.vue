<template>
  <div class="teacher-portrait">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="card-header">
          <h2 class="page-title">教师画像</h2>
          <el-button type="primary" :icon="Refresh" @click="handleGenerate" :loading="loading">
            生成画像
          </el-button>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>

      <div v-else-if="portraitData" class="portrait-content">
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
            <!-- 文字描述 -->
            <el-card class="description-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span class="card-title">教师特点分析</span>
                </div>
              </template>
              <div class="description-content" v-if="portraitData.description" v-html="formatDescription(portraitData.description)"></div>
              <div class="description-content" v-else>
                <p class="empty-description">暂无教师特点分析内容，请点击"生成画像"按钮生成分析报告</p>
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

        <!-- 打分规则说明 - 页面底部纯文本 -->
        <div class="rule-text">
          <div class="rule-title">打分规则说明</div>
          <div class="rule-content-text">{{ portraitData.scoringRule }}</div>
        </div>
      </div>

      <div v-else class="empty-container">
        <el-empty description="点击'生成画像'按钮开始分析" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, User } from '@element-plus/icons-vue'
import axios from 'axios'
import * as echarts from 'echarts'

// 响应式数据
const loading = ref(false)
const portraitData = ref(null)
const userAvatar = ref('')
const hexagramChart = ref(null)
let chartInstance = null
let resizeHandler = null

// 加载画像数据
const loadPortrait = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/teacher-portrait/get', {
      withCredentials: true
    })
    
    if (response.data && response.data.code === 0 && response.data.data) {
      portraitData.value = response.data.data
      
      // 调试日志：确认description字段存在（从数据库teacher_portrait表读取）
      console.log('教师画像数据加载成功（从数据库读取）:', {
        userId: portraitData.value.userId,
        username: portraitData.value.username,
        hasDescription: !!portraitData.value.description,
        descriptionLength: portraitData.value.description ? portraitData.value.description.length : 0,
        descriptionPreview: portraitData.value.description ? portraitData.value.description.substring(0, 100) + '...' : '无描述',
        hasHexagramScore: !!portraitData.value.hexagramScore,
        hexagramScore: portraitData.value.hexagramScore,
        fullData: JSON.stringify(portraitData.value, null, 2)
      })
      
      // 获取用户头像（优先使用数据库中的头像，如果没有则使用默认头像）
      if (portraitData.value.avatarUrl) {
        userAvatar.value = portraitData.value.avatarUrl
      } else if (portraitData.value.username) {
        userAvatar.value = 'https://ui-avatars.com/api/?name=' + encodeURIComponent(portraitData.value.username) + '&size=200'
      } else {
        userAvatar.value = ''
      }
      
      // 等待DOM更新后绘制图表
      await nextTick()
      // 延迟一下确保DOM完全渲染
      setTimeout(() => {
        drawHexagramChart()
      }, 100)
    } else if (response.data && response.data.code === 404) {
      // 没有画像数据，显示空状态
      portraitData.value = null
    } else {
      ElMessage.error(response.data?.msg || '加载画像失败')
    }
  } catch (error) {
    console.error('加载画像失败:', error)
    if (error.response && error.response.status === 404) {
      // 没有画像数据，显示空状态
      portraitData.value = null
    } else {
      ElMessage.error('加载画像失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 生成画像
const handleGenerate = async () => {
  loading.value = true
  try {
    // 确保请求携带Cookie
    const response = await axios.post('/api/teacher-portrait/generate', {}, {
      withCredentials: true
    })
    
    console.log('教师画像生成响应:', response.data)
    
    if (response.data && response.data.code === 0 && response.data.data) {
      portraitData.value = response.data.data
      
      // 调试日志：确认description字段存在
      console.log('教师画像生成成功:', {
        userId: portraitData.value.userId,
        username: portraitData.value.username,
        hasDescription: !!portraitData.value.description,
        descriptionLength: portraitData.value.description ? portraitData.value.description.length : 0,
        descriptionPreview: portraitData.value.description ? portraitData.value.description.substring(0, 100) + '...' : '无描述',
        hasHexagramScore: !!portraitData.value.hexagramScore,
        hexagramScore: portraitData.value.hexagramScore,
        fullData: JSON.stringify(portraitData.value, null, 2)
      })
      
      // 获取用户头像（优先使用数据库中的头像，如果没有则使用默认头像）
      if (portraitData.value.avatarUrl) {
        userAvatar.value = portraitData.value.avatarUrl
      } else if (portraitData.value.username) {
        userAvatar.value = 'https://ui-avatars.com/api/?name=' + encodeURIComponent(portraitData.value.username) + '&size=200'
      } else {
        userAvatar.value = ''
      }
      
      // 等待DOM更新后绘制图表
      await nextTick()
      // 延迟一下确保DOM完全渲染
      setTimeout(() => {
        drawHexagramChart()
      }, 100)
      
      ElMessage.success('画像生成成功')
    } else {
      ElMessage.error(response.data?.msg || '生成画像失败')
    }
  } catch (error) {
    console.error('生成画像失败:', error)
    ElMessage.error(error.response?.data?.msg || '生成画像失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 绘制六芒星矩阵图
const drawHexagramChart = () => {
  console.log('开始绘制六芒星图', {
    hasChartRef: !!hexagramChart.value,
    hasPortraitData: !!portraitData.value,
    hasHexagramScore: !!(portraitData.value && portraitData.value.hexagramScore),
    portraitData: portraitData.value,
    hexagramScore: portraitData.value?.hexagramScore
  })
  
  if (!hexagramChart.value) {
    console.warn('图表容器不存在，尝试延迟重试')
    // 如果容器不存在，延迟重试
    setTimeout(() => {
      if (hexagramChart.value) {
        drawHexagramChart()
      } else {
        console.error('图表容器仍然不存在，无法绘制图表')
      }
    }, 200)
    return
  }
  
  if (!portraitData.value) {
    console.warn('画像数据不存在')
    return
  }
  
  // 如果没有分数数据，创建一个默认的分数对象（全为0）用于显示
  if (!portraitData.value.hexagramScore) {
    console.warn('六芒星分数数据不存在，使用默认值', portraitData.value)
    console.warn('完整portraitData:', JSON.stringify(portraitData.value, null, 2))
    // 不返回，继续绘制，使用默认值0
  } else {
    console.log('六芒星分数数据:', JSON.stringify(portraitData.value.hexagramScore, null, 2))
  }
  
  // 销毁旧图表
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  
  // 创建新图表
  try {
    chartInstance = echarts.init(hexagramChart.value)
    console.log('图表实例创建成功', chartInstance)
  } catch (error) {
    console.error('创建图表实例失败:', error)
    return
  }
  
  const scores = portraitData.value.hexagramScore || {}
  console.log('提取的分数数据:', scores)
  
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
  
  console.log('六芒星各维度分数:', values)
  
  // 计算平均值用于显示
  const average = values.reduce((sum, val) => sum + val, 0) / values.length
  console.log('综合得分:', average)
  
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
        // 对于雷达图，当鼠标悬停在某个维度区域时
        // 使用存储的维度索引（通过鼠标移动事件计算）
        let index = 0
        
        // 优先使用通过鼠标位置计算的维度索引
        if (chartInstance && chartInstance._currentDimensionIndex !== undefined) {
          index = chartInstance._currentDimensionIndex
        } else if (params.dataIndex !== undefined) {
          // 如果没有计算出的索引，尝试使用dataIndex
          index = params.dataIndex % dimensions.length
        }
        
        // 确保索引在有效范围内
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
      },
      axisLabel: {
        show: true,
        formatter: (value) => {
          return value + ''
        },
        color: '#718096',
        fontSize: 11
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
    console.log('图表配置设置成功，option:', JSON.stringify(option, null, 2))
    
    // 添加鼠标移动事件监听，用于确定当前悬停的维度
    const mouseMoveHandler = (e) => {
      try {
        // 获取雷达图的坐标系
        const model = chartInstance.getModel()
        const radarModel = model.getComponent('radar', 0)
        
        if (radarModel) {
          const coordSys = radarModel.coordinateSystem
          if (coordSys && coordSys.center && coordSys.radius) {
            const center = coordSys.center
            const dx = e.offsetX - center[0]
            const dy = e.offsetY - center[1]
            const angle = Math.atan2(dy, dx) * 180 / Math.PI
            
            // 雷达图从90度开始，每个维度间隔60度（360/6）
            const startAngle = 90
            const anglePerDimension = 360 / dimensions.length
            
            // 将角度转换为0-360范围，并考虑起始角度
            let normalizedAngle = (angle - startAngle + 360) % 360
            if (normalizedAngle < 0) normalizedAngle += 360
            
            // 计算维度索引
            let dimensionIndex = Math.floor(normalizedAngle / anglePerDimension) % dimensions.length
            
            // 确保索引在有效范围内
            if (dimensionIndex < 0) dimensionIndex = 0
            if (dimensionIndex >= dimensions.length) dimensionIndex = dimensions.length - 1
            
            // 存储当前维度索引，供tooltip使用
            chartInstance._currentDimensionIndex = dimensionIndex
          }
        }
      } catch (err) {
        // 如果计算失败，使用默认值
        console.warn('计算维度索引失败:', err)
      }
    }
    
    // 绑定鼠标移动事件
    chartInstance.getZr().on('mousemove', mouseMoveHandler)
    
    // 存储事件处理器，以便后续清理
    chartInstance._mouseMoveHandler = mouseMoveHandler
    
    // 响应式调整 - 先移除旧的监听器，再添加新的
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

// 监听画像数据变化，自动绘制图表
watch(
  () => portraitData.value,
  (newData) => {
    console.log('portraitData变化，触发watch:', newData)
    if (newData) {
      nextTick(() => {
        setTimeout(() => {
          drawHexagramChart()
        }, 200)
      })
    }
  },
  { deep: true, immediate: false }
)

// 同时监听hexagramScore的变化
watch(
  () => portraitData.value?.hexagramScore,
  (newScore) => {
    console.log('hexagramScore变化，触发watch:', newScore)
    if (portraitData.value) {
      nextTick(() => {
        setTimeout(() => {
          drawHexagramChart()
        }, 200)
      })
    }
  },
  { deep: true, immediate: false }
)

// 组件挂载时加载画像
onMounted(() => {
  loadPortrait()
})

// 组件卸载时销毁图表和事件监听器
onUnmounted(() => {
  if (chartInstance) {
    // 移除鼠标移动事件监听器
    if (chartInstance._mouseMoveHandler) {
      chartInstance.getZr().off('mousemove', chartInstance._mouseMoveHandler)
    }
    chartInstance.dispose()
    chartInstance = null
  }
  // 移除resize事件监听器
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
    resizeHandler = null
  }
})

// 格式化描述内容，将换行符转换为HTML
// 确保显示teacher_portrait表的description字段内容
const formatDescription = (text) => {
  console.log('formatDescription被调用，text类型:', typeof text, 'text值:', text)
  
  if (!text) {
    console.warn('description字段为空或undefined')
    return '<p class="empty-description">暂无教师特点分析内容</p>'
  }
  
  if (typeof text !== 'string') {
    console.warn('description字段不是字符串类型:', typeof text, text)
    return '<p class="empty-description">数据格式错误</p>'
  }
  
  console.log('格式化description内容，长度:', text.length, '前100字符:', text.substring(0, 100))
  
  // 转义HTML特殊字符（但保留换行符）
  let formatted = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
  
  // 识别加粗文本（**文本**）
  formatted = formatted.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  
  // 处理多个连续空行，最多保留一个空行
  formatted = formatted.replace(/\n{3,}/g, '\n\n')
  
  // 将换行符转换为段落，并处理格式
  const lines = formatted.split('\n')
  const result = []
  let lastWasEmpty = false
  let lastWasTitle = false
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim()
    
    // 跳过空行，只在标题后保留一个空行
    if (!line) {
      if (!lastWasEmpty && lastWasTitle && i < lines.length - 1) {
        // 如果上一行是标题，且不是最后一行，添加一个空行
        result.push('<div class="paragraph-spacer"></div>')
        lastWasEmpty = true
      }
      continue
    }
    
    lastWasEmpty = false
    
    // 识别标题行（以数字、中文数字、特殊符号开头）
    if (/^(\d+[\.、]|[\u4e00-\u9fa5]+[、：:]|【|##|一、|二、|三、|四、|五、|六、)/.test(line)) {
      result.push(`<p class="section-title">${line}</p>`)
      lastWasTitle = true
    }
    // 识别列表项（以-、•、数字开头）
    else if (/^[-•\d]/.test(line)) {
      result.push(`<p class="list-item">${line}</p>`)
      lastWasTitle = false
    }
    // 普通段落
    else {
      result.push(`<p class="paragraph">${line}</p>`)
      lastWasTitle = false
    }
  }
  
  return result.join('')
}

// 处理头像加载失败
const handleAvatarError = () => {
  // 如果头像加载失败，使用默认头像生成
  if (portraitData.value && portraitData.value.username) {
    userAvatar.value = 'https://ui-avatars.com/api/?name=' + encodeURIComponent(portraitData.value.username) + '&size=200'
  }
}

// 计算综合得分
const calculateAverageScore = () => {
  if (!portraitData.value || !portraitData.value.hexagramScore) {
    return '0.0'
  }
  
  const scores = portraitData.value.hexagramScore
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
</script>

<style scoped lang="scss">
.teacher-portrait {
  width: 100%;
  height: 100%;
  min-height: calc(100vh - 50px);
  
  .page-card {
    width: 100%;
    height: 100%;
    min-height: calc(100vh - 50px);
    border: none;
    box-shadow: none;
    background: transparent;
    
    :deep(.el-card__body) {
      padding: 24px;
      height: 100%;
      display: flex;
      flex-direction: column;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0;
  }

  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #1A202C;
    margin: 0;
  }

  .loading-container {
    padding: 40px;
    flex: 1;
  }

  .portrait-content {
    display: flex;
    flex-direction: column;
    gap: 24px;
    flex: 1;
    width: 100%;
  }

  .main-content-layout {
    display: grid;
    grid-template-columns: 1fr;
    gap: 24px;
    flex: 1;
    align-items: stretch;
    min-height: 0;
    
    // 如果有六芒星图，使用两列布局
    &.has-hexagram {
      grid-template-columns: 1fr 1fr;
      
      @media (max-width: 1200px) {
        grid-template-columns: 1fr;
      }
    }
  }

  .left-content {
    display: flex;
    flex-direction: column;
    min-height: 0;
    height: 100%;
  }

  .right-content {
    display: flex;
    flex-direction: column;
    min-height: 0;
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
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 6px 20px rgba(91, 141, 239, 0.3);
      transform: translateY(-2px);
    }

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

  .description-card {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    height: 100%;
    
    :deep(.el-card__body) {
      flex: 1;
      min-height: 0;
      padding: 0;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      height: 100%;
    }
    
    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid #E2E8F0;
    }
    
    .card-header {
      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #1A202C;
      }
    }
  }

  .hexagram-card {
    height: 100%;
    display: flex;
    flex-direction: column;
    min-height: 0;
    
    :deep(.el-card__body) {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding: 0;
      min-height: 0;
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
    height: 600px;
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
    
    // 确保滚动条始终可见（当内容超出时）
    &::-webkit-scrollbar-thumb:active {
      background: #718096;
    }
    
    // 确保滚动条始终可见（当内容超出时）
    &::-webkit-scrollbar-thumb:active {
      background: #718096;
    }
    
    :deep(strong) {
      color: #1A202C;
      font-weight: 600;
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
        text-indent: 0;
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

  .empty-container {
    padding: 80px 40px;
    text-align: center;
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>

