package polaroid.client.modules.api;

import org.apache.logging.log4j.core.jmx.Server;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface ModuleSystem {
    String name();
    int key() default 0;
    Category type();
    ServerCategory server();
    String description();
}


