<template>
  <div class="video-examples">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="card-header">
          <h2 class="page-title">视频范例管理</h2>
          <el-button type="primary" :icon="Plus" @click="handleAdd">添加视频范例</el-button>
        </div>
      </template>

      <!-- 搜索条件区域 -->
      <div class="search-section">
        <el-form :model="searchForm" :inline="true" class="search-form">
          <el-form-item label="视频主题">
            <el-input
              v-model="searchForm.topic"
              placeholder="请输入视频主题（支持模糊查询）"
              clearable
              style="width: 300px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          
          <el-form-item label="关联教案">
            <el-input
              v-model="searchForm.lessonPlanId"
              placeholder="请输入教案ID"
              clearable
              style="width: 200px"
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
          :default-sort="{ prop: 'createTime', order: 'descending' }"
        >
          <el-table-column prop="id" label="ID" width="80" align="center" />
          
          <el-table-column prop="topic" label="视频主题" min-width="200" show-overflow-tooltip />
          
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
          
          <el-table-column prop="lessonPlanId" label="关联教案ID" width="120" align="center" />
          
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <span>{{ row.description || '暂无描述' }}</span>
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

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="handleCloseDialog"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="关联教案ID" prop="lessonPlanId">
          <el-input-number
            v-model="form.lessonPlanId"
            :min="1"
            placeholder="请输入关联的教案ID"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="视频URL" prop="videoUrl">
          <el-input
            v-model="form.videoUrl"
            placeholder="请输入视频URL"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="视频主题" prop="topic">
          <el-input
            v-model="form.topic"
            placeholder="请输入视频主题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="视频描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入视频描述（可选）"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="handleCloseDialog">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="视频范例详情"
      width="70%"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ currentRecord.id }}</el-descriptions-item>
        <el-descriptions-item label="视频主题">{{ currentRecord.topic }}</el-descriptions-item>
        <el-descriptions-item label="视频URL" :span="2">
          <el-link :href="currentRecord.videoUrl" target="_blank" type="primary">
            {{ currentRecord.videoUrl }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item label="关联教案ID">{{ currentRecord.lessonPlanId }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(currentRecord.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          {{ currentRecord.description || '暂无描述' }}
        </el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, View, Plus } from '@element-plus/icons-vue'
import axios from 'axios'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  topic: '',
  lessonPlanId: '',
  startTime: null,
  endTime: null
})

const dialogVisible = ref(false)
const dialogTitle = computed(() => '添加视频范例')
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  lessonPlanId: null,
  videoUrl: '',
  topic: '',
  description: ''
})

const rules = {
  lessonPlanId: [
    { required: true, message: '请输入关联教案ID', trigger: 'blur' },
    { type: 'number', min: 1, message: '教案ID必须大于0', trigger: 'blur' }
  ],
  videoUrl: [
    { required: true, message: '请输入视频URL', trigger: 'blur' },
    { max: 500, message: 'URL长度不能超过500个字符', trigger: 'blur' }
  ],
  topic: [
    { required: true, message: '请输入视频主题', trigger: 'blur' },
    { max: 200, message: '主题长度不能超过200个字符', trigger: 'blur' }
  ]
}

const detailDialogVisible = ref(false)
const currentRecord = ref({
  id: null,
  topic: '',
  videoUrl: '',
  lessonPlanId: null,
  description: '',
  createTime: null
})

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
    
    if (searchForm.topic && searchForm.topic.trim()) {
      params.topic = searchForm.topic.trim()
    }
    
    if (searchForm.lessonPlanId && searchForm.lessonPlanId.trim()) {
      const lessonPlanId = parseInt(searchForm.lessonPlanId.trim())
      if (!isNaN(lessonPlanId)) {
        params.lessonPlanId = lessonPlanId
      }
    }
    
    if (searchForm.startTime) {
      params.startTime = searchForm.startTime
    }
    
    if (searchForm.endTime) {
      params.endTime = searchForm.endTime
    }
    
    const response = await axios.get('/api/video-example/list', { params })
    
    if (response.status === 200 && response.data) {
      const pageData = response.data
      tableData.value = pageData.content || []
      total.value = pageData.totalElements || 0
      currentPage.value = (pageData.number || 0) + 1
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
  searchForm.topic = ''
  searchForm.lessonPlanId = ''
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
  form.lessonPlanId = null
  form.videoUrl = ''
  form.topic = ''
  form.description = ''
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const params = new URLSearchParams()
        params.append('lessonPlanId', form.lessonPlanId)
        params.append('videoUrl', form.videoUrl)
        params.append('topic', form.topic)
        if (form.description) {
          params.append('description', form.description)
        }
        
        const response = await axios.post('/api/video-example/save', params, {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        })
        
        if (response.status === 200) {
          ElMessage.success('保存成功')
          handleCloseDialog()
          loadData()
        }
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error(error.response?.data?.message || '保存失败，请稍后重试')
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
  form.lessonPlanId = null
  form.videoUrl = ''
  form.topic = ''
  form.description = ''
}

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const response = await axios.get(`/api/video-example/${row.id}`)
    if (response.status === 200) {
      const data = response.data
      currentRecord.value = {
        id: data.id,
        topic: data.topic,
        videoUrl: data.videoUrl,
        lessonPlanId: data.lessonPlan?.id || data.lessonPlanId,
        description: data.description || '',
        createTime: data.createTime
      }
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.video-examples {
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
}
</style>






