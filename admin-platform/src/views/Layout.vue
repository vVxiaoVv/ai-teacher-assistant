<template>
  <div class="layout-container">
    <header class="navbar">
      <div class="navbar-left">
        <el-button
          class="sidebar-toggle"
          :icon="sidebarCollapsed ? Expand : Fold"
          @click="toggleSidebar"
        />
        <h1 class="logo">好师多磨管理平台</h1>
      </div>
      <div class="navbar-right">
        <el-dropdown trigger="click" placement="bottom-end">
          <div class="user-info">
            <el-avatar 
              :src="avatarUrl || getDefaultAvatar()" 
              :size="56" 
              class="avatar"
              @error="handleAvatarError"
            >
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <span class="username">{{ username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="layout-body">
      <aside class="sidebar" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
        <el-menu
          :default-active="activeMenu"
          :collapse="sidebarCollapsed"
          class="sidebar-menu"
          router
          background-color="#2D3748"
          text-color="#E8EDF3"
          active-text-color="#5B8DEF"
        >
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          
          <el-menu-item index="/classroom">
            <el-icon><School /></el-icon>
            <template #title>课堂管理</template>
          </el-menu-item>
          
          <el-menu-item index="/student-portrait">
            <el-icon><User /></el-icon>
            <template #title>学生画像管理</template>
          </el-menu-item>
          
          <el-sub-menu index="lesson-plan-menu">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>教案管理</span>
            </template>
            <el-menu-item index="/lesson-plan">
              <el-icon><List /></el-icon>
              <template #title>教案列表</template>
            </el-menu-item>
            <el-menu-item index="/lesson-plan/upload">
              <el-icon><Upload /></el-icon>
              <template #title>上传教案</template>
            </el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="/video-examples">
            <el-icon><VideoCamera /></el-icon>
            <template #title>视频范例管理</template>
          </el-menu-item>
          
          <el-menu-item index="/teacher-portrait">
            <el-icon><Avatar /></el-icon>
            <template #title>教师画像</template>
          </el-menu-item>
        </el-menu>
      </aside>

      <main class="main-content" :class="{ 'main-content-expanded': sidebarCollapsed }">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowDown, SwitchButton, Fold, Expand, 
  HomeFilled, User, Document, VideoCamera, 
  UserFilled, School, Upload, List, Avatar 
} from '@element-plus/icons-vue'
import { getCookie, clearLoginInfo } from '@/utils'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

const username = ref(getCookie('username') || '管理员')
const avatarUrl = ref('')
const sidebarCollapsed = ref(false)

const getDefaultAvatar = () => {
  if (username.value) {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(username.value)}&size=200&background=5B8DEF&color=fff`
  }
  return ''
}

const handleAvatarError = () => {
  avatarUrl.value = getDefaultAvatar()
}

const fetchUserInfo = async () => {
  try {
    const response = await axios.get('/api/user/info', {
      withCredentials: true
    })
    
    if (response.data && response.data.code === 0 && response.data.data) {
      const userInfo = response.data.data
      username.value = userInfo.username || username.value
      if (userInfo.avatarUrl && userInfo.avatarUrl.trim()) {
        avatarUrl.value = userInfo.avatarUrl.trim()
      } else {
        avatarUrl.value = getDefaultAvatar()
      }
    } else {
      avatarUrl.value = getDefaultAvatar()
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    avatarUrl.value = getDefaultAvatar()
  }
}

onMounted(() => {
  fetchUserInfo()
})

const activeMenu = computed(() => {
  const path = route.path
  if (path === '/lesson-plan' || path === '/lesson-plan/upload') {
    return path
  }
  return path
})

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    clearLoginInfo()
    ElMessage.success('退出成功')
    router.push('/login')
  } catch (error) {
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #F5F7FA;
}

.navbar {
  height: 75px;
  background: linear-gradient(135deg, #5B8DEF 0%, #4A7AD9 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .sidebar-toggle {
    background: transparent;
    border: none;
    color: #FFFFFF;
    padding: 8px;
    cursor: pointer;
    font-size: 18px;
    
    &:hover {
      background-color: rgba(255, 255, 255, 0.1);
    }
  }
  
  .logo {
    margin: 0;
    font-size: 27px;
    font-weight: 600;
    color: #FFFFFF;
    letter-spacing: 0.5px;
  }
}

.navbar-right {
  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 6px 12px;
    border-radius: 6px;
    transition: background-color 0.3s;
    
    &:hover {
      background-color: rgba(255, 255, 255, 0.1);
    }
    
    .avatar {
      border: 2px solid rgba(255, 255, 255, 0.3);
      flex-shrink: 0;
      background-color: rgba(255, 255, 255, 0.2);
      border-radius: 16px !important;
      
      :deep(.el-avatar) {
        border-radius: 16px !important;
      }
      
      :deep(img) {
        border-radius: 16px;
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
    
    .username {
      color: #FFFFFF;
      font-weight: 500;
      font-size: 21px;
    }
    
    .el-icon {
      color: #FFFFFF;
      font-size: 21px;
    }
  }
}

.layout-body {
  display: flex;
  margin-top: 75px;
  min-height: calc(100vh - 75px);
}

.sidebar {
  width: 230px;
  background-color: #2D3748;
  position: fixed;
  top: 75px;
  left: 0;
  bottom: 0;
  z-index: 999;
  transition: width 0.3s;
  overflow: hidden;
  
  &.sidebar-collapsed {
    width: 64px;
  }
}

.sidebar-menu {
  border-right: none;
  width: 100%;
  height: 100%;
  overflow-y: auto;
  
  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    height: 50px;
    line-height: 50px;
    padding-left: 20px !important;
    color: #E8EDF3;
    
    &:hover {
      background-color: rgba(91, 141, 239, 0.1);
      color: #5B8DEF;
    }
  }
  
  :deep(.el-menu-item.is-active) {
    background-color: rgba(91, 141, 239, 0.15);
    color: #5B8DEF;
    border-right: 3px solid #5B8DEF;
  }
  
  :deep(.el-sub-menu) {
    .el-menu {
      background-color: rgba(0, 0, 0, 0.15);
      
      .el-menu-item {
        padding-left: 40px !important;
        height: 45px;
        line-height: 45px;
      }
    }
  }
  
  :deep(.el-menu-icon) {
    width: 24px;
    margin-right: 8px;
    font-size: 18px;
  }
}

.main-content {
  flex: 1;
  margin-left: 230px;
  padding: 20px;
  overflow-y: auto;
  transition: margin-left 0.3s;
  
  &.main-content-expanded {
    margin-left: 64px;
  }
}
</style>
