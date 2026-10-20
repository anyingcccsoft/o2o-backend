# 外卖订餐系统 后端服务（o2o-backend）

外卖订餐系统的后端服务，基于 **Spring Boot 3.2 + MyBatis** 构建。对外提供 `/api/**` 的 REST 接口供用户端前端调用，同时内置一套管理端静态页面（`/admin/`），以及图片上传与静态资源访问能力。

---

## 技术栈

| 组件 | 版本 / 说明 |
| --- | --- |
| Spring Boot | 3.2.12（`spring-boot-starter-parent`） |
| Java | 17（`maven.compiler.release=17`） |
| Web 层 | `spring-boot-starter-web`（内嵌 Tomcat，端口 8080） |
| 持久层 | MyBatis `mybatis-spring-boot-starter` 3.0.3 + HikariCP |
| 数据库 | H2（dev，内存库 MySQL 兼容模式） / MySQL 8.0（prod，`mysql-connector-j`） |
| 密码加密 | `spring-security-crypto` 的 `BCryptPasswordEncoder`（未引入完整 Spring Security） |
| 参数校验 | `spring-boot-starter-validation` |
| 代码简化 | Lombok（`provided` 作用域，打包时已 exclude） |
| 测试 | `spring-boot-starter-test` |
| 构建 | Maven，`spring-boot-maven-plugin` 打成可执行 JAR |

会话与鉴权：基于 `HttpSession`，由 `UserAuthInterceptor` / `AdminAuthInterceptor` 两个拦截器实现用户端与管理端的双会话隔离；`ApiCacheControlInterceptor` 为所有 `/api/**` 响应加上禁止缓存的头。

---

## 目录结构

```
o2o-backend/
├── .mvn/
│   └── settings.xml              # 项目级 Maven settings（本地仓库 + 阿里云镜像）
├── docs/
│   └── sql/
│       ├── schema.sql            # MySQL 建库建表脚本（prod 必需）
│       └── init_data.sql         # MySQL 初始化种子数据（prod 必需）
├── scripts/
│   └── ConcurrencyTest.java      # 并发下单测试脚本（独立运行，非构建产物）
├── src/main/
│   ├── java/com/shangda/o2o/
│   │   ├── O2oApplication.java   # 启动类
│   │   ├── common/               # 统一响应体、常量
│   │   ├── config/               # CorsConfig、WebConfig
│   │   ├── controller/           # 用户端接口；controller/admin 为管理端接口
│   │   ├── dto/                  # 请求/响应传输对象；dto/admin 为管理端专用
│   │   ├── entity/               # 数据库实体
│   │   ├── exception/            # 业务异常与全局异常处理
│   │   ├── interceptor/          # 登录鉴权、API 缓存控制拦截器
│   │   ├── mapper/               # MyBatis Mapper 接口
│   │   ├── service/              # 业务接口，service/impl 为实现
│   │   └── util/                 # 工具类
│   └── resources/
│       ├── application.yml       # 主配置（默认激活 dev）
│       ├── application-dev.yml   # H2 内存库
│       ├── application-prod.yml  # MySQL 数据源
│       ├── schema-h2.sql         # H2 建表脚本（dev 自动执行）
│       ├── data.sql              # H2 种子数据（dev 自动执行）
│       ├── mapper/*.xml          # MyBatis 映射文件
│       └── static/
│           ├── admin/            # 管理端页面（index/login/dishes/orders/...）+ css/js
│           └── images/dish/      # 菜品内置示意图
├── upload/                       # 运行时图片上传目录（相对路径，已在 .gitignore 中）
├── pom.xml
└── README.md
```

---

## 环境要求

- **JDK 17**：本机安装于 `D:\JDK\JDK17`
- **Maven**：本机安装于 `D:\maven\apache-maven-3.9.11`
- **MySQL 8.0**：仅 prod profile 需要；dev 使用内存 H2，免安装

---

## 构建

在项目根目录（PowerShell）执行：

```powershell
$env:JAVA_HOME='D:\JDK\JDK17'; D:\maven\apache-maven-3.9.11\bin\mvn.cmd -gs .mvn\settings.xml -s .mvn\settings.xml clean package
```

> **为什么必须带 `-gs` 和 `-s`**：本机 Maven 安装目录下的全局 `conf/settings.xml` 存在 XML 语法错误，不指定时构建会直接解析失败。`-gs`（全局 settings）与 `-s`（用户 settings）同时指向项目内的 `.mvn\settings.xml`，用它替换掉损坏的全局配置。

产物：`target/o2o-1.0.0.jar`（可执行 JAR）。

---

## 启动

> ⚠️ **必须从项目根目录启动**。`application.yml` 中 `o2o.upload-dir: ./upload/` 是相对路径，`WebConfig` 会基于当前工作目录解析成绝对路径；在其他目录启动会导致上传的图片写入错误位置、`/upload/**` 访问 404。

### 方式一：dev profile（H2 内存库，开箱即用）

`application.yml` 默认 `spring.profiles.active=dev`，直接启动即可：

```powershell
$env:JAVA_HOME='D:\JDK\JDK17'; java -jar target\o2o-1.0.0.jar
```

启动时会自动执行 `schema-h2.sql` 建表、`data.sql` 灌入种子数据（管理员、测试用户、菜品分类与菜品）。H2 控制台：<http://localhost:8080/h2-console>（JDBC URL `jdbc:h2:mem:o2o`，用户名 `sa`，密码为空）。

数据仅存在于内存，**进程重启即清空**。

### 方式二：prod profile（MySQL 持久化）

1. 先在 MySQL 中依次执行建库脚本：

   ```powershell
   mysql -uroot -p < docs\sql\schema.sql
   mysql -uroot -p < docs\sql\init_data.sql
   ```

2. 按实际情况核对 `application-prod.yml` 中的连接信息（默认 `jdbc:mysql://localhost:3306/o2o`，账号 `root/root`）。

3. 以 prod profile 启动（该 profile 下 `spring.sql.init.mode=never`，不会再自动执行脚本）：

   ```powershell
   $env:JAVA_HOME='D:\JDK\JDK17'; java -jar target\o2o-1.0.0.jar --spring.profiles.active=prod
   ```

---

## 访问入口

| 入口 | 地址 | 说明 |
| --- | --- | --- |
| 管理端 | <http://localhost:8080/admin/> | 内置静态页面，种子账号 **admin / 123456** |
| REST API | `http://localhost:8080/api/**` | 供用户端前端调用 |
| 上传图片 | `http://localhost:8080/upload/**` | 映射到本地 `./upload/` 目录 |
| H2 控制台 | <http://localhost:8080/h2-console> | 仅 dev profile |

免登录的公开接口：`/api/user/register`、`/api/user/login`、`/api/categories`、`/api/dishes/**`、`/api/notices`、`/api/admin/login`。

---

## 关联项目

- **用户端前端**：`../o2o-frontend`（Vue 3 + Vite，开发服务器 5173，通过 `/api` 访问本服务）
- **毕业设计文档**：`../o2o-docs`（需求分析、概要/详细设计、数据库设计、测试与部署文档）

---

## 注意事项

1. **`.mvn/settings.xml` 硬编码了本地仓库路径 `D:\maven\repository`**。换机器或换用户开发时，需要把 `<localRepository>` 改成该机器上的实际仓库目录，否则依赖会被下载到不存在/不期望的位置。
2. **CORS 白名单硬编码在 `CorsConfig.java` 中**，当前只放行 `http://localhost:5173` 与 `http://127.0.0.1:5173`（携带凭证访问 `/api/**`）。前端开发服务器端口变更、或部署到其他域名时，必须同步修改这个类并重新构建，否则浏览器端会因跨域被拦截。
3. **prod 数据库账号密码明文写在 `application-prod.yml` 中**，请勿将真实生产凭据提交到版本库。
4. `upload/`、`target/`、`out/` 已在 `.gitignore` 中忽略；上传的图片属于运行时数据，不随代码版本管理。

---

## 后续可选优化建议（非当前范围）

以下均为当前未实施的改进方向，记录备查：

- **CORS 白名单配置化**：把允许的来源从 `CorsConfig.java` 抽到 `application.yml`（如 `o2o.cors.allowed-origins`），改端口/域名时只改配置、不必重新编译。
- **upload-dir 生产使用绝对路径**：prod profile 下将 `o2o.upload-dir` 配置为绝对路径（如 `/data/o2o/upload/`），消除"必须从项目根目录启动"这一隐式约束，也便于挂载独立数据卷。
- **prod 数据源外部化为环境变量**：改为 `${MYSQL_URL}`、`${MYSQL_USER}`、`${MYSQL_PASSWORD}` 形式，凭据不入库，方便 Docker / CI 环境按需注入。
