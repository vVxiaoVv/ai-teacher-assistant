<template>
  <div class="teacher-portrait-list">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="card-header">
          <h2 class="page-title">教师画像</h2>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="portraitList.length > 0" class="portrait-list-container">
        <div class="portrait-grid">
          <el-card
            v-for="portrait in portraitList"
            :key="portrait.userId"
            class="portrait-card"
            shadow="hover"
            @click="handleCardClick(portrait)"
          >
            <div class="card-header-section">
              <el-avatar
                :src="getAvatarUrl(portrait)"
                :size="48"
                @error="handleAvatarError"
                class="card-avatar"
              >
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="card-title-section">
                <h3 class="card-title">{{ portrait.username }}</h3>
                <p class="card-subtitle">已使用 {{ portrait.historyCount || 0 }} 条历史记录</p>
              </div>
            </div>
            
            <div class="card-content">
              <div v-if="portrait.hexagramScore && portrait.description" class="score-summary">
                <div class="score-item">
                  <span class="score-label">综合得分</span>
                  <span class="score-value">{{ calculateAverageScore(portrait.hexagramScore) }}</span>
                  <span class="score-unit">分</span>
                </div>
              </div>
              <div v-else class="no-score">
                <el-tag type="info" size="small">暂无画像数据</el-tag>
              </div>
            </div>
            
            <div class="card-footer">
              <el-button 
                v-if="portrait.description" 
                type="primary" 
                size="small" 
                @click.stop="handleViewDetail(portrait)"
              >
                查看详情
              </el-button>
              <el-button 
                v-if="portrait.description" 
                type="warning" 
                size="small" 
                :loading="generatingPortrait[portrait.userId]"
                @click.stop="handleRegeneratePortrait(portrait)"
              >
                重新生成
              </el-button>
              <el-button 
                v-else 
                type="primary" 
                size="small" 
                :loading="generatingPortrait[portrait.userId]"
                @click.stop="handleGeneratePortrait(portrait)"
              >
                生成画像
              </el-button>
            </div>
          </el-card>
        </div>
      </div>

      <div v-else class="empty-container">
        <el-empty description="暂无教师画像数据" />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`${selectedPortrait?.username} - 教师画像详情`"
      width="90%"
      :close-on-click-modal="false"
      class="portrait-detail-dialog"
    >
      <TeacherPortraitDetail
        v-if="selectedPortrait"
        :portrait-data="selectedPortrait"
        @close="detailDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import axios from 'axios'
import TeacherPortraitDetail from './TeacherPortraitDetail.vue'

// 响应式数据
const loading = ref(false)
const portraitList = ref([])
const detailDialogVisible = ref(false)
const selectedPortrait = ref(null)
const generatingPortrait = ref({}) // 记录正在生成画像的用户ID

// 加载画像列表
const loadPortraitList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/teacher-portrait/list', {
      withCredentials: true
    })
    
    if (response.data && response.data.code === 0 && response.data.data) {
      portraitList.value = response.data.data
      console.log('教师画像列表加载成功，数量:', portraitList.value.length)
    } else {
      ElMessage.error(response.data?.msg || '加载画像列表失败')
      portraitList.value = []
    }
  } catch (error) {
    console.error('加载画像列表失败:', error)
    ElMessage.error('加载画像列表失败，请稍后重试')
    portraitList.value = []
  } finally {
    loading.value = false
  }
}

// 获取头像URL
const getAvatarUrl = (portrait) => {
  if (portrait.avatarUrl) {
    return portrait.avatarUrl
  } else if (portrait.username) {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(portrait.username)}&size=200`
  }
  return ''
}

// 处理头像加载失败
const handleAvatarError = () => {
  // 头像加载失败时的处理
}

// 计算平均分
const calculateAverageScore = (hexagramScore) => {
  if (!hexagramScore) return '0.0'
  
  const scores = hexagramScore
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

// 点击卡片
const handleCardClick = (portrait) => {
  handleViewDetail(portrait)
}

// 查看详情
const handleViewDetail = (portrait) => {
  selectedPortrait.value = portrait
  detailDialogVisible.value = true
}

// 生成画像（首次生成）
const handleGeneratePortrait = async (portrait) => {
  if (!portrait || !portrait.userId) {
    ElMessage.error('用户信息不完整')
    return
  }
  
  generatingPortrait.value[portrait.userId] = true
  
  try {
    const response = await axios.post('/api/teacher-portrait/generate', {
      userId: portrait.userId
    }, {
      withCredentials: true
    })
    
    if (response.data && response.data.code === 0 && response.data.data) {
      ElMessage.success('画像生成成功')
      // 重新加载列表
      await loadPortraitList()
      
      // 如果生成成功，自动打开详情
      const updatedPortrait = portraitList.value.find(p => p.userId === portrait.userId)
      if (updatedPortrait && updatedPortrait.description) {
        selectedPortrait.value = updatedPortrait
        detailDialogVisible.value = true
      }
    } else {
      ElMessage.error(response.data?.msg || '生成画像失败')
    }
  } catch (error) {
    console.error('生成画像失败:', error)
    const errorMessage = error.response?.data?.msg || error.message || '生成画像失败，请稍后重试'
    ElMessage.error(errorMessage)
  } finally {
    generatingPortrait.value[portrait.userId] = false
  }
}

// 重新生成画像（覆盖历史结果）
const handleRegeneratePortrait = async (portrait) => {
  if (!portrait || !portrait.userId) {
    ElMessage.error('用户信息不完整')
    return
  }
  
  // 确认对话框
  try {
    await ElMessageBox.confirm(
      '重新生成画像将覆盖当前画像数据，是否继续？',
      '确认重新生成',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    // 用户取消
    return
  }
  
  generatingPortrait.value[portrait.userId] = true
  
  try {
    const response = await axios.post('/api/teacher-portrait/generate', {
      userId: portrait.userId
    }, {
      withCredentials: true
    })
    
    if (response.data && response.data.code === 0 && response.data.data) {
      ElMessage.success('画像重新生成成功，已覆盖历史数据')
      // 重新加载列表
      await loadPortraitList()
      
      // 如果生成成功，自动打开详情显示新生成的画像
      const updatedPortrait = portraitList.value.find(p => p.userId === portrait.userId)
      if (updatedPortrait && updatedPortrait.description) {
        selectedPortrait.value = updatedPortrait
        detailDialogVisible.value = true
      }
    } else {
      ElMessage.error(response.data?.msg || '重新生成画像失败')
    }
  } catch (error) {
    console.error('重新生成画像失败:', error)
    const errorMessage = error.response?.data?.msg || error.message || '重新生成画像失败，请稍后重试'
    ElMessage.error(errorMessage)
  } finally {
    generatingPortrait.value[portrait.userId] = false
  }
}

// 组件挂载时加载列表
onMounted(() => {
  loadPortraitList()
})
</script>

<style scoped lang="scss">
.teacher-portrait-list {
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
  }

  .portrait-list-container {
    width: 100%;
  }

  .portrait-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;
    padding: 0;
  }

  .portrait-card {
    cursor: pointer;
    transition: all 0.3s ease;
    border: 1px solid #E2E8F0;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(91, 141, 239, 0.15);
      border-color: #5B8DEF;
    }
    
    :deep(.el-card__body) {
      padding: 20px;
    }
  }

  .card-header-section {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid #E2E8F0;
  }

  .card-avatar {
    flex-shrink: 0;
  }

  .card-title-section {
    flex: 1;
    min-width: 0;
    
    .card-title {
      margin: 0 0 4px 0;
      font-size: 16px;
      font-weight: 600;
      color: #1A202C;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .card-subtitle {
      margin: 0;
      font-size: 12px;
      color: #718096;
    }
  }

  .card-content {
    margin-bottom: 16px;
    min-height: 40px;
  }

  .score-summary {
    .score-item {
      display: flex;
      align-items: baseline;
      gap: 8px;
      
      .score-label {
        font-size: 14px;
        color: #4A5568;
      }
      
      .score-value {
        font-size: 24px;
        font-weight: 700;
        color: #5B8DEF;
      }
      
      .score-unit {
        font-size: 14px;
        color: #718096;
      }
    }
  }

  .no-score {
    color: #A0AEC0;
    font-size: 14px;
    text-align: center;
    padding: 10px 0;
  }

  .card-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding-top: 12px;
    border-top: 1px solid #F1F5F9;
  }

  .empty-container {
    padding: 80px 40px;
    text-align: center;
  }

  .portrait-detail-dialog {
    :deep(.el-dialog__body) {
      padding: 0;
      max-height: 80vh;
      overflow-y: auto;
    }
  }
}

@media (max-width: 768px) {
  .portrait-grid {
    grid-template-columns: 1fr;
  }
}
</style>

