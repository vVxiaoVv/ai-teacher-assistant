<template>
  <view class="page">
    <view class="form-container">
      <view class="form-title">{{ isEdit ? '编辑课堂' : '创建课堂' }}</view>

      <view class="form-item">
        <view class="form-label">课堂名称 <text class="required">*</text></view>
        <input
          class="form-input"
          v-model="form.classroomName"
          placeholder="请输入课堂名称"
          maxlength="100"
        />
      </view>

      <view class="form-item">
        <view class="form-label">选择学生 <text class="required">*</text></view>
        <view class="student-selector">
          <view
            v-for="student in availableStudents"
            :key="student.id"
            class="student-option"
            :class="{ selected: isStudentSelected(student.id) }"
            @tap="toggleStudent(student.id)"
          >
            <view class="option-checkbox">
              <text v-if="isStudentSelected(student.id)" class="check-icon">✓</text>
            </view>
            <view class="option-content">
              <view class="option-name">{{ student.studentName }}</view>
            </view>
          </view>
        </view>
        <view v-if="availableStudents.length === 0" class="empty-students">
          <text>暂无学生数据，请先添加学生</text>
        </view>
      </view>

      <view class="form-item">
        <view class="form-label">课堂描述</view>
        <textarea
          class="form-textarea"
          v-model="form.description"
          placeholder="请输入课堂描述（可选）"
          maxlength="500"
        />
        <view class="char-count">{{ form.description.length }}/500</view>
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
      availableStudents: [],
      form: {
        id: null,
        classroomName: '',
        studentIds: [],
        description: ''
      }
    };
  },
  onLoad(options) {
    if (options.id) {
      this.isEdit = true;
      this.form.id = parseInt(options.id);
      this.loadClassroomData();
    }
    this.loadStudents();
  },
  methods: {
    async loadStudents() {
      try {
        const res = await uniRequest({
          url: '/api/student-portrait/list',
          method: 'GET',
          data: {
            page: 0,
            size: 1000
          }
        });
        
        if (res.statusCode === 200 && res.data) {
          // 后端直接返回 PageResultDto，不是包装在 R 对象中
          if (res.data.content !== undefined) {
            this.availableStudents = res.data.content || [];
          } else if (res.data.code === 0 && res.data.data) {
            // 兼容包装在 R 对象中的情况
            this.availableStudents = res.data.data.content || [];
          }
        }
      } catch (error) {
        console.error('加载学生列表失败:', error);
      }
    },
    async loadClassroomData() {
      try {
        const res = await uniRequest({
          url: `/api/classroom/${this.form.id}`,
          method: 'GET'
        });
        
        if (res.statusCode === 200 && res.data) {
          if (res.data.code === 0 && res.data.data) {
            const classroom = res.data.data;
            this.form.classroomName = classroom.classroomName || '';
            this.form.description = classroom.description || '';
            // 假设返回的数据中有 studentIds 或 students 数组
            if (classroom.studentIds && Array.isArray(classroom.studentIds)) {
              this.form.studentIds = classroom.studentIds;
            } else if (classroom.students && Array.isArray(classroom.students)) {
              this.form.studentIds = classroom.students.map(s => s.id || s);
            }
          } else {
            uni.showToast({
              title: res.data.msg || '加载失败',
              icon: 'none'
            });
            setTimeout(() => {
              uni.navigateBack();
            }, 1500);
          }
        }
      } catch (error) {
        console.error('加载课堂数据失败:', error);
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        });
        setTimeout(() => {
          uni.navigateBack();
        }, 1500);
      }
    },
    toggleStudent(studentId) {
      const index = this.form.studentIds.indexOf(studentId);
      if (index > -1) {
        this.form.studentIds.splice(index, 1);
      } else {
        this.form.studentIds.push(studentId);
      }
    },
    isStudentSelected(studentId) {
      return this.form.studentIds.includes(studentId);
    },
    async handleSubmit() {
      if (!this.form.classroomName || !this.form.classroomName.trim()) {
        uni.showToast({
          title: '请输入课堂名称',
          icon: 'none'
        });
        return;
      }

      if (this.form.studentIds.length === 0) {
        uni.showToast({
          title: '请至少选择一个学生',
          icon: 'none'
        });
        return;
      }

      this.submitting = true;
      try {
        // 后端使用 @RequestBody，发送JSON格式
        const data = {
          classroomName: this.form.classroomName.trim(),
          description: this.form.description ? this.form.description.trim() : '',
          studentIds: this.form.studentIds || []
        };

        let res;
        if (this.isEdit) {
          res = await uniRequest({
            url: `/api/classroom/update/${this.form.id}`,
            method: 'PUT',
            data: data
          });
        } else {
          res = await uniRequest({
            url: '/api/classroom/create',
            method: 'POST',
            data: data
          });
        }

        if (res.statusCode === 200 && res.data) {
          uni.showToast({
            title: this.isEdit ? '更新成功' : '创建成功',
            icon: 'success'
          });
          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        } else {
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

.student-selector {
  max-height: 600rpx;
  overflow-y: auto;
  border: 1rpx solid #E5E5E5;
  border-radius: 8rpx;
  background: #F7F8FA;
}

.student-option {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx;
  border-bottom: 1rpx solid #E5E5E5;
  background: #FFFFFF;
}

.student-option:last-child {
  border-bottom: none;
}

.student-option.selected {
  background: #E8F4FF;
}

.option-checkbox {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid #86909C;
  border-radius: 6rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.student-option.selected .option-checkbox {
  background: #3498DB;
  border-color: #3498DB;
}

.check-icon {
  color: #FFFFFF;
  font-size: 24rpx;
  font-weight: 600;
}

.option-content {
  flex: 1;
}

.option-name {
  font-size: 28rpx;
  color: #1F2329;
}

.empty-students {
  padding: 40rpx;
  text-align: center;
  color: #86909C;
  font-size: 28rpx;
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
  background: linear-gradient(135deg, #3498DB, #2980B9);
  color: #FFFFFF;
}

.submit-btn.loading {
  opacity: 0.6;
}
</style>

