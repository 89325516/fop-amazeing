# AI AGENT RULES: TEXTURE GENERATION & MANAGEMENT
# AI 代理规则：纹理生成与管理

**CRITICAL INSTRUCTION TO AI ASSISTANTS**:
You must strictly follow this pipeline when assisting the user with game assets. Do not deviate from these paths or standards.
**给AI助手的重要指令**:
在协助用户处理游戏素材时，必须严格遵守此管线。不得偏离这些路径或标准。

## 1. Asset Generation Constraints (生成约束)
- **Save Location (保存位置)**: ALWAYS save generated raw images to `raw_assets/ai_generated_raw/`.
  - ❌ NEVER save directly to `assets/` or `core/` folders.
- **Content (内容)**: One entity per PNG image (一物一图). For animations, use "Horizontal Strip 4 Frames".
- **Naming (命名)**: Strict snake_case.
  - Tiles/Items: `tile_lava.png`, `item_key.png`.
  - Animations: MUST start with `anim_` (e.g., `anim_torch.png`).

## 2. Mandatory Processing Workflow (强制处理流程)
After generating assets or receiving images, you MUST instruct the user to run (or run yourself if capable) the following workflow:

1.  **Background Removal**:
    ```bash
    python3 scripts/process_assets.py
    ```
    *Effect*: Cleans white backgrounds, saves to `raw_assets/ai_processed_transparent/`.

2.  **Standardization (64x64)**:
    ```bash
    python3 scripts/standardize_assets.py
    ```
    *Effect*: Resizes to strict 64x64 grid, saves to `raw_assets/ai_ready_optimized/`.

## 3. Final Output Specifications (最终输出规格)
- **Resolution**:
  - Standard: Exactly **64x64 pixels**.
  - Animation (`anim_`): Exactly **256x64 pixels** (Horizontal Strip).
- **Tile Logic**: Texture fills the entire square.
- **Item Logic**: Item is centered and scaled within the square.
- **Location**: `raw_assets/ai_ready_optimized/` is the ONLY source of truth for game-ready textures.

## 4. Forbidden Actions (禁止操作)
- ❌ DO NOT create spritesheets manually unless using a specific packing tool on the optimized folder.
- ❌ DO NOT modify files in `ai_processed_transparent` or `ai_ready_optimized` manually; always regenerate from raw.
- ❌ DO NOT bypass the standardization script.
