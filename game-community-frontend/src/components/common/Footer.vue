<template>
  <footer class="app-footer">
    <div class="footer-content">
      <div class="footer-section">
        <h3>关于我们</h3>
        <p>游戏社区平台致力于为玩家提供优质的游戏交流和活动服务。</p>
      </div>
      <div class="footer-section">
        <h3>快速链接</h3>
        <ul>
          <li><router-link to="/about">关于我们</router-link></li>
          <li><router-link to="/terms">服务条款</router-link></li>
          <li><router-link to="/privacy">隐私政策</router-link></li>
          <li><router-link to="/contact">联系我们</router-link></li>
        </ul>
      </div>
      <div class="footer-section">
        <h3>联系方式</h3>
        <p>邮箱：contact@gameplatform.com</p>
      </div>
    </div>
    <div class="footer-bottom">
      <p>&copy; {{ currentYear }} 游戏社区平台 版权所有</p>
    </div>
  </footer>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'AppFooter',

  setup() {
    const currentYear = computed(() => new Date().getFullYear())

    return {
      currentYear
    }
  }
}
</script>

<style scoped>
.app-footer {
  background-color: #2c3e50;
  color: #fff;
  padding: 40px 20px 20px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 40px;
}

.footer-section h3 {
  font-size: 18px;
  margin-bottom: 20px;
  color: #fff;
}

.footer-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-section ul li {
  margin-bottom: 10px;
}

.footer-section a {
  color: #fff;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-section a:hover {
  color: #42b983;
}

.footer-bottom {
  margin-top: 40px;
  padding-top: 20px;
  text-align: center;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-bottom p {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}
</style>