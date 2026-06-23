# KTV-AI-Agent-Assistant（KTV人工智能代理助手）

> 基于 Java SpringBoot + Vue3 + AI Agent 开发的 KTV 智能点歌助手，实现自然语言点歌、场景化歌单推荐、多轮对话交互，是一套端到端的 AI Native 全栈应用，可适配雷石KTV/车载音乐终端场景。

## 项目定位
本项目是 **AI Native 架构的 KTV 智能交互助手**，核心是用 Agent 工作流重构点歌体验：用户通过自然语言/语音输入，AI 自动解析意图、检索歌曲、生成场景化歌单，并支持多轮对话澄清需求，让点歌更智能、更高效。

## 技术栈
### 后端
- SpringBoot 3 + Spring AI + LangChain4j
- MySQL 8.x（存储歌单、用户偏好、对话日志）
- Redis（缓存热门歌曲、对话上下文）
- 对接第三方音乐 API（模拟歌曲数据获取）

### 前端
- Vue3 + Element Plus
- Web Speech API（语音输入识别）
- 响应式布局（适配 KTV 大屏/车机终端）

### AI 能力
- AI Agent 意图识别与多轮对话
- RAG 检索增强（结合场景标签+用户偏好精准推荐）
- 工具调用（获取歌曲信息、生成歌单）

## 核心功能
1.  **自然语言点歌**：支持「我要唱周杰伦的慢歌」「来首适合聚会的嗨歌」等口语化指令
2.  **场景化歌单生成**：根据「生日」「聚会」「车载」等场景一键生成专属歌单
3.  **多轮对话澄清**：模糊需求时自动追问，如「你说的粤语歌是《富士山下》这类吗？」
4.  **歌单管理**：收藏、编辑、删除歌单，模拟终端同步逻辑
5.  **用户偏好学习**：基于历史点歌记录做个性化推荐
6.  **终端界面模拟**：前端复刻 KTV 点歌屏/车机交互风格

## 开发心得
- 全程使用 Cursor / Claude Code 辅助编码，深刻体会到 AI 工具对开发效率与架构设计的提升
- 对 AI Agent 工作流有了系统性思考：Agent 不是为了快，而是能更好地处理复杂意图解析与多轮交互，让产品更懂用户
- 本项目可直接扩展至雷石 KTV 终端或车载音乐 Copilot 场景
<img width="1451" height="765" alt="image" src="https://github.com/user-attachments/assets/97c532d1-538d-4ee6-91a3-2f4abed7acbd" />
<img width="1451" height="765" alt="image" src="https://github.com/user-attachments/assets/d3e34034-42bd-48fa-a0d7-08e9f896c387" />
<img width="1451" height="765" alt="image" src="https://github.com/user-attachments/assets/b2457c57-f3d8-469a-b47d-400fc1a9a8ec" />

