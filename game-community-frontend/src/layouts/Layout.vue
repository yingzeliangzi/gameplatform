<template>
  <el-container class="app-container">
    <el-header height="60px">
      <app-header />
    </el-header>

    <el-main>
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>

    <el-footer height="auto">
      <app-footer />
    </el-footer>

    <el-backtop :right="20" :bottom="20" />
  </el-container>
</template>

<script setup>
import AppHeader from '@/components/Header.vue'
import AppFooter from '@/components/Footer.vue'
</script>

<style lang="scss" scoped>
.app-container {
  min-height: 100vh;
  background-color: #f5f7fa;

  .el-main {
    padding: 20px;
    max-width: 1200px;
    margin: 0 auto;
    width: 100%;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>