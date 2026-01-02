# Maze Runner Project Guide

这是一个基于 LibGDX 开发的迷宫冒险游戏项目。本开发指南将帮助你理解项目的代码结构、核心机制以及由于我们之前的协作而实现的主要功能。

## 1. 项目结构 (Project Structure)

核心代码位于 `core/src/de/tum/cit/fop/maze/` 目录下：

*   **`MazeRunnerGame.java`**: 游戏的入口主类。负责管理 `Screen` (屏幕) 的切换，以及全局资源（如 `SpriteBatch`，`Skin`）的生命周期。
*   **`screens/`**: 存放所有的游戏界面。
    *   `MenuScreen.java`: 主菜单 (New Game, Exit 等)。
    *   `StoryScreen.java`: 剧情介绍界面 (Doctor's dialogue)。
    *   `GameScreen.java`: **核心游戏循环**。负责渲染地图、处理玩家输入、更新敌人和陷阱逻辑。此文件包含了**素材加载与裁切 (createTexture)** 的关键逻辑。
    *   `VictoryScreen.java`: 胜利结算界面 (含无限关卡生成逻辑)。
    *   `GameOverScreen.java`: 失败结算界面。
*   **`model/`**: 游戏实体类 (数据模型)。
    *   `GameObject.java`: 基类，包含 x, y 坐标。
    *   `Player.java`: 玩家类 (生命值, 攻击冷却, 移动)。
    *   `Enemy.java`: 智能敌人 (巡逻/追逐状态机, 血量)。
    *   `MobileTrap.java`: 移动陷阱 (随机游走的无敌障碍, ID=6)。
    *   `Wall.java`, `Key.java`, `Exit.java`, `Trap.java`: 静态物体。
    *   `GameMap.java`: 存储地图数据 (Objects List, Safe Grid)。
    *   `CollisionManager.java`: 处理物体与墙壁的具体碰撞逻辑。
*   **`ui/`**: 界面 UI 组件。
    *   `GameHUD.java`: 游戏内的抬头显示 (HUD)，显示生命值、钥匙状态、和**导航箭头**。
*   **`utils/`**: 工具类。
    *   `MapLoader.java`: 读取 `.properties` 地图文件。
    *   `MapGenerator.java`: 程序化生成迷宫 (Prim/Backtracker 算法)。
    *   `SaveManager.java`: 处理存档读写。

## 2. 核心机制详解 (Core Mechanics)

### A. 移动与碰撞 (Movement & Collision)
*   **玩家**: 采用无网格自由移动 (`Free Movement`)，但通过 `snapToGrid` 在静止时对齐网格，保持手感整洁。
*   **敌人**: 使用 `safeGrid` (A* 或简单的距离场) 来判断能否追逐。
*   **MobileTrap**: 使用简单的随机向量反射逻辑 (像台球一样反弹)。

### B. 地图系统 (Map System)
*   游戏支持 `.properties` 格式的地图文件。
*   `level-1` 到 `level-5` 是预设关卡。
*   **无限模式**: 当找不到下一关的预设文件时，`MapGenerator` 会自动生成一个新的迷宫，并根据文件名编号计算 Biome (生态) 颜色。

### C. 战斗系统 (Combat)
*   **攻击**: 按下 `SPACE` 键。
*   **判定**: 遍历 `enemies` 列表，计算距离 (`< 1.5` 单位)。
*   **反馈**: 敌人扣血 (Console 输出)，血量 <= 0 时从列表移除。

### D. 视觉风格 (Visuals)
*   **Biome Tags**: `GameScreen` 根据当前地图路径 (filename) 来决定 `biomeColor`。
*   **Tinting**: 在 `SpriteBatch.draw` 之前调用 `setColor`，渲染完地形后再 `setColor(Color.WHITE)` 恢复原本颜色渲染角色。

## 3. 素材与资源引用 (Asset Reference)

我们在 `GameScreen` 中根据以下 16x16 的 Grid 坐标裁切了素材：

| 图片文件 | 内容 | 坐标 (Row, Col) | 说明 |
| :--- | :--- | :--- | :--- |
| **basictiles.png** | Wall (墙壁) | [0][6] | 石头墙 |
| | Exit (出口) | [0][2] | 关闭的门 |
| | Entry (入口) | [3][6] | 楼梯向下 |
| | Trap (陷阱) | [9][5] | 地刺 |
| | Chest/Key | [1][4] | 宝箱 (作为钥匙逻辑) |
| **character.png** | Player Down | [0][0~2] | 向下行走 |
| | Player Up | [1][0~2] | 向上行走 |
| | Player Right | [2][0~2] | 向右行走 |
| | Player Left | [3][0~2] | 向左行走 |
| **mobs.png** | Slime (Enemy) | [4][0~1] | 史莱姆移动 |
| | Bat | [5][0~1] | 蝙蝠 |

## 4. 关键代码片段 (Key Code Snippets)

### 敌人受伤 (Enemy.java)
```java
public boolean takeDamage(int amount) {
    this.health -= amount;
    return this.health <= 0; // 返回 true 表示死亡
}
```

### 自动生成地图 (VictoryScreen.java)
```java
if (!Gdx.files.internal(nextMapPath).exists()) {
    // 动态生成新关卡
    new MapGenerator().generateAndSave(nextMapPath);
}
game.setScreen(new GameScreen(game, nextMapPath));
```

## 5. 如何继续扩展 (Future Work)

如果你想继续开发，可以尝试：
1.  **商店系统**: 在 `GameScreen` 添加一个 `ShopScreen` 覆盖层，消耗金币（需要添加金币系统）。
2.  **更复杂的 AI**: 在 `Enemy.java` 中引入 A* 寻路算法，替代目前的视线追逐。
3.  **粒子特效**: 在攻击或受伤时生成短暂的粒子效果。

祝你学习愉快！
