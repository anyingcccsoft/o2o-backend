/**
 * API 测试用例表格生成器
 * 扫描本项目业务 Controller，生成 xlsx 格式的测试用例表格
 * 运行：node scripts/generate-api-test-cases.js
 */
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const OUTPUT_DIR = path.resolve(__dirname, '..');
const OUTPUT_FILE = path.join(OUTPUT_DIR, 'api-test-cases.xlsx');
const TEMP_ZIP = path.join(OUTPUT_DIR, 'api-test-cases.zip');
const TEMP_DIR = path.join(OUTPUT_DIR, '.xlsx_tmp');

// ===================== 测试用例数据 =====================
const headers = [
  '序号', '模块', '接口名称', 'HTTP方法', '请求路径',
  '用例编号', '用例标题', '前置条件', '请求参数/请求体',
  '预期结果', '优先级', '备注'
];

function api(module, name, method, path, cases) {
  return cases.map((c, idx) => ({
    模块: module,
    接口名称: name,
    HTTP方法: method,
    请求路径: path,
    ...c
  }));
}

const rawCases = [
  // 用户端 - 账户
  ...api('用户端-账户', '用户注册', 'POST', '/api/user/register', [
    { 用例编号: 'TC-U001', 用例标题: '正常注册新用户', 前置条件: '用户名/手机号未被注册', 请求参数: '{"username":"test01","password":"123456","phone":"13800138001"}', 预期结果: 'code=0，msg=注册成功', 优先级: 'P0', 备注: '' },
    { 用例编号: 'TC-U002', 用例标题: '用户名已存在', 前置条件: '用户名 test01 已存在', 请求参数: '{"username":"test01","password":"123456","phone":"13800138002"}', 预期结果: 'code≠0，提示用户名已存在', 优先级: 'P1', 备注: '' },
    { 用例编号: 'TC-U003', 用例标题: '手机号格式错误', 前置条件: '无', 请求参数: '{"username":"test02","password":"123456","phone":"138"}', 预期结果: '参数校验失败，提示手机号格式不正确', 优先级: 'P1', 备注: '@Pattern' },
    { 用例编号: 'TC-U004', 用例标题: '缺少必填参数', 前置条件: '无', 请求参数: '{}', 预期结果: '参数校验失败，提示用户名/密码不能为空', 优先级: 'P1', 备注: '@NotBlank' }
  ]),
  ...api('用户端-账户', '用户登录', 'POST', '/api/user/login', [
    { 用例编号: 'TC-U005', 用例标题: '正常登录', 前置条件: '用户 test01 已注册', 请求参数: '{"username":"test01","password":"123456"}', 预期结果: 'code=0，返回用户信息，session 写入 userId', 优先级: 'P0', 备注: '' },
    { 用例编号: 'TC-U006', 用例标题: '密码错误', 前置条件: '用户 test01 已注册', 请求参数: '{"username":"test01","password":"wrong"}', 预期结果: 'code≠0，提示用户名或密码错误', 优先级: 'P1', 备注: '' },
    { 用例编号: 'TC-U007', 用例标题: '用户不存在', 前置条件: '无', 请求参数: '{"username":"notexist","password":"123456"}', 预期结果: 'code≠0，提示用户名或密码错误', 优先级: 'P1', 备注: '' }
  ]),
  ...api('用户端-账户', '用户退出', 'POST', '/api/user/logout', [
    { 用例编号: 'TC-U008', 用例标题: '正常退出登录', 前置条件: '用户已登录', 请求参数: '无', 预期结果: 'code=0，msg=已退出登录，session 失效', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('用户端-账户', '获取当前用户信息', 'GET', '/api/user/info', [
    { 用例编号: 'TC-U009', 用例标题: '已登录获取信息', 前置条件: '用户已登录', 请求参数: '无', 预期结果: 'code=0，返回当前用户信息', 优先级: 'P0', 备注: '需登录' },
    { 用例编号: 'TC-U010', 用例标题: '未登录获取信息', 前置条件: '未登录', 请求参数: '无', 预期结果: 'code≠0，提示请先登录/未授权', 优先级: 'P1', 备注: '需登录' }
  ]),
  ...api('用户端-账户', '修改用户信息', 'PUT', '/api/user/info', [
    { 用例编号: 'TC-U011', 用例标题: '正常修改昵称和手机号', 前置条件: '用户已登录', 请求参数: '{"nickname":"新昵称","phone":"13800138003"}', 预期结果: 'code=0，msg=修改成功', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('用户端-账户', '修改密码', 'POST', '/api/user/password', [
    { 用例编号: 'TC-U012', 用例标题: '正常修改密码', 前置条件: '用户已登录，旧密码正确', 请求参数: '{"oldPassword":"123456","newPassword":"654321"}', 预期结果: 'code=0，msg=密码修改成功', 优先级: 'P0', 备注: '需登录' },
    { 用例编号: 'TC-U013', 用例标题: '旧密码错误', 前置条件: '用户已登录', 请求参数: '{"oldPassword":"wrong","newPassword":"654321"}', 预期结果: 'code≠0，提示旧密码错误', 优先级: 'P1', 备注: '需登录' },
    { 用例编号: 'TC-U014', 用例标题: '新密码过短', 前置条件: '用户已登录', 请求参数: '{"oldPassword":"123456","newPassword":"123"}', 预期结果: '参数校验失败，提示新密码长度至少6位', 优先级: 'P1', 备注: '需登录' }
  ]),

  // 用户端 - 地址
  ...api('用户端-地址', '地址列表', 'GET', '/api/addresses', [
    { 用例编号: 'TC-A001', 用例标题: '查看地址列表', 前置条件: '用户已登录且存在地址', 请求参数: '无', 预期结果: 'code=0，返回当前用户地址列表', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('用户端-地址', '新增地址', 'POST', '/api/addresses', [
    { 用例编号: 'TC-A002', 用例标题: '正常新增地址', 前置条件: '用户已登录', 请求参数: '{"receiver":"张三","phone":"13800138001","detail":"xx小区1栋","isDefault":0}', 预期结果: 'code=0，msg=添加成功', 优先级: 'P0', 备注: '需登录' },
    { 用例编号: 'TC-A003', 用例标题: '缺少收件人', 前置条件: '用户已登录', 请求参数: '{"phone":"13800138001","detail":"xx小区"}', 预期结果: '参数校验失败，提示收件人不能为空', 优先级: 'P1', 备注: '需登录' }
  ]),
  ...api('用户端-地址', '修改地址', 'PUT', '/api/addresses/{id}', [
    { 用例编号: 'TC-A004', 用例标题: '正常修改地址', 前置条件: '用户已登录，地址存在', 请求参数: '{"receiver":"李四","phone":"13800138002","detail":"yy小区2栋","isDefault":1}', 预期结果: 'code=0，msg=修改成功', 优先级: 'P0', 备注: '需登录' },
    { 用例编号: 'TC-A005', 用例标题: '修改他人地址', 前置条件: '用户已登录，地址不属于当前用户', 请求参数: '同上', 预期结果: 'code≠0，提示无权限/资源不存在', 优先级: 'P1', 备注: '需登录' }
  ]),
  ...api('用户端-地址', '删除地址', 'DELETE', '/api/addresses/{id}', [
    { 用例编号: 'TC-A006', 用例标题: '正常删除地址', 前置条件: '用户已登录，地址存在', 请求参数: '无', 预期结果: 'code=0，msg=删除成功', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('用户端-地址', '设置默认地址', 'PUT', '/api/addresses/{id}/default', [
    { 用例编号: 'TC-A007', 用例标题: '设置默认地址', 前置条件: '用户已登录，地址存在', 请求参数: '无', 预期结果: 'code=0，msg=已设为默认地址', 优先级: 'P0', 备注: '需登录' }
  ]),

  // 用户端 - 购物车
  ...api('用户端-购物车', '购物车列表', 'GET', '/api/cart', [
    { 用例编号: 'TC-C001', 用例标题: '查看购物车', 前置条件: '用户已登录', 请求参数: '无', 预期结果: 'code=0，返回购物车条目', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('用户端-购物车', '加入购物车', 'POST', '/api/cart', [
    { 用例编号: 'TC-C002', 用例标题: '正常加入购物车', 前置条件: '用户已登录，菜品在售', 请求参数: '{"dishId":1,"quantity":2}', 预期结果: 'code=0，msg=已加入购物车', 优先级: 'P0', 备注: '需登录' },
    { 用例编号: 'TC-C003', 用例标题: '加入下架菜品', 前置条件: '用户已登录，菜品 status=0', 请求参数: '{"dishId":1,"quantity":1}', 预期结果: 'code≠0，提示菜品已下架或库存不足', 优先级: 'P1', 备注: '需登录' }
  ]),
  ...api('用户端-购物车', '修改购物车数量', 'PUT', '/api/cart/{id}', [
    { 用例编号: 'TC-C004', 用例标题: '正常修改数量', 前置条件: '用户已登录，购物车条目存在', 请求参数: '{"quantity":3}', 预期结果: 'code=0，msg=修改成功', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('用户端-购物车', '删除购物车项', 'DELETE', '/api/cart/{id}', [
    { 用例编号: 'TC-C005', 用例标题: '删除购物车项', 前置条件: '用户已登录，购物车条目存在', 请求参数: '无', 预期结果: 'code=0，msg=删除成功', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('用户端-购物车', '清空购物车', 'DELETE', '/api/cart', [
    { 用例编号: 'TC-C006', 用例标题: '清空购物车', 前置条件: '用户已登录', 请求参数: '无', 预期结果: 'code=0，msg=购物车已清空', 优先级: 'P0', 备注: '需登录' }
  ]),

  // 用户端 - 分类/菜品/公告
  ...api('用户端-分类', '分类列表', 'GET', '/api/categories', [
    { 用例编号: 'TC-CA001', 用例标题: '获取启用分类列表', 前置条件: '无', 请求参数: '无', 预期结果: 'code=0，返回启用的分类列表', 优先级: 'P0', 备注: '公开接口' }
  ]),
  ...api('用户端-菜品', '菜品分页', 'GET', '/api/dishes', [
    { 用例编号: 'TC-D001', 用例标题: '分页查询在售菜品', 前置条件: '无', 请求参数: '?page=1&pageSize=10', 预期结果: 'code=0，返回分页数据', 优先级: 'P0', 备注: '公开接口' },
    { 用例编号: 'TC-D002', 用例标题: '按分类筛选菜品', 前置条件: '无', 请求参数: '?categoryId=1&page=1&pageSize=10', 预期结果: 'code=0，返回该分类下的菜品', 优先级: 'P1', 备注: '公开接口' }
  ]),
  ...api('用户端-菜品', '热门菜品', 'GET', '/api/dishes/hot', [
    { 用例编号: 'TC-D003', 用例标题: '获取热门菜品', 前置条件: '无', 请求参数: '无', 预期结果: 'code=0，返回销量前N的菜品', 优先级: 'P0', 备注: '公开接口' }
  ]),
  ...api('用户端-菜品', '菜品详情', 'GET', '/api/dishes/{id}', [
    { 用例编号: 'TC-D004', 用例标题: '获取菜品详情', 前置条件: '菜品存在且在售', 请求参数: '无', 预期结果: 'code=0，返回菜品详情', 优先级: 'P0', 备注: '公开接口' },
    { 用例编号: 'TC-D005', 用例标题: '获取不存在的菜品', 前置条件: '无', 请求参数: 'id=999999', 预期结果: 'code≠0，提示菜品不存在', 优先级: 'P1', 备注: '公开接口' }
  ]),
  ...api('用户端-公告', '公告列表', 'GET', '/api/notices', [
    { 用例编号: 'TC-N001', 用例标题: '获取已发布公告', 前置条件: '无', 请求参数: '无', 预期结果: 'code=0，返回公告列表', 优先级: 'P0', 备注: '公开接口' }
  ]),

  // 用户端 - 订单
  ...api('用户端-订单', '创建订单', 'POST', '/api/orders', [
    { 用例编号: 'TC-O001', 用例标题: '正常创建订单', 前置条件: '用户已登录，购物车有商品且地址存在', 请求参数: '{"addressId":1,"remark":"少辣"}', 预期结果: 'code=0，返回订单号与订单ID', 优先级: 'P0', 备注: '需登录' },
    { 用例编号: 'TC-O002', 用例标题: '购物车为空创建订单', 前置条件: '用户已登录，购物车为空', 请求参数: '{"addressId":1}', 预期结果: 'code≠0，提示购物车为空', 优先级: 'P1', 备注: '需登录' }
  ]),
  ...api('用户端-订单', '支付订单', 'POST', '/api/orders/{id}/pay', [
    { 用例编号: 'TC-O003', 用例标题: '正常支付订单', 前置条件: '用户已登录，订单状态待支付', 请求参数: '无', 预期结果: 'code=0，msg=支付成功', 优先级: 'P0', 备注: '需登录' },
    { 用例编号: 'TC-O004', 用例标题: '重复支付', 前置条件: '用户已登录，订单状态已支付', 请求参数: '无', 预期结果: 'code≠0，提示订单状态不正确', 优先级: 'P1', 备注: '需登录' }
  ]),
  ...api('用户端-订单', '取消订单', 'POST', '/api/orders/{id}/cancel', [
    { 用例编号: 'TC-O005', 用例标题: '正常取消订单', 前置条件: '用户已登录，订单状态待支付/待接单', 请求参数: '无', 预期结果: 'code=0，msg=订单已取消', 优先级: 'P0', 备注: '需登录' },
    { 用例编号: 'TC-O006', 用例标题: '取消非本人订单', 前置条件: '用户已登录', 请求参数: 'id=他人订单ID', 预期结果: 'code≠0，提示无权限/订单不存在', 优先级: 'P1', 备注: '需登录' }
  ]),
  ...api('用户端-订单', '订单列表', 'GET', '/api/orders', [
    { 用例编号: 'TC-O007', 用例标题: '查看订单列表', 前置条件: '用户已登录', 请求参数: '?page=1&pageSize=10', 预期结果: 'code=0，返回当前用户订单分页', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('用户端-订单', '订单详情', 'GET', '/api/orders/{id}', [
    { 用例编号: 'TC-O008', 用例标题: '查看订单详情', 前置条件: '用户已登录，订单存在', 请求参数: '无', 预期结果: 'code=0，返回订单详情', 优先级: 'P0', 备注: '需登录' }
  ]),

  // 管理端 - 认证
  ...api('管理端-认证', '员工登录', 'POST', '/api/admin/login', [
    { 用例编号: 'TC-MA001', 用例标题: '正常登录', 前置条件: '员工账号存在且状态启用', 请求参数: '{"username":"admin","password":"123456"}', 预期结果: 'code=0，返回员工信息，session 写入 adminId', 优先级: 'P0', 备注: '' },
    { 用例编号: 'TC-MA002', 用例标题: '密码错误', 前置条件: '员工账号存在', 请求参数: '{"username":"admin","password":"wrong"}', 预期结果: 'code≠0，提示用户名或密码错误', 优先级: 'P1', 备注: '' }
  ]),
  ...api('管理端-认证', '员工退出', 'POST', '/api/admin/logout', [
    { 用例编号: 'TC-MA003', 用例标题: '正常退出', 前置条件: '员工已登录', 请求参数: '无', 预期结果: 'code=0，msg=已退出登录', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('管理端-认证', '获取当前员工信息', 'GET', '/api/admin/info', [
    { 用例编号: 'TC-MA004', 用例标题: '已登录获取信息', 前置条件: '员工已登录', 请求参数: '无', 预期结果: 'code=0，返回当前员工信息', 优先级: 'P0', 备注: '需登录' },
    { 用例编号: 'TC-MA005', 用例标题: '未登录获取信息', 前置条件: '未登录', 请求参数: '无', 预期结果: 'code≠0，提示未登录', 优先级: 'P1', 备注: '需登录' }
  ]),

  // 管理端 - 分类
  ...api('管理端-分类', '分类列表', 'GET', '/api/admin/categories', [
    { 用例编号: 'TC-MC001', 用例标题: '获取全部分类', 前置条件: '员工已登录', 请求参数: '无', 预期结果: 'code=0，返回分类列表', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('管理端-分类', '新增分类', 'POST', '/api/admin/categories', [
    { 用例编号: 'TC-MC002', 用例标题: '正常新增分类', 前置条件: '员工已登录', 请求参数: '{"name":"川菜","sort":1,"status":1}', 预期结果: 'code=0，msg=新增成功', 优先级: 'P0', 备注: '需登录' },
    { 用例编号: 'TC-MC003', 用例标题: '分类名称重复', 前置条件: '员工已登录，分类名已存在', 请求参数: '{"name":"川菜","sort":1,"status":1}', 预期结果: 'code≠0，提示分类已存在', 优先级: 'P1', 备注: '需登录' }
  ]),
  ...api('管理端-分类', '修改分类', 'PUT', '/api/admin/categories/{id}', [
    { 用例编号: 'TC-MC004', 用例标题: '正常修改分类', 前置条件: '员工已登录，分类存在', 请求参数: '{"name":"川菜-改","sort":2,"status":1}', 预期结果: 'code=0，msg=修改成功', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('管理端-分类', '删除分类', 'DELETE', '/api/admin/categories/{id}', [
    { 用例编号: 'TC-MC005', 用例标题: '正常删除分类', 前置条件: '员工已登录，分类下无菜品', 请求参数: '无', 预期结果: 'code=0，msg=删除成功', 优先级: 'P0', 备注: '需登录' },
    { 用例编号: 'TC-MC006', 用例标题: '删除有菜品的分类', 前置条件: '员工已登录，分类下存在菜品', 请求参数: '无', 预期结果: 'code≠0，提示分类下存在菜品无法删除', 优先级: 'P1', 备注: '需登录' }
  ]),

  // 管理端 - 菜品
  ...api('管理端-菜品', '菜品分页', 'GET', '/api/admin/dishes', [
    { 用例编号: 'TC-MD001', 用例标题: '分页查询菜品', 前置条件: '员工已登录', 请求参数: '?page=1&pageSize=10', 预期结果: 'code=0，返回分页数据', 优先级: 'P0', 备注: '需登录' },
    { 用例编号: 'TC-MD002', 用例标题: '按分类和关键词查询', 前置条件: '员工已登录', 请求参数: '?categoryId=1&keyword=鸡&page=1&pageSize=10', 预期结果: 'code=0，返回筛选结果', 优先级: 'P1', 备注: '需登录' }
  ]),
  ...api('管理端-菜品', '新增菜品', 'POST', '/api/admin/dishes', [
    { 用例编号: 'TC-MD003', 用例标题: '正常新增菜品', 前置条件: '员工已登录', 请求参数: '{"categoryId":1,"name":"测试菜","price":18.00,"imageUrl":"/images/dish/test.svg","stock":100,"status":1}', 预期结果: 'code=0，msg=新增成功', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('管理端-菜品', '修改菜品', 'PUT', '/api/admin/dishes/{id}', [
    { 用例编号: 'TC-MD004', 用例标题: '正常修改菜品', 前置条件: '员工已登录，菜品存在', 请求参数: '{"categoryId":1,"name":"测试菜-改","price":20.00,"stock":50,"status":1}', 预期结果: 'code=0，msg=修改成功', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('管理端-菜品', '删除菜品', 'DELETE', '/api/admin/dishes/{id}', [
    { 用例编号: 'TC-MD005', 用例标题: '正常删除菜品', 前置条件: '员工已登录，菜品存在且无未处理订单', 请求参数: '无', 预期结果: 'code=0，msg=删除成功', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('管理端-菜品', '修改菜品状态', 'PUT', '/api/admin/dishes/{id}/status', [
    { 用例编号: 'TC-MD006', 用例标题: '上架/下架菜品', 前置条件: '员工已登录，菜品存在', 请求参数: '{"status":0}', 预期结果: 'code=0，msg=操作成功', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('管理端-菜品', '调整库存', 'PUT', '/api/admin/dishes/{id}/stock', [
    { 用例编号: 'TC-MD007', 用例标题: '正常调整库存', 前置条件: '员工已登录，菜品存在', 请求参数: '{"quantity":10,"type":"in","reason":"补货"}', 预期结果: 'code=0，返回最新库存', 优先级: 'P0', 备注: '需登录' },
    { 用例编号: 'TC-MD008', 用例标题: '库存扣减超出可用', 前置条件: '员工已登录，菜品库存为5', 请求参数: '{"quantity":10,"type":"out","reason":"损耗"}', 预期结果: 'code≠0，提示库存不足', 优先级: 'P1', 备注: '需登录' }
  ]),

  // 管理端 - 公告
  ...api('管理端-公告', '公告列表', 'GET', '/api/admin/notices', [
    { 用例编号: 'TC-MN001', 用例标题: '获取全部公告', 前置条件: '员工已登录', 请求参数: '无', 预期结果: 'code=0，返回公告列表', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('管理端-公告', '新增公告', 'POST', '/api/admin/notices', [
    { 用例编号: 'TC-MN002', 用例标题: '正常新增公告', 前置条件: '员工已登录', 请求参数: '{"title":"活动公告","content":"全场满减","status":1}', 预期结果: 'code=0，msg=新增成功', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('管理端-公告', '修改公告', 'PUT', '/api/admin/notices/{id}', [
    { 用例编号: 'TC-MN003', 用例标题: '正常修改公告', 前置条件: '员工已登录，公告存在', 请求参数: '{"title":"活动公告-改","content":"更新内容","status":1}', 预期结果: 'code=0，msg=修改成功', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('管理端-公告', '删除公告', 'DELETE', '/api/admin/notices/{id}', [
    { 用例编号: 'TC-MN004', 用例标题: '正常删除公告', 前置条件: '员工已登录，公告存在', 请求参数: '无', 预期结果: 'code=0，msg=删除成功', 优先级: 'P0', 备注: '需登录' }
  ]),

  // 管理端 - 订单
  ...api('管理端-订单', '订单分页', 'GET', '/api/admin/orders', [
    { 用例编号: 'TC-MO001', 用例标题: '分页查询订单', 前置条件: '员工已登录', 请求参数: '?page=1&pageSize=10', 预期结果: 'code=0，返回订单分页', 优先级: 'P0', 备注: '需登录' },
    { 用例编号: 'TC-MO002', 用例标题: '按状态筛选订单', 前置条件: '员工已登录', 请求参数: '?status=0&page=1&pageSize=10', 预期结果: 'code=0，返回待接单订单', 优先级: 'P1', 备注: '需登录' }
  ]),
  ...api('管理端-订单', '订单详情', 'GET', '/api/admin/orders/{id}', [
    { 用例编号: 'TC-MO003', 用例标题: '查看订单详情', 前置条件: '员工已登录，订单存在', 请求参数: '无', 预期结果: 'code=0，返回订单详情', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('管理端-订单', '修改订单状态', 'PUT', '/api/admin/orders/{id}/status', [
    { 用例编号: 'TC-MO004', 用例标题: '接单', 前置条件: '员工已登录，订单状态待接单', 请求参数: '{"status":1}', 预期结果: 'code=0，msg=操作成功', 优先级: 'P0', 备注: '需登录' },
    { 用例编号: 'TC-MO005', 用例标题: '非法状态流转', 前置条件: '员工已登录，订单状态已完成', 请求参数: '{"status":1}', 预期结果: 'code≠0，提示状态流转非法', 优先级: 'P1', 备注: '需登录' }
  ]),

  // 管理端 - 员工
  ...api('管理端-员工', '员工分页', 'GET', '/api/admin/employees', [
    { 用例编号: 'TC-ME001', 用例标题: '分页查询员工', 前置条件: '员工已登录', 请求参数: '?page=1&pageSize=10', 预期结果: 'code=0，返回员工分页', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('管理端-员工', '新增员工', 'POST', '/api/admin/employees', [
    { 用例编号: 'TC-ME002', 用例标题: '正常新增员工', 前置条件: '员工已登录', 请求参数: '{"username":"emp01","password":"123456","realName":"员工甲","phone":"13800138004","status":1}', 预期结果: 'code=0，msg=新增成功', 优先级: 'P0', 备注: '需登录' },
    { 用例编号: 'TC-ME003', 用例标题: '员工用户名重复', 前置条件: '员工已登录，用户名 emp01 已存在', 请求参数: '同上', 预期结果: 'code≠0，提示用户名已存在', 优先级: 'P1', 备注: '需登录' }
  ]),
  ...api('管理端-员工', '修改员工', 'PUT', '/api/admin/employees/{id}', [
    { 用例编号: 'TC-ME004', 用例标题: '正常修改员工', 前置条件: '员工已登录，员工存在', 请求参数: '{"realName":"员工甲-改","phone":"13800138005","status":1}', 预期结果: 'code=0，msg=修改成功', 优先级: 'P0', 备注: '需登录' }
  ]),
  ...api('管理端-员工', '删除员工', 'DELETE', '/api/admin/employees/{id}', [
    { 用例编号: 'TC-ME005', 用例标题: '正常删除员工', 前置条件: '员工已登录，员工存在且非当前登录员工', 请求参数: '无', 预期结果: 'code=0，msg=删除成功', 优先级: 'P0', 备注: '需登录' },
    { 用例编号: 'TC-ME006', 用例标题: '删除当前登录员工', 前置条件: '员工已登录', 请求参数: 'id=当前员工ID', 预期结果: 'code≠0，提示不能删除自己', 优先级: 'P1', 备注: '需登录' }
  ]),
  ...api('管理端-员工', '修改员工状态', 'PUT', '/api/admin/employees/{id}/status', [
    { 用例编号: 'TC-ME007', 用例标题: '禁用/启用员工', 前置条件: '员工已登录，员工存在且非当前登录员工', 请求参数: '{"status":0}', 预期结果: 'code=0，msg=操作成功', 优先级: 'P0', 备注: '需登录' }
  ]),

  // 管理端 - 上传
  ...api('管理端-上传', '图片上传', 'POST', '/api/admin/upload', [
    { 用例编号: 'TC-MU001', 用例标题: '正常上传图片', 前置条件: '员工已登录', 请求参数: 'file: valid.jpg', 预期结果: 'code=0，返回图片访问 URL', 优先级: 'P0', 备注: 'multipart/form-data，需登录' },
    { 用例编号: 'TC-MU002', 用例标题: '上传非法格式文件', 前置条件: '员工已登录', 请求参数: 'file: invalid.txt', 预期结果: 'code≠0，提示仅支持 jpg/jpeg/png/gif/webp', 优先级: 'P1', 备注: '需登录' },
    { 用例编号: 'TC-MU003', 用例标题: '上传超大文件', 前置条件: '员工已登录', 请求参数: 'file: >10MB.jpg', 预期结果: 'code≠0，提示文件大小不能超过10MB', 优先级: 'P1', 备注: '需登录' }
  ])
];

// 按顺序追加序号
const cases = rawCases.map((c, idx) => ({
  序号: idx + 1,
  ...c
}));

// ===================== xlsx 生成逻辑 =====================
function escapeXml(str) {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;');
}

function colName(index) {
  let name = '';
  let n = index + 1;
  while (n > 0) {
    const rem = (n - 1) % 26;
    name = String.fromCharCode(65 + rem) + name;
    n = Math.floor((n - 1) / 26);
  }
  return name;
}

// 构建 sharedStrings
const stringSet = new Set();
headers.forEach(h => stringSet.add(h));
cases.forEach(row => {
  headers.forEach(h => stringSet.add(row[h] || ''));
});
const stringList = Array.from(stringSet);
const stringIndex = new Map(stringList.map((s, i) => [s, i]));

const sharedStringsXml = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="${stringList.length}" uniqueCount="${stringList.length}">
${stringList.map(s => `  <si><t>${escapeXml(s)}</t></si>`).join('\n')}
</sst>`;

// 构建 worksheet
const rows = [headers, ...cases.map(row => headers.map(h => row[h] || ''))];
let worksheetXml = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <sheetData>`;

rows.forEach((row, rIdx) => {
  const rNum = rIdx + 1;
  worksheetXml += `\n    <row r="${rNum}">`;
  row.forEach((cell, cIdx) => {
    const ref = colName(cIdx) + rNum;
    const sIdx = stringIndex.get(cell);
    worksheetXml += `<c r="${ref}" t="s"><v>${sIdx}</v></c>`;
  });
  worksheetXml += '</row>';
});

worksheetXml += `\n  </sheetData>\n</worksheet>`;

const stylesXml = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="1"><font><sz val="11"/><name val="Calibri"/></font></fonts>
  <fills count="2">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFD9E1F2"/><bgColor indexed="64"/></patternFill></fill>
  </fills>
  <borders count="1"><border/></borders>
  <cellStyleXfs count="2">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
    <xf numFmtId="0" fontId="0" fillId="1" borderId="0" applyFill="1"/>
  </cellStyleXfs>
  <cellXfs count="2">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
    <xf numFmtId="0" fontId="0" fillId="1" borderId="0" xfId="1" applyFill="1"/>
  </cellXfs>
</styleSheet>`;

const workbookXml = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="API测试用例" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>`;

const relsRels = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>`;

const workbookRels = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings" Target="sharedStrings.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>`;

const contentTypesXml = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>`;

// 创建临时目录结构
function ensureDir(dir) {
  if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
}

if (fs.existsSync(TEMP_DIR)) fs.rmSync(TEMP_DIR, { recursive: true, force: true });
ensureDir(path.join(TEMP_DIR, '_rels'));
ensureDir(path.join(TEMP_DIR, 'xl', '_rels'));
ensureDir(path.join(TEMP_DIR, 'xl', 'worksheets'));

fs.writeFileSync(path.join(TEMP_DIR, '[Content_Types].xml'), contentTypesXml, 'utf-8');
fs.writeFileSync(path.join(TEMP_DIR, '_rels', '.rels'), relsRels, 'utf-8');
fs.writeFileSync(path.join(TEMP_DIR, 'xl', 'workbook.xml'), workbookXml, 'utf-8');
fs.writeFileSync(path.join(TEMP_DIR, 'xl', '_rels', 'workbook.xml.rels'), workbookRels, 'utf-8');
fs.writeFileSync(path.join(TEMP_DIR, 'xl', 'worksheets', 'sheet1.xml'), worksheetXml, 'utf-8');
fs.writeFileSync(path.join(TEMP_DIR, 'xl', 'sharedStrings.xml'), sharedStringsXml, 'utf-8');
fs.writeFileSync(path.join(TEMP_DIR, 'xl', 'styles.xml'), stylesXml, 'utf-8');

// 使用 PowerShell Compress-Archive 先打包为 zip，再重命名为 xlsx
const psCmd = `Compress-Archive -Path '${TEMP_DIR}\\*' -DestinationPath '${TEMP_ZIP}' -Force`;
try {
  execSync(psCmd, { shell: 'powershell.exe', stdio: 'inherit' });
  if (fs.existsSync(OUTPUT_FILE)) fs.rmSync(OUTPUT_FILE, { force: true });
  fs.renameSync(TEMP_ZIP, OUTPUT_FILE);
  console.log(`测试用例表格已生成: ${OUTPUT_FILE}`);
} catch (e) {
  console.error('生成 xlsx 失败:', e.message);
  process.exit(1);
} finally {
  if (fs.existsSync(TEMP_DIR)) fs.rmSync(TEMP_DIR, { recursive: true, force: true });
  if (fs.existsSync(TEMP_ZIP)) fs.rmSync(TEMP_ZIP, { force: true });
}
