<template>
  <div class="dashboard">

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card" shadow="hover" @click="navigateTo('student-portrait')">
        <div class="stat-content">
          <div class="stat-icon student-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.studentCount }}</div>
            <div class="stat-label">学生画像</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover" @click="navigateTo('lesson-plan')">
        <div class="stat-content">
          <div class="stat-icon lesson-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.lessonCount }}</div>
            <div class="stat-label">教案文件</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover" @click="navigateTo('video-examples')">
        <div class="stat-content">
          <div class="stat-icon video-icon">
            <el-icon><VideoCamera /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.videoCount }}</div>
            <div class="stat-label">视频范例</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 最近活动 -->
    <el-card class="activity-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">最近活动</span>
        </div>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="(activity, index) in recentActivities"
          :key="index"
          :timestamp="activity.time"
          :icon="getActivityIcon(activity.type)"
          placement="top"
        >
          <div class="activity-content">
            <p class="activity-text">{{ activity.content }}</p>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Document, VideoCamera, Clock, SuccessFilled } from '@element-plus/icons-vue'
import axios from 'axios'
import { getCookie } from '@/utils'

const router = useRouter()

const username = ref(getCookie('username') || '管理员')

const stats = ref({
  studentCount: 0,
  lessonCount: 0,
  videoCount: 0
})

const recentActivities = ref([
  {
    time: '2024-12-20 14:30',
    content: '上传了新的视频范例："小学语文阅读教学示范课"',
    type: 'video'
  },
  {
    time: '2024-12-19 09:15',
    content: '创建了15个新的学生画像',
    type: 'student'
  },
  {
    time: '2024-12-18 16:45',
    content: '更新了教案列表，添加了5个新教案',
    type: 'lesson'
  }
])

const getActivityIcon = (type) => {
  const iconMap = {
    video: VideoCamera,
    student: User,
    lesson: Document,
    system: SuccessFilled
  }
  return iconMap[type] || Clock
}

// 导航到指定页面
const navigateTo = (path) => {
  router.push(`/${path}`)
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const response = await axios.get('/api/dashboard/stats')
    stats.value = response.data
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 失败时使用默认值
    stats.value = {
      studentCount: 0,
      lessonCount: 0,
      videoCount: 0
    }
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped lang="scss">
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.welcome-section {
  margin-bottom: 32px;
  padding-bottom: 24px;
}

.welcome-title {
  font-size: 32px;
  font-weight: 700;
  color: #1A202C;
  margin: 0 0 12px 0;
  letter-spacing: -0.5px;
}

.welcome-subtitle {
  font-size: 16px;
  color: #4A5568;
  margin: 0;
  font-weight: 500;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  transition: all 0.3s ease;
  cursor: pointer;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(91, 141, 239, 0.15);
    border-color: #5B8DEF;
  }
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 8px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  flex-shrink: 0;
  
  &.student-icon {
    background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%);
  }
  
  &.lesson-icon {
    background: linear-gradient(135deg, #F093FB 0%, #F5576C 100%);
  }
  
  &.video-icon {
    background: linear-gradient(135deg, #4FACFE 0%, #00F2FE 100%);
  }
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 36px;
  font-weight: 800;
  color: #1A202C;
  line-height: 1;
  margin-bottom: 8px;
  letter-spacing: -1px;
}

.stat-label {
  font-size: 14px;
  color: #4A5568;
  font-weight: 500;
}

.activity-card {
  margin-bottom: 24px;
  border-radius: 12px;
  border: 1px solid #E2E8F0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1A202C;
}

.activity-content {
  padding: 8px 0;
}

.activity-text {
  margin: 0;
  color: #2D3748;
  font-size: 14px;
  line-height: 1.6;
  font-weight: 500;
}

:deep(.el-timeline-item__timestamp) {
  color: #718096;
  font-size: 13px;
  font-weight: 500;
}

:deep(.el-timeline-item__node) {
  background-color: #5B8DEF;
  border-color: #5B8DEF;
  width: 12px;
  height: 12px;
}

:deep(.el-card__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #E2E8F0;
  background-color: #FAFBFC;
}

:deep(.el-card__body) {
  padding: 24px;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-actions {
    flex-direction: column;
    
    .el-button {
      width: 100%;
    }
  }
}
</style>

