package org.eonnations.eonpluginapi.api.records;

import org.bukkit.Location;
import org.bukkit.World;

public class Spawn extends Location {

    public Spawn(double x, double y, double z, float yaw, float pitch) {
        super(null, x, y, z, yaw, pitch);
    }

    public Spawn(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public Spawn(World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
    }
}
