# 短信验证码转发

个人 sideload 用的 Android MVP：收到短信后提取验证码，确认是验证码短信后调用 Bark `/push`，推送内容保留发件人标题和原始短信全文。

## 功能

- 监听 `SMS_RECEIVED`
- 提取普通 4-8 位验证码、`123-456`、`G-123456` / `G123456`
- Bark JSON POST：`title = {sender}`，`body = {原始短信全文}`
- Bark 推送会加 `isArchive=1`，便于在 Bark 历史里回看
- Bark 推送会加 `group=短信验证码`，便于在 Bark 里分组查看
- 最近 50 条短信按 `发件人 + 正文` 去重
- Bark 失败不重试，只写应用内日志

## 构建

```bash
./gradlew assembleDebug
```

APK 路径：

```bash
app/build/outputs/apk/debug/app-debug.apk
```

安装：

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 配置

打开应用后填写：

- Bark server：默认 `https://api.day.app`
- Bark device_key：Bark app 里的 key
- 打开“启用转发”
- 点“请求短信权限”

也可以把 Bark App 里复制出来的完整地址填到 Bark server，例如 `https://api.day.app/你的key`，这时 `device_key` 可以留空。应用内部仍会用正确的 `/push` JSON 请求。

可以先点“发送测试”验证 Bark 是否能收到推送。

## 注意

这个项目按个人使用设计，不适合上架 Play Store。短信权限属于敏感权限，Android 会要求手动授权。
