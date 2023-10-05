# Wurst Client v7

## 下载（供用户使用）

[https://www.wurstclient.net/download/](https://www.wurstclient.net/download/?utm_source=GitHub&utm_medium=Wurst7&utm_campaign=README.md&utm_content=Downloads+%28for+users%29)

## 设置（供开发者使用）

（假设您正在使用已经安装了 [Eclipse](https://www.eclipse.org/downloads/) 和 [Java Development Kit 17](https://adoptium.net/?variant=openjdk17&jvmVariant=hotspot) 的 Windows 操作系统。）

1. 在 PowerShell 中运行以下两个命令：

```
./gradlew.bat genSources
./gradlew.bat eclipse
```

2. 在 Eclipse 中，转到 `导入...` > `从现有项目中导入` 并选择此项目。

## 贡献

如果您想提供帮助但不确定该做什么，可以查看我们的 [规划板](https://github.com/orgs/Wurst-Imperium/projects/5/views/1) 或 [帮助需求列表](https://github.com/Wurst-Imperium/Wurst7/issues?q=is%3Aissue+is%3Aopen+label%3A%22help+wanted%22)。当然，您可以提供任何您喜欢的内容，但这些问题尤其有用。

如果您要贡献多个无关的功能，请为每个功能创建单独的拉取请求。将所有内容压缩到一个巨大的拉取请求中会使我很难添加您的功能，因为我必须逐个测试、验证并添加它们。

感谢您的理解 - 再次感谢您抽出时间来贡献！！

## 翻译

我们有一个用于翻译的 [Crowdin 项目](https://crowdin.com/project/wurst7)。您也可以在 GitHub 上提交翻译，但优先使用 Crowdin，因为它更容易解决问题。

要在游戏中启用翻译，请转到 Wurst 选项 > 翻译 > 打开。

功能（作弊/命令等）的名称应始终保持为英文。这确保每个人都可以使用相同的命令、快捷键等，无论其语言设置如何。这也使与使用不同语言的 Wurst 用户进行沟通更加容易。

翻译文件位于 [此文件夹](https://github.com/Wurst-Imperium/Wurst7/tree/master/src/main/resources/assets/wurst/lang)，以防您需要它们。

## 许可证

此代码根据 GNU 通用公共许可证第 3 版许可。**您只能在发布在相同许可下的开源客户端中使用此代码！在闭源/专有客户端中使用它是不允许的！**