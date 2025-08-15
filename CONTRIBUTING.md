# Contributing Guidelines
Thank you for considering to contribute! Here are some guidelines to help you get started. ![](https://img.wimods.net/github.com/Wurst-Imperium/Wurst7/CONTRIBUTING.md)

## Pull Requests

### 1. Keep Pull Requests Small and Focused
- **1 PR = 1 change**: Each pull request should address a single issue or add a single feature.
- **Avoid Bloat**: Aim to keep the diff small and digestible. Don't stuff PRs with unrelated changes.

### 2. Respect the Project's Scope and Vision
- **Communicate Before Coding**: Open an issue to discuss any major changes before you start working on them. This can save you a lot of time and effort in case your idea is rejected. When in doubt, ask first.
- **Avoid Breaking Changes**: When modifying existing features, it's usually better to make your changes optional. Your version may work better for you, but other people will often have different use cases that rely on the original behavior.

### 3. Ensure Quality and Completeness
- **Finish the Code**: Submit a PR only when it's complete, tested, and ready for review. Don't use pull requests as a dumping ground for half-baked prototypes.
- If you need early feedback on a larger change, clearly mark the PR as a draft. You should have already started a discussion and gotten the go-ahead for your idea at this point.
- **Watch the Checks**: Make sure that all automated checks are passing and that there aren't any merge conflicts. Fix such issues before asking for a review.

### 4. Follow the Code Style
- Run Eclipse's Clean Up and Format tools with the settings from the [codestyle folder](codestyle).
- If you don't use Eclipse, you can run `./gradlew spotlessApply` instead. However, be aware that this isn't as thorough as Eclipse's tools.
- For anything that these automated tools don't cover, please try to match the existing code style as closely as possible.

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
- 帮助处理[Wurst Wiki](https://wurst.wiki/)
  - 将Wurst Wiki文章翻译成另一种语言
  - 在适当的地方添加功能截图
  - 确保最近的Wurst更新已记录
  - 挖掘旧的Wurst更新，找出某个功能的确切添加时间
- 帮助那些无法弄清楚如何安装Wurst的人
- 制作教程/操作视频
  - 如何制作[AutoBuild模板](https://wurst.wiki/autobuild#creating_templates)
  - 如何使用[配置系统](https://www.wurstclient.net/updates/wurst-7-1/)
- 创建更多Wurst的备份/存档
  - [创建一个分支](https://github.com/Wurst-Imperium/Wurst7/fork) / 镜像此仓库
  - 将[WurstClient.net](https://www.wurstclient.net/)页面添加到互联网档案馆
  - 将[Wurst Wiki](https://wurst.wiki/)文章添加到互联网档案馆
  - 存档旧的[Wurst发布版本](https://www.wurstclient.net/download/)和源代码，以防它们被删除
  - 存档[WiZARDHAX Wurst视频](https://www.youtube.com/c/wizardhax/videos)，以防频道被删除
- 只是传播信息，告诉人们关于Wurst等。
- 扩展此列表，添加更多人们可以帮助的事情（目前我能想到的就这些。）