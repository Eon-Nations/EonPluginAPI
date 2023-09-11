package org.eonnations.eonpluginapi.database.dao;

import org.eonnations.eonpluginapi.api.nations.Nation;
import org.eonnations.eonpluginapi.api.nations.Town;

import java.util.List;

public class DynamicNation implements Nation {
    String name;
    int level;

    public DynamicNation(String name, int level) {
        this.name = name;
        this.level = level;
    }

    @Override
    public List<Town> members() {
        return List.of();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int level() {
        return level;
    }
}
