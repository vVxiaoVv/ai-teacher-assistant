<template>
  <div class="lesson-plan-list">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="card-header">
          <h2 class="page-title">教案列表</h2>
          <el-button type="primary" :icon="Plus" @click="handleUpload">上传教案</el-button>
        </div>
      </template>

      <!-- 搜索条件区域 -->
      <div class="search-section">
        <el-form :model="searchForm" :inline="true" class="search-form">
          <el-form-item label="教案标题">
            <el-input
              v-model="searchForm.title"
              placeholder="请输入教案标题（支持模糊查询）"
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

      <!-- 数据表格 -->
      <div class="table-section">
        <el-table
          v-loading="loading"
          :data="tableData"
          stripe
          border
          style="width: 100%"
          :default-sort="{ prop: 'uploadTime', order: 'descending' }"
        >
          <el-table-column prop="id" label="ID" width="80" align="center" />
          
          <el-table-column prop="title" label="教案标题" min-width="300" show-overflow-tooltip />
          
          <el-table-column prop="uploadTime" label="上传时间" width="180" align="center" sortable="custom">
            <template #default="{ row }">
              {{ formatTime(row.uploadTime) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="280" align="center" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                link
                :icon="View"
                @click="handleViewDetail(row)"
              >
                查看详情
              </el-button>
              <el-button
                v-if="!row.hasScript"
                type="success"
                link
                :icon="Document"
                :loading="generatingScript[row.id]"
                @click="handleGenerateScript(row)"
              >
                生成逐字稿
              </el-button>
              <el-button
                v-else
                type="info"
                link
                :icon="Document"
                @click="handleViewScript(row)"
              >
                查看逐字稿
              </el-button>
            </template>
          </el-table-column>
        </el-table>

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
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="教案详情"
      width="70%"
      :before-close="handleCloseDetail"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ currentRecord.id }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentRecord.title }}</el-descriptions-item>
        <el-descriptions-item label="上传时间" :span="2">
          {{ formatTime(currentRecord.uploadTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">
          <div class="content-display">{{ currentRecord.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleCopyContent">复制内容</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Refresh, View, Document, Plus } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  title: '',
  startTime: null,
  endTime: null
})

const detailDialogVisible = ref(false)
const currentRecord = ref({
  id: null,
  title: '',
  content: '',
  uploadTime: null
})

const generatingScript = ref({})

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
    
    if (searchForm.title && searchForm.title.trim()) {
      params.title = searchForm.title.trim()
    }
    
    if (searchForm.startTime) {
      params.startTime = searchForm.startTime
    }
    
    if (searchForm.endTime) {
      params.endTime = searchForm.endTime
    }
    
    const response = await axios.get('/api/lesson-plan/list', { params })
    
    if (response.status === 200 && response.data) {
      const pageData = response.data
      tableData.value = pageData.content || []
      total.value = pageData.totalElements || 0
      currentPage.value = (pageData.number || 0) + 1
      
      // 检查每个教案是否有逐字稿
      for (const item of tableData.value) {
        await checkScriptStatus(item)
      }
    } else {
      ElMessage.error('加载数据失败')
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
  searchForm.title = ''
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

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const response = await axios.get(`/api/lesson-plan/${row.id}`)
    if (response.status === 200) {
      currentRecord.value = response.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 关闭详情
const handleCloseDetail = () => {
  detailDialogVisible.value = false
  currentRecord.value = {
    id: null,
    title: '',
    content: '',
    uploadTime: null
  }
}

// 复制内容
const handleCopyContent = async () => {
  const text = currentRecord.value.content || ''
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('复制成功')
  } catch (error) {
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

// 检查逐字稿状态
const checkScriptStatus = async (lessonPlan) => {
  try {
    const response = await axios.get(`/api/script/lesson-plan/${lessonPlan.id}`)
    lessonPlan.hasScript = response.status === 200 && response.data != null
  } catch (error) {
    lessonPlan.hasScript = false
  }
}

// 生成逐字稿
const handleGenerateScript = async (row) => {
  generatingScript.value[row.id] = true
  try {
    const response = await axios.post(`/api/script/generate/${row.id}`)
    if (response.status === 200 && response.data) {
      ElMessage.success('逐字稿生成成功')
      row.hasScript = true
      // 跳转到逐字稿详情页
      router.push(`/script/${response.data.id}`)
    } else {
      ElMessage.error('生成逐字稿失败')
    }
  } catch (error) {
    console.error('生成逐字稿失败:', error)
    let errorMessage = '生成逐字稿失败，请稍后重试'
    if (error.response) {
      // 后端返回的错误信息
      if (typeof error.response.data === 'string') {
        errorMessage = error.response.data
      } else if (error.response.data?.message) {
        errorMessage = error.response.data.message
      } else if (error.response.status === 400) {
        errorMessage = '请求参数错误，请检查教案是否存在'
      }
    }
    ElMessage.error(errorMessage)
  } finally {
    generatingScript.value[row.id] = false
  }
}

// 查看逐字稿
const handleViewScript = async (row) => {
  try {
    const response = await axios.get(`/api/script/lesson-plan/${row.id}`)
    if (response.status === 200 && response.data) {
      router.push(`/script/${response.data.id}`)
    } else {
      ElMessage.error('获取逐字稿失败')
    }
  } catch (error) {
    console.error('获取逐字稿失败:', error)
    ElMessage.error('获取逐字稿失败')
  }
}

// 跳转到上传页面
const handleUpload = () => {
  router.push('/lesson-plan/upload')
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.lesson-plan-list {
  .page-card {
    border-radius: 8px;
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .page-title {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
  }

  .search-section {
    margin-bottom: 20px;
  }

  .table-section {
    .pagination-section {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .content-display {
    max-height: 400px;
    overflow-y: auto;
    white-space: pre-wrap;
    word-break: break-word;
  }
}
</style>

