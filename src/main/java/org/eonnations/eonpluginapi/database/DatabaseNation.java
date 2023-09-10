package org.eonnations.eonpluginapi.database;

import org.eonnations.eonpluginapi.api.nations.Nation;
import org.eonnations.eonpluginapi.api.nations.Town;

import java.util.List;

public class DatabaseNation implements Nation {
    List<Town> members;
    String name;
    int level;

    public DatabaseNation(List<Town> members, String name, int level) {
        this.members = members;
        this.name = name;
        this.level = level;
    }

    @Override
    public List<Town> members() {
        return members;
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
