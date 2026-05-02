import { ElMessage } from 'element-plus'

// 业务错误码 → 中文提示映射
const ERROR_MESSAGE_MAP: Record<number | string, string> = {
  // 认证模块 (1000-1999)
  1000: '用户名或密码错误',
  1001: '登录已过期，请重新登录',
  1002: '登录信息无效，请重新登录',

  // 用户模块 (2000-2999)
  2001: '用户名已存在',
  2002: '该账号已被禁用',
  2003: '邮箱已被注册',

  // 文章模块 (3000-3999)
  3001: '文章不存在',
  3002: '文章标题不能为空',
  3003: '文章内容不能为空',
  3004: '分类不存在',
  3005: '文章状态无效',
  3006: '无权编辑他人的文章',
  3007: '无权删除他人的文章',
  3008: '标签不存在',
  3009: '文章已在回收站',
  3010: '文章在回收站，无法发布',
  3011: '文章不在回收站，无法恢复',

  // 评论模块 (4000-4999)
  4001: '评论不存在',
  4002: '评论内容不能为空',
  4003: '评论内容超长',
  4004: '无权编辑他人的评论',
  4005: '无权删除他人的评论',
  4006: '评论状态无效',
  4007: '文章不存在',
  4008: '评论审核状态无法修改',
  4009: '已审核通过的评论无法删除',

  // 分类模块 (5000-5999)
  5000: '分类不存在',
  5001: '分类下存在文章，无法删除',
  5002: '分类名称不能为空',
  5003: '分类名称已存在',
  5004: '分类标识已存在',
  5005: '分类名称格式错误',
  5006: '分类标识格式错误',

  // 标签模块 (6000-6999)
  6000: '未知错误',
  6001: '标签不存在',
  6002: '标签名称不能为空',
  6003: '标签名称已存在',
  6004: '标签标识已存在',
  6005: '标签名称格式错误',
  6006: '标签标识格式错误',

  // 系统级错误 (9000-9999)
  9000: '系统内部错误',
  9001: '数据库连接错误',
  9999: '未知错误',
}

export function getErrorMessage(code: number | string, defaultMessage?: string): string {
  return ERROR_MESSAGE_MAP[code] || defaultMessage || '请求失败'
}

export function showErrorMessage(code: number | string, message?: string): void {
  const errorMsg = getErrorMessage(code, message)
  ElMessage.error(errorMsg)
}

export function showSuccessMessage(message: string = '操作成功'): void {
  ElMessage.success(message)
}
