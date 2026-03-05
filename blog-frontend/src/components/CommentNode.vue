<script setup>
defineProps({
  node: { type: Object, required: true },
  replyingTo: { type: Number, default: null },
  commentError: { type: String, default: '' },
  commentSubmitting: { type: Boolean, default: false },
  replyAuthor: { type: String, default: '' },
  replyContent: { type: String, default: '' }
})
defineEmits(['reply', 'submit-reply', 'cancel-reply', 'update:replyAuthor', 'update:replyContent'])
</script>

<template>
  <li class="comment-item" :class="{ 'comment-reply': node.parentId }">
    <div class="comment-meta">{{ node.author }} · {{ node.address || '未知地址' }} · {{ node.createdAt?.slice(0, 16)?.replace('T', ' ') }}</div>
    <div class="comment-content">{{ node.content }}</div>
    <button type="button" class="comment-reply-btn" @click="$emit('reply', node)">回复</button>
    <div v-if="replyingTo === node.id" class="comment-reply-form">
      <input :value="replyAuthor" @input="$emit('update:replyAuthor', $event.target.value)" type="text" placeholder="昵称（选填，默认匿名用户）" maxlength="50" />
      <textarea :value="replyContent" @input="$emit('update:replyContent', $event.target.value)" placeholder="回复内容（必填）…" rows="2"></textarea>
      <p v-if="commentError && replyingTo === node.id" class="error">{{ commentError }}</p>
      <div class="reply-actions">
        <button type="button" class="btn btn-comment" :disabled="commentSubmitting" @click="$emit('submit-reply', node.id)">提交</button>
        <button type="button" class="btn btn-ghost" @click="$emit('cancel-reply')">取消</button>
      </div>
    </div>
    <ul v-if="node.replies?.length" class="comment-replies">
      <CommentNode
        v-for="r in node.replies"
        :key="r.id"
        :node="r"
        :replying-to="replyingTo"
        :comment-error="commentError"
        :comment-submitting="commentSubmitting"
        :reply-author="replyAuthor"
        :reply-content="replyContent"
        @reply="$emit('reply', $event)"
        @submit-reply="$emit('submit-reply', $event)"
        @cancel-reply="$emit('cancel-reply')"
        @update:reply-author="$emit('update:replyAuthor', $event)"
        @update:reply-content="$emit('update:replyContent', $event)"
      />
    </ul>
  </li>
</template>
