# Wurst Client汉化 v7
## ⚠ 我们正在寻找翻译人员 ⚠

### 翻译现状

|Language|地位|
|--------|--------|
|Chinese (简体/大陆)|146/146 完成|
|Chinese (Traditional/Taiwan)|正在进行中,请检查 [#549](https://github.com/Wurst-Imperium/Wurst7/pull/549).|
|French (法国)|目前有3份待提交的文件 ([#515](https://github.com/Wurst-Imperium/Wurst7/pull/515), [#531](https://github.com/Wurst-Imperium/Wurst7/pull/531), [#552](https://github.com/Wurst-Imperium/Wurst7/pull/552)) 有不同的翻译,我不知道该合并哪一个.如果你会说法语,,请检查这些提交的文件是否有任何语法错误,并让我知道哪一个对讲母语的人来说听起来最好.|
|German (德国)|46/146 完成. 我可能会自己做其余的事情,因为我可以说母语.|
|Italian (意大利)|146/146 完成|
|Japanese (日本)|137/146 完成|
|Polish (波兰)|待定,需要审查,检查 [#553](https://github.com/Wurst-Imperium/Wurst7/pull/553).|
|Portugese (巴西)|待定,需要审查,检查 [#528](https://github.com/Wurst-Imperium/Wurst7/pull/528).|
|Russian (俄罗斯)|137/146 完成|
|Turkish (土耳其)|目前有2份待提交的文件 ([#511](https://github.com/Wurst-Imperium/Wurst7/pull/511), [#512](https://github.com/Wurst-Imperium/Wurst7/pull/512)) 有不同的翻译,我不知道该合并哪一个.如果你会说土耳其语,请检查这些提交的文件是否有任何语法错误,并让我知道哪一个对讲母语的人来说听起来最好. |
|Ukrainian (乌克兰)|137/146 完成|

如果你会说英语和其他语言,请帮助我们翻译Wurst或审查现有翻译.翻译文件位于`src/main/resources/assets/wurst/lang`中,与其他Minecraft mods的工作方式相同.

目前,只有黑客描述可以被翻译.其他描述和工具提示将在未来成为可翻译的.

关于翻译的讨论,请参见这里的[问题#404](https://github.com/Wurst-Imperium/Wurst7/issues/404)或我们RocketChat服务器上的[#wurst-translations](https://chat.wurstimperium.net/channel/wurst-translations).

关于翻译的讨论见[问题#404](https://github.com/Wurst-Imperium/Wurst7/issues/404).
## 下载

https://www.wurstclient.net/download/

## 设置（针对开发人员）（使用Windows 10和Eclipse）

要求: [JDK 17](https://adoptium.net/?variant=openjdk17&jvmVariant=hotspot)

1. 在PowerShell中运行这两个命令:

```
./gradlew.bat genSources
./gradlew.bat eclipse
```

2. 在Eclipse中，进入 `Import...` > `Existing Projects into Workspace`，选择这个项目。.

## 许可证

This code is licensed under the GNU General Public License v3. **You can only use this code in open-source clients that you release under the same license! Using it in closed-source/proprietary clients is not allowed!**

## 关于拉动请求的说明

如果你正在贡献多个不相关的功能，请为每个功能创建一个单独的拉动请求。把所有东西都挤到一个巨大的拉动请求中，会使我们很难添加你的功能，因为我们必须一个一个地测试、验证和添加它们。

谢谢你的理解--并再次感谢你花时间做出的贡献!
