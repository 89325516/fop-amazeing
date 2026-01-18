package de.tum.cit.fop.maze.utils;

import de.tum.cit.fop.maze.model.Puzzle;
import de.tum.cit.fop.maze.model.Puzzle.PuzzleType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 谜题库 (Puzzle Library)
 * 
 * 包含预定义的谜题集合，供宝箱系统随机抽取。
 * 支持数学题、逻辑题和动态记忆题。
 */
public class PuzzleLibrary {

    private static final List<Puzzle> MATH_PUZZLES = new ArrayList<>();
    private static final List<Puzzle> LOGIC_PUZZLES = new ArrayList<>();

    // 静态初始化题库
    static {
        initMathPuzzles();
        initLogicPuzzles();
    }

    /**
     * 初始化数学题库
     */
    private static void initMathPuzzles() {
        // 简单加减乘除
        MATH_PUZZLES.add(new Puzzle("15 × 4 = ?", "60", PuzzleType.MATH));
        MATH_PUZZLES.add(new Puzzle("7 + 8 = ?", "15", PuzzleType.MATH));
        MATH_PUZZLES.add(new Puzzle("100 - 37 = ?", "63", PuzzleType.MATH));
        MATH_PUZZLES.add(new Puzzle("12 × 12 = ?", "144", PuzzleType.MATH));
        MATH_PUZZLES.add(new Puzzle("81 ÷ 9 = ?", "9", PuzzleType.MATH));
        MATH_PUZZLES.add(new Puzzle("25 + 17 = ?", "42", PuzzleType.MATH));
        MATH_PUZZLES.add(new Puzzle("6 × 7 = ?", "42", PuzzleType.MATH));
        MATH_PUZZLES.add(new Puzzle("50 - 23 = ?", "27", PuzzleType.MATH));
        MATH_PUZZLES.add(new Puzzle("9 × 9 = ?", "81", PuzzleType.MATH));
        MATH_PUZZLES.add(new Puzzle("144 ÷ 12 = ?", "12", PuzzleType.MATH));

        // 稍复杂的计算
        MATH_PUZZLES.add(new Puzzle("(5 + 3) × 4 = ?", "32", PuzzleType.MATH));
        MATH_PUZZLES.add(new Puzzle("100 - 45 + 20 = ?", "75", PuzzleType.MATH));
        MATH_PUZZLES.add(new Puzzle("16 × 5 = ?", "80", PuzzleType.MATH));
    }

    /**
     * 初始化逻辑题库
     */
    private static void initLogicPuzzles() {
        // 经典谜语 - 选择题
        LOGIC_PUZZLES.add(new Puzzle(
                "什么东西早上四条腿，中午两条腿，晚上三条腿？",
                "A",
                new String[] { "A. 人", "B. 狗", "C. 桌子" },
                PuzzleType.LOGIC));

        LOGIC_PUZZLES.add(new Puzzle(
                "你有一根火柴和一个房间，房间里有蜡烛、油灯和火炉，你应该先点燃什么？",
                "B",
                new String[] { "A. 蜡烛", "B. 火柴", "C. 油灯" },
                PuzzleType.LOGIC));

        LOGIC_PUZZLES.add(new Puzzle(
                "一个农夫有17只羊，除了9只，其他都死了。还剩几只？",
                "B",
                new String[] { "A. 8只", "B. 9只", "C. 17只" },
                PuzzleType.LOGIC));

        LOGIC_PUZZLES.add(new Puzzle(
                "什么东西越洗越脏？",
                "C",
                new String[] { "A. 衣服", "B. 碗", "C. 水" },
                PuzzleType.LOGIC));

        LOGIC_PUZZLES.add(new Puzzle(
                "一只鸡和一只鹅同时下水，谁的羽毛先湿？",
                "A",
                new String[] { "A. 都会湿", "B. 鸡", "C. 鹅" },
                PuzzleType.LOGIC));

        LOGIC_PUZZLES.add(new Puzzle(
                "世界上什么最快？",
                "C",
                new String[] { "A. 光", "B. 声音", "C. 思想" },
                PuzzleType.LOGIC));

        LOGIC_PUZZLES.add(new Puzzle(
                "5个苹果分给5个人，怎么分才能让篮子里还剩1个？",
                "B",
                new String[] { "A. 不可能", "B. 连篮子一起给", "C. 切开分" },
                PuzzleType.LOGIC));
    }

    /**
     * 获取随机谜题
     * 
     * @param random 随机数生成器
     * @return 随机选取的谜题
     */
    public static Puzzle getRandomPuzzle(Random random) {
        // 50% 概率数学题，50% 概率逻辑题
        if (random.nextBoolean()) {
            return getRandomMathPuzzle(random);
        } else {
            return getRandomLogicPuzzle(random);
        }
    }

    /**
     * 获取随机数学题
     */
    public static Puzzle getRandomMathPuzzle(Random random) {
        if (MATH_PUZZLES.isEmpty()) {
            return null;
        }
        return MATH_PUZZLES.get(random.nextInt(MATH_PUZZLES.size()));
    }

    /**
     * 获取随机逻辑题
     */
    public static Puzzle getRandomLogicPuzzle(Random random) {
        if (LOGIC_PUZZLES.isEmpty()) {
            return null;
        }
        return LOGIC_PUZZLES.get(random.nextInt(LOGIC_PUZZLES.size()));
    }

    /**
     * 生成动态记忆题（如当前是第几层）
     * 
     * @param currentFloor 当前层数（用于无尽模式）
     * @return 记忆题
     */
    public static Puzzle getMemoryPuzzle(int currentFloor) {
        String question = "当前是第几层？";
        String correctAnswer = String.valueOf(currentFloor);

        // 生成干扰选项
        String[] options = new String[3];
        options[0] = "A. " + (currentFloor - 1);
        options[1] = "B. " + currentFloor;
        options[2] = "C. " + (currentFloor + 1);

        return new Puzzle(question, "B", options, PuzzleType.MEMORY);
    }

    /**
     * 生成基于游戏状态的动态谜题
     * 
     * @param playerKills 玩家击杀数
     * @return 动态谜题
     */
    public static Puzzle getKillCountPuzzle(int playerKills) {
        String question = "你目前击杀了多少敌人？";
        String correctAnswer = String.valueOf(playerKills);
        return new Puzzle(question, correctAnswer, PuzzleType.MEMORY);
    }

    /**
     * 获取题库大小
     */
    public static int getTotalPuzzleCount() {
        return MATH_PUZZLES.size() + LOGIC_PUZZLES.size();
    }

    /**
     * 获取数学题数量
     */
    public static int getMathPuzzleCount() {
        return MATH_PUZZLES.size();
    }

    /**
     * 获取逻辑题数量
     */
    public static int getLogicPuzzleCount() {
        return LOGIC_PUZZLES.size();
    }
}
