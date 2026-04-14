import Vue from 'vue';
import App from './App';

Vue.config.productionTip = false;

// 按 uni-app 官方写法，标记应用类型
App.mpType = 'app';

const app = new Vue({
  ...App
});

app.$mount();
