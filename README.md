# SealStudio

基于 [localhost02/SealUtil](https://github.com/localhost02/SealUtil) 原 Java Graphics2D 绘制核心增加桌面 UI 的印章设计器。

## 直接使用

Windows 用户下载或克隆整个仓库后，直接双击：

```text
启动软件.bat
```

不需要提前配置 Maven，也不要求你手动安装 Java。

启动器会自动完成：

1. 检查 Windows 环境；
2. 优先寻找项目内已缓存的 JDK、`JAVA_HOME` / `JDK_HOME` 和系统 `PATH`；
3. 确认 JDK 版本不低于 17，并确认存在 `javac`；
4. 如果 JDK 缺失、只有 JRE、或版本低于 17，则从 Eclipse Adoptium 官方接口下载 Temurin JDK 17；
5. JDK 只解压到项目目录的 `.runtime/jdk`，不修改系统环境变量，也不要求管理员权限；
6. 自动重新编译全部 Java 源码到 `out/`；
7. 自动启动 SealStudio。

第一次在没有 JDK 的电脑上运行需要联网下载运行环境。以后会直接复用 `.runtime/jdk`。

旧的 `run.bat` 仍保留，但它现在只是转到 `启动软件.bat`。

## 当前功能

- 保留原 `SealUtil.java`、`SealConfiguration.java`、`SealFont.java`、`SealCircle.java` 绘制逻辑；
- 公章 / 圆章参数可视化编辑，并在参数变化后自动实时刷新；
- 支持圆形和椭圆形外圈、内边线、内环；
- 支持主文字、副文字、中心文字、抬头文字；
- 字体列表直接读取系统字体；
- 支持字号、粗体、字距、边距、线宽、横纵半径、画布尺寸和印章颜色；
- 私章继续直接调用原 `SealUtil.buildPersonSeal()`；
- 透明背景实时预览；
- PNG 导出；
- 内置“原项目示例”“标准圆章”“椭圆章”预设。

## 手动开发运行

如果电脑已经装好 JDK 17+，仍可使用 Maven：

```powershell
mvn package
java -jar target/seal-studio-0.1.0-SNAPSHOT.jar
```

也可以只验证自动启动器的编译流程而不打开界面：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\tools\Start-SealStudio.ps1 -CompileOnly
```

## 代码结构

```text
.
├─ 启动软件.bat                       # 用户双击入口
├─ run.bat                            # 兼容入口
├─ tools/
│  └─ Start-SealStudio.ps1            # 环境检测、JDK 下载、编译和启动
├─ pom.xml
└─ src/
   └─ cn/localhost01/seal/
      ├─ SealUtil.java                 # 原项目绘制核心
      ├─ configuration/
      │  ├─ SealConfiguration.java     # 原项目配置
      │  ├─ SealFont.java              # 原项目配置
      │  └─ SealCircle.java            # 原项目配置
      └─ ui/
         ├─ SealStudioApp.java
         ├─ SealStudioFrame.java
         ├─ PublicSealPanel.java
         ├─ PersonSealPanel.java
         └─ SealPreviewPanel.java
```

`.runtime/`、`out/` 和 `target/` 都是自动生成目录，已加入 `.gitignore`。

## 实现原则

UI 不重新实现 Canvas/SVG 印章算法。

公章编辑器每次参数变化后都会重新构造原项目的 `SealConfiguration`、`SealFont` 和 `SealCircle`，然后调用：

```java
SealUtil.buildSeal(configuration)
```

私章编辑器调用：

```java
SealUtil.buildPersonSeal(...)
```

因此预览和导出的图像仍来自原项目 Graphics2D 绘制逻辑。

## 上游来源

原绘制核心来源及许可证状态说明见 [UPSTREAM_NOTICE.md](UPSTREAM_NOTICE.md)。
