# AI AGENT RULES: TEXTURE GENERATION & MANAGEMENT
# AI 代理规则：纹理生成与管理

**CRITICAL INSTRUCTION TO AI ASSISTANTS**:
You must strictly follow this pipeline when assisting the user with game assets.

---

## 1. Asset Generation Constraints (生成约束)

### Save Location
- **Raw Input**: `raw_assets/ai_generated_raw/`
- ❌ NEVER save directly to `assets/` or `core/` folders.

### Content Rules
- One entity per PNG image (一物一图).
- ✅ **MANDATORY**: Use prompts from `AI_TEXTURE_PROMPT_BIBLE.md`.
- ✅ **MANDATORY**: **Creative Variety**. Do NOT generate 10 variations of the exact same rock. Use the "Creative Alternatives" list.
- ✅ ALWAYS ask for **"Solid White Background"**.

### Naming Convention (命名规范)
| Type | Pattern | Example |
|------|---------|---------|
| Wall | `wall_{theme}_{WxH}_v{variant}.png` | `wall_grassland_2x2_v1.png` |
| Tile | `tile_{theme}_{name}.png` | `tile_grassland_grass.png` |
| Item | `item_{name}.png` | `item_key_gold.png` |

**Wall Themes**: `grassland`, `desert`, `ice`, `jungle`, `space`
**Wall Sizes (COMPLETE LIST)**: `2x2`, `2x3`, `2x4`, `3x2`, `3x3`, `4x2`, `4x4`
**Variants**: `v1`, `v2`

### ⚠️ Completeness Checklist (完整性检查清单 - MANDATORY)
**BEFORE providing prompts, ALWAYS verify ALL sizes exist for the theme:**

| Size | Description | Pixel Size |
|------|-------------|------------|
| 2x2 | 小方形 (Small Square) | 128×160px |
| 2x3 | 窄高型 (Narrow Tall) | 128×224px |
| 2x4 | 窄塔型 (Narrow Tower) | 128×288px |
| 3x2 | 宽矮型 (Wide Short) | 192×160px |
| 3x3 | 中方形 (Medium Square) | 192×224px |
| 4x2 | 宽横型 (Wide Horizontal) | 256×160px |
| 4x4 | 大方形 (Large Square) | 256×288px |

**Each size needs v1 AND v2 = 14 files per theme, 70 files total for all 5 themes.**

---

## 2. Processing Workflow (处理流程)

```bash
python3 scripts/process_assets.py
python3 scripts/standardize_assets.py
cp raw_assets/ai_ready_optimized/wall_*.png assets/images/walls/
```

---

## 3. Wall Height Logic (墙体高度逻辑)

Walls have visual height = **logical height + 0.5** (for isolated walls).

| Logical Size | Texture Size | Pixel Size |
|--------------|--------------|------------|
| 2x2 | 2x2.5 | 128x160 |
| 3x2 | 3x2.5 | 192x160 |
| 4x2 | 4x2.5 | 256x160 |
| 3x3 | 3x3.5 | 192x224 |
| 4x4 | 4x4.5 | 256x288 |

---

## 4. External AI Generation Workflow (外部生成流程)

When user says: **"我要去外部生成素材装填进 {THEME} 主题的纹理素材"**

Agent provides prompt from `AI_TEXTURE_PROMPT_BIBLE.md`, user generates externally, places in `raw_assets/ai_generated_raw/`, says "素材已放入".

---

## 5. Cleanup Rules (清理规则)

When replacing assets, delete from ALL directories:
- `raw_assets/ai_generated_raw/`
- `raw_assets/ai_processed_transparent/`
- `raw_assets/ai_ready_optimized/`
- `assets/images/walls/`

---

## 6. Interaction Protocol (交互协议)

**CRITICAL**: When the user asks for asset generation, **NEVER** just give a chat response. You MUST generate a standalone Markdown file (or a clear Markdown artifact) containing a **Table**.

### Format Requirement:
| Filename | Visual Description | Exact AI Prompt (Copy-Paste Ready) |
|----------|--------------------|------------------------------------|
| `...v1.png` | ... | ... |
| `...v2.png` | ... | ... |

**Rules:**
1. **Distinct Prompts**: `v1` and `v2` MUST have different prompts (e.g., different material wear, pattern, or structure) to ensure variety.
2. **Clear Filenames**: Pre-fill the exact filenames the user should use.
3. **One-Stop Shop**: The user should be able to just copy-paste from your table.

---

## 7. Asset Dissatisfaction Workflow (素材不满意重新生成流程)

**Trigger**: When user says **"我对 [filename] 不满意"** or similar.

**MANDATORY Steps (必须按顺序执行)**:

### Step 1: Delete Old Files First (先删除旧文件)
Before generating new prompts, **IMMEDIATELY** delete the unsatisfactory asset(s) from ALL directories:
```bash
# Example: User is unhappy with wall_desert_4x4_v2.png
rm raw_assets/ai_generated_raw/wall_desert_4x4_v2.png
rm raw_assets/ai_processed_transparent/wall_desert_4x4_v2.png
rm raw_assets/ai_ready_optimized/wall_desert_4x4_v2.png
rm assets/images/walls/wall_desert_4x4_v2.png
```
**Purpose**: Free up the naming space so user can regenerate with the same filename.

### Step 2: Identify Missing Assets (识别缺失素材)
Scan the `raw_assets/ai_generated_raw/` folder and cross-reference with expected assets to identify:
- Assets user wants to **replace** (just deleted)
- Assets that are **missing entirely** (never existed)

### Step 3: Generate Regeneration Prompt Table (生成提示词表格)
Create/update a Markdown file with ONLY the assets that need (re)generation. Follow Section 6 format.

**❌ DON'T**: List all assets again.
**✅ DO**: List only the deleted + missing assets.

