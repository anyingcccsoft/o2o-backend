/* ============================================================
 * 管理端公共脚本 admin-common.js
 * 职责：fetch 封装（401 跳登录 / code=0 toast 报错）、Toast、
 *      确认弹窗、分页渲染、侧边栏渲染与高亮、登录人信息与退出、
 *      订单/支付等状态码映射常量、通用格式化工具
 * ============================================================ */

/* ---------- 状态码映射常量 ---------- */

/** 订单状态：码 → 文案 */
var ORDER_STATUS_TEXT = {
  0: '待接单',
  1: '制作中',
  2: '配送中',
  3: '已完成',
  4: '已取消'
};

/** 订单状态：码 → badge 样式类 */
var ORDER_STATUS_BADGE = {
  0: 'badge-warning',
  1: 'badge-info',
  2: 'badge-purple',
  3: 'badge-success',
  4: 'badge-gray'
};

/** 支付方式：码 → 文案 */
var PAY_METHOD_TEXT = {
  1: '微信支付',
  2: '支付宝',
  3: '货到付款'
};

/** 支付状态：码 → 文案 */
var PAY_STATUS_TEXT = {
  0: '待支付',
  1: '已支付'
};

/* ---------- 通用工具 ---------- */

/** HTML 转义，防止拼接模板时的 XSS */
function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

/** 金额格式化：¥ + 两位小数 */
function fmtMoney(value) {
  var num = Number(value);
  if (isNaN(num)) num = 0;
  return '¥' + num.toFixed(2);
}

/** 时间格式化：yyyy-MM-dd HH:mm（兼容后端 LocalDateTime 的 ISO 字符串） */
function fmtTime(value) {
  if (!value) return '-';
  var d = new Date(value);
  if (isNaN(d.getTime())) return String(value);
  function p(n) { return n < 10 ? '0' + n : '' + n; }
  return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) +
    ' ' + p(d.getHours()) + ':' + p(d.getMinutes());
}

/* ---------- Toast 组件 ---------- */

/**
 * 显示轻提示
 * @param {string} msg  提示内容
 * @param {string} type success | error | info
 */
function toast(msg, type) {
  type = type || 'info';
  var container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }
  var item = document.createElement('div');
  item.className = 'toast toast-' + type;
  item.textContent = msg;
  container.appendChild(item);
  // 2.5 秒后淡出移除
  setTimeout(function () {
    item.classList.add('fade-out');
    setTimeout(function () { item.remove(); }, 320);
  }, 2500);
}

/* ---------- 确认弹窗组件 ---------- */

/**
 * 通用确认弹窗，返回 Promise<boolean>
 * @param {string} message 提示文案（支持 \n 换行）
 * @param {string} [title] 标题，默认「操作确认」
 */
function confirmDialog(message, title) {
  return new Promise(function (resolve) {
    var mask = document.createElement('div');
    mask.className = 'modal-mask show';
    mask.innerHTML =
      '<div class="modal modal-sm">' +
      '  <div class="modal-header"><h3>' + escapeHtml(title || '操作确认') + '</h3></div>' +
      '  <div class="modal-body" style="white-space:pre-line;">' + escapeHtml(message) + '</div>' +
      '  <div class="modal-footer">' +
      '    <button type="button" class="btn" data-act="cancel">取消</button>' +
      '    <button type="button" class="btn btn-primary" data-act="ok">确定</button>' +
      '  </div>' +
      '</div>';
    document.body.appendChild(mask);
    mask.addEventListener('click', function (e) {
      var act = e.target.getAttribute && e.target.getAttribute('data-act');
      if (act === 'ok') { mask.remove(); resolve(true); }
      else if (act === 'cancel' || e.target === mask) { mask.remove(); resolve(false); }
    });
  });
}

/* ---------- Modal 通用开关 ---------- */

/** 打开指定 id 的弹窗（页面内静态定义的 .modal-mask） */
function openModal(id) {
  var el = document.getElementById(id);
  if (el) el.classList.add('show');
}

/** 关闭指定 id 的弹窗 */
function closeModal(id) {
  var el = document.getElementById(id);
  if (el) el.classList.remove('show');
}

/** 为页面内所有弹窗绑定：点击遮罩 / [data-close] 元素关闭 */
function bindModalClose() {
  document.querySelectorAll('.modal-mask').forEach(function (mask) {
    mask.addEventListener('click', function (e) {
      if (e.target === mask || (e.target.hasAttribute && e.target.hasAttribute('data-close'))) {
        mask.classList.remove('show');
      }
    });
  });
}

/* ---------- fetch 封装 ---------- */

/**
 * 统一请求封装（同源，Cookie 自动携带）
 * - code=401：跳转登录页
 * - code=0：默认 toast 后端 msg 并 reject（opts.silent=true 时不 toast）
 * - 成功：resolve(data)
 * @param {string} url
 * @param {object} [opts] { method, body(对象自动 JSON)、rawBody(FormData 等原样发送)、silent }
 */
function request(url, opts) {
  opts = opts || {};
  var init = { method: opts.method || 'GET', headers: {} };
  if (opts.rawBody !== undefined) {
    init.body = opts.rawBody; // FormData：浏览器自动设置 multipart 边界
  } else if (opts.body !== undefined) {
    init.headers['Content-Type'] = 'application/json';
    init.body = JSON.stringify(opts.body);
  }
  return fetch(url, init)
    .then(function (res) { return res.json(); })
    .then(function (json) {
      if (json.code === 401) {
        // 会话失效 → 跳管理端登录页
        location.href = '/admin/login.html';
        return new Promise(function () {}); // 阻断后续 then
      }
      if (json.code !== 1) {
        if (!opts.silent) toast(json.msg || '操作失败', 'error');
        var err = new Error(json.msg || '操作失败');
        err.handled = true;
        throw err;
      }
      return json.data;
    })
    .catch(function (e) {
      if (!e.handled && !opts.silent) toast('网络异常，请稍后重试', 'error');
      throw e;
    });
}

/** GET 快捷方式 */
function apiGet(url) { return request(url); }
/** POST 快捷方式（JSON body） */
function apiPost(url, body) { return request(url, { method: 'POST', body: body }); }
/** PUT 快捷方式（JSON body） */
function apiPut(url, body) { return request(url, { method: 'PUT', body: body }); }
/** DELETE 快捷方式 */
function apiDelete(url) { return request(url, { method: 'DELETE' }); }

/* ---------- 分页渲染组件 ---------- */

/**
 * 渲染分页条
 * @param {HTMLElement|string} container 容器元素或其 id
 * @param {number} total    总记录数
 * @param {number} page     当前页（从 1 开始）
 * @param {number} pageSize 每页条数
 * @param {function} onPage 页码点击回调 (page) => void
 */
function renderPagination(container, total, page, pageSize, onPage) {
  var el = typeof container === 'string' ? document.getElementById(container) : container;
  if (!el) return;
  var totalPages = Math.max(1, Math.ceil(total / pageSize));
  var html = '<span class="page-total">共 ' + total + ' 条 / ' + totalPages + ' 页</span>';
  html += '<button type="button" data-page="' + (page - 1) + '"' + (page <= 1 ? ' disabled' : '') + '>上一页</button>';

  // 页码窗口：始终显示首尾页，当前页前后各 2 页，间隔用省略号
  var shown = {};
  var pages = [1, totalPages];
  for (var i = page - 2; i <= page + 2; i++) pages.push(i);
  pages = pages.filter(function (p) { return p >= 1 && p <= totalPages; }).sort(function (a, b) { return a - b; });
  var last = 0;
  pages.forEach(function (p) {
    if (shown[p]) return;
    shown[p] = true;
    if (last && p - last > 1) html += '<span class="page-ellipsis">…</span>';
    html += '<button type="button" data-page="' + p + '" class="' + (p === page ? 'active' : '') + '">' + p + '</button>';
    last = p;
  });

  html += '<button type="button" data-page="' + (page + 1) + '"' + (page >= totalPages ? ' disabled' : '') + '>下一页</button>';
  el.innerHTML = html;
  el.querySelectorAll('button[data-page]').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var p = parseInt(btn.getAttribute('data-page'), 10);
      if (!btn.disabled && !btn.classList.contains('active') && p >= 1 && p <= totalPages) onPage(p);
    });
  });
}

/* ---------- 布局：侧边栏渲染 + 登录人信息 + 退出 ---------- */

/** 侧边栏菜单配置：key 用于高亮 */
var ADMIN_MENUS = [
  { key: 'index',      href: 'index.html',      icon: '📊', label: '系统概览' },
  { key: 'orders',     href: 'orders.html',     icon: '📋', label: '订单管理' },
  { key: 'dishes',     href: 'dishes.html',     icon: '🍜', label: '菜品管理' },
  { key: 'categories', href: 'categories.html', icon: '🗂️', label: '分类管理' },
  { key: 'employees',  href: 'employees.html',  icon: '👥', label: '员工管理' },
  { key: 'notices',    href: 'notices.html',    icon: '📢', label: '公告管理' }
];

/**
 * 初始化管理端页面框架（除登录页外每个页面调用）
 * 1. 渲染侧边栏并高亮当前菜单
 * 2. GET /api/admin/info 校验会话并展示登录人（401 由 request 统一跳登录页）
 * 3. 绑定退出登录
 * @param {string} activeKey 当前菜单 key
 * @param {string} pageTitle 顶栏页面标题
 * @returns {Promise} 登录人信息加载完成后 resolve
 */
function initAdminPage(activeKey, pageTitle) {
  // 1. 侧边栏
  var sidebar = document.getElementById('sidebar');
  if (sidebar) {
    var nav = ADMIN_MENUS.map(function (m) {
      return '<a href="' + m.href + '" class="' + (m.key === activeKey ? 'active' : '') + '">' +
        '<span class="nav-icon">' + m.icon + '</span><span>' + m.label + '</span></a>';
    }).join('');
    sidebar.innerHTML =
      '<div class="logo"><span class="logo-icon">🍱</span><span>外卖订餐管理端</span></div>' +
      '<nav>' + nav + '</nav>' +
      '<div class="sidebar-footer">外卖订餐系统 v1.0</div>';
  }
  // 2. 顶栏标题
  var titleEl = document.getElementById('pageTitle');
  if (titleEl && pageTitle) titleEl.textContent = pageTitle;
  // 3. 会话校验 + 登录人信息
  return apiGet('/api/admin/info').then(function (info) {
    var nameEl = document.getElementById('adminName');
    if (nameEl && info) {
      nameEl.innerHTML = '欢迎，<b>' + escapeHtml(info.realName || info.username) + '</b>';
    }
    // 4. 退出登录
    var logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', function () {
        confirmDialog('确定要退出登录吗？').then(function (ok) {
          if (!ok) return;
          apiPost('/api/admin/logout').then(function () {
            location.href = '/admin/login.html';
          });
        });
      });
    }
    return info;
  });
}
