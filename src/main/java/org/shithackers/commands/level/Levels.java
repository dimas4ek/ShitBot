package org.shithackers.commands.level;

import java.util.HashMap;
import java.util.Map;

public class Levels {
    public Levels() {
        for (Map.Entry<Integer, Integer> level : levels.entrySet()) {
            System.out.println(level.getKey() + " " + level.getValue());
        }
    }

    protected static final Map<Integer, Integer> levels = new HashMap<>();

    static {
        for (int i = 1; i <= 100; i++) {
            levels.put(i, i * 5);
        }
    }

    public static int getXPForLevel(int level) {
        return levels.get(level);
    }
}
