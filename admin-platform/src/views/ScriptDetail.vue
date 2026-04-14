<template>
  <div class="script-detail">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="card-header">
          <h2 class="page-title">{{ script.title || '逐字稿详情' }}</h2>
          <div class="header-actions">
            <el-button type="primary" :icon="Download" @click="handleExportTxt">导出TXT</el-button>
            <el-button type="success" :icon="Download" @click="handleExportDocx">导出DOCX</el-button>
            <el-button :icon="Back" @click="handleBack">返回</el-button>
          </div>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>

      <div v-else-if="script.content" class="script-content">
        <div class="content-display" v-html="formatContent(script.content)"></div>
      </div>

      <div v-else class="empty-container">
        <el-empty description="逐字稿内容为空" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download, Back } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const script = ref({
  id: null,
  title: '',
  content: '',
  lessonPlanId: null,
  createTime: null
})

// 加载逐字稿详情
const loadScript = async () => {
  const scriptId = route.params.id
  if (!scriptId) {
    ElMessage.error('逐字稿ID不能为空')
    router.back()
    return
  }

  loading.value = true
  try {
    const response = await axios.get(`/api/script/${scriptId}`)
    if (response.status === 200 && response.data) {
      script.value = response.data
    } else {
      ElMessage.error('加载逐字稿失败')
      router.back()
    }
  } catch (error) {
    console.error('加载逐字稿失败:', error)
    ElMessage.error('加载逐字稿失败，请稍后重试')
    router.back()
  } finally {
    loading.value = false
  }
}

// 格式化内容显示
const formatContent = (content) => {
  if (!content) return ''
  
  // 将换行符转换为HTML换行
  let formatted = content.replace(/\n/g, '<br>')
  
  // 高亮"老师："和"学生姓名："
  formatted = formatted.replace(/(老师：|[\u4e00-\u9fa5]+：)/g, '<strong style="color: #5B8DEF;">$1</strong>')
  
  return formatted
}

// 导出TXT
const handleExportTxt = async () => {
  try {
    const response = await axios.get(`/api/script/${script.value.id}/export`, {
      responseType: 'blob'
    })
    
    if (response.status === 200) {
      const blob = new Blob([response.data], { type: 'text/plain;charset=utf-8' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${script.value.title || '逐字稿'}.txt`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      ElMessage.success('导出成功')
    }
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  }
}

// 导出DOCX
const handleExportDocx = async () => {
  try {
    const response = await axios.get(`/api/script/${script.value.id}/export/docx`, {
      responseType: 'blob'
    })
    
    if (response.status === 200) {
      const blob = new Blob([response.data], { 
        type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' 
      })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${script.value.title || '逐字稿'}.docx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      ElMessage.success('导出成功')
    }
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  }
}

// 返回
const handleBack = () => {
  router.back()
}

// 初始化
onMounted(() => {
  loadScript()
})
</script>

<style scoped lang="scss">
.script-detail {
  .page-card {
    border-radius: 8px;
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0;

    .page-title {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }

    .header-actions {
      display: flex;
      gap: 8px;
    }
  }

  .loading-container {
    padding: 40px;
  }

  .script-content {
    padding: 20px;
  }

  .content-display {
    max-height: calc(100vh - 300px);
    overflow-y: auto;
    white-space: pre-wrap;
    word-break: break-word;
    line-height: 1.8;
    font-size: 14px;
    color: #303133;
    padding: 20px;
    background-color: #fafafa;
    border-radius: 4px;
    border: 1px solid #e4e7ed;
  }

  .empty-container {
    padding: 80px 40px;
    text-align: center;
  }
}
</style>

