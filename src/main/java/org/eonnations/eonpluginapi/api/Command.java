package org.eonnations.eonpluginapi.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String name();
    String usage();
    Alias[] aliases() default {};
    String description() default "";
    String permission() default "";
}
