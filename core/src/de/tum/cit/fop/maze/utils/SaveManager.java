package de.tum.cit.fop.maze.utils;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import de.tum.cit.fop.maze.model.GameState;

import java.util.Arrays;
import java.util.Comparator;

public class SaveManager {

    // 所有的存档都放在 saves 文件夹下
    private static final String SAVE_DIR = "saves/";

    /**
     * 保存游戏，允许指定文件名
     * @param state 游戏状态
     * @param filename 用户输入的文件名 (不需要带 .json 后缀)
     */
    public static void saveGame(GameState state, String filename) {
        Json json = new Json();
        // 关键设置：输出标准的 JSON 格式
        json.setOutputType(JsonWriter.OutputType.json);

        // 确保文件夹存在
        if (!Gdx.files.local(SAVE_DIR).exists()) {
            Gdx.files.local(SAVE_DIR).mkdirs();
        }

        // 自动加上 .json 后缀
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        String text = json.prettyPrint(state);
        FileHandle file = Gdx.files.local(SAVE_DIR + filename);
        file.writeString(text, false);

        Gdx.app.log("SaveManager", "Saved to: " + file.path());
    }

    /**
     * 兼容方法：默认保存 (保存为 auto_save.json)
     */
    public static void saveGame(GameState state) {
        saveGame(state, "auto_save");
    }

    /**
     * 读取指定文件名的存档
     */
    public static GameState loadGame(String filename) {
        if (filename == null || filename.isEmpty()) {
            filename = "auto_save.json";
        }
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        FileHandle file = Gdx.files.local(SAVE_DIR + filename);

        if (!file.exists()) {
            Gdx.app.log("SaveManager", "Save file not found: " + filename);
            return null;
        }

        Json json = new Json();
        try {
            return json.fromJson(GameState.class, file.readString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 兼容方法：默认读取 (读取 auto_save.json)
     */
    public static GameState loadGame() {
        return loadGame("auto_save.json");
    }

    /**
     * 获取所有存档文件，并按修改时间倒序排列（最新的在最前面）
     * 供 GameScreen 的读档列表使用
     */
    public static FileHandle[] getSaveFiles() {
        FileHandle dir = Gdx.files.local(SAVE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            return new FileHandle[0];
        }

        // 获取所有 .json 结尾的文件
        FileHandle[] files = dir.list(".json");

        // 按时间排序 (最新的排前面)
        Arrays.sort(files, new Comparator<FileHandle>() {
            @Override
            public int compare(FileHandle f1, FileHandle f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });

        return files;
    }
    public static boolean deleteSave(String filename) {
        if (filename == null || filename.isEmpty()) return false;

        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        FileHandle file = Gdx.files.local(SAVE_DIR + filename);

        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Gdx.app.log("SaveManager", "Deleted save file: " + filename);
            } else {
                Gdx.app.error("SaveManager", "Failed to delete: " + filename);
            }
            return deleted;
        }
        return false;
    }
}
