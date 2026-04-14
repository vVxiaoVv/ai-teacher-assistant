<script>
export default {
  // H5 端内部会用到 keepAliveInclude，这里声明为响应式数据，避免警告
  data() {
    return {
      keepAliveInclude: []
    };
  },
  onLaunch() {
    console.log('App Launch');
  },
  onShow() {
    console.log('App Show');
  },
  onHide() {
    console.log('App Hide');
  }
};
</script>

<style>
/* 全局样式，可在此设置全局背景色、字体等 */
page {
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
    'Helvetica Neue', Arial, 'Noto Sans', 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', sans-serif;
}
</style>


