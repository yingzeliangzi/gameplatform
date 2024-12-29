<template>
  <aside class="app-sidebar">
    <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        :unique-opened="true"
        :collapse-transition="false"
        mode="vertical"
    >
      <el-sub-menu v-for="menu in menus" :key="menu.path" :index="menu.path">
        <template #title>
          <el-icon>
            <component :is="menu.icon" />
          </el-icon>
          <span>{{ menu.title }}</span>
        </template>
        <el-menu-item
            v-for="subMenu in menu.children"
            :key="subMenu.path"
            :index="menu.path + '/' + subMenu.path"
            @click="handleMenuClick(menu.path + '/' + subMenu.path)"
        >
          {{ subMenu.title }}
        </el-menu-item>
      </el-sub-menu>
    </el-menu>
  </aside>
</template>

<script>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  House,
  Game,
  ChatDotRound,
  Calendar,
  Setting,
  User,
  Document
} from '@element-plus/icons-vue'

export default {
  name: 'AppSidebar',

  setup() {
    const route = useRoute()
    const router = useRouter()
    const isCollapse = ref(false)

    const menus = [
      {
        title: '主页',
        path: '/admin',
        icon: House,
        children: [
          { title: '仪表盘', path: 'dashboard' }
        ]
      },
      {
        title: '游戏管理',
        path: '/admin/games',
        icon: Game,
        children: [
          { title: '游戏列表', path: 'list' },
          { title: '分类管理', path: 'categories' }
        ]
      },
      {
        title: '社区管理',
        path: '/admin/community',
        icon: ChatDotRound,
        children: [
          { title: '帖子管理', path: 'posts' },
          { title: '评论管理', path: 'comments' }
        ]
      },
      {
        title: '活动管理',
        path: '/admin/events',
        icon: Calendar,
        children: [
          { title: '活动列表', path: 'list' },
          { title: '报名管理', path: 'registrations' }
        ]
      },
      {
        title: '用户管理',
        path: '/admin/users',
        icon: User,
        children: [
          { title: '用户列表', path: 'list' },
          { title: '角色管理', path: 'roles' }
        ]
      },
      {
        title: '内容管理',
        path: '/admin/content',
        icon: Document,
        children: [
          { title: '内容审核', path: 'audit' },
          { title: '举报管理', path: 'reports' }
        ]
      },
      {
        title: '系统设置',
        path: '/admin/settings',
        icon: Setting,
        children: [
          { title: '基本设置', path: 'basic' },
          { title: '通知设置', path: 'notifications' }
        ]
      }
    ]

    const activeMenu = computed(() => route.path)

    const handleMenuClick = (path) => {
      router.push(path)
    }

    return {
      isCollapse,
      menus,
      activeMenu,
      handleMenuClick
    }
  }
}
</script>

<style scoped>
.app-sidebar {
  width: 260px;
  height: 100%;
  background-color: #304156;
  transition: width 0.3s;
}

.sidebar-menu {
  height: 100%;
  border-right: none;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 260px;
}

:deep(.el-submenu__title) {
  display: flex;
  align-items: center;
}

:deep(.el-submenu__icon-arrow) {
  margin-top: -4px;
}
</style>