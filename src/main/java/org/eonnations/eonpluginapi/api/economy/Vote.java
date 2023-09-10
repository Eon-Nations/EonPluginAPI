package org.eonnations.eonpluginapi.api.economy;

import java.sql.Timestamp;
import java.util.UUID;

public interface Vote {

    UUID uuid();
    Timestamp date();
    String website();
}
