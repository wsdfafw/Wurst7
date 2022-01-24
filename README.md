# Wurst Client汉化 v7
## ⚠ We Are Looking For Translators ⚠

### Current Status of Translations

|Language|Status|
|--------|--------|
|Chinese (Simplified/Mainland)|146/146 done|
|Chinese (Traditional/Taiwan)|In progress, check [#549](https://github.com/Wurst-Imperium/Wurst7/pull/549).|
|French (France)|There are 3 pending submissions ([#515](https://github.com/Wurst-Imperium/Wurst7/pull/515), [#531](https://github.com/Wurst-Imperium/Wurst7/pull/531), [#552](https://github.com/Wurst-Imperium/Wurst7/pull/552)) with different translations and I don't know which one to merge. If you speak French, please check these submissions for any grammatical errors and let me know which one sounds best to a native speaker.|
|German (Germany)|46/146 done. I'll probably do the rest myself since I can speak it natively.|
|Italian (Italy)|146/146 done|
|Japanese (Japan)|137/146 done|
|Polish (Poland)|Pending, needs reviews, check [#553](https://github.com/Wurst-Imperium/Wurst7/pull/553).|
|Portugese (Brazil)|Pending, needs reviews, check [#528](https://github.com/Wurst-Imperium/Wurst7/pull/528).|
|Russian (Russia)|137/146 done|
|Turkish (Turkey)|There are 2 pending submissions ([#511](https://github.com/Wurst-Imperium/Wurst7/pull/511), [#512](https://github.com/Wurst-Imperium/Wurst7/pull/512)) with different translations and I don't know which one to merge. If you speak Turkish, please check these submissions for any grammatical errors and let me know which one sounds best to a native speaker. |
|Ukrainian (Ukraine)|137/146 done|

If you speak both English and some other language, please help us by translating Wurst or reviewing existing translations. The translation files are located in `src/main/resources/assets/wurst/lang` and work the same as in other Minecraft mods.

目前，只有黑客描述可以被翻译.其他描述和工具提示将在未来成为可翻译的.

For discussion about translations, see [Issue #404](https://github.com/Wurst-Imperium/Wurst7/issues/404) here or [#wurst-translations](https://chat.wurstimperium.net/channel/wurst-translations) on our RocketChat server.

关于翻译的讨论见[问题#404](https://github.com/Wurst-Imperium/Wurst7/issues/404).
## Downloads (for users)

https://www.wurstclient.net/download/

## Setup (for developers) (using Windows 10 & Eclipse)

要求: [JDK 17](https://adoptium.net/?variant=openjdk17&jvmVariant=hotspot)

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
