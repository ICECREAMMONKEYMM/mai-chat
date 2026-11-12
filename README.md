# 麻衣 · 安卓 App（自动发现地址）

## 这是什么

一个极简安卓 App（WebView 壳），功能：

1. 启动时自动向固定发现 topic（`mai-chat-6c1f2e9b8a`）查询**当前公网聊天地址**；
2. 自动加载聊天页（含照片/表情/密码门）；
3. 拿不到地址时可**手动输入**，地址与登录 cookie 会记住。

## 怎么构建出 APK（无需本地装 Android SDK）

1. 去 GitHub 新建一个**空仓库**（Public 或 Private 均可，例如 `mai-chat`）。
2. 把本目录（`android/`）里的**全部内容**推到仓库根目录（.github 目录也要）。
   - 用 GitHub 网页上传也可以：Add file → Upload files，把第 12~20 步的文件都传上去。
3. push 后打开仓库的 **Actions** 页签 → `Build APK` 工作流会自动运行（约 3-5 分钟）。
4. 运行完成后点进这次运行 → 底部 **Artifacts** → 下载 `mai-chat-apk.zip` → 解压得到 `app-debug.apk`。
5. 手机安装该 APK（需要允许“安装未知来源应用”）。

## 备选：本地构建（如果你有 Android Studio）

用 Android Studio 打开本目录，直接 Run/Build APK 即可。

## 使用

1. 电脑上双击 `webchat-online.bat`（聊天室 + 隧道 + 地址发布 三个窗口）。
2. 手机打开 App → 自动获取地址 → 出现登录页 → 输入访问密码（`.env` 里的 `WEBCHAT_PASSWORD`）。
3. 之后 cookie 保留，点开 App 直接进聊天。

## 注意

- 地址发现依赖 ntfy.sh（公共免费服务）；若被网络屏蔽，App 里手动输入地址即可。
- 聊天页本身有密码门；topic 只泄露地址，不泄露密码。
- 换 topic 需同时改：`app/src/main/res/values/strings.xml` 的 `ntfy_topic`、以及电脑端 `publish_addr.py` 的 `DEFAULT_TOPIC`（或环境变量 `NTFY_TOPIC`）。
