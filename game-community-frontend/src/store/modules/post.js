import {
    getPosts,
    getPostDetail,
    createPost,
    updatePost,
    deletePost,
    likePost,
    unlikePost,
    addComment,
    deleteComment,
    reportPost,
    getHotPosts,
    collectPost,
    uncollectPost
} from '@/api/post';

const state = {
    postList: [],
    postDetail: null,
    hotPosts: [],
    total: 0,
    loading: false,
    submitting: false
};

const mutations = {
    SET_POST_LIST(state, { posts, total }) {
        state.postList = posts;
        state.total = total;
    },
    SET_POST_DETAIL(state, post) {
        state.postDetail = post;
    },
    SET_HOT_POSTS(state, posts) {
        state.hotPosts = posts;
    },
    SET_LOADING(state, loading) {
        state.loading = loading;
    },
    SET_SUBMITTING(state, submitting) {
        state.submitting = submitting;
    },
    UPDATE_POST_LIKE(state, { postId, liked, likeCount }) {
        if (state.postDetail?.id === postId) {
            state.postDetail.liked = liked;
            state.postDetail.likeCount = likeCount;
        }
        const post = state.postList.find(p => p.id === postId);
        if (post) {
            post.liked = liked;
            post.likeCount = likeCount;
        }
    },
    UPDATE_POST_COLLECT(state, { postId, collected }) {
        if (state.postDetail?.id === postId) {
            state.postDetail.collected = collected;
        }
        const post = state.postList.find(p => p.id === postId);
        if (post) {
            post.collected = collected;
        }
    },
    ADD_COMMENT(state, { postId, comment }) {
        if (state.postDetail?.id === postId) {
            state.postDetail.comments.unshift(comment);
            state.postDetail.commentCount++;
        }
    },
    DELETE_COMMENT(state, { postId, commentId }) {
        if (state.postDetail?.id === postId) {
            const index = state.postDetail.comments.findIndex(c => c.id === commentId);
            if (index > -1) {
                state.postDetail.comments.splice(index, 1);
                state.postDetail.commentCount--;
            }
        }
    }
};

const actions = {
    async getPostList({ commit }, params) {
        commit('SET_LOADING', true);
        try {
            const { data } = await getPosts(params);
            commit('SET_POST_LIST', {
                posts: data.content,
                total: data.totalElements
            });
            return data;
        } finally {
            commit('SET_LOADING', false);
        }
    },

    async getPostDetail({ commit }, id) {
        commit('SET_LOADING', true);
        try {
            const { data } = await getPostDetail(id);
            commit('SET_POST_DETAIL', data);
            return data;
        } finally {
            commit('SET_LOADING', false);
        }
    },

    async createPost({ commit }, postData) {
        commit('SET_SUBMITTING', true);
        try {
            const { data } = await createPost(postData);
            return data;
        } finally {
            commit('SET_SUBMITTING', false);
        }
    },

    async likePost({ commit }, postId) {
        try {
            await likePost(postId);
            commit('UPDATE_POST_LIKE', {
                postId,
                liked: true,
                likeCount: (state.postDetail?.likeCount || 0) + 1
            });
        } catch (error) {
            console.error('点赞失败:', error);
            return Promise.reject(error);
        }
    },

    async unlikePost({ commit }, postId) {
        try {
            await unlikePost(postId);
            commit('UPDATE_POST_LIKE', {
                postId,
                liked: false,
                likeCount: Math.max((state.postDetail?.likeCount || 0) - 1, 0)
            });
        } catch (error) {
            console.error('取消点赞失败:', error);
            return Promise.reject(error);
        }
    },

    async addComment({ commit }, { postId, commentData }) {
        try {
            const { data } = await addComment(postId, commentData);
            commit('ADD_COMMENT', { postId, comment: data });
            return data;
        } catch (error) {
            console.error('添加评论失败:', error);
            return Promise.reject(error);
        }
    },

    async deleteComment({ commit }, { postId, commentId }) {
        try {
            await deleteComment(commentId);
            commit('DELETE_COMMENT', { postId, commentId });
        } catch (error) {
            console.error('删除评论失败:', error);
            return Promise.reject(error);
        }
    },

    async getHotPosts({ commit }) {
        try {
            const { data } = await getHotPosts();
            commit('SET_HOT_POSTS', data);
            return data;
        } catch (error) {
            console.error('获取热门帖子失败:', error);
            return [];
        }
    },

    async collectPost({ commit }, postId) {
        try {
            await collectPost(postId);
            commit('UPDATE_POST_COLLECT', { postId, collected: true });
        } catch (error) {
            console.error('收藏失败:', error);
            return Promise.reject(error);
        }
    },

    async uncollectPost({ commit }, postId) {
        try {
            await uncollectPost(postId);
            commit('UPDATE_POST_COLLECT', { postId, collected: false });
        } catch (error) {
            console.error('取消收藏失败:', error);
            return Promise.reject(error);
        }
    }
};

const getters = {
    postList: state => state.postList,
    postDetail: state => state.postDetail,
    hotPosts: state => state.hotPosts,
    loading: state => state.loading,
    submitting: state => state.submitting,
    total: state => state.total
};

export default {
    namespaced: true,
    state,
    mutations,
    actions,
    getters
};