<template>
  <div class="app-wrapper" :class="{'mobile': device === 'mobile'}">
    <div v-if="device === 'mobile' && sidebar.opened" class="drawer-bg" @click="handleClickOutside"/>

    <sidebar class="sidebar-container"/>

    <div class="main-container">
      <div :class="{'fixed-header': fixedHeader}">
        <navbar/>
        <tags-view v-if="showTags"/>
      </div>
      <app-main/>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useStore } from 'vuex';
import Sidebar from './components/Sidebar/index.vue';
import Navbar from './components/Navbar.vue';
import TagsView from './components/TagsView/index.vue';
import AppMain from './components/AppMain.vue';

const store = useStore();

const sidebar = computed(() => store.state.app.sidebar);
const device = computed(() => store.state.app.device);
const fixedHeader = computed(() => store.state.app.settings.fixedHeader);
const showTags = computed(() => store.state.app.settings.showTags);

const handleClickOutside = () => {
  store.dispatch('app/closeSideBar', { withoutAnimation: false });
};
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.app-wrapper {
  position: relative;
  height: 100%;
  width: 100%;

  &.mobile {
    .sidebar-container {
      transition: transform .28s;
      width: $sideBarWidth !important;
    }

    &.openSidebar {
      position: fixed;
      top: 0;
    }
  }
}

.drawer-bg {
  background: #000;
  opacity: 0.3;
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
  z-index: 999;
}

.fixed-header {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 9;
  width: calc(100% - #{$sideBarWidth});
  transition: width 0.28s;
}

.hideSidebar .fixed-header {
  width: calc(100% - 54px)
}

.mobile .fixed-header {
  width: 100%;
}
</style>