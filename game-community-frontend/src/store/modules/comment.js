import commentApi from '@/api/comment'

export default {
    namespaced: true,

    state: () => ({
        comments: [],
        total: 0,
        loading: false
    }),

    mutations: {
        SET_COMMENTS(state, comments) {
            state.comments = comments
        },
        SET_TOTAL(state, total) {
            state.total = total
        },
        SET_LOADING(state, loading) {
            state.loading = loading
        },
        ADD_COMMENT(state, comment) {
            state.comments.unshift(comment)
            state.total++
        },
        UPDATE_COMMENT(state, updatedComment) {
            const index = state.comments.findIndex(c => c.id === updatedComment.id)
            if (index !== -1) {
                state.comments[index] = updatedComment
            }
        },
        DELETE_COMMENT(state, commentId) {
            const index = state.comments.findIndex(c => c.id === commentId)
            if (index !== -1) {
                state.comments.splice(index, 1)
                state.total--
            }
        },
        UPDATE_LIKE_STATUS(state, { commentId, liked }) {
            const comment = state.comments.find(c => c.id === commentId)
            if (comment) {
                comment.isLiked = liked
                comment.likeCount += liked ? 1 : -1
            }
        }
    },

    actions: {
        async getComments({ commit }, params) {
            commit('SET_LOADING', true)
            try {
                const response = await commentApi.getPostComments(params.postId, {
                    page: params.page,
                    size: params.size,
                    sort: params.sort,
                    onlyShowAuthor: params.onlyShowAuthor,
                    reportStatus: params.reportStatus
                })

                if (params.page === 0) {
                    commit('SET_COMMENTS', response.content)
                } else {
                    commit('SET_COMMENTS', [...state.comments, ...response.content])
                }

                commit('SET_TOTAL', response.totalElements)
                return response
            } finally {
                commit('SET_LOADING', false)
            }
        },

        async createComment({ commit }, data) {
            const comment = await commentApi.createComment(data)
            if (!data.parentId) {
                commit('ADD_COMMENT', comment)
            }
            return comment
        },

        async updateComment({ commit }, { id, content }) {
            const updatedComment = await commentApi.updateComment(id, content)
            commit('UPDATE_COMMENT', updatedComment)
            return updatedComment
        },

        async deleteComment({ commit }, id) {
            await commentApi.deleteComment(id)
            commit('DELETE_COMMENT', id)
        },

        async likeComment({ commit }, id) {
            await commentApi.likeComment(id)
            commit('UPDATE_LIKE_STATUS', { commentId: id, liked: true })
        },

        async unlikeComment({ commit }, id) {
            await commentApi.unlikeComment(id)
            commit('UPDATE_LIKE_STATUS', { commentId: id, liked: false })
        }
    },

    getters: {
        commentsList: state => state.comments,
        commentsTotal: state => state.total,
        isLoading: state => state.loading
    }
}