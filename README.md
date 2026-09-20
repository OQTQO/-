# SealStudio V2

基于 [localhost02/SealUtil](https://github.com/localhost02/SealUtil) Java Graphics2D 印章绘制核心扩展的桌面可视化印章设计器。

V2 保留原项目的圆形/椭圆形文字排版和边框绘制思路，并在兼容层上增加文字 XY 精调、印泥老化、固定实时预览和更适合普通用户的参数编辑界面。

## 直接使用

Windows 用户下载或克隆整个仓库后，直接双击：

```text
启动软件.bat
```

不需要提前配置 Maven，也不要求手动安装 Java。

启动器会自动完成：

1. 检查 Windows 环境；
2. 优先寻找项目缓存 JDK、`JAVA_HOME` / `JDK_HOME` 和系统 `PATH`；
3. 确认 JDK 版本不低于 17，并确认存在 `javac`；
4. JDK 缺失、只有 JRE 或版本过低时，从 Eclipse Adoptium 下载 Temurin JDK 17；
5. JDK 解压到项目目录 `.runtime/jdk`，不修改系统环境变量，不要求管理员权限；
6. 自动编译全部 Java 源码到 `out/`；
7. 自动启动 SealStudio。

第一次在没有 JDK 的电脑上运行需要联网下载运行环境，以后直接复用项目内的 JDK。

旧的 `run.bat` 仍保留，并转到 `启动软件.bat`。

## V2 功能

### 公章设计

编辑页按实际操作对象划分：

- 基础
- 老化
- 主文字
- 中心内容
- 底部文字
- 抬头文字
- 外边线
- 内边线
- 内环

支持：

- 圆形 / 椭圆形印章；
- 主文字、中心内容、底部文字、抬头文字；
- 系统字体、字号、粗体、文字展开角度、边距；
- 每一类文字独立 X / Y 偏移；
- X 正值向右、Y 正值向下；
- XY 滑杆、数值步进和位置归零；
- 预览区拖动当前文字；
- 方向键 1 px 微调；
- Shift + 方向键 5 px 微调；
- 外圈、内边线、内环独立编辑；
- 界面使用完整宽高，内部自动转换为原 `SealCircle` 半径；
- 标准圆章、标准椭圆章、原项目示例预设；
- 印章颜色与输出尺寸；
- PNG 透明背景导出。

### 私章设计

继续使用原 `SealUtil.buildPersonSeal()` 绘制逻辑，并增加：

- 基础 / 老化 / 文字 / 边框分类；
- 文字独立 X / Y 偏移；
- 预览拖动与键盘微调；
- 印泥老化；
- PNG 导出。

## 印泥老化

老化在已经完成的 `BufferedImage` 上做独立后处理，不重写圆弧排字算法。

效果由以下层组成：

- 微小颗粒掉墨；
- 块状缺损；
- 短划痕；
- 透明度衰减。

老化只处理已有印泥像素，不会向透明背景新增颜色。

参数包括：

- 开关；
- 老化程度；
- 颗粒大小；
- 缺损尺寸；
- 划痕强度；
- 纹理 X / Y；
- 纹理种子；
- 轻微 / 自然 / 明显预设。

纹理使用固定 seed。同一 seed、同一参数下，预览和导出结果可重复。

老化颗粒会根据输出尺寸按比例缩放，因此 300 / 600 / 1200 px 输出保持较接近的视觉比例。

## 实时预览

右侧预览固定显示，不跟随参数页滚动。

支持：

- 透明棋盘背景；
- 中心辅助线；
- 50%～200% 缩放；
- 当前文字拖动；
- 渲染耗时显示；
- 老化状态显示。

辅助线只存在于编辑器中，不会进入导出的 PNG。

## 核心实现

### XY

`SealFont` 新增：

```java
offsetX
offsetY
```

默认都是 `0`。

原 SealUtil 先按原算法计算文字坐标，再叠加 XY 偏移，因此 `offsetX = 0`、`offsetY = 0` 时不会主动改变原排版位置。

### 老化

新增：

```text
configuration/SealAging.java
effect/SealAgingEffect.java
```

公章渲染链：

```text
SealConfiguration
      ↓
SealUtil.buildSeal()
      ↓
原 Graphics2D 排版
      ↓
文字 XY 偏移
      ↓
SealAgingEffect
      ↓
最终 BufferedImage
```

私章提供兼容重载：

```java
SealUtil.buildPersonSeal(imageSize, lineSize, font, addString, aging)
```

原四参数方法仍然保留。

## 自动化验证

GitHub Actions 在 Windows + JDK 17 环境执行：

- Maven 编译；
- `启动软件.bat` 底层启动器的 CompileOnly 校验；
- V2 核心烟雾测试。

烟雾测试覆盖：

- XY 偏移确实改变文字输出；
- 同一个老化 seed 输出完全一致；
- 不同 seed 产生不同纹理；
- 老化不会污染透明背景；
- 私章 XY 和老化管线可正常工作。

## 手动开发运行

已有 JDK 17+ 时：

```powershell
mvn package
java -jar target/seal-studio-0.2.0-SNAPSHOT.jar
```

只验证自动启动器：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\tools\Start-SealStudio.ps1 -CompileOnly
```

运行核心烟雾测试：

```powershell
java -cp target\classes cn.localhost01.seal.test.SealCoreSmokeTest
```

## 代码结构

```text
.
├─ 启动软件.bat
├─ run.bat
├─ tools/
│  └─ Start-SealStudio.ps1
├─ pom.xml
└─ src/cn/localhost01/seal/
   ├─ SealUtil.java
   ├─ configuration/
   │  ├─ SealConfiguration.java
   │  ├─ SealFont.java
   │  ├─ SealCircle.java
   │  └─ SealAging.java
   ├─ effect/
   │  └─ SealAgingEffect.java
   ├─ test/
   │  └─ SealCoreSmokeTest.java
   └─ ui/
      ├─ SealStudioApp.java
      ├─ SealStudioFrame.java
      ├─ PublicSealPanel.java
      ├─ PersonSealPanel.java
      ├─ SealPreviewPanel.java
      ├─ components/
      │  ├─ NumberStepper.java
      │  └─ PositionControl.java
      └─ model/
         ├─ SealEditorState.java
         └─ PersonSealState.java
```

`.runtime/`、`out/` 和 `target/` 都是自动生成目录，已加入 `.gitignore`。

## 上游来源

原绘制核心来源及许可证状态说明见 [UPSTREAM_NOTICE.md](UPSTREAM_NOTICE.md)。

上游仓库当前没有发现根目录 `LICENSE` 文件，因此公开分发或商业使用前应自行确认原作者授权范围。
