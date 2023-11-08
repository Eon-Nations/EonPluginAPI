package database.sql;

import java.util.concurrent.atomic.AtomicInteger;

public class NameGenerator {

    private final AtomicInteger count = new AtomicInteger(0);

    public String name() {
        return "Player" + count.incrementAndGet();
    }
}
