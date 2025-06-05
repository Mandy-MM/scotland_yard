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

sy/
├── report.pdf # 项目报告 / Project summary & reflection
├── cw-model/ # 核心游戏模型 / Game mechanics
├── cw-ai/ # AI 策略逻辑 / Mr X AI logic

---

## 💻 How to Run 如何运行

### 1️⃣ 编译项目 / Compile the project

```bash
cd cw-model
./mvnw clean test   # 运行模型测试 / Run model unit tests
