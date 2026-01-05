package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import de.tum.cit.fop.maze.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 工具类：负责读取 .properties 文件并将其转换为 GameMap 对象。
 */
public class MapLoader {

    /**
     * 加载指定路径的地图文件
     * 
     * @param internalPath assets 文件夹下的相对路径，例如 "maps/level1.properties"
     * @return 解析好的 GameMap 对象
     */
    public static GameMap loadMap(String internalPath) {
        GameMap map = new GameMap();
        Properties props = new Properties();

        // 使用 LibGDX 的文件处理
        // 优先检查 Internal (Assets)，如果不存在则检查 Local
        FileHandle file = Gdx.files.internal(internalPath);
        if (!file.exists()) {
            file = Gdx.files.local(internalPath);
        }

        if (!file.exists()) {
            Gdx.app.error("MapLoader", "Map file not found in Internal or Local: " + internalPath);
            return createFallbackMap();
        }

        try (InputStream input = file.read()) {
            props.load(input);

            // 遍历 Properties 中的每一个 Key
            for (String key : props.stringPropertyNames()) {

                // 过滤掉非坐标格式的行 (必须包含逗号)
                if (!key.contains(",")) {
                    continue;
                }

                try {
                    // 1. 解析坐标 Key "x,y"
                    String[] coords = key.split(",");
                    int x = Integer.parseInt(coords[0].trim());
                    int y = Integer.parseInt(coords[1].trim());

                    // 2. 解析类型 Value (ID)
                    String value = props.getProperty(key).trim();
                    int typeId = Integer.parseInt(value);

                    // 3. 根据文档 ID 表创建对应的对象
                    // 使用 GameConfig 常量
                    if (typeId == de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_ENTRY) {
                        map.setPlayerStart(x, y);
                    } else {
                        GameObject obj = EntityFactory.createEntity(typeId, (float) x, (float) y);
                        if (obj != null) {
                            map.addGameObject(obj);
                        } else {
                            Gdx.app.log("MapLoader",
                                    "Unknown or unhandled object type ID: " + typeId + " at " + x + "," + y);
                        }
                    }

                } catch (NumberFormatException e) {
                    Gdx.app.error("MapLoader", "Invalid format in map file at line: " + key);
                }
            }

        } catch (IOException e) {
            Gdx.app.error("MapLoader", "Failed to load map file", e);
            return createFallbackMap();
        }

        // Validation: If map is empty or player has no start?
        if (map.getWidth() == 0 || map.getHeight() == 0) {
            Gdx.app.error("MapLoader", "Map is empty! Using fallback.");
            return createFallbackMap();
        }

        Gdx.app.log("MapLoader", "Map loaded successfully! Size: " + map.getWidth() + "x" + map.getHeight());
        return map;
    }

    private static GameMap createFallbackMap() {
        Gdx.app.log("MapLoader", "Creating Fallback Map...");
        GameMap map = new GameMap();
        // Create a 5x5 enclosed room
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                if (x == 0 || x == 4 || y == 0 || y == 4) {
                    map.addGameObject(
                            EntityFactory.createEntity(de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_WALL, x, y));
                }
            }
        }
        // Player Start at 2,2
        map.setPlayerStart(2, 2);
        // Exit at 3,3
        map.addGameObject(EntityFactory.createEntity(de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_EXIT, 3, 3));
        return map;
    }
}