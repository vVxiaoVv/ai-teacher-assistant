<template>
  <div class="student-portrait-detail">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button :icon="ArrowLeft" @click="handleBack" class="back-button">返回</el-button>
            <h2 class="page-title">学生画像详情</h2>
          </div>
          <el-button type="primary" :icon="Edit" @click="handleEdit">编辑</el-button>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>

      <div v-else-if="student" class="detail-container">
        <!-- 基本信息卡片 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-title-section">
              <el-icon class="card-icon"><User /></el-icon>
              <span class="card-title-text">基本信息</span>
            </div>
          </template>
          
          <div class="basic-info-content">
            <div class="avatar-section">
              <el-avatar
                :src="getAvatarUrl(student)"
                :size="120"
                @error="handleAvatarError"
                class="detail-avatar"
                shape="square"
              >
                <el-icon :size="60"><User /></el-icon>
              </el-avatar>
            </div>
            
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">学生姓名</span>
                <span class="info-value">{{ student.studentName || '未填写' }}</span>
              </div>
              
              <div class="info-item">
                <span class="info-label">年龄</span>
                <span class="info-value">{{ student.age ? student.age + '岁' : '未填写' }}</span>
              </div>
              
              <div class="info-item">
                <span class="info-label">创建时间</span>
                <span class="info-value">{{ formatTime(student.createTime) }}</span>
              </div>
              
              <div class="info-item">
                <span class="info-label">更新时间</span>
                <span class="info-value">{{ formatTime(student.updateTime) }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 特点描述卡片 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-title-section">
              <el-icon class="card-icon"><Document /></el-icon>
              <span class="card-title-text">特点描述</span>
            </div>
          </template>
          
          <div class="characteristics-content">
            <p v-if="student.characteristics" class="characteristics-text">
              {{ student.characteristics }}
            </p>
            <p v-else class="no-data">暂无特点描述</p>
          </div>
        </el-card>

        <!-- 历史考试信息卡片 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-title-section">
              <el-icon class="card-icon"><DataAnalysis /></el-icon>
              <span class="card-title-text">历史考试信息</span>
            </div>
          </template>
          
          <div v-if="examHistoryList.length > 0" class="exam-history-content">
            <el-table :data="examHistoryList" stripe style="width: 100%">
              <el-table-column prop="examName" label="考试名称" min-width="150" />
              <el-table-column prop="examDate" label="考试日期" width="120" />
              <el-table-column prop="subject" label="科目" width="100" />
              <el-table-column prop="score" label="分数" width="80">
                <template #default="scope">
                  <span :class="getScoreClass(scope.row.score)">
                    {{ scope.row.score }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="fullScore" label="满分" width="80" />
              <el-table-column prop="rank" label="排名" width="80" />
              <el-table-column prop="remarks" label="备注" min-width="150" show-overflow-tooltip />
            </el-table>
          </div>
          <div v-else class="no-data">
            <el-empty description="暂无历史考试信息" :image-size="80" />
          </div>
        </el-card>
      </div>

      <div v-else class="empty-container">
        <el-empty description="学生信息不存在" />
      </div>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑学生信息"
      width="700px"
      :before-close="handleCloseDialog"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="学生姓名" prop="studentName">
          <el-input
            v-model="form.studentName"
            placeholder="请输入学生姓名"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="年龄">
          <el-input-number
            v-model="form.age"
            :min="1"
            :max="100"
            placeholder="请输入年龄"
            style="width: 200px"
          />
          <span class="form-tip">岁</span>
        </el-form-item>

        <el-form-item label="学生头像">
          <div class="avatar-url-section">
            <el-input
              v-model="form.photoUrl"
              placeholder="请输入图片URL（支持 http:// 或 https:// 开头的完整URL）"
              maxlength="500"
              show-word-limit
              clearable
              @input="handleUrlInput"
            >
              <template #append>
                <el-button @click="handlePreviewAvatar" :disabled="!form.photoUrl">预览</el-button>
              </template>
            </el-input>
            <div v-if="form.photoUrl" class="avatar-preview-section">
              <div class="avatar-preview-wrapper">
                <img 
                  :src="getFullAvatarUrl(form.photoUrl)" 
                  class="avatar-preview" 
                  @error="handlePreviewError"
                  alt="头像预览"
                />
                <div v-if="previewError" class="preview-error">
                  <el-icon><Picture /></el-icon>
                  <p>图片加载失败</p>
                </div>
              </div>
              <el-button
                type="danger"
                link
                size="small"
                @click="handleRemoveAvatar"
              >
                清除URL
              </el-button>
            </div>
            <div class="avatar-tips">
              <p>请输入完整的图片URL地址，例如：https://example.com/image.jpg</p>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="概括描述">
          <el-input
            v-model="form.characteristics"
            type="textarea"
            :rows="4"
            placeholder="请输入学生的概括描述（可选）"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="历史考试">
          <div class="exam-history-section">
            <div class="exam-list">
              <div v-for="(exam, index) in formExamHistory" :key="index" class="exam-item">
                <el-input
                  v-model="exam.examName"
                  placeholder="考试名称"
                  style="width: 150px"
                  size="small"
                />
                <el-date-picker
                  v-model="exam.examDate"
                  type="date"
                  placeholder="考试日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 130px"
                  size="small"
                />
                <el-input
                  v-model="exam.subject"
                  placeholder="科目"
                  style="width: 80px"
                  size="small"
                />
                <el-input-number
                  v-model="exam.score"
                  :min="0"
                  :max="1000"
                  placeholder="分数"
                  style="width: 90px"
                  size="small"
                  controls-position="right"
                />
                <el-input-number
                  v-model="exam.fullScore"
                  :min="0"
                  :max="1000"
                  placeholder="满分"
                  style="width: 90px"
                  size="small"
                  controls-position="right"
                />
                <el-input-number
                  v-model="exam.rank"
                  :min="1"
                  :max="1000"
                  placeholder="排名"
                  style="width: 80px"
                  size="small"
                  controls-position="right"
                />
                <el-input
                  v-model="exam.remarks"
                  placeholder="备注"
                  style="width: 120px"
                  size="small"
                />
                <el-button
                  type="danger"
                  :icon="Delete"
                  circle
                  size="small"
                  @click="removeExam(index)"
                />
              </div>
            </div>
            <el-button type="primary" :icon="Plus" size="small" @click="addExam">
              添加考试记录
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="handleCloseDialog">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Edit, User, Document, DataAnalysis, Plus, Delete, Picture } from '@element-plus/icons-vue'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const student = ref(null)
const examHistoryList = ref([])

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const previewError = ref(false)

const form = reactive({
  id: null,
  studentName: '',
  photoUrl: '',
  characteristics: '',
  age: null
})

const formExamHistory = ref([])

const rules = {
  studentName: [
    { required: true, message: '请输入学生姓名', trigger: 'blur' },
    { max: 100, message: '姓名长度不能超过100个字符', trigger: 'blur' }
  ]
}

// 解析考试历史JSON
const parseExamHistory = (examHistoryStr) => {
  if (!examHistoryStr) return []
  try {
    const parsed = JSON.parse(examHistoryStr)
    return Array.isArray(parsed) ? parsed : []
  } catch (e) {
    console.error('解析考试历史失败:', e)
    return []
  }
}

// 获取头像URL
const getAvatarUrl = (studentData) => {
  if (studentData.photoUrl) {
    return getFullAvatarUrl(studentData.photoUrl)
  } else if (studentData.studentName) {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(studentData.studentName)}&size=200&background=5B8DEF&color=fff`
  }
  return ''
}

// 获取完整头像URL
const getFullAvatarUrl = (photoUrl) => {
  if (!photoUrl) return ''
  if (photoUrl.startsWith('http://') || photoUrl.startsWith('https://')) {
    return photoUrl
  }
  return photoUrl
}

// 处理头像加载失败
const handleAvatarError = () => {
  // 头像加载失败时的处理
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  try {
    const date = new Date(timeStr)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  } catch (e) {
    return timeStr
  }
}

// 获取分数样式类
const getScoreClass = (score) => {
  if (score === null || score === undefined) return ''
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

// 加载学生详情
const loadStudentDetail = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('学生ID不存在')
    router.push('/student-portrait')
    return
  }

  loading.value = true
  try {
    const response = await axios.get(`/api/student-portrait/${id}`)
    if (response.status === 200 && response.data) {
      student.value = response.data
      examHistoryList.value = parseExamHistory(student.value.examHistory)
    } else {
      ElMessage.error('加载学生信息失败')
      student.value = null
    }
  } catch (error) {
    console.error('加载学生信息失败:', error)
    ElMessage.error('加载学生信息失败，请稍后重试')
    student.value = null
  } finally {
    loading.value = false
  }
}

// 返回
const handleBack = () => {
  router.push('/student-portrait')
}

// 编辑
const handleEdit = () => {
  if (!student.value) return
  
  form.id = student.value.id
  form.studentName = student.value.studentName || ''
  form.photoUrl = student.value.photoUrl || ''
  form.characteristics = student.value.characteristics || ''
  form.age = student.value.age || null
  
  // 深拷贝考试历史数据
  formExamHistory.value = JSON.parse(JSON.stringify(examHistoryList.value))
  
  previewError.value = false
  dialogVisible.value = true
}

// 添加考试记录
const addExam = () => {
  formExamHistory.value.push({
    examName: '',
    examDate: '',
    subject: '',
    score: null,
    fullScore: 100,
    rank: null,
    remarks: ''
  })
}

// 删除考试记录
const removeExam = (index) => {
  formExamHistory.value.splice(index, 1)
}

// URL输入时重置预览错误状态
const handleUrlInput = () => {
  previewError.value = false
}

// 预览头像
const handlePreviewAvatar = () => {
  if (!form.photoUrl) {
    ElMessage.warning('请输入图片URL')
    return
  }
  previewError.value = false
}

// 预览图片加载失败
const handlePreviewError = () => {
  previewError.value = true
}

// 清除头像URL
const handleRemoveAvatar = () => {
  form.photoUrl = ''
  previewError.value = false
  ElMessage.success('已清除头像URL')
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const params = new URLSearchParams()
        params.append('studentName', form.studentName.trim())
        if (form.photoUrl) {
          params.append('photoUrl', form.photoUrl)
        }
        if (form.characteristics) {
          params.append('characteristics', form.characteristics.trim())
        }
        if (form.age !== null && form.age !== undefined) {
          params.append('age', form.age)
        }
        // 转换考试历史为JSON字符串
        if (formExamHistory.value.length > 0) {
          params.append('examHistory', JSON.stringify(formExamHistory.value))
        }
        
        const response = await axios.put(`/api/student-portrait/update/${form.id}`, params, {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        })
        
        if (response.status === 200) {
          ElMessage.success('更新成功')
          handleCloseDialog()
          loadStudentDetail()
        }
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error(error.response?.data?.message || error.response?.data || '保存失败，请稍后重试')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 关闭对话框
const handleCloseDialog = () => {
  dialogVisible.value = false
  formRef.value?.resetFields()
  form.id = null
  form.studentName = ''
  form.photoUrl = ''
  form.characteristics = ''
  form.age = null
  formExamHistory.value = []
  previewError.value = false
}

// 初始化
onMounted(() => {
  loadStudentDetail()
})

// 监听路由参数变化
watch(() => route.params.id, () => {
  loadStudentDetail()
})
</script>

<style scoped lang="scss">
.student-portrait-detail {
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

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .back-button {
    padding: 8px 16px;
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

  .detail-container {
    display: flex;
    flex-direction: column;
    gap: 24px;
  }

  .info-card {
    border: 1px solid #E2E8F0;
    border-radius: 12px;
    
    :deep(.el-card__header) {
      background-color: #FAFBFC;
      border-bottom: 1px solid #E2E8F0;
      padding: 16px 20px;
    }
    
    :deep(.el-card__body) {
      padding: 20px;
    }
  }

  .card-title-section {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .card-icon {
    font-size: 18px;
    color: #5B8DEF;
  }

  .card-title-text {
    font-size: 16px;
    font-weight: 600;
    color: #1A202C;
  }

  .basic-info-content {
    display: flex;
    gap: 40px;
    align-items: flex-start;
  }

  .avatar-section {
    flex-shrink: 0;
  }

  .detail-avatar {
    border-radius: 12px;
    border: 2px solid #E2E8F0;
  }

  .info-grid {
    flex: 1;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }

  .info-item {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .info-label {
    font-size: 14px;
    color: #718096;
    font-weight: 500;
  }

  .info-value {
    font-size: 16px;
    color: #1A202C;
    font-weight: 600;
  }

  .characteristics-content {
    min-height: 60px;
  }

  .characteristics-text {
    margin: 0;
    font-size: 14px;
    color: #4A5568;
    line-height: 1.8;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .no-data {
    text-align: center;
    color: #A0AEC0;
    font-size: 14px;
    padding: 20px 0;
  }

  .exam-history-content {
    :deep(.el-table) {
      border: 1px solid #E2E8F0;
      border-radius: 8px;
    }
  }

  .score-excellent {
    color: #67C23A;
    font-weight: 600;
  }

  .score-good {
    color: #409EFF;
    font-weight: 600;
  }

  .score-pass {
    color: #E6A23C;
    font-weight: 600;
  }

  .score-fail {
    color: #F56C6C;
    font-weight: 600;
  }

  .empty-container {
    padding: 80px 40px;
    text-align: center;
  }

  // 表单样式
  .form-tip {
    margin-left: 8px;
    color: #909399;
    font-size: 14px;
  }

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
    width: 120px;
    height: 120px;
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
      font-size: 32px;
      margin-bottom: 8px;
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

  // 考试历史表单样式
  .exam-history-section {
    width: 100%;
  }

  .exam-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: 12px;
  }

  .exam-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    background-color: #F7FAFC;
    border-radius: 8px;
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .basic-info-content {
    flex-direction: column;
    gap: 24px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .exam-item {
    flex-direction: column;
    align-items: stretch;
    
    .el-input,
    .el-date-picker,
    .el-input-number {
      width: 100% !important;
    }
  }
}
</style>
