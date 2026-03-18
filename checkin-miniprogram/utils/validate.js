// 参数验证工具函数

/**
 * 验证参数是否为有效值（非undefined、非null、非空字符串）
 * @param {*} value - 要验证的参数值
 * @returns {boolean} - 是否为有效值
 */
export function isValid(value) {
  if (value === undefined || value === null) {
    return false;
  }
  if (typeof value === 'string' && value.trim() === '') {
    return false;
  }
  return true;
}

/**
 * 清理对象中的无效参数（undefined、null、空字符串）
 * @param {Object} obj - 要清理的对象
 * @returns {Object} - 清理后的对象
 */
export function cleanObject(obj) {
  if (!obj || typeof obj !== 'object') {
    return {};
  }
  const cleaned = {};
  for (const key in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, key)) {
      const value = obj[key];
      if (isValid(value)) {
        cleaned[key] = value;
      }
    }
  }
  return cleaned;
}

/**
 * 构建查询字符串，自动过滤无效参数
 * @param {Object} params - 查询参数对象
 * @returns {string} - 构建好的查询字符串
 */
export function buildQueryString(params) {
  if (!params || typeof params !== 'object') {
    return '';
  }
  const cleaned = cleanObject(params);
  const pairs = [];
  for (const key in cleaned) {
    if (Object.prototype.hasOwnProperty.call(cleaned, key)) {
      const value = cleaned[key];
      pairs.push(`${encodeURIComponent(key)}=${encodeURIComponent(value)}`);
    }
  }
  return pairs.length > 0 ? `?${pairs.join('&')}` : '';
}

/**
 * 验证打卡表单参数
 * @param {Object} form - 打卡表单数据
 * @returns {Object} - 验证结果，包含isValid和error字段
 */
export function validateCheckinForm(form) {
  if (!form) {
    return { isValid: false, error: '表单数据不能为空' };
  }
  if (!isValid(form.title)) {
    return { isValid: false, error: '请输入打卡标题' };
  }
  if (form.title.length > 50) {
    return { isValid: false, error: '标题不能超过50字' };
  }
  if (form.content && form.content.length > 500) {
    return { isValid: false, error: '内容不能超过500字' };
  }
  return { isValid: true, error: '' };
}

/**
 * 验证主题表单参数
 * @param {Object} form - 主题表单数据
 * @returns {Object} - 验证结果，包含isValid和error字段
 */
export function validateTopicForm(form) {
  if (!form) {
    return { isValid: false, error: '表单数据不能为空' };
  }
  if (!isValid(form.title)) {
    return { isValid: false, error: '请输入主题标题' };
  }
  if (form.title.length < 10) {
    return { isValid: false, error: '主题标题至少需要10个字符' };
  }
  if (form.title.length > 100) {
    return { isValid: false, error: '主题标题不能超过100个字符' };
  }
  if (!isValid(form.durationDays)) {
    return { isValid: false, error: '请选择主题有效期' };
  }
  return { isValid: true, error: '' };
}

/**
 * 验证token是否存在
 * @returns {boolean} - token是否存在
 */
export function hasToken() {
  const token = wx.getStorageSync('token');
  return isValid(token);
}

/**
 * 获取有效的token
 * @returns {string|null} - 有效的token或null
 */
export function getValidToken() {
  const token = wx.getStorageSync('token');
  return isValid(token) ? token : null;
}
