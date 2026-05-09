# cap4k-reference-content-studio

`cap4k-reference-content-studio` 是 `cap4k` 的可运行参考项目。
它展示了一个小而完整的内容发布流程，建立在生成出来的
domain / application / adapter 分层之上：

- 创建内容草稿
- 提交并通过审核
- 通过 HTTP 集成回调触发媒体处理完成
- 观察内容最终进入已发布状态

你应该能够先克隆这个仓库，再直接跑起本地流程，而不必先读完整个
`cap4k` 仓库。

## 仓库内容

这个仓库分成 4 个 Gradle 模块：

- `cap4k-reference-content-studio-domain`：领域模型，以及提交进仓的生成领域快照
- `cap4k-reference-content-studio-application`：应用层命令、查询和订阅器
- `cap4k-reference-content-studio-adapter`：HTTP 控制器、查询适配、持久化适配
- `cap4k-reference-content-studio-start`：你本地实际启动的 Spring Boot 运行时

本地运行时可以把它当作一个只有单个 Spring Boot 进程、单个内存 H2 数据库的参考应用。

## 前置条件

1. 使用 JDK 21。
2. 在构建或启动这个仓库之前，先把所需的 `cap4k` 快照工件发布到 `mavenLocal()`。

从你的本地 `cap4k` 目录先执行发布步骤：

```bash
./gradlew publishToMavenLocal
```

这个仓库依赖 `mavenLocal()` 里的 `0.5.0-SNAPSHOT` 工件。如果跳过这一步，
即使这个仓库本身检出正确，Gradle 也会解析失败。

## 最短启动路径

在仓库根目录执行：

```bash
./gradlew :cap4k-reference-content-studio-start:bootRun
```

Windows 上：

```powershell
.\gradlew.bat :cap4k-reference-content-studio-start:bootRun
```

应用默认运行在 `http://localhost:8080`。

## 主要操作面

v1 的主要手工操作面是仓库里的 `.http` 文件，而不是把 Swagger UI 当成主入口。
Swagger / OpenAPI 仍然存在，但它们更像契约快照，而不是整条 happy path 的主操作面。

优先使用 `http/` 目录下的文件和真实 HTTP 返回值来驱动流程。

## `.http` 执行顺序

按这个顺序执行：

1. `http/content.http`
   创建草稿，并复制响应里的 `contentId`。
2. `http/review.http`
   填入 `contentId`，然后按顺序执行两个请求：提交审核、审核通过。
3. `http/query.http`
   填入 `contentId`，然后调用 `GET /media-processing/{contentId}`，直到出现 `task` 对象。
   复制 `task.externalTaskId`。
4. `http/media-processing.http`
   填入 `contentId` 和 `externalTaskId`，然后发送标记媒体处理成功的回调事件。
5. 再运行一次 `http/query.http`
   直到内容显示 `contentStatus = PUBLISHED`，并且任务显示 `processingStatus = SUCCEEDED`。

这条顺序和仓库里的本地 happy-path smoke 覆盖一致：
审核通过会创建媒体处理任务，真正发布要等回调完成之后才发生。

## OpenAPI 位置

这个仓库里有两种 OpenAPI 入口：

- 运行时文档：`http://localhost:8080/v3/api-docs`
- 提交进仓的静态快照：`openapi/content-studio-openapi.json`

你可以用运行时端点和静态 JSON 文件查看当前生成出来的 HTTP contract。
但在 v1 里，这两个 OpenAPI 面并不能完整描述回调消费路径和整个 happy-path
流程用到的全部响应形状，所以应该把 `.http` 文件和真实响应当作主要操作真相源。

## `src-generated` 的含义

`src-generated/main/kotlin` 目录是提交进仓的生成快照。
它们用于保留生成结果的参考证据。

它们不是主要的手写源码根目录，也不是你理解运行时流程时该先看的地方。
真正的手写代码仍然在正常的 `src/main/kotlin` 下。
当生成器输出刷新时，`syncGeneratedSnapshots` 会把最新产物复制到
`src-generated`，用于审阅和快照跟踪。

## v1 范围

v1 的范围刻意收得很窄，只覆盖本地参考流程：

- 一个本地 Spring Boot 进程
- 一个内存 H2 运行时
- 通过 `.http` 文件手动操作
- 通过 HTTP 集成回调模拟媒体处理完成
- 保留提交进仓的生成快照和 OpenAPI 快照供检查

v1 不覆盖这些内容：

- 前端或运营控制台
- 认证和授权
- 真实外部媒体处理系统
- 生产部署、可观测性或扩缩容指南
- 超出这条 happy path 的更大编辑流程
