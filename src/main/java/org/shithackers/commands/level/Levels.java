package org.shithackers.commands.level;

import java.util.HashMap;
import java.util.Map;

public class Levels {
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
