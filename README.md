# cap4k-reference-content-studio

[![CI](https://github.com/LDmoxeii/cap4k-reference-content-studio/actions/workflows/ci.yml/badge.svg)](https://github.com/LDmoxeii/cap4k-reference-content-studio/actions/workflows/ci.yml)
[![GitHub Release](https://img.shields.io/github/v/release/LDmoxeii/cap4k-reference-content-studio)](https://github.com/LDmoxeii/cap4k-reference-content-studio/releases)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/LDmoxeii/cap4k-reference-content-studio/blob/master/LICENSE)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/LDmoxeii/cap4k-reference-content-studio)

`cap4k-reference-content-studio` 是 `cap4k` 的可运行参考项目。
它展示了一个小而完整的内容发布流程，建立在生成出来的
domain / application / adapter 分层之上：

- 创建内容草稿
- 提交并通过审核
- 通过 HTTP 集成回调触发媒体处理完成
- 观察内容最终进入已发布状态

你应该能够先克隆这个仓库，再直接跑起本地流程，而不必先读完整个
`cap4k` 仓库。

## 如何阅读这个仓库

- 如果你想先理解业务规则，先看 domain 行为测试和 factory 测试。
- 如果你想先理解整条可运行主链路，先看 smoke tests。
- 第一次阅读时先看 `design/design.json`、数据库 schema、`build/cap4k/plan.json`，再对照 `src/main/kotlin` 下的真实实现文件；不要把 `src-generated` 当成阅读入口。

## 仓库内容

这个仓库分成 4 个 Gradle 模块：

- `cap4k-reference-content-studio-domain`：领域模型、factory、domain service 和领域行为测试
- `cap4k-reference-content-studio-application`：应用层命令、查询、订阅器、Saga 和 job
- `cap4k-reference-content-studio-adapter`：HTTP 控制器、查询适配、持久化适配、外部客户端适配
- `cap4k-reference-content-studio-start`：你本地实际启动的 Spring Boot 运行时

本地运行时可以把它当作一个只有单个 Spring Boot 进程、单个内存 H2 数据库的参考应用。

## 前置条件

1. 使用 JDK 17。
2. 克隆仓库后直接运行 Gradle 即可。这个仓库从 Maven Central 消费
   `cap4k 0.6.0`，不再需要单独检出 `cap4k` 仓库，也不需要先执行
   本地工件引导发布步骤。
3. cap4k pipeline 相关任务使用的公开 Gradle plugin id 是
   `io.github.ldmoxeii.cap4k.pipeline`。

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

## 主要 Gradle 入口

- 运行应用：
  - `./gradlew :cap4k-reference-content-studio-start:bootRun`
- 重新生成与检查 ownership：
  - `./gradlew cap4kPlan cap4kGenerate`
- 生成分析报告：
  - `./gradlew cap4kAnalysisPlan cap4kAnalysisGenerate`

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

如果要运行显式 opt-in 的 paid publication 路线，先理解默认顺序，再通过
`http/paid-publication.http` 创建 paid draft。

## 高级编写示例映射

默认路径仍然是即时发布：媒体处理成功后，application 通过 `PublishContentCmd`
发布内容。

高级路径是显式 opt-in 的付费内容发布。它演示的是补偿型 Saga，而不是等待型 Saga。
Saga 会预留创作者收益、创建访问权益计划、发布内容并激活权益计划；如果后续步骤失败，
会通过幂等补偿命令释放或取消前面已经完成且业务允许撤销的副作用。

- `MediaProcessingResultSnapshot` 是手写 JSON-backed 值概念，通过 `media_processing_task.result_snapshot` 持久化。
- `PublicationEligibilityDomainService` 返回可审计的发布资格结论。
- paid content 使用 `PaidPublicationTask` 记录跨步骤发布状态，并由
  `PaidPublicationSaga` 协调 payout hold、entitlement plan、内容发布、激活和失败补偿。
- `codegen/templates/design/api_payload.kt.peb` 演示项目级模板覆盖：
  生成的 API payload 能保持稳定 OpenAPI schema 名称，而不需要手改生成文件。

这条 paid 路线是真实 Saga 示例，但它不是默认发布路径。默认路径仍然是媒体处理成功后
直接发布；只有显式创建 paid content 时，才会在媒体处理成功后进入 paid publication Saga。
Saga 会推进 paid publication 子步骤，并在下游 paid-publication 步骤失败时记录补偿。

`MediaProcessingResultSnapshot` 仍然是手写结果快照，不应被理解成完整生成器能力。
一等 value object 生成能力仍是 cap4k 后续迭代项。

## OpenAPI 位置

这个仓库里有两种 OpenAPI 入口：

- 运行时文档：`http://localhost:8080/v3/api-docs`
- 提交进仓的静态快照：`openapi/content-studio-openapi.json`

你可以用运行时端点和静态 JSON 文件查看当前生成出来的 HTTP contract。
但在 v1 里，这两个 OpenAPI 面并不能完整描述回调消费路径和整个 happy-path
流程用到的全部响应形状，所以应该把 `.http` 文件和真实响应当作主要操作真相源。

## 测试面

如果你打算通过测试理解项目，建议按这个顺序看：

- domain / factory 测试：
  - 关注规则、状态推进、聚合创建
- smoke tests：
  - 关注运行时启动
  - 关注完整 HTTP happy path

仓库里剩下的测试只服务于可运行参考流程，不是主要学习入口。

## 分析报告

这个仓库同时启用了基于 IR 的分析生成，用来观察手写代码和生成代码拼接后的运行流转结构。

常用命令：

```bash
./gradlew cap4kAnalysisPlan
./gradlew cap4kAnalysisGenerate
```

Windows 上：

```powershell
.\gradlew.bat cap4kAnalysisPlan
.\gradlew.bat cap4kAnalysisGenerate
```

运行后应能看到：

- 各模块自己的 IR 快照目录：
  - `cap4k-reference-content-studio-domain/build/cap4k-code-analysis`
  - `cap4k-reference-content-studio-application/build/cap4k-code-analysis`
  - `cap4k-reference-content-studio-adapter/build/cap4k-code-analysis`
- 根分析计划文件：
  - `build/cap4k/analysis-plan.json`
- 已提交进仓的 flow 分析产物：
  - `analysis/flows/*.json`
  - `analysis/flows/*.mmd`

这些产物不是主 happy-path 操作面的一部分，而是作为参考证据面，用来检查
controller、subscriber、job 以及 application 流程的结构。

## 如何检查生成 ownership

这个仓库不再提交 `src-generated` 快照目录。
如果你要检查 generator 是否严格按照输入合同和 ownership 输出，请优先看：

- `design/design.json`
- `cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql`
- `build/cap4k/plan.json`

其中：

- `design/design.json` 和 schema 决定 generator 应该消费什么事实；
- `build/cap4k/plan.json` 决定当前 generator 计划写哪些文件、使用哪个 generator、采用什么冲突策略；
- 真正提交进仓的 command/query/client/payload/subscriber surface 仍然在各模块自己的 `src/main/kotlin` 下。

## v1 范围

v1 的范围刻意收得很窄，只覆盖本地参考流程：

- 一个本地 Spring Boot 进程
- 一个内存 H2 运行时
- 通过 `.http` 文件手动操作
- 通过 HTTP 集成回调模拟媒体处理完成
- 保留 OpenAPI 快照和分析产物供检查

v1 不覆盖这些内容：

- 前端或运营控制台
- 认证和授权
- 真实外部媒体处理系统
- 生产部署、可观测性或扩缩容指南
- 超出这条 happy path 的更大编辑流程
