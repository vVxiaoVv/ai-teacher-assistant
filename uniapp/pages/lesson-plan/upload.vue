<template>
  <view class="page">
    <view class="container">
      <view class="header">
        <text class="title">上传教案</text>
      </view>
      
      <view class="form">
        <view class="form-item">
          <text class="label required">教案标题</text>
          <input 
            class="title-input"
            id="title-input"
            v-model="title"
            placeholder="请输入教案标题"
            type="text"
            focus
            @input="handleInputDebug"
          />
        </view>
        
        <view class="form-item">
          <text class="label required">关联课堂</text>
          <view class="classroom-select" @tap="showClassroomPicker">
            <text :class="{'placeholder': !selectedClassroomName}">
              {{ selectedClassroomName || '请选择关联课堂' }}
            </text>
            <text v-if="selectedClassroomName" class="selected-count">
              ({{ selectedStudentCount }} 名学生)
            </text>
            <text class="arrow-right">›</text>
          </view>
          <view v-if="!hasClassrooms && !classroomLoading" class="no-classroom-tip">
            <text>暂无课堂，</text>
            <text class="create-link" @tap="goToCreateClassroom">去创建课堂</text>
          </view>
        </view>
        
        <view class="form-item">
          <text class="label required">教案文件</text>
          <view class="file-upload">
            <button 
              class="upload-btn"
              @tap="chooseFile"
              :disabled="isSubmitting"
            >
              {{ selectedFileName || '选择教案文件' }}
            </button>
            <view class="file-tip">支持TXT、DOC、DOCX等文本文件格式</view>
          </view>
        </view>
        
        <button class="submit-btn" @tap="upload" :disabled="isSubmitting || !selectedFile || !selectedClassroomId">
          {{ isSubmitting ? '上传中...' : '上传教案' }}
        </button>
      </view>
      
      <!-- 课堂选择弹窗 -->
      <view v-if="showClassroomPickerModal" class="picker-modal" @tap="hideClassroomPicker">
        <view class="picker-content" @tap.stop>
          <view class="picker-header">
            <text class="picker-title">选择关联课堂</text>
            <text class="picker-close" @tap="hideClassroomPicker">×</text>
          </view>
          <view class="picker-search">
            <input
              class="picker-search-input"
              v-model="classroomSearchKeyword"
              placeholder="搜索课堂名称"
              @input="filterClassrooms"
            />
          </view>
          <scroll-view class="picker-list" scroll-y>
            <view
              v-for="classroom in filteredClassrooms"
              :key="classroom.id"
              class="picker-item"
              :class="{ selected: selectedClassroomId === classroom.id }"
              @tap="selectClassroom(classroom)"
            >
              <view class="picker-item-content">
                <text class="picker-item-name">{{ classroom.name || classroom.classroomName }}</text>
                <text class="picker-item-count">{{ classroom.studentCount || 0 }} 名学生</text>
              </view>
              <text v-if="selectedClassroomId === classroom.id" class="picker-item-check">✓</text>
            </view>
            <view v-if="filteredClassrooms.length === 0" class="picker-empty">
              <text>暂无课堂数据</text>
            </view>
          </scroll-view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { uniRequest, API_BASE_URL } from '../../common/request.js';

export default {
  data() {
    return {
      title: '',
      selectedFile: null,
      selectedFileName: '',
      isSubmitting: false,
      // 课堂选择相关
      showClassroomPickerModal: false,
      classroomList: [],
      filteredClassrooms: [],
      classroomSearchKeyword: '',
      selectedClassroomId: null,
      selectedClassroomName: '',
      selectedStudentCount: 0,
      classroomLoading: false,
      hasClassrooms: true,
      // 重定向参数
      redirectTo: ''
    };
  },
  onLoad(options) {
    this.loadClassrooms();
    // 检查是否有重定向参数
    if (options.redirect) {
      this.redirectTo = options.redirect;
    }
  },
  methods: {
    chooseFile() {
      uni.chooseFile({
        count: 1,
        extension: ['.txt', '.doc', '.docx', '.pdf'],
        success: (res) => {
          this.selectedFile = res.tempFiles[0];
          this.selectedFileName = this.selectedFile.name;
          
          // 自动填充标题（如果标题为空）
          if (!this.title || this.title.trim() === '') {
            const filename = this.selectedFile.name;
            const lastDotIndex = filename.lastIndexOf('.');
            if (lastDotIndex > 0) {
              this.title = filename.substring(0, lastDotIndex);
            } else {
              this.title = filename;
            }
          }
        },
        fail: (err) => {
          console.error('选择文件失败：', err);
          uni.showToast({
            title: '选择文件失败，请重试',
            icon: 'none'
          });
        }
      });
    },
    handleInputDebug(e) {
      console.log('输入事件触发:', e);
      console.log('当前输入值:', e.detail.value);
      console.log('数据中的title:', this.title);
    },
    async loadClassrooms() {
      this.classroomLoading = true;
      try {
        const res = await uniRequest({
          url: '/api/classroom/list',
          method: 'GET',
          data: {
            page: 0,
            size: 1000
          }
        });
        
        if (res.statusCode === 200 && res.data) {
          if (res.data.content !== undefined) {
            this.classroomList = res.data.content || [];
            this.filteredClassrooms = this.classroomList;
            this.hasClassrooms = this.classroomList.length > 0;
          } else if (res.data.code === 0 && res.data.data) {
            this.classroomList = res.data.data.content || [];
            this.filteredClassrooms = this.classroomList;
            this.hasClassrooms = this.classroomList.length > 0;
          }
        }
      } catch (error) {
        console.error('加载课堂列表失败:', error);
        this.hasClassrooms = false;
      } finally {
        this.classroomLoading = false;
      }
    },
    showClassroomPicker() {
      if (this.classroomList.length === 0) {
        this.loadClassrooms();
      }
      this.showClassroomPickerModal = true;
      this.classroomSearchKeyword = '';
      this.filteredClassrooms = this.classroomList;
    },
    hideClassroomPicker() {
      this.showClassroomPickerModal = false;
    },
    filterClassrooms() {
      const keyword = this.classroomSearchKeyword.trim().toLowerCase();
      if (!keyword) {
        this.filteredClassrooms = this.classroomList;
      } else {
        this.filteredClassrooms = this.classroomList.filter(classroom => {
          const name = (classroom.name || classroom.classroomName || '').toLowerCase();
          return name.includes(keyword);
        });
      }
    },
    selectClassroom(classroom) {
      this.selectedClassroomId = classroom.id;
      this.selectedClassroomName = classroom.name || classroom.classroomName;
      this.selectedStudentCount = classroom.studentCount || 0;
      this.hideClassroomPicker();
    },
    goToCreateClassroom() {
      uni.navigateTo({
        url: '/pages/classroom/add'
      });
    },
    upload() {
      // 表单验证
      if (!this.title.trim()) {
        uni.showToast({
          title: '请输入教案标题',
          icon: 'none'
        });
        return;
      }
      
      if (!this.selectedClassroomId) {
        uni.showToast({
          title: '请选择关联课堂',
          icon: 'none'
        });
        return;
      }
      
      if (!this.selectedFile) {
        uni.showToast({
          title: '请选择教案文件',
          icon: 'none'
        });
        return;
      }
      
      // 标记为正在提交
      this.isSubmitting = true;
      
      // 调用后端API上传教案
      uni.uploadFile({
        url: API_BASE_URL + '/api/lesson-plan/upload',
        filePath: this.selectedFile.path,
        name: 'content',
        formData: {
          title: this.title.trim(),
          classroomId: this.selectedClassroomId
        },
        success: async (res) => {
          if (res.statusCode === 200) {
            try {
              const data = JSON.parse(res.data);
              if (data && data.id) {
                const lessonPlanId = data.id;
                
                // 如果是虚拟课堂重定向，等待逐字稿生成后跳转
                if (this.redirectTo === 'virtual-classroom') {
                  uni.showLoading({
                    title: '正在生成逐字稿...'
                  });
                  
                  try {
                    const scriptRes = await uniRequest({
                      url: `/api/script/generate/${lessonPlanId}`,
                      method: 'POST',
                      timeout: 300000 // 5分钟超时
                    });
                    
                    uni.hideLoading();
                    
                    if (scriptRes.statusCode === 200 && scriptRes.data) {
                      const scriptContent = scriptRes.data.content || scriptRes.data;
                      
                      uni.showToast({
                        title: '逐字稿生成成功',
                        icon: 'success',
                        duration: 1500
                      });
                      
                      // 跳转到虚拟课堂录制页面
                      setTimeout(() => {
                        uni.navigateTo({
                          url: '/pages/virtual-classroom/record',
                          success: (navRes) => {
                            navRes.eventChannel.emit('scriptData', {
                              lessonPlanId: lessonPlanId,
                              lessonPlanTitle: this.title,
                              scriptContent: typeof scriptContent === 'string' ? scriptContent : JSON.stringify(scriptContent)
                            });
                          }
                        });
                      }, 1500);
                    } else {
                      uni.showToast({
                        title: '逐字稿生成失败',
                        icon: 'none'
                      });
                    }
                  } catch (scriptError) {
                    uni.hideLoading();
                    console.error('生成逐字稿失败:', scriptError);
                    uni.showToast({
                      title: '逐字稿生成失败，请稍后重试',
                      icon: 'none'
                    });
                  }
                } else {
                  // 原有逻辑：先启动逐字稿生成（不等待结果）
                  const generateScriptPromise = uniRequest({
                    url: `/api/script/generate/${lessonPlanId}`,
                    method: 'POST',
                    timeout: 300000 // 5分钟超时
                  }).then(scriptRes => {
                    if (scriptRes.statusCode === 200) {
                      console.log('逐字稿生成成功');
                    } else {
                      console.warn('逐字稿生成失败');
                    }
                  }).catch(scriptError => {
                    console.error('生成逐字稿失败:', scriptError);
                  });
                  
                  // 显示成功提示
                  uni.showToast({
                    title: '教案上传成功',
                    icon: 'success',
                    duration: 1500
                  });
                  
                  // 立即跳转到教案列表页（不等待逐字稿生成）
                  setTimeout(() => {
                    // 优先使用 navigateBack，如果失败则使用 redirectTo
                    uni.navigateBack({
                      delta: 1,
                      fail: (err) => {
                        console.log('navigateBack 失败，使用 redirectTo:', err);
                        uni.redirectTo({
                          url: '/pages/lesson-plan/index',
                          fail: (redirectErr) => {
                            console.error('redirectTo 也失败:', redirectErr);
                            // 如果都失败，尝试使用 switchTab（如果教案列表是 tabBar 页面）
                            uni.switchTab({
                              url: '/pages/lesson-plan/index',
                              fail: (switchErr) => {
                                console.error('所有跳转方式都失败:', switchErr);
                                uni.showToast({
                                  title: '请手动返回列表页',
                                  icon: 'none',
                                  duration: 2000
                                });
                              }
                            });
                          }
                        });
                      }
                    });
                  }, 1500);
                }
              } else {
                throw new Error('无效的响应数据');
              }
            } catch (e) {
              console.error('解析响应失败：', e);
              uni.showToast({
                title: '上传失败，请重试',
                icon: 'none'
              });
            }
          } else {
            uni.showToast({
              title: '上传失败，请重试',
              icon: 'none'
            });
          }
        },
        fail: (err) => {
          console.error('上传失败：', err);
          uni.showToast({
            title: '网络错误，请重试',
            icon: 'none'
          });
        },
        complete: () => {
          // 重置提交状态
          this.isSubmitting = false;
        }
      });
    }
  }
};
</script>

<style>
.page {
  min-height: 100vh;
  background-color: #F7F8FA;
}

.container {
  padding: 20rpx;
  width: 100%;
  box-sizing: border-box;
}

.header {
  margin-bottom: 30rpx;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.form {
  background-color: #fff;
  padding: 20rpx;
  border-radius: 10rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
  width: 100%;
  box-sizing: border-box;
}

.form-item {
  margin-bottom: 30rpx;
  width: 100%;
  box-sizing: border-box;
}

.label {
  display: block;
  margin-bottom: 10rpx;
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}

.input {
  width: 100%;
  padding: 20rpx;
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  z-index: 10;
  position: relative;
}

.title-input {
  display: block;
  width: 100%;
  padding: 20rpx;
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  z-index: 9999;
  position: relative;
  min-height: 80rpx;
  line-height: 40rpx;
  background-color: #ffffff;
  color: #333333;
  outline: none;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-upload {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.upload-btn {
  width: 100%;
  padding: 20rpx;
  background-color: #f5f5f5;
  color: #333;
  border: 1rpx dashed #ddd;
  border-radius: 8rpx;
  font-size: 28rpx;
}

.upload-btn[disabled] {
  background-color: #f0f0f0;
  color: #999;
}

.file-tip {
  font-size: 24rpx;
  color: #999;
  text-align: center;
}

.submit-btn {
  width: 100%;
  padding: 20rpx;
  background-color: #007AFF;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  font-size: 32rpx;
  font-weight: 500;
}

.submit-btn[disabled] {
  background-color: #ccc;
}

.required {
  color: #F53F3F;
}

.classroom-select {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx;
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
  background-color: #fff;
  min-height: 80rpx;
}

.classroom-select .placeholder {
  color: #999;
}

.classroom-select .selected-count {
  color: #666;
  font-size: 24rpx;
  margin-left: 10rpx;
}

.arrow-right {
  color: #999;
  font-size: 32rpx;
  margin-left: auto;
}

.no-classroom-tip {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #999;
}

.create-link {
  color: #007AFF;
  text-decoration: underline;
}

/* 课堂选择弹窗 */
.picker-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 9999;
  display: flex;
  align-items: flex-end;
}

.picker-content {
  width: 100%;
  max-height: 80vh;
  background-color: #fff;
  border-radius: 20rpx 20rpx 0 0;
  display: flex;
  flex-direction: column;
}

.picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #E5E5E5;
}

.picker-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2329;
}

.picker-close {
  font-size: 48rpx;
  color: #86909C;
  line-height: 1;
}

.picker-search {
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #E5E5E5;
}

.picker-search-input {
  width: 100%;
  padding: 20rpx 24rpx;
  background-color: #F7F8FA;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #1F2329;
}

.picker-list {
  flex: 1;
  max-height: 60vh;
}

.picker-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #F0F0F0;
}

.picker-item.selected {
  background-color: #E8F4FF;
}

.picker-item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.picker-item-name {
  font-size: 30rpx;
  color: #1F2329;
  font-weight: 500;
}

.picker-item-count {
  font-size: 24rpx;
  color: #86909C;
}

.picker-item-check {
  font-size: 32rpx;
  color: #007AFF;
  font-weight: 600;
}

.picker-empty {
  padding: 100rpx 0;
  text-align: center;
  color: #86909C;
  font-size: 28rpx;
}
</style>