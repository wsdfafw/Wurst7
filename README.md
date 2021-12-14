# Wurst Client汉化 v7
⚡ 汉化不易, 欢迎支持[微信/qq](https://docs.qq.com/doc/DYWJKZ2ZtdmVPZmVY?groupUin=T5BcGlDMhmyDFAE2uMfQvQ%253D%253D&ADUIN=750215287&ADSESSION=1632535109&ADTAG=CLIENT.QQ.5827_.0&ADPUBNO=27151&jumpuin=750215287)
## ⚠ We Are Looking For Translators ⚠

如果你会说英语和其他语言，请通过翻译Wurst来帮助我们.翻译文件位于`src/main/resources/assets/wurst/lang`中，其工作原理与其他Minecraft mods相同.

目前，只有黑客描述可以被翻译.其他描述和工具提示将在未来成为可翻译的.

特技的名称（黑客/命令/等等）应始终保持英文.这可以确保每个人都能使用相同的命令、键盘等，而不管他们的语言设置如何.这也使得与使用Wurst的人用不同的语言进行交流更加容易.

关于翻译的讨论见[问题#404](https://github.com/Wurst-Imperium/Wurst7/issues/404).
## Downloads (for users)

https://www.wurstclient.net/download/

## Setup (for developers) (using Windows 10 & Eclipse)

Requirements: [JDK 17](https://adoptium.net/?variant=openjdk17&jvmVariant=hotspot)

1. Run these two commands in PowerShell:

```
./gradlew.bat genSources
./gradlew.bat eclipse
```

2. In Eclipse, go to `Import...` > `Existing Projects into Workspace` and select this project.

## License

This code is licensed under the GNU General Public License v3. **You can only use this code in open-source clients that you release under the same license! Using it in closed-source/proprietary clients is not allowed!**

## Note about Pull Requests

If you are contributing multiple unrelated features, please create a separate pull request for each feature. Squeezing everything into one giant pull request makes it very difficult for us to add your features, as we have to test, validate and add them one by one.

Thank you for your understanding - and thanks again for taking the time to contribute!!
