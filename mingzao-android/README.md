# 明早再想 · Android

原生 Android MVP，使用 Kotlin 与 Jetpack Compose。

## 当前能力

- 领养猫猫或狗狗并保存本地资料
- 白天首页、21 天旅程、情绪与小鱼状态
- 夜间钓鱼场景与实时生成的柔和白噪音
- 本地文字寄存
- 本地真实语音录制
- “昨夜来信”整理与完成状态
- 伙伴小院与后续解锁预览

## 本地构建

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug
```

Debug APK 位于：

`app/build/outputs/apk/debug/app-debug.apk`

## 下一阶段

- 锁屏通知快捷记录
- 使用情况访问与五分钟防分心判断
- 睡眠承诺、补救机会和情绪规则
- Room 数据库、语音回放与导出
