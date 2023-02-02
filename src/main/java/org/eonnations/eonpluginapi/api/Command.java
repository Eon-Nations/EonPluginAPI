package org.eonnations.eonpluginapi.api;

public @interface Command {
    String name();
    String usage();
    Alias[] aliases() default {};
    String description() default "";
}
