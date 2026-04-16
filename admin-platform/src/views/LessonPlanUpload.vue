<template>
  <div class="lesson-plan-upload">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="card-header">
          <h2 class="page-title">教案上传</h2>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="教案标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入教案标题"
            style="width: 500px"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="关联课堂" prop="classroomId">
          <el-select
            v-model="form.classroomId"
            placeholder="请选择关联课堂"
            style="width: 500px"
            filterable
            :loading="classroomLoading"
            @focus="loadClassrooms"
          >
            <el-option
              v-for="classroom in classroomList"
              :key="classroom.id"
              :label="`${classroom.name || classroom.classroomName} (${classroom.studentCount || 0} 名学生)`"
              :value="classroom.id"
            />
          </el-select>
          <div v-if="!hasClassrooms && !classroomLoading" style="margin-top: 8px; color: #909399; font-size: 12px">
            暂无课堂，<el-link type="primary" :underline="false" @click="goToCreateClassroom">去创建课堂</el-link>
          </div>
        </el-form-item>

        <el-form-item label="上传文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            accept=".txt,.doc,.docx,.pdf"
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 .txt, .doc, .docx, .pdf 格式文件
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Upload" @click="handleSubmit" :loading="submitting">
            上传教案
          </el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Upload, Refresh, UploadFilled } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()

const formRef = ref(null)
const uploadRef = ref(null)
const submitting = ref(false)
const selectedFile = ref(null)
const classroomList = ref([])
const classroomLoading = ref(false)
const hasClassrooms = ref(true)

const form = reactive({
  title: '',
  classroomId: null
})

const rules = {
  title: [
    { required: true, message: '请输入教案标题', trigger: 'blur' },
    { min: 1, max: 200, message: '标题长度在 1 到 200 个字符', trigger: 'blur' }
  ]
}

// 加载课堂列表
const loadClassrooms = async () => {
  if (classroomList.value.length > 0) return // 已加载过，不再重复加载
  
  classroomLoading.value = true
  try {
    const response = await axios.get('/api/classroom/list', {
      params: {
        page: 0,
        size: 1000
      }
    })
    
    if (response.status === 200 && response.data) {
      classroomList.value = response.data.content || []
      hasClassrooms.value = classroomList.value.length > 0
    }
  } catch (error) {
    console.error('加载课堂列表失败:', error)
    hasClassrooms.value = false
  } finally {
    classroomLoading.value = false
  }
}

// 跳转到创建课堂页面
const goToCreateClassroom = () => {
  router.push('/classroom')
}

onMounted(() => {
  loadClassrooms()
})

// 从文件名提取标题（去掉扩展名）
const extractTitleFromFilename = (filename) => {
  if (!filename) return ''
  // 去掉扩展名
  const lastDotIndex = filename.lastIndexOf('.')
  if (lastDotIndex > 0) {
    return filename.substring(0, lastDotIndex)
  }
  return filename
}

// 处理文件选择
const handleFileChange = (file) => {
  selectedFile.value = file.raw
  // 自动填充标题（如果标题为空）
  if (!form.title || form.title.trim() === '') {
    const filename = file.name
    const title = extractTitleFromFilename(filename)
    form.title = title
    ElMessage.success('已自动填充教案标题')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      // 检查是否选择了文件
      if (!selectedFile.value) {
        ElMessage.warning('请先选择教案文件')
        return
      }
      
      submitting.value = true
      try {
        const formData = new FormData()
        formData.append('title', form.title.trim())
        formData.append('content', selectedFile.value)
        if (form.classroomId) {
          formData.append('classroomId', form.classroomId)
        }
        
        const response = await axios.post('/api/lesson-plan/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        
        if (response.status === 200 && response.data) {
          const lessonPlanId = response.data.id
          ElMessage.success('教案上传成功，正在生成逐字稿...')
          
          // 自动生成逐字稿
          try {
            const scriptResponse = await axios.post(`/api/script/generate/${lessonPlanId}`)
            if (scriptResponse.status === 200 && scriptResponse.data) {
              ElMessage.success('逐字稿生成成功')
            } else {
              ElMessage.warning('教案上传成功，但逐字稿生成失败')
            }
          } catch (scriptError) {
            console.error('生成逐字稿失败:', scriptError)
            ElMessage.warning('教案上传成功，但逐字稿生成失败：' + (scriptError.response?.data || scriptError.message || '未知错误'))
          }
          
          // 跳转到教案列表页
          router.push('/lesson-plan')
        }
      } catch (error) {
        console.error('上传失败:', error)
        ElMessage.error(error.response?.data?.message || '上传失败，请稍后重试')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 重置表单
const handleReset = () => {
  formRef.value?.resetFields()
  uploadRef.value?.clearFiles()
  selectedFile.value = null
  form.title = ''
  form.classroomId = null
}
</script>

<style scoped lang="scss">
.lesson-plan-upload {
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
}
</style>






