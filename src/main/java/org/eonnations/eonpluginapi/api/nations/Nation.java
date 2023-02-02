package org.eonnations.eonpluginapi.api.nations;

import java.util.List;

public interface Nation {
    List<Town> members();

    String name();
    String description();
    Spawn spawn();
}
