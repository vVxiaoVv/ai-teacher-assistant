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
          >
            <div class="card-header-section">
              <el-avatar
                :src="getAvatarUrl(portrait)"
                :size="64"
                @error="handleAvatarError"
                class="card-avatar"
                shape="square"
              >
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="card-title-section">
                <h3 class="card-title">{{ portrait.username }}</h3>
                <p class="card-subtitle">
                  <span v-if="portrait.age">年龄：{{ portrait.age }}岁</span>
                  <span v-if="portrait.age && portrait.subject" class="separator">|</span>
                  <span v-if="portrait.subject">学科：{{ portrait.subject }}</span>
                </p>
              </div>
              <div class="card-actions">
                <el-tooltip content="编辑教师信息" placement="top">
                  <el-button
                    type="primary"
                    :icon="Edit"
                    circle
                    size="small"
                    @click="handleEditTeacher(portrait)"
                  />
                </el-tooltip>
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

    <!-- 编辑教师信息对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑教师信息"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
        <el-form-item label="姓名" prop="username">
          <el-input
            v-model="editForm.username"
            placeholder="请输入姓名"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="年龄" prop="age">
          <el-input-number
            v-model="editForm.age"
            :min="1"
            :max="120"
            placeholder="请输入年龄"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="学科" prop="subject">
          <el-select
            v-model="editForm.subject"
            placeholder="请选择学科"
            clearable
            style="width: 100%"
          >
            <el-option label="语文" value="语文" />
            <el-option label="数学" value="数学" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
            <el-option label="生物" value="生物" />
            <el-option label="历史" value="历史" />
            <el-option label="地理" value="地理" />
            <el-option label="政治" value="政治" />
            <el-option label="体育" value="体育" />
            <el-option label="音乐" value="音乐" />
            <el-option label="美术" value="美术" />
            <el-option label="医学" value="医学" />
            <el-option label="文学" value="文学" />
            <el-option label="信息技术" value="信息技术" />
          </el-select>
        </el-form-item>

        <el-form-item label="头像">
          <div class="avatar-url-section">
            <el-input
              v-model="editForm.avatarUrl"
              placeholder="请输入图片URL（支持 http:// 或 https:// 开头的完整URL）"
              maxlength="500"
              show-word-limit
              clearable
              @input="handleEditUrlInput"
            >
              <template #append>
                <el-button @click="handleEditPreviewAvatar" :disabled="!editForm.avatarUrl">预览</el-button>
              </template>
            </el-input>
            <div v-if="editForm.avatarUrl" class="avatar-preview-section">
              <div class="avatar-preview-wrapper">
                <img 
                  :src="getFullAvatarUrl(editForm.avatarUrl)" 
                  class="avatar-preview" 
                  @error="handleEditPreviewError"
                  alt="头像预览"
                />
                <div v-if="editPreviewError" class="preview-error">
                  <el-icon><Picture /></el-icon>
                  <p>图片加载失败</p>
                </div>
              </div>
              <el-button
                type="danger"
                link
                size="small"
                @click="handleEditRemoveAvatar"
              >
                清除URL
              </el-button>
            </div>
            <div class="avatar-tips">
              <p>请输入完整的图片URL地址，例如：https://example.com/image.jpg</p>
            </div>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="handleEditClose">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="editSubmitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Edit, Picture } from '@element-plus/icons-vue'
import axios from 'axios'
import TeacherPortraitDetail from './TeacherPortraitDetail.vue'

// 响应式数据
const loading = ref(false)
const portraitList = ref([])
const detailDialogVisible = ref(false)
const selectedPortrait = ref(null)
const generatingPortrait = ref({}) // 记录正在生成画像的用户ID

// 编辑教师信息相关
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editSubmitting = ref(false)
const editPreviewError = ref(false)
const editingTeacher = ref(null)

const editForm = ref({
  userId: null,
  username: '',
  age: null,
  subject: '',
  avatarUrl: ''
})

const editRules = {
  username: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 50, message: '姓名长度不能超过50个字符', trigger: 'blur' }
  ]
}

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

// 获取完整头像URL
const getFullAvatarUrl = (avatarUrl) => {
  if (!avatarUrl) return ''
  if (avatarUrl.startsWith('http://') || avatarUrl.startsWith('https://')) {
    return avatarUrl
  }
  return avatarUrl
}

// 获取头像URL
const getAvatarUrl = (portrait) => {
  if (portrait.avatarUrl) {
    return getFullAvatarUrl(portrait.avatarUrl)
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

// 编辑教师信息
const handleEditTeacher = (portrait) => {
  editingTeacher.value = portrait
  editForm.value = {
    userId: portrait.userId,
    username: portrait.username || '',
    age: portrait.age || null,
    subject: portrait.subject || '',
    avatarUrl: portrait.avatarUrl || ''
  }
  editPreviewError.value = false
  editDialogVisible.value = true
}

// URL输入时重置预览错误状态
const handleEditUrlInput = () => {
  editPreviewError.value = false
}

// 预览头像
const handleEditPreviewAvatar = () => {
  if (!editForm.value.avatarUrl) {
    ElMessage.warning('请输入图片URL')
    return
  }
  editPreviewError.value = false
  // 预览功能通过图片加载自动触发，这里只是重置错误状态
}

// 预览图片加载失败
const handleEditPreviewError = () => {
  editPreviewError.value = true
}

// 清除头像URL
const handleEditRemoveAvatar = () => {
  editForm.value.avatarUrl = ''
  editPreviewError.value = false
  ElMessage.success('已清除头像URL')
}

// 提交编辑
const handleEditSubmit = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editSubmitting.value = true
      try {
        const requestData = {}
        if (editForm.value.username) {
          requestData.username = editForm.value.username.trim()
        }
        if (editForm.value.age) {
          requestData.age = editForm.value.age
        }
        if (editForm.value.subject) {
          requestData.subject = editForm.value.subject
        }
        if (editForm.value.avatarUrl) {
          requestData.avatarUrl = editForm.value.avatarUrl
        }
        
        const response = await axios.put(
          `/api/user/info/${editForm.value.userId}`,
          requestData,
          {
            withCredentials: true
          }
        )
        
        if (response.data && response.data.code === 0) {
          ElMessage.success('更新成功')
          handleEditClose()
          // 重新加载列表
          await loadPortraitList()
        } else {
          ElMessage.error(response.data?.msg || '更新失败')
        }
      } catch (error) {
        console.error('更新教师信息失败:', error)
        ElMessage.error(error.response?.data?.msg || error.message || '更新失败，请稍后重试')
      } finally {
        editSubmitting.value = false
      }
    }
  })
}

// 关闭编辑对话框
const handleEditClose = () => {
  editDialogVisible.value = false
  editFormRef.value?.resetFields()
  editForm.value = {
    userId: null,
    username: '',
    age: null,
    subject: '',
    avatarUrl: ''
  }
  editPreviewError.value = false
  editingTeacher.value = null
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
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid #E2E8F0;
    position: relative;
  }

  .card-avatar {
    flex-shrink: 0;
  }

  .card-title-section {
    flex: 1;
    min-width: 0;
    
    .card-title {
      margin: 0 0 4px 0;
      font-size: 18px;
      font-weight: 600;
      color: #1A202C;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .card-subtitle {
      margin: 0;
      font-size: 13px;
      color: #718096;
      
      .separator {
        margin: 0 8px;
        color: #CBD5E0;
      }
    }
  }

  .card-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
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

  // 头像URL输入样式
  .avatar-url-section {
    display: flex;
    flex-direction: column;
    gap: 12px;
    width: 100%;
  }

  .avatar-preview-section {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    margin-top: 8px;
  }

  .avatar-preview-wrapper {
    position: relative;
    width: 100px;
    height: 100px;
    border: 1px solid #E2E8F0;
    border-radius: 6px;
    overflow: hidden;
    background-color: #F7FAFC;
  }

  .avatar-preview {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  .preview-error {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background-color: #F7FAFC;
    color: #909399;
    
    .el-icon {
      font-size: 28px;
      margin-bottom: 6px;
    }
    
    p {
      margin: 0;
      font-size: 12px;
    }
  }

  .avatar-tips {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
    
    p {
      margin: 0;
    }
  }
}

@media (max-width: 768px) {
  .portrait-grid {
    grid-template-columns: 1fr;
  }
}
</style>
