package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.graphics.Color;

/**
 * UI常量类 (UI Constants)
 * 
 * 定义全局统一的UI尺寸、颜色等常量，确保界面一致性。
 */
public final class UIConstants {

    private UIConstants() {
    } // 防止实例化

    // ==================== 按钮尺寸 ====================

    /** 大按钮宽度（主菜单按钮、主要操作按钮） */
    public static final float BTN_WIDTH_LARGE = 300f;
    /** 大按钮高度 */
    public static final float BTN_HEIGHT_LARGE = 60f;

    /** 中等按钮宽度（对话框按钮、次要操作） */
    public static final float BTN_WIDTH_MEDIUM = 200f;
    /** 中等按钮高度 */
    public static final float BTN_HEIGHT_MEDIUM = 50f;

    /** 小按钮宽度（工具栏按钮、标签页） */
    public static final float BTN_WIDTH_SMALL = 120f;
    /** 小按钮高度 */
    public static final float BTN_HEIGHT_SMALL = 40f;

    // ==================== Viewport ====================

    /** 标准虚拟宽度 */
    public static final float VIEWPORT_WIDTH = 1920f;
    /** 标准虚拟高度 */
    public static final float VIEWPORT_HEIGHT = 1080f;

    // ==================== 背景颜色 ====================

    /** 默认深色背景 */
    public static final Color BG_COLOR_DEFAULT = new Color(0.08f, 0.08f, 0.1f, 1f);
    /** 菜单背景 */
    public static final Color BG_COLOR_MENU = new Color(0.05f, 0.05f, 0.08f, 1f);
    /** 商店背景 */
    public static final Color BG_COLOR_SHOP = new Color(0.1f, 0.1f, 0.15f, 1f);
    /** 设置背景 */
    public static final Color BG_COLOR_SETTINGS = new Color(0.1f, 0.1f, 0.15f, 1f);
    /** 胜利背景（深绿） */
    public static final Color BG_COLOR_VICTORY = new Color(0f, 0.15f, 0.05f, 1f);
    /** 失败背景（深红） */
    public static final Color BG_COLOR_GAMEOVER = new Color(0.15f, 0.02f, 0.02f, 1f);

    // ==================== 卡片/面板颜色 ====================

    /** 卡片背景（半透明深灰） */
    public static final Color CARD_BG = new Color(0.15f, 0.15f, 0.2f, 0.9f);
    /** 卡片边框颜色 */
    public static final Color CARD_BORDER = new Color(0.3f, 0.3f, 0.4f, 1f);

    // ==================== 间距 ====================

    /** 标准内边距 */
    public static final float PAD_STANDARD = 20f;
    /** 小内边距 */
    public static final float PAD_SMALL = 10f;
    /** 大内边距 */
    public static final float PAD_LARGE = 40f;

    // ==================== 稀有度颜色 ====================

    /** 普通稀有度 */
    public static final Color RARITY_COMMON = Color.WHITE;
    /** 稀有 */
    public static final Color RARITY_RARE = new Color(0.4f, 0.7f, 1.0f, 1f);
    /** 史诗 */
    public static final Color RARITY_EPIC = new Color(0.8f, 0.4f, 1.0f, 1f);
    /** 传说 */
    public static final Color RARITY_LEGENDARY = new Color(1.0f, 0.85f, 0.2f, 1f);

    // ==================== Help Screen 颜色 ====================

    /** 帮助界面科技蓝边框 */
    public static final Color HELP_BORDER_CYAN = new Color(0f, 0.83f, 1f, 1f); // #00D4FF

    /** 帮助界面导航项选中背景 */
    public static final Color HELP_NAV_SELECTED = new Color(0.1f, 0.3f, 0.5f, 0.9f);

    /** 帮助界面导航项默认背景 */
    public static final Color HELP_NAV_DEFAULT = new Color(0.12f, 0.12f, 0.16f, 0.8f);

    /** 警告/危险提示颜色 */
    public static final Color HELP_WARN = new Color(1f, 0.42f, 0.21f, 1f); // #FF6B35

    /** 帮助界面内容卡片背景 */
    public static final Color HELP_CARD_BG = new Color(0.1f, 0.1f, 0.14f, 0.95f);

    /** 帮助标题颜色 (金色) */
    public static final Color HELP_TITLE_GOLD = new Color(1f, 0.85f, 0.2f, 1f);
}
