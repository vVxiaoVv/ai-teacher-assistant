<template>
  <div class="video-correction-history">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="card-header">
          <h2 class="page-title">视频纠偏历史</h2>
        </div>
      </template>

      <!-- 搜索条件区域 -->
      <div class="search-section">
        <el-form :model="searchForm" :inline="true" class="search-form">
          <el-form-item label="视频URL">
            <el-input
              v-model="searchForm.videoUrl"
              placeholder="请输入视频URL（支持模糊查询）"
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
          
          <el-form-item label="提交人ID">
            <el-input
              v-model="searchForm.createUserId"
              placeholder="请输入提交人ID"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <div class="table-section">
        <el-table
          v-loading="loading"
          :data="tableData"
          stripe
          border
          style="width: 100%"
          :default-sort="{ prop: 'createTime', order: 'descending' }"
        >
          <el-table-column prop="id" label="ID" width="80" align="center" />
          
          <el-table-column prop="createUsername" label="提交人" width="120" align="center">
            <template #default="{ row }">
              <span>{{ row.createUsername || '未知' }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="videoUrl" label="视频URL" min-width="300" show-overflow-tooltip>
            <template #default="{ row }">
              <el-link
                :href="row.videoUrl"
                target="_blank"
                type="primary"
                :underline="false"
              >
                {{ row.videoUrl }}
              </el-link>
            </template>
          </el-table-column>
          
          <el-table-column prop="formattedMessage" label="分析结果摘要" min-width="400" show-overflow-tooltip>
            <template #default="{ row }">
              <span>{{ getSummary(row.formattedMessage) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="createTime" label="创建时间" width="180" align="center" sortable="custom">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="150" align="center" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                link
                :icon="View"
                @click="handleViewDetail(row)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页区域 -->
      <div class="pagination-section">
        <div class="pagination-left">
          <span class="pagination-info">共 {{ total }} 条记录</span>
          <el-select
            v-model="pageSize"
            style="width: 120px; margin-left: 16px"
            @change="handlePageSizeChange"
          >
            <el-option label="10 条/页" :value="10" />
            <el-option label="20 条/页" :value="20" />
            <el-option label="50 条/页" :value="50" />
            <el-option label="100 条/页" :value="100" />
          </el-select>
        </div>
        
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="prev, pager, next, jumper"
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="分析结果详情"
      width="70%"
      :close-on-click-modal="false"
    >
      <div class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="ID">{{ currentRecord.id }}</el-descriptions-item>
          <el-descriptions-item label="视频URL">
            <el-link
              :href="currentRecord.videoUrl"
              target="_blank"
              type="primary"
            >
              {{ currentRecord.videoUrl }}
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatTime(currentRecord.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="提交人">
            {{ currentRecord.createUsername || (currentRecord.createUserId ? `ID: ${currentRecord.createUserId}` : '未知') }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="detail-message">
          <h3>分析结果：</h3>
          <div class="message-content">
            <pre>{{ currentRecord.formattedMessage || currentRecord.rawMessage || '暂无内容' }}</pre>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleCopyResult">复制结果</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View } from '@element-plus/icons-vue'
import axios from 'axios'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索表单
const searchForm = reactive({
  videoUrl: '',
  startTime: null,
  endTime: null,
  createUserId: ''
})

// 详情对话框
const detailDialogVisible = ref(false)
const currentRecord = ref({
  id: null,
  videoUrl: '',
  formattedMessage: '',
  rawMessage: '',
  createTime: null,
  createUserId: null,
  createUsername: ''
})

// 获取摘要
const getSummary = (message) => {
  if (!message) return '暂无概述'
  // 取前150个字符
  return message.length > 150 ? message.substring(0, 150) + '...' : message
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
    // 构建查询参数
    const params = {
      page: currentPage.value - 1, // 后端从0开始
      size: pageSize.value
    }
    
    if (searchForm.videoUrl && searchForm.videoUrl.trim()) {
      params.videoUrl = searchForm.videoUrl.trim()
    }
    
    if (searchForm.startTime) {
      params.startTime = searchForm.startTime
    }
    
    if (searchForm.endTime) {
      params.endTime = searchForm.endTime
    }
    
    if (searchForm.createUserId && searchForm.createUserId.trim()) {
      const userId = parseInt(searchForm.createUserId.trim())
      if (!isNaN(userId)) {
        params.createUserId = userId
      }
    }
    
    const response = await axios.get('/api/video-analysis/history', { params })
    
    if (response.data && response.data.code === 0 && response.data.data) {
      const pageData = response.data.data
      tableData.value = pageData.content || []
      total.value = pageData.totalElements || 0
      currentPage.value = (pageData.number || 0) + 1 // 前端从1开始
    } else {
      ElMessage.error(response.data?.msg || '加载数据失败')
      tableData.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败，请稍后重试')
    tableData.value = []
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
  searchForm.videoUrl = ''
  searchForm.startTime = null
  searchForm.endTime = null
  searchForm.createUserId = ''
  currentPage.value = 1
  loadData()
}

// 页码改变
const handlePageChange = (page) => {
  currentPage.value = page
  loadData()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 每页条数改变
const handlePageSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

// 查看详情
const handleViewDetail = (row) => {
  currentRecord.value = { ...row }
  detailDialogVisible.value = true
}

// 复制结果
const handleCopyResult = async () => {
  const text = currentRecord.value.formattedMessage || currentRecord.value.rawMessage || ''
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('复制成功')
  } catch (error) {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    try {
      document.execCommand('copy')
      ElMessage.success('复制成功')
    } catch (err) {
      ElMessage.error('复制失败，请手动复制')
    }
    document.body.removeChild(textarea)
  }
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.video-correction-history {
  .page-card {
    border-radius: 8px;
    border: 1px solid #E2E8F0;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .page-title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #1A202C;
  }

  .search-section {
    margin-bottom: 20px;
    padding: 20px;
    background-color: #FAFBFC;
    border-radius: 8px;
    border: 1px solid #E2E8F0;
  }

  .search-form {
    margin: 0;
  }

  .table-section {
    margin-bottom: 20px;
  }

  .pagination-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 0;
    border-top: 1px solid #E2E8F0;
  }

  .pagination-left {
    display: flex;
    align-items: center;
  }

  .pagination-info {
    font-size: 14px;
    color: #4A5568;
    font-weight: 500;
  }

  .detail-content {
    .detail-message {
      margin-top: 20px;
      
      h3 {
        margin: 0 0 12px 0;
        font-size: 16px;
        font-weight: 600;
        color: #1A202C;
      }
      
      .message-content {
        background-color: #F7FAFC;
        border: 1px solid #E2E8F0;
        border-radius: 6px;
        padding: 16px;
        max-height: 500px;
        overflow-y: auto;
        
        pre {
          margin: 0;
          font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
          font-size: 14px;
          line-height: 1.6;
          color: #2D3748;
          white-space: pre-wrap;
          word-wrap: break-word;
        }
      }
    }
  }
}

:deep(.el-card__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #E2E8F0;
  background-color: #FAFBFC;
}

:deep(.el-card__body) {
  padding: 24px;
}

:deep(.el-table) {
  .el-table__header {
    th {
      background-color: #F7FAFC;
      color: #2D3748;
      font-weight: 600;
    }
  }
  
  .el-table__row {
    &:hover {
      background-color: #F7FAFC;
    }
  }
}

:deep(.el-pagination) {
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .search-form {
    :deep(.el-form-item) {
      width: 100%;
      margin-bottom: 16px;
    }
  }
  
  .pagination-section {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>











