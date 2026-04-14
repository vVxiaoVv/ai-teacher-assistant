<template>
  <div class="student-portrait-list">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="card-header">
          <h2 class="page-title">学生画像管理</h2>
          <el-button type="primary" :icon="Plus" @click="handleAdd">添加学生</el-button>
        </div>
      </template>

      <!-- 搜索条件区域 -->
      <div class="search-section">
        <el-form :model="searchForm" :inline="true" class="search-form">
          <el-form-item label="学生姓名">
            <el-input
              v-model="searchForm.studentName"
              placeholder="请输入学生姓名（支持模糊查询）"
              clearable
              style="width: 300px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          
          <el-form-item label="开始时间">
            <el-date-picker
              v-model="searchForm.startTime"
              type="datetime"
              placeholder="选择开始时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DDTHH:mm:ss"
              style="width: 200px"
              clearable
            />
          </el-form-item>
          
          <el-form-item label="结束时间">
            <el-date-picker
              v-model="searchForm.endTime"
              type="datetime"
              placeholder="选择结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DDTHH:mm:ss"
              style="width: 200px"
              clearable
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 卡片网格 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="studentList.length > 0" class="portrait-list-container">
        <div class="portrait-grid">
          <el-card
            v-for="student in studentList"
            :key="student.id"
            class="portrait-card"
            shadow="hover"
          >
            <div class="card-header-section">
              <el-avatar
                :src="getAvatarUrl(student)"
                :size="64"
                @error="handleAvatarError"
                class="card-avatar"
                shape="square"
              >
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="card-title-section">
                <h3 class="card-title">{{ student.studentName }}</h3>
                <p class="card-subtitle">{{ formatTime(student.createTime) }}</p>
              </div>
              <div class="card-actions">
                <el-button
                  type="primary"
                  :icon="Edit"
                  circle
                  size="small"
                  @click="handleEdit(student)"
                />
                <el-button
                  type="danger"
                  :icon="Delete"
                  circle
                  size="small"
                  @click="handleDelete(student)"
                />
              </div>
            </div>
            
            <div class="card-content">
              <div v-if="student.characteristics" class="characteristics-content">
                <p class="characteristics-text">
                  {{ getSummary(student.characteristics) }}
                </p>
              </div>
              <div v-else class="no-characteristics">
                <el-tag type="info" size="small">暂无描述</el-tag>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 分页 -->
        <div class="pagination-section">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handlePageSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>

      <div v-else class="empty-container">
        <el-empty description="暂无学生画像数据" />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
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
            :rows="6"
            placeholder="请输入学生的概括描述（可选）"
            maxlength="2000"
            show-word-limit
          />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, User, Picture } from '@element-plus/icons-vue'
import axios from 'axios'

const loading = ref(false)
const studentList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  studentName: '',
  startTime: null,
  endTime: null
})

const dialogVisible = ref(false)
const dialogTitle = computed(() => isEditMode.value ? '编辑学生信息' : '添加学生信息')
const isEditMode = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  studentName: '',
  photoUrl: '',
  characteristics: ''
})

const rules = {
  studentName: [
    { required: true, message: '请输入学生姓名', trigger: 'blur' },
    { max: 100, message: '姓名长度不能超过100个字符', trigger: 'blur' }
  ]
}

// 预览错误状态
const previewError = ref(false)

// 获取头像URL
const getAvatarUrl = (student) => {
  if (student.photoUrl) {
    return getFullAvatarUrl(student.photoUrl)
  } else if (student.studentName) {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(student.studentName)}&size=200&background=5B8DEF&color=fff`
  }
  return ''
}

// 获取完整头像URL
const getFullAvatarUrl = (photoUrl) => {
  if (!photoUrl) return ''
  if (photoUrl.startsWith('http://') || photoUrl.startsWith('https://')) {
    return photoUrl
  }
  // 如果是相对路径，通过代理访问
  return photoUrl
}

// 处理头像加载失败
const handleAvatarError = () => {
  // 头像加载失败时的处理，使用默认头像
}

// 获取描述摘要
const getSummary = (text) => {
  if (!text) return ''
  return text.length > 100 ? text.substring(0, 100) + '...' : text
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

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    
    if (searchForm.studentName && searchForm.studentName.trim()) {
      params.studentName = searchForm.studentName.trim()
    }
    
    if (searchForm.startTime) {
      params.startTime = searchForm.startTime
    }
    
    if (searchForm.endTime) {
      params.endTime = searchForm.endTime
    }
    
    const response = await axios.get('/api/student-portrait/list', { params })
    
    if (response.status === 200 && response.data) {
      const pageData = response.data
      studentList.value = pageData.content || []
      total.value = pageData.totalElements || 0
      currentPage.value = (pageData.number || 0) + 1
    } else {
      ElMessage.error('加载数据失败')
      studentList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败，请稍后重试')
    studentList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.studentName = ''
  searchForm.startTime = null
  searchForm.endTime = null
  currentPage.value = 1
  loadData()
}

// 页码改变
const handlePageChange = (page) => {
  currentPage.value = page
  loadData()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 每页条数改变
const handlePageSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

// 添加
const handleAdd = () => {
  isEditMode.value = false
  form.id = null
  form.studentName = ''
  form.photoUrl = ''
  form.characteristics = ''
  previewError.value = false
  dialogVisible.value = true
}

// 编辑
const handleEdit = (student) => {
  isEditMode.value = true
  form.id = student.id
  form.studentName = student.studentName || ''
  form.photoUrl = student.photoUrl || ''
  form.characteristics = student.characteristics || ''
  previewError.value = false
  dialogVisible.value = true
}

// 删除
const handleDelete = async (student) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除学生"${student.studentName}"的信息吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  try {
    const response = await axios.delete(`/api/student-portrait/${student.id}`)
    if (response.status === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error(error.response?.data || '删除失败，请稍后重试')
  }
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
  // 预览功能通过图片加载自动触发，这里只是重置错误状态
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
        
        let response
        if (isEditMode.value) {
          // 更新
          response = await axios.put(`/api/student-portrait/update/${form.id}`, params, {
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded'
            }
          })
        } else {
          // 创建
          response = await axios.post('/api/student-portrait/create', params, {
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded'
            }
          })
        }
        
        if (response.status === 200) {
          ElMessage.success(isEditMode.value ? '更新成功' : '创建成功')
          handleCloseDialog()
          loadData()
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
  previewError.value = false
  isEditMode.value = false
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.student-portrait-list {
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

  .search-section {
    margin-bottom: 20px;
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
    margin-bottom: 20px;
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
      font-size: 12px;
      color: #718096;
    }
  }

  .card-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
  }

  .card-content {
    margin-bottom: 0;
    min-height: 40px;
  }

  .characteristics-content {
    .characteristics-text {
      margin: 0;
      font-size: 14px;
      color: #4A5568;
      line-height: 1.6;
      word-break: break-word;
    }
  }

  .no-characteristics {
    color: #A0AEC0;
    font-size: 14px;
    text-align: center;
    padding: 10px 0;
  }

  .pagination-section {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .empty-container {
    padding: 80px 40px;
    text-align: center;
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
}

@media (max-width: 768px) {
  .portrait-grid {
    grid-template-columns: 1fr;
  }
}
</style>
