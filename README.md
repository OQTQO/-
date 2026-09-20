# SealStudio

基于 [localhost02/SealUtil](https://github.com/localhost02/SealUtil) 原 Java Graphics2D 绘制核心增加桌面 UI 的印章设计器。

## 当前实现

- 保留原 `SealUtil.java`、`SealConfiguration.java`、`SealFont.java`、`SealCircle.java` 绘制逻辑；
- 公章 / 圆章参数可视化编辑，并在参数变化后自动实时刷新；
- 支持圆形和椭圆形外圈、内边线、内环；
- 支持主文字、副文字、中心文字、抬头文字；
- 字体列表直接读取系统字体；
- 支持字号、粗体、字距、边距、线宽、横纵半径、画布尺寸和印章颜色；
- 私章继续直接调用原 `SealUtil.buildPersonSeal()`；
- 透明背景预览；
- PNG 导出；
- 内置“原项目示例”“标准圆章”“椭圆章”预设。

## 运行

要求 Windows + JDK 17 或更高版本。

最简单的方式：

```text
双击 run.bat
```

`run.bat` 会使用 JDK 自带的 `javac` 编译源码并启动桌面程序，不需要 Maven。

也可以使用 Maven：

```powershell
mvn package
java -jar target/seal-studio-0.1.0-SNAPSHOT.jar
```

## 代码结构

```text
src/
└─ cn/localhost01/seal/
   ├─ SealUtil.java                       # 原项目绘制核心
   ├─ configuration/
   │  ├─ SealConfiguration.java           # 原项目配置
   │  ├─ SealFont.java                    # 原项目配置
   │  └─ SealCircle.java                  # 原项目配置
   └─ ui/
      ├─ SealStudioApp.java                # 桌面入口
      ├─ SealStudioFrame.java              # 主窗口
      ├─ PublicSealPanel.java              # 公章实时编辑
      ├─ PersonSealPanel.java              # 私章实时编辑
      └─ SealPreviewPanel.java             # 透明背景预览
```

## 实现原则

UI 不重新实现一套 Canvas/SVG 印章算法。

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
