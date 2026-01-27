package de.tum.cit.fop.maze.model;

import java.util.Random;

/**
 * 宝箱实体类 (Treasure Chest)
 * 
 * 可交互的游戏对象，占地 1x1。
 * 支持触碰开启的普通宝箱。
 * 
 * 动画状态流程：
 * CLOSED -> OPENING -> OPEN
 * 关闭时反向播放：OPEN -> OPENING -> CLOSED
 */
public class TreasureChest extends GameObject {

    // ========== 状态枚举 ==========

    /**
     * 宝箱状态
     */
    public enum ChestState {
        CLOSED, // 关闭状态
        OPENING, // 开启中（播放动画）
        OPEN // 完全开启
    }

    /**
     * 宝箱类型
     */
    public enum ChestType {
        NORMAL // 普通宝箱：触碰开启
    }

    // ========== 配置常量 ==========

    /** 动画每帧持续时间（秒） */
    public static final float FRAME_DURATION = 0.2f;

    /** 总动画时间（3帧 × 0.2秒 = 0.6秒） */
    public static final float TOTAL_ANIMATION_TIME = FRAME_DURATION * 3f;

    // ========== 成员变量 ==========

    private ChestState state;
    private ChestType type;
    private ChestReward reward; // 奖励
    private float animationTimer; // 动画计时器
    private boolean interacted; // 是否已交互（防止重复触发）
    private boolean rewardClaimed; // 奖励是否已领取
    private boolean reverseAnimation; // 是否正在播放关闭动画

    // ========== 构造函数 ==========

    /**
     * 创建默认普通宝箱
     */
    public TreasureChest(float x, float y) {
        super(x, y);
        this.state = ChestState.CLOSED;
        this.type = ChestType.NORMAL;
        this.reward = null;
        this.animationTimer = 0f;
        this.interacted = false;
        this.rewardClaimed = false;
        this.reverseAnimation = false;
        this.width = 1f;
        this.height = 1f;
    }

    /**
     * 创建指定类型的宝箱
     * 
     * @param x    X坐标
     * @param y    Y坐标
     * @param type 宝箱类型
     */
    public TreasureChest(float x, float y, ChestType type) {
        this(x, y);
        this.type = type;
    }

    // ========== 静态工厂方法 ==========

    /**
     * 创建随机宝箱
     * 
     * @param x      X坐标
     * @param y      Y坐标
     * @param random 随机数生成器
     * @return 新创建的宝箱
     */
    public static TreasureChest createRandom(float x, float y, Random random) {
        return new TreasureChest(x, y, ChestType.NORMAL);
    }

    // ========== 核心方法 ==========

    /**
     * 更新宝箱状态（每帧调用）
     * 
     * @param delta 帧间隔时间
     */
    public void update(float delta) {
        if (state == ChestState.OPENING) {
            if (reverseAnimation) {
                // 反向播放（关闭动画）
                animationTimer -= delta;
                if (animationTimer <= 0f) {
                    animationTimer = 0f;
                    state = ChestState.CLOSED;
                    reverseAnimation = false;
                }
            } else {
                // 正向播放（开启动画）
                animationTimer += delta;
                if (animationTimer >= TOTAL_ANIMATION_TIME) {
                    animationTimer = TOTAL_ANIMATION_TIME;
                    state = ChestState.OPEN;
                }
            }
        }
    }

    /**
     * 开始开启宝箱
     */
    public void startOpening() {
        if (state == ChestState.CLOSED && !interacted) {
            state = ChestState.OPENING;
            animationTimer = 0f;
            reverseAnimation = false;
            interacted = true;
        }
    }

    /**
     * 开始关闭宝箱（播放反向动画）
     */
    public void startClosing() {
        if (state == ChestState.OPEN) {
            state = ChestState.OPENING;
            animationTimer = TOTAL_ANIMATION_TIME;
            reverseAnimation = true;
        }
    }

    /**
     * 直接设置为已开启状态（跳过动画）
     */
    public void forceOpen() {
        state = ChestState.OPEN;
        animationTimer = TOTAL_ANIMATION_TIME;
        interacted = true;
    }

    /**
     * 领取奖励
     * 
     * @param player 玩家
     * @return true 如果成功领取
     */
    public boolean claimReward(Player player) {
        if (rewardClaimed || reward == null) {
            return false;
        }
        boolean success = reward.applyToPlayer(player);
        if (success) {
            rewardClaimed = true;
        }
        return success;
    }

    // ========== 动画相关 ==========

    /**
     * 获取当前动画帧索引（0-2）
     * 0 = closed, 1 = half, 2 = open
     */
    public int getCurrentFrameIndex() {
        if (state == ChestState.CLOSED) {
            return 0;
        } else if (state == ChestState.OPEN) {
            return 2;
        } else {
            // OPENING 状态：根据计时器计算帧
            float progress = animationTimer / TOTAL_ANIMATION_TIME;
            if (progress < 0.33f) {
                return 0;
            } else if (progress < 0.66f) {
                return 1;
            } else {
                return 2;
            }
        }
    }

    /**
     * 获取动画进度 (0.0 - 1.0)
     */
    public float getAnimationProgress() {
        return Math.min(1f, animationTimer / TOTAL_ANIMATION_TIME);
    }

    // ========== Getters & Setters ==========

    public ChestState getState() {
        return state;
    }

    public ChestType getType() {
        return type;
    }

    public void setType(ChestType type) {
        this.type = type;
    }

    public ChestReward getReward() {
        return reward;
    }

    public void setReward(ChestReward reward) {
        this.reward = reward;
    }

    public boolean isInteracted() {
        return interacted;
    }

    public void setInteracted(boolean interacted) {
        this.interacted = interacted;
    }

    public boolean isRewardClaimed() {
        return rewardClaimed;
    }

    public boolean isOpen() {
        return state == ChestState.OPEN;
    }

    public boolean isClosed() {
        return state == ChestState.CLOSED;
    }

    public boolean isAnimating() {
        return state == ChestState.OPENING;
    }

    @Override
    public String toString() {
        return "TreasureChest{" +
                "pos=(" + x + "," + y + ")" +
                ", state=" + state +
                ", type=" + type +
                ", interacted=" + interacted +
                ", rewardClaimed=" + rewardClaimed +
                '}';
    }
}
