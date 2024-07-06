# Wurst Client v7

## 下载（供用户使用）

[https://www.wurstclient.net/download/](https://www.wurstclient.net/download/?utm_source=GitHub&utm_medium=Wurst7&utm_campaign=README.md&utm_content=Downloads+%28for+users%29)

## 设置（供开发者使用）

（假设您正在使用已经安装了 [Eclipse](https://www.eclipse.org/downloads/) 和 [Java Development Kit 21](https://adoptium.net/?variant=openjdk21&jvmVariant=hotspot) 的 Windows 操作系统。）

1. Run this command in PowerShell:

```
./gradlew.bat genSources eclipse --no-daemon
```

2. 在 Eclipse 中，转到 `导入...` > `从现有项目中导入` 并选择此项目。

## 贡献

Pull requests are welcome, but please make sure to read the [contributing guidelines](CONTRIBUTING.md) first.

## 翻译

我们有一个用于翻译的 [Crowdin 项目](https://crowdin.com/project/wurst7)。您也可以在 GitHub 上提交翻译，但优先使用 Crowdin，因为它更容易解决问题。

要在游戏中启用翻译，请转到 Wurst 选项 > 翻译 > 打开。

功能（作弊/命令等）的名称应始终保持为英文。这确保每个人都可以使用相同的命令、快捷键等，无论其语言设置如何。这也使与使用不同语言的 Wurst 用户进行沟通更加容易。

翻译文件位于 [此文件夹](https://github.com/Wurst-Imperium/Wurst7/tree/master/src/main/resources/assets/wurst/lang)，以防您需要它们。

## 许可证

此代码根据 GNU 通用公共许可证第 3 版许可。**您只能在发布在相同许可下的开源客户端中使用此代码！在闭源/专有客户端中使用它是不允许的！**