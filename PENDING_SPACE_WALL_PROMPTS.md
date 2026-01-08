# 待生成的太空主题墙体纹理提示词

> **配额重置时间**: 2026-01-08 18:12 UTC (约 5 小时后)
> 
> 当配额重置后，请让 AI 继续生成以下素材，或复制提示词到外部 AI 工具生成后放入 `raw_assets/ai_generated_raw/`

---

## 待生成资产列表

| 文件名 | 尺寸 | 设计概念 | Prompt (Copy-Paste Ready) |
|--------|------|----------|---------------------------|
| `wall_space_3x2_v2.png` | 192×160px | 全息屏障发生器 | 见下方 |
| `wall_space_3x3_v1.png` | 192×224px | 等离子反应堆核心 | 见下方 |
| `wall_space_3x3_v2.png` | 192×224px | 外星神器收容单元 | 见下方 |
| `wall_space_4x2_v1.png` | 256×160px | 飞船观景窗廊 | 见下方 |
| `wall_space_4x2_v2.png` | 256×160px | 武器装备库 | 见下方 |
| `wall_space_4x4_v1.png` | 256×288px | 巨型能量核心 | 见下方 |
| `wall_space_4x4_v2.png` | 256×288px | 外星科技控制中心 | 见下方 |

---

## 完整提示词 (AI Prompts)

### wall_space_3x2_v2.png
```
Sci-fi holographic barrier generator wall block for Top-Down RPG Game, Orthographic Projection. Wide horizontal modular block showing clearly defined Top Face (lighter with energy field projector) and Front Face (darker with tech panels and glowing force field emitters). Style: Hand-painted stylized texture, futuristic shield generator array, dark chrome with brilliant blue holographic barriers, high-tech military aesthetic. Composition: Isolated on Solid White Background, Centered, 20% transparent padding around object. Sharp edges, no cast shadow on ground, game ready asset, fully visible. Wall dimensions: 192x160 pixels (3x2.5 tiles). --no perspective slant, 3d render, photorealism, cropped, cut off
```

### wall_space_3x3_v1.png
```
Sci-fi plasma reactor core wall block for Top-Down RPG Game, Orthographic Projection. Medium square modular block showing clearly defined Top Face (lighter with energy vents) and Front Face (darker with massive glowing reactor coils and containment panels). Style: Hand-painted stylized texture, dangerous power generator, dark industrial metal with bright orange plasma glow and warning lights, heavy industrial feel. Composition: Isolated on Solid White Background, Centered, 20% transparent padding around object. Sharp edges, no cast shadow on ground, game ready asset, fully visible. Wall dimensions: 192x224 pixels (3x3.5 tiles). --no perspective slant, 3d render, photorealism, cropped, cut off
```

### wall_space_3x3_v2.png
```
Sci-fi alien artifact containment unit wall block for Top-Down RPG Game, Orthographic Projection. Medium square modular block showing clearly defined Top Face (lighter with scanning equipment) and Front Face (darker with mysterious glowing alien relic in energy cage). Style: Hand-painted stylized texture, secure containment cell with alien technology, dark chrome with purple ethereal glow from artifact, mysterious sci-fi aesthetic. Composition: Isolated on Solid White Background, Centered, 20% transparent padding around object. Sharp edges, no cast shadow on ground, game ready asset, fully visible. Wall dimensions: 192x224 pixels (3x3.5 tiles). --no perspective slant, 3d render, photorealism, cropped, cut off
```

### wall_space_4x2_v1.png
```
Sci-fi spacecraft window observation deck wall block for Top-Down RPG Game, Orthographic Projection. Wide horizontal modular block showing clearly defined Top Face (lighter metal frame) and Front Face (darker with large reinforced windows showing starfield view). Style: Hand-painted stylized texture, spaceship observation gallery, dark chrome frame with blue-tinted glass panels showing stars and nebula, elegant space station aesthetic. Composition: Isolated on Solid White Background, Centered, 20% transparent padding around object. Sharp edges, no cast shadow on ground, game ready asset, fully visible. Wall dimensions: 256x160 pixels (4x2.5 tiles). --no perspective slant, 3d render, photorealism, cropped, cut off
```

### wall_space_4x2_v2.png
```
Sci-fi weapon rack armory wall block for Top-Down RPG Game, Orthographic Projection. Wide horizontal modular block showing clearly defined Top Face (lighter with equipment rails) and Front Face (darker with mounted laser rifles, energy weapons, and ammo storage). Style: Hand-painted stylized texture, military arsenal storage, dark gunmetal with red accent lights and glowing weapon cells, tactical military aesthetic. Composition: Isolated on Solid White Background, Centered, 20% transparent padding around object. Sharp edges, no cast shadow on ground, game ready asset, fully visible. Wall dimensions: 256x160 pixels (4x2.5 tiles). --no perspective slant, 3d render, photorealism, cropped, cut off
```

### wall_space_4x4_v1.png
```
Sci-fi massive energy core reactor wall block for Top-Down RPG Game, Orthographic Projection. Large square modular block showing clearly defined Top Face (lighter with glowing power conduits) and Front Face (darker with enormous pulsing reactor core and containment field rings). Style: Hand-painted stylized texture, central ship power source, dark industrial metal with intense cyan and white energy glow, dramatic sci-fi industrial feel. Composition: Isolated on Solid White Background, Centered, 20% transparent padding around object. Sharp edges, no cast shadow on ground, game ready asset, fully visible. Wall dimensions: 256x288 pixels (4x4.5 tiles). --no perspective slant, 3d render, photorealism, cropped, cut off
```

### wall_space_4x4_v2.png
```
Sci-fi alien technology command center wall block for Top-Down RPG Game, Orthographic Projection. Large square modular block showing clearly defined Top Face (lighter with holographic star maps) and Front Face (darker with complex alien control panels and mysterious symbols). Style: Hand-painted stylized texture, extraterrestrial bridge technology, dark chrome mixed with organic alien materials, ethereal purple and green glows, mysterious advanced technology aesthetic. Composition: Isolated on Solid White Background, Centered, 20% transparent padding around object. Sharp edges, no cast shadow on ground, game ready asset, fully visible. Wall dimensions: 256x288 pixels (4x4.5 tiles). --no perspective slant, 3d render, photorealism, cropped, cut off
```

---

## 生成后处理步骤

1. 将生成的图片放入: `raw_assets/ai_generated_raw/`
2. 运行处理脚本:
   ```bash
   python3 scripts/process_assets.py
   python3 scripts/standardize_assets.py
   ```
3. 复制到游戏目录:
   ```bash
   cp raw_assets/ai_ready_optimized/wall_space_*.png assets/images/walls/
   ```
