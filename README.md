# 🕵️‍♂️ Scotland Yard Java Implementation / 苏格兰场游戏实现

🎮 This is a full Java implementation of the classic board game **Scotland Yard**, developed as part of a university coursework.  
这是一个完整的 Java 项目，复刻了经典桌游《苏格兰场》的核心玩法与规则，作为大学课程的大型作业完成。

---

## 📌 Project Overview 项目概述

The goal of this project is to model the **full ruleset** of Scotland Yard using object-oriented programming.  
本项目通过 Java 面向对象编程实现完整的《苏格兰场》游戏机制，包括：

- ✅ Mr X 和侦探的完整对战逻辑  
- 🎭 黑票与双动票（Secret & Double Moves）支持  
- 👮 禁止侦探重叠、Mr X 不可移动到侦探位置  
- 🧠 支持 AI 模拟 Mr X 决策  
- 🎨 JavaFX 图形化界面交互  

---

## 🗂️ Directory Structure 项目结构

```
sy/
├── report.pdf        # 项目报告 / Project summary & reflection
├── cw-model/         # 核心游戏模型 / Game mechanics
├── cw-ai/            # AI 策略逻辑 / Mr X AI logic
```
---
## 💻 How to Run 如何运行

### 1️⃣ 编译项目 / Compile the project

```bash
cd cw-model
./mvnw clean test   # 运行模型测试 / Run model unit tests
```
---
## 📦 Core Game Engine / 核心游戏逻辑模块
###cw-model：

This folder implements the full logic of the Scotland Yard game, including:  
该文件夹实现了《苏格兰场》游戏的完整规则逻辑，包括：

- `MyGameStateFactory.java`: Constructs new game states and maintains full game logic.  
  构造新的游戏状态对象并维护整体游戏逻辑。

- `MyModelFactory.java`: Implements the observer pattern for UI integration.  
  实现观察者模式，用于将游戏状态与界面连接。

- `Move.java`, `Board.java`, `Player.java`: Provided interfaces for the game model.  
  游戏模型所需的接口，由框架提供。

- `GameState#advance()`: The core function that transitions from one turn to the next, supports single/double/secret moves and win condition checks.  
  核心方法，用于处理游戏状态推进（支持普通/双动/黑票移动），并判断胜负条件。

**Key design patterns / 关键设计模式**:
- ✅ **Visitor Pattern 访问者模式** — 用于区分不同类型的移动（如普通与双动）  
- ✅ **Observer Pattern 观察者模式** — 用于通知 UI 状态变更  
- ✅ **Immutability 不可变性设计** — 使用 Guava 集合确保状态安全与防御式编程
  
---

## 👨‍💻 My Contribution 我的实现部分

In this coursework, I was responsible for implementing the **core game logic and model integration**, specifically:  
在本次课程项目中，我负责了**游戏核心逻辑与模型与界面的集成部分**的实现，主要包括：

---

### 📌 `MyGameStateFactory.java`

This class constructs the main game state (`GameState`) and controls:  
该类构造主游戏状态对象（`GameState`），并负责以下内容：

- Game rules (valid moves, tickets, player turns, game over)  
  游戏规则（合法移动、票据管理、轮换回合、判断胜负）

- State transitions with `advance(Move move)` method  
  通过 `advance(Move move)` 方法实现状态推进

- Move validation, winner detection, Mr X’s travel log updates  
  移动合法性验证、胜负判断、Mr X 的行踪记录更新

👉 这是整个游戏运行的核心逻辑部分，包含所有玩家行为与规则判断，我在此实现了：

- 移动规则（普通移动、黑票、双动票）  
- 胜负判断逻辑（侦探抓住 Mr X、Mr X 成功逃脱等）  
- 状态更新机制（回合转换、票据转移、位置记录）

---

### 🧭 `MyModelFactory.java`

This class implements the **observer pattern** to integrate the model with the UI:  
该类使用**观察者模式**将模型逻辑与图形界面连接起来：

- Maintains the game model state  
  管理游戏状态

- Notifies observers after each move (`MOVE_MADE`, `GAME_OVER`)  
  在每次操作后通知观察者（事件包括 `MOVE_MADE` 和 `GAME_OVER`）

- Allows external input from the GUI  
  接收来自图形界面的操作输入

👉 该模块负责将游戏逻辑与界面连接起来，实现了观察者设计模式，确保每次操作后界面状态正确刷新。

