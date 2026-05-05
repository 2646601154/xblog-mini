<script setup lang="ts">
// @ts-nocheck
/**
 * 富文本编辑器组件
 * 基于 wangEditor 5.x + Vue 3
 */
import { ref, shallowRef, onBeforeUnmount, watch } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'

interface Props {
  modelValue?: string
  placeholder?: string
  height?: number
  mode?: 'default' | 'simple'
  toolbarKeys?: string[]
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '请输入内容...',
  height: 500,
  mode: 'default',
  toolbarKeys: () => [
    'headerSelect',
    '|',
    'bold', 'italic', 'underline', 'strikethrough',
    '|',
    'color', 'bgColor',
    '|',
    'fontSize', 'fontFamily',
    '|',
    'lineHeight',
    '|',
    'sup', 'sub',
    '|',
    'clearStyle',
    '|',
    'insertLink',
    '|',
    'editLink', 'unlink', 'todo',
    '|',
    'insertTable', 'deleteTable',
    '|',
    'undo', 'redo',
    '|',
    'fullScreen',
  ],
  disabled: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'onCreated': [editor: any]
  'onChange': [editor: any]
  'fullscreenChange': [isFullscreen: boolean]
}>()

// 编辑器实例，必须使用 shallowRef
const editorRef = shallowRef()
const valueHtml = ref(props.modelValue)
const isFullscreen = ref(false)

// 工具栏配置
const toolbarConfig = {
  excludeKeys: ['group-video'], // 暂不启用视频功能
}

// 编辑器配置
const editorConfig = {
  placeholder: props.placeholder,
  MENU_CONF: {
    uploadImage: {
      // 暂时使用 base64 模式（避免配置服务器）
      base64LimitSize: 5 * 1024, // 5KB 以下的图片转为 base64
    },
  },
}

// 监听外部 modelValue 变化
watch(
  () => props.modelValue,
  (newVal) => {
    if (newVal !== valueHtml.value) {
      valueHtml.value = newVal
    }
  }
)

// 编辑器创建完成
function handleCreated(editor: any) {
  editorRef.value = editor
  if (props.disabled) {
    editor.disable()
  }

  // 监听全屏状态变化
  editor.on('fullScreen', () => {
    isFullscreen.value = true
    emit('fullscreenChange', true)
  })

  editor.on('unFullScreen', () => {
    isFullscreen.value = false
    emit('fullscreenChange', false)
  })

  emit('onCreated', editor)
}

// 内容变化
function handleChange(editor: any) {
  const html = editor.getHtml()
  valueHtml.value = html
  emit('update:modelValue', html)
  emit('onChange', editor)
}

// 组件销毁前必须销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})

// 暴露编辑器实例和获取 HTML 的方法
defineExpose({
  editor: editorRef,
  isFullscreen,
  getHtml: () => editorRef.value?.getHtml() || '',
  insertText: (text: string) => editorRef.value?.insertText(text),
})
</script>

<template>
  <div class="rich-editor-container" :class="{ 'is-disabled': disabled }">
    <Toolbar
      v-if="!disabled"
      style="border-bottom: 1px solid #d9d9d9"
      :editor="editorRef"
      :defaultConfig="toolbarConfig"
      :mode="mode"
    />
    <Editor
      v-model="valueHtml"
      style="overflow-y: hidden"
      :defaultConfig="editorConfig"
      :mode="mode"
      :disabled="disabled"
      @onCreated="handleCreated"
      @onChange="handleChange"
    />
  </div>
</template>

<style scoped>
.rich-editor-container {
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.rich-editor-container.is-disabled {
  background-color: #f5f5f5;
}

.rich-editor-container :deep(.w-e-toolbar) {
  flex-wrap: wrap;
  flex-shrink: 0;
}

.rich-editor-container :deep(.w-e-text-container) {
  flex: 1;
  min-height: v-bind("`${height}px`");
  overflow-y: auto;
}
</style>