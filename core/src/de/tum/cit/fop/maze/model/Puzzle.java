package de.tum.cit.fop.maze.model;

/**
 * 谜题数据类 (Puzzle)
 * 
 * 用于宝箱系统的谜题验证。
 * 支持多种题型：数学、记忆、逻辑。
 */
public class Puzzle {

    /**
     * 谜题类型枚举
     */
    public enum PuzzleType {
        MATH, // 数学计算题
        MEMORY, // 记忆题（如当前是第几层）
        LOGIC // 逻辑推理题
    }

    private final String question;
    private final String correctAnswer;
    private final String[] options; // null 表示需要文本输入，非 null 表示选择题
    private final PuzzleType type;

    /**
     * 创建文本输入型谜题
     * 
     * @param question      题目内容
     * @param correctAnswer 正确答案
     * @param type          谜题类型
     */
    public Puzzle(String question, String correctAnswer, PuzzleType type) {
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.options = null;
        this.type = type;
    }

    /**
     * 创建选择题型谜题
     * 
     * @param question      题目内容
     * @param correctAnswer 正确答案（应与某个 option 匹配）
     * @param options       选项数组 (如 ["A", "B", "C"])
     * @param type          谜题类型
     */
    public Puzzle(String question, String correctAnswer, String[] options, PuzzleType type) {
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.options = options != null ? options.clone() : null;
        this.type = type;
    }

    /**
     * 验证答案是否正确
     * 
     * @param input 用户输入
     * @return true 如果答案正确
     */
    public boolean checkAnswer(String input) {
        if (input == null) {
            return false;
        }
        // 忽略大小写和前后空格
        return correctAnswer.trim().equalsIgnoreCase(input.trim());
    }

    /**
     * 是否为选择题
     */
    public boolean isMultipleChoice() {
        return options != null && options.length > 0;
    }

    // ========== Getters ==========

    public String getQuestion() {
        return question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    /**
     * 获取选项副本
     */
    public String[] getOptions() {
        return options != null ? options.clone() : null;
    }

    public PuzzleType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Puzzle{" +
                "type=" + type +
                ", question='" + question + '\'' +
                ", hasOptions=" + isMultipleChoice() +
                '}';
    }
}
