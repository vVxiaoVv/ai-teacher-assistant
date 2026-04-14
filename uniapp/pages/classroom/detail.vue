<template>
  <view class="page">
    <view v-if="loading" class="loading-container">
      <text>加载中...</text>
    </view>

    <view v-else-if="classroom" class="detail-container">
      <view class="detail-header">
        <view class="header-title">{{ classroom.classroomName || '未命名课堂' }}</view>
        <view class="header-id">ID: {{ classroom.id }}</view>
      </view>

      <view class="detail-section">
        <view class="section-title">基本信息</view>
        <view class="info-item">
          <text class="info-label">课堂名称：</text>
          <text class="info-value">{{ classroom.classroomName || '-' }}</text>
        </view>
        <view v-if="classroom.description" class="info-item">
          <text class="info-label">课堂描述：</text>
          <text class="info-value">{{ classroom.description }}</text>
        </view>
      </view>

      <view class="detail-section">
        <view class="section-title">学生列表</view>
        <view v-if="students.length > 0" class="student-list">
          <view
            v-for="student in students"
            :key="student.id"
            class="student-item"
          >
            <view class="student-avatar">
              <image
                v-if="getAvatarUrl(student)"
                :src="getAvatarUrl(student)"
                mode="aspectFill"
                class="avatar-img"
              />
              <view v-else class="avatar-placeholder">
                <text>{{ getInitial(student.studentName) }}</text>
              </view>
            </view>
            <view class="student-info">
              <view class="student-name">{{ student.studentName }}</view>
              <view v-if="student.characteristics" class="student-desc">
                {{ getSummary(student.characteristics) }}
              </view>
            </view>
          </view>
        </view>
        <view v-else class="empty-students">
          <text>暂无学生</text>
        </view>
      </view>

      <view class="detail-actions">
        <view class="btn edit-btn" @tap="handleEdit">编辑</view>
      </view>
    </view>

    <view v-else class="error-container">
      <text>加载失败</text>
    </view>
  </view>
</template>

<script>
import { uniRequest } from '../../common/request.js';

export default {
  data() {
    return {
      loading: true,
      classroom: null,
      students: []
    };
  },
  onLoad(options) {
    if (options.id) {
      this.loadClassroomDetail(parseInt(options.id));
    } else {
      uni.showToast({
        title: '参数错误',
        icon: 'none'
      });
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
    }
  },
  methods: {
    async loadClassroomDetail(id) {
      this.loading = true;
      try {
        const res = await uniRequest({
          url: `/api/classroom/${id}`,
          method: 'GET'
        });
        
        if (res.statusCode === 200 && res.data) {
          // 后端直接返回 ClassroomDto，不是包装在 R 对象中
          let classroomData = null;
          if (res.data.classroomName !== undefined) {
            // 直接是 DTO 对象
            classroomData = res.data;
          } else if (res.data.code === 0 && res.data.data) {
            // 兼容包装在 R 对象中的情况
            classroomData = res.data.data;
          }
          
          if (classroomData) {
            this.classroom = classroomData;
            // 处理学生数据
            if (classroomData.students && Array.isArray(classroomData.students)) {
              this.students = classroomData.students;
            } else if (classroomData.studentIds && Array.isArray(classroomData.studentIds)) {
              // 如果有 studentIds，需要单独加载学生详情
              await this.loadStudentsByIds(classroomData.studentIds);
            }
          } else {
            console.error('响应数据格式错误:', res.data);
            uni.showToast({
              title: res.data.msg || '加载失败',
              icon: 'none'
            });
            setTimeout(() => {
              uni.navigateBack();
            }, 1500);
          }
        } else {
          console.error('请求失败:', res);
          uni.showToast({
            title: '请求失败',
            icon: 'none'
          });
          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        }
      } catch (error) {
        console.error('加载课堂详情失败:', error);
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        });
        setTimeout(() => {
          uni.navigateBack();
        }, 1500);
      } finally {
        this.loading = false;
      }
    },
    async loadStudentsByIds(studentIds) {
      try {
        // 批量加载学生信息
        const promises = studentIds.map(id =>
          uniRequest({
            url: `/api/student-portrait/${id}`,
            method: 'GET'
          })
        );
        const results = await Promise.all(promises);
        this.students = results
          .filter(res => res.statusCode === 200 && res.data && res.data.code === 0 && res.data.data)
          .map(res => res.data.data);
      } catch (error) {
        console.error('加载学生信息失败:', error);
      }
    },
    handleEdit() {
      uni.navigateTo({
        url: `/pages/classroom/add?id=${this.classroom.id}`
      });
    },
    getAvatarUrl(student) {
      if (student.photoUrl) {
        if (student.photoUrl.startsWith('http://') || student.photoUrl.startsWith('https://')) {
          return student.photoUrl;
        }
        return student.photoUrl;
      }
      return '';
    },
    getInitial(name) {
      if (!name) return '?';
      return name.charAt(0).toUpperCase();
    },
    getSummary(text) {
      if (!text) return '';
      if (text.length > 30) {
        return text.substring(0, 30) + '...';
      }
      return text;
    }
  }
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #F7F8FA;
  padding: 32rpx;
}

.loading-container,
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 200rpx 0;
  color: #86909C;
  font-size: 28rpx;
}

.detail-container {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 32rpx;
}

.detail-header {
  margin-bottom: 32rpx;
  padding-bottom: 24rpx;
  border-bottom: 1rpx solid #E5E5E5;
}

.header-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1F2329;
  margin-bottom: 8rpx;
}

.header-id {
  font-size: 24rpx;
  color: #86909C;
}

.detail-section {
  margin-bottom: 32rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #1F2329;
  margin-bottom: 16rpx;
}

.info-item {
  margin-bottom: 16rpx;
  font-size: 28rpx;
  line-height: 1.6;
}

.info-label {
  color: #646A73;
}

.info-value {
  color: #1F2329;
}

.student-list {
  margin-top: 16rpx;
}

.student-item {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  background: #F7F8FA;
  border-radius: 12rpx;
}

.student-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: #E5E5E5;
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #9B59B6, #8E44AD);
  color: #FFFFFF;
  font-size: 32rpx;
  font-weight: 600;
}

.student-info {
  flex: 1;
  min-width: 0;
}

.student-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #1F2329;
  margin-bottom: 8rpx;
}

.student-desc {
  font-size: 24rpx;
  color: #646A73;
  line-height: 1.5;
}

.empty-students {
  padding: 40rpx;
  text-align: center;
  color: #86909C;
  font-size: 28rpx;
}

.detail-actions {
  margin-top: 48rpx;
  padding-top: 32rpx;
  border-top: 1rpx solid #E5E5E5;
}

.btn {
  padding: 24rpx;
  border-radius: 8rpx;
  text-align: center;
  font-size: 32rpx;
  font-weight: 500;
}

.edit-btn {
  background: linear-gradient(135deg, #3498DB, #2980B9);
  color: #FFFFFF;
}
</style>

