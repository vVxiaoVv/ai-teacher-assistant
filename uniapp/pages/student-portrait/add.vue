<template>
  <view class="page">
    <view class="form-container">
      <view class="form-title">{{ isEdit ? '编辑学生' : '添加学生' }}</view>

      <view class="form-item">
        <view class="form-label">学生姓名 <text class="required">*</text></view>
        <input
          class="form-input"
          v-model="form.studentName"
          placeholder="请输入学生姓名"
          maxlength="100"
        />
      </view>

      <view class="form-item">
        <view class="form-label">学生头像</view>
        <input
          class="form-input"
          v-model="form.photoUrl"
          placeholder="请输入图片URL（支持 http:// 或 https:// 开头的完整URL）"
          maxlength="500"
        />
        <view v-if="form.photoUrl" class="avatar-preview">
          <image
            :src="getFullAvatarUrl(form.photoUrl)"
            mode="aspectFill"
            class="preview-img"
            @error="handlePreviewError"
          />
          <view v-if="previewError" class="preview-error">图片加载失败</view>
        </view>
        <view class="form-tip">请输入完整的图片URL地址，例如：https://example.com/image.jpg</view>
      </view>

      <view class="form-item">
        <view class="form-label">概括描述</view>
        <textarea
          class="form-textarea"
          v-model="form.characteristics"
          placeholder="请输入学生的概括描述（可选）"
          maxlength="2000"
        />
        <view class="char-count">{{ form.characteristics.length }}/2000</view>
      </view>

      <view class="form-actions">
        <view class="btn cancel-btn" @tap="handleCancel">取消</view>
        <view class="btn submit-btn" @tap="handleSubmit" :class="{ loading: submitting }">
          {{ submitting ? '提交中...' : '确定' }}
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { uniRequest } from '../../common/request.js';

export default {
  data() {
    return {
      isEdit: false,
      submitting: false,
      previewError: false,
      form: {
        id: null,
        studentName: '',
        photoUrl: '',
        characteristics: ''
      }
    };
  },
  onLoad(options) {
    if (options.id) {
      this.isEdit = true;
      this.form.id = parseInt(options.id);
      this.loadStudentData();
    }
  },
  methods: {
    async loadStudentData() {
      try {
        const res = await uniRequest({
          url: `/api/student-portrait/${this.form.id}`,
          method: 'GET'
        });
        
        if (res.statusCode === 200 && res.data) {
          // 后端直接返回 StudentPortrait 实体，不是包装在 R 对象中
          let student = null;
          if (res.data.studentName !== undefined) {
            // 直接是实体对象
            student = res.data;
          } else if (res.data.code === 0 && res.data.data) {
            // 兼容包装在 R 对象中的情况
            student = res.data.data;
          }
          
          if (student) {
            this.form.studentName = student.studentName || '';
            this.form.photoUrl = student.photoUrl || '';
            this.form.characteristics = student.characteristics || '';
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
        console.error('加载学生数据失败:', error);
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        });
        setTimeout(() => {
          uni.navigateBack();
        }, 1500);
      }
    },
    async handleSubmit() {
      if (!this.form.studentName || !this.form.studentName.trim()) {
        uni.showToast({
          title: '请输入学生姓名',
          icon: 'none'
        });
        return;
      }

      if (this.form.studentName.length > 100) {
        uni.showToast({
          title: '学生姓名长度不能超过100个字符',
          icon: 'none'
        });
        return;
      }

      this.submitting = true;
      try {
        const params = new URLSearchParams();
        params.append('studentName', this.form.studentName.trim());
        if (this.form.photoUrl) {
          params.append('photoUrl', this.form.photoUrl);
        }
        if (this.form.characteristics) {
          params.append('characteristics', this.form.characteristics.trim());
        }

        let res;
        if (this.isEdit) {
          res = await uniRequest({
            url: `/api/student-portrait/update/${this.form.id}`,
            method: 'PUT',
            header: {
              'Content-Type': 'application/x-www-form-urlencoded'
            },
            data: params.toString()
          });
        } else {
          res = await uniRequest({
            url: '/api/student-portrait/create',
            method: 'POST',
            header: {
              'Content-Type': 'application/x-www-form-urlencoded'
            },
            data: params.toString()
          });
        }

        if (res.statusCode === 200 && res.data) {
          // 后端直接返回 StudentPortrait 实体或成功消息
          uni.showToast({
            title: this.isEdit ? '更新成功' : '创建成功',
            icon: 'success'
          });
          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        } else {
          // 处理错误响应
          let errorMsg = '保存失败';
          if (res.data) {
            if (typeof res.data === 'string') {
              errorMsg = res.data;
            } else if (res.data.msg) {
              errorMsg = res.data.msg;
            } else if (res.data.message) {
              errorMsg = res.data.message;
            }
          }
          uni.showToast({
            title: errorMsg,
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('保存失败:', error);
        uni.showToast({
          title: '保存失败，请稍后重试',
          icon: 'none'
        });
      } finally {
        this.submitting = false;
      }
    },
    handleCancel() {
      uni.navigateBack();
    },
    getFullAvatarUrl(photoUrl) {
      if (!photoUrl) return '';
      if (photoUrl.startsWith('http://') || photoUrl.startsWith('https://')) {
        return photoUrl;
      }
      return photoUrl;
    },
    handlePreviewError() {
      this.previewError = true;
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

.form-container {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 32rpx;
}

.form-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1F2329;
  margin-bottom: 32rpx;
}

.form-item {
  margin-bottom: 32rpx;
}

.form-label {
  font-size: 28rpx;
  color: #1F2329;
  margin-bottom: 16rpx;
  font-weight: 500;
}

.required {
  color: #F53F3F;
}

.form-input {
  width: 100%;
  padding: 24rpx;
  background: #F7F8FA;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #1F2329;
}

.form-textarea {
  width: 100%;
  min-height: 200rpx;
  padding: 24rpx;
  background: #F7F8FA;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #1F2329;
}

.char-count {
  text-align: right;
  font-size: 24rpx;
  color: #86909C;
  margin-top: 8rpx;
}

.avatar-preview {
  margin-top: 16rpx;
  display: flex;
  justify-content: center;
}

.preview-img {
  width: 200rpx;
  height: 200rpx;
  border-radius: 8rpx;
  background: #F7F8FA;
}

.preview-error {
  padding: 40rpx;
  color: #F53F3F;
  font-size: 24rpx;
  text-align: center;
}

.form-tip {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #86909C;
  line-height: 1.5;
}

.form-actions {
  display: flex;
  gap: 24rpx;
  margin-top: 48rpx;
}

.btn {
  flex: 1;
  padding: 24rpx;
  border-radius: 8rpx;
  text-align: center;
  font-size: 32rpx;
  font-weight: 500;
}

.cancel-btn {
  background: #F7F8FA;
  color: #646A73;
}

.submit-btn {
  background: linear-gradient(135deg, #3370FF, #1E6FFF);
  color: #FFFFFF;
}

.submit-btn.loading {
  opacity: 0.6;
}
</style>

