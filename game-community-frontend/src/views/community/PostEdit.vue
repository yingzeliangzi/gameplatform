<template>
  <div class="post-edit-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑帖子' : '发布帖子' }}</span>
        </div>
      </template>

      <el-form
          ref="postForm"
          :model="postData"
          :rules="rules"
          label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input
              v-model="postData.title"
              placeholder="请输入标题"
              maxlength="100"
              show-word-limit
          />
        </el-form-item>

        <el-form-item label="关联游戏" prop="gameId">
          <el-select
              v-model="postData.gameId"
              placeholder="选择关联游戏"
              clearable
              filterable
          >
            <el-option
                v-for="game in games"
                :key="game.id"
                :label="game.title"
                :value="game.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
              v-model="postData.content"
              type="textarea"
              :rows="10"
              placeholder="请输入帖子内容"
              maxlength="10000"
              show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitPost" :loading="submitting">
            {{ isEdit ? '保存' : '发布' }}
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPostDetail, updatePost, createPost } from '@/api/post'
import { getGameList } from '@/api/game'

export default {
  name: 'PostEdit',

  setup() {
    const route = useRoute()
    const router = useRouter()
    const postForm = ref(null)
    const loading = ref(false)
    const submitting = ref(false)
    const games = ref([])

    const postData = reactive({
      title: '',
      content: '',
      gameId: null
    })

    const isEdit = computed(() => !!route.params.id)

    const rules = {
      title: [
        { required: true, message: '请输入标题', trigger: 'blur' },
        { min: 5, max: 100, message: '标题长度在5-100个字符之间', trigger: 'blur' }
      ],
      content: [
        { required: true, message: '请输入内容', trigger: 'blur' },
        { min: 10, message: '内容至少10个字符', trigger: 'blur' }
      ]
    }

    // 获取游戏列表
    const fetchGames = async () => {
      try {
        const res = await getGameList()
        games.value = res.data
      } catch (error) {
        ElMessage.error('获取游戏列表失败')
      }
    }

    // 获取帖子详情
    const fetchPostDetail = async () => {
      if (!isEdit.value) return

      loading.value = true
      try {
        const res = await getPostDetail(route.params.id)
        const { title, content, gameId } = res.data
        Object.assign(postData, { title, content, gameId })
      } catch (error) {
        ElMessage.error('获取帖子详情失败')
      } finally {
        loading.value = false
      }
    }

    // 提交帖子
    const submitPost = async () => {
      await postForm.value.validate(async (valid) => {
        if (!valid) return

        submitting.value = true
        try {
          if (isEdit.value) {
            await updatePost(route.params.id, postData)
            ElMessage.success('更新成功')
          } else {
            await createPost(postData)
            ElMessage.success('发布成功')
          }
          router.push('/community')
        } catch (error) {
          ElMessage.error(isEdit.value ? '更新失败' : '发布失败')
        } finally {
          submitting.value = false
        }
      })
    }

    const goBack = () => {
      router.back()
    }

    onMounted(() => {
      fetchGames()
      fetchPostDetail()
    })

    return {
      postForm,
      postData,
      loading,
      submitting,
      games,
      isEdit,
      rules,
      submitPost,
      goBack
    }
  }
}
</script>

<style scoped>
.post-edit-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>