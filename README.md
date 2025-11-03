# DirtyBowlsDelight

本模组是 **TFC Artisanal** 与 **FarmersDelight** 的联动 mod，用于将碗装食物（包括原版和 FarmersDelight 的汤类食物）在食用后返回脏碗而不是普通碗。

## 功能

- ✅ **原版汤类食物**：蘑菇煲、甜菜汤等食用后返回脏碗
- ✅ **FarmersDelight 食物**：所有 FD 的碗装食物食用后返回脏碗
- ✅ **单堆叠支持**：单个汤食用后返回脏碗
- ✅ **多堆叠支持**：多个汤堆叠食用时，返回的碗自动替换为脏碗
- ✅ **背包满处理**：背包满时掉落的碗也会替换为脏碗

## 依赖

### 必需依赖

- **Minecraft**: 1.20.1
- **Forge**: 47.0.0 或更高版本
- **Artisanal Mod**: 1.7.0 或更高版本
- **FarmersDelight Mod**: 1.2.0 或更高版本

### 安装

1. 确保已安装上述必需依赖
2. 将 `dirtybowlsdelight-1.0.0.jar` 放入 `mods` 文件夹
3. 启动游戏

## 构建

### 前提条件

- JDK 17 或更高版本
- Gradle（已包含在项目中）

### 构建命令

**不使用代理**：
```bash
gradlew build
```

**Windows PowerShell**：
```powershell
.\gradlew.bat build
```

构建完成后，JAR 文件位于 `build/libs/` 目录。

## 技术实现

本 mod 使用 **Mixin** 技术拦截物品消耗流程，通过以下方式实现：

1. **状态追踪**：使用 `SoupConsumptionTracker` 在多个 Mixin 间共享状态，确保只替换因食用汤类食物而产生的碗
2. **多重拦截**：
   - 拦截 `finishUsingItem()` 返回值（单堆叠情况）
   - 拦截 `Inventory.add()` 调用（多堆叠且背包未满）
   - 拦截 `Player.drop()` 调用（多堆叠且背包满）
3. **精确识别**：通过检测物品的 `getCraftingRemainingItem()` 返回碗来识别汤类食物
4. **NBT 标签管理**：
   - 使用 `DirtyBowlHelper` 工具类统一创建带 NBT 的脏碗
   - 创建的脏碗带有固定 NBT 标签：`{bowl:{Count:1b,id:"minecraft:bowl"}}`
   - 使用 `ItemStack.isSameItemSameTags()` 检查 NBT 匹配，确保只有 NBT 相同的脏碗才能合并
   - 这样可以防止与 NBT 不同的脏碗（如来自其他来源的脏碗）合并在一起，保持堆叠的独立性

## 兼容性

- ✅ 完全兼容 FarmersDelight 的堆叠汤功能
- ✅ 兼容原版 Minecraft 的汤类食物
- ✅ 兼容其他 mod 的碗装食物（只要它们返回普通碗）

## 许可证

MIT License

## 作者

AmicBeam

## AI 声明

本项目的开发过程中使用了 AI 辅助工具（Cursor AI）进行代码编写和问题解决。

## Logo 投稿

欢迎投稿更符合乐事风格的 logo！如果您有更好的 logo 设计，欢迎通过 Issue 或 Pull Request 提交。

## 更新计划

**目前没有更新的计划**。本模组已完成预期功能，不打算继续更新。
