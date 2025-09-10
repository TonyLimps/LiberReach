# LiberReach

[![Status](https://img.shields.io/badge/状态-开发中-blue)](https://github.com/your_username/your_repo)
[![LICENSE](https://img.shields.io/badge/协议-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

一个简单易用的点对点文件传输工具，让你在不同设备之间直接共享文件，无需经过网盘或第三方服务器。

## 主要特点

-   **点对点直传**：文件直接在两台设备之间传输，速度快且私密
-   **完整文件浏览**：获得授权后，可以像浏览自己电脑一样查看对方设备上的文件
-   **一次授权，永久使用**：首次验证成功后，以后传输文件无需重复操作
-   **操作简单**：所有连接设备都可以在一台电脑上统一管理

## 如何使用

### 主界面介绍

-   **设备名称**：你的设备在局域网中显示的名字（可以在设置里修改）
-   **地址**：你的设备地址，其他人通过这个地址添加你的设备
-   **设备列表**：
    -   **绿色**：已授权设备（可以查看和传输文件）
    -   **橙色**：已添加但还未授权设备
    -   **灰色**：当前离线的设备
-   **右键菜单**：在设备上点右键可以进行授权、取消授权或移除设备
-   **文件管理**：可以像操作自己电脑一样浏览已授权设备的文件

### 设置界面

-   **语言**：切换软件显示的语言
-   **设备名称**：修改本机显示的名称
-   **默认下载路径**：设置接收文件的保存位置
-   **令牌**：用于安全添加新设备的一次性密码
-   **端口号**：设置软件使用的网络端口（默认：`16318`）

### 添加新设备

1.  获取对方的**设备地址**和**当前的有效令牌**
2.  在主界面点击"添加设备"
3.  输入地址和令牌，完成安全验证

## 自定义语言包

1.  将语言包文件（如 `liberreach_en.properties`）放到 `CustomResources/LanguageBundles` 文件夹里
2.  重启软件后，就可以在设置中选择新的语言了

## 🛠 从源码构建 (Windows 版本)

如果你想自己编译 Windows 版本的程序，可以按照以下步骤操作。

### 需要准备

-   Java JDK 17 或更高版本
-   JavaFX SDK 17.0.16 或更高版本
-   GCC 编译器 (MinGW-w64)

### 构建步骤

1.  **先编译构建工具**：

    ```bash
    gcc -o rebuild.exe windows/src/rebuild/rebuild.c
    ```

2.  **执行构建命令**：

    ```bash
    rebuild.exe <jar文件路径> <javafx-sdk路径> <launch.c文件路径> <gcc编译器路径>
    ```

    参数说明：
    -   `jar文件路径`：打包好的 JAR 文件位置
    -   `javafx-sdk路径`：JavaFX SDK 的安装目录
    -   `launch.c文件路径`：`windows/src/launch/launch.c` 这个文件的位置
    -   `gcc编译器路径`：`gcc.exe` 的位置（例如 `C:\mingw64\bin`）

构建完成后，会生成一个 `LiberReach` 文件夹，里面的 `LiberReach.exe` 就是可以直接运行的程序。

联系我: tonylimps@qq.com

---

© 2025 Tony Limps
