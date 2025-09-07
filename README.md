# LiberReach
用于设备互传文件
开发中

# 构建:
编译windows/src/rebuild/rebuild.c

使用此工具来构建此项目的发行版，使其可以在Windows设备上运行。

用法：rebuild.exe <jar-file> <javafx-sdk-path> <launch-program-path> <gcc-path>

jar-file: windows模块打包的jar

javafx-sdk-path: javafx sdk路径(版本最好和项目匹配, 17.0.16)

launch-program-path: 使用C语言编写的启动程序(在项目windows/src/launch/launch.c)

gcc-path: gcc.exe编译器所在路径

构建后生成LiberReach目录，主程序为LiberReach.exe
