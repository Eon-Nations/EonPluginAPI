package org.eonnations.eonpluginapi.api.records;

import java.util.Date;
import java.util.UUID;

public record Vote(UUID uuid, Date timeStamp, String website) {
}
