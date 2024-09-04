# 贡献指南
感谢您考虑贡献！以下是一些帮助您开始的指南。

## 拉取请求

### 1. 保持拉取请求小而专注
- **1 PR = 1 变更**：每个拉取请求应解决单个问题或添加单个功能。
- **避免臃肿**：尽量保持差异小且易消化。不要在PR中塞入不相关的更改。

### 2. 尊重项目的范围和愿景
- **在编码前沟通**：在开始任何重大更改之前，请打开一个问题进行讨论。这可以节省您很多时间和精力，以防您的想法被拒绝。如有疑问，请先询问。
- **避免破坏性更改**：在修改现有功能时，通常最好使您的更改成为可选的。您的版本可能对您更好，但其他人通常会有依赖于原始行为的不同用例。

### 3. 确保质量和完整性
- **完成代码**：只有在完整、测试并准备好审查时才提交PR。不要将拉取请求作为半成品原型的倾倒场。
- 如果您需要对较大更改的早期反馈，请明确将PR标记为草稿。此时，您应该已经开始了讨论并获得了对您想法的认可。
- **关注检查**：确保所有自动化检查通过且没有合并冲突。在请求审查之前修复这些问题。

### 4. 遵循代码风格
- 使用[codestyle文件夹](codestyle)中的设置运行Eclipse的Clean Up和Format工具。
- 如果您不使用Eclipse，可以改为运行`./gradlew spotlessApply`。但是，请注意这不如Eclipse的工具彻底。
- 对于这些自动化工具未涵盖的任何内容，请尽量与现有代码风格保持一致。

## 其他帮助方式

- 修复拼写错误
  - 在Wurst Client本身（查找右上角的笔形图标）
  - 在WurstClient.net上（滚动到底部并点击“编辑此页面”）
  - 在Wurst Wiki上（登录并点击“编辑此页面”）
- 改进现有的错误报告
  - 找出受该错误影响的Minecraft版本
  - 你能找出每次都能重现该错误的方法吗？如果是，请告诉我。
    - “无法重现”标签列出了我无法解决的错误报告。
- 报告Minecraft中的新漏洞/利用，可以添加到Wurst中
- 帮助处理功能请求
  - 你能解释该功能是如何工作的吗？
  - 你能通过拉取请求添加该功能吗？
  - 你还知道该功能的其他未提及的内容吗？
- 帮助处理[Wurst Wiki](https://wiki.wurstclient.net/)
  - 将Wurst Wiki文章翻译成另一种语言
  - 在适当的地方添加功能截图
  - 确保最近的Wurst更新已记录
  - 挖掘旧的Wurst更新，找出某个功能的确切添加时间
- 帮助那些无法弄清楚如何安装Wurst的人
- 制作教程/操作视频
  - 如何制作[AutoBuild模板](https://wiki.wurstclient.net/_detail/autobuild_templates_explained_ll.webp?id=autobuild)
  - 如何使用[配置系统](https://www.wurstclient.net/updates/wurst-7-1/)
- 创建更多Wurst的备份/存档
  - [创建一个分支](https://github.com/Wurst-Imperium/Wurst7/fork) / 镜像此仓库
  - 将[WurstClient.net](https://www.wurstclient.net/)页面添加到互联网档案馆
  - 将[Wurst Wiki](https://wiki.wurstclient.net/)文章添加到互联网档案馆
  - 存档旧的[Wurst发布版本](https://www.wurstclient.net/download/)和源代码，以防它们被删除
  - 存档[WiZARDHAX Wurst视频](https://www.youtube.com/c/wizardhax/videos)，以防频道被删除
- 只是传播信息，告诉人们关于Wurst等。
- 扩展此列表，添加更多人们可以帮助的事情（目前我能想到的就这些。）