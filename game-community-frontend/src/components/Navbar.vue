<template>
  <div class="navbar">
    <hamburger :is-active="sidebar.opened" @toggle-click="toggleSideBar"/>
    <breadcrumb v-if="showBreadcrumb" class="breadcrumb-container"/>

    <div class="right-menu">
      <template v-if="device !== 'mobile'">
        <search class="right-menu-item"/>
        <error-log class="right-menu-item"/>
      </template>

      <notification class="right-menu-item"/>

      <el-dropdown class="right-menu-item" trigger="click">
        <div class="avatar-wrapper">
          <img :src="userInfo?.avatar || '/avatar.png'" class="user-avatar">
          <i class="el-icon-caret-bottom"/>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <router-link to="/user/profile">
              <el-dropdown-item>个人中心</el-dropdown-item>
            </router-link>
            <router-link to="/user/settings">
              <el-dropdown-item>设置</el-dropdown-item>
            </router-link>
            <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import Breadcrumb from './Breadcrumb.vue';
import Hamburger from './Hamburger.vue';
import ErrorLog from './ErrorLog.vue';
import Notification from './Notification.vue';
import Search from './Search.vue';

const store = useStore();
const router = useRouter();

const sidebar = computed(() => store.state.app.sidebar);
const device = computed(() => store.state.app.device);
const userInfo = computed(() => store.state.user.userInfo);
const showBreadcrumb = computed(() => store.state.app.settings.showBreadcrumb);

const toggleSideBar = () => {
  store.dispatch('app/toggleSideBar');
};

const logout = async () => {
  try {
    await store.dispatch('user/logout');
    router.push('/login');
  } catch (error) {
    console.error('退出登录失败:', error);
  }
};
</script>

<style lang="scss" scoped>
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);

  .breadcrumb-container {
    float: left;
  }

  .right-menu {
    float: right;
    height: 100%;
    line-height: 50px;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background .3s;

        &:hover {
          background: rgba(0, 0, 0, .025)
        }
      }
    }

    .avatar-wrapper {
      margin-right: 30px;
      position: relative;

      .user-avatar {
        cursor: pointer;
        width: 40px;
        height: 40px;
        border-radius: 10px;
      }

      .el-icon-caret-bottom {
        position: absolute;
        right: -20px;
        top: 25px;
        font-size: 12px;
      }
    }
  }
}
</style>