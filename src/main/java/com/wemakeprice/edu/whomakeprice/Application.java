package com.wemakeprice.edu.whomakeprice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * An application class.
 *
 * @author Jin Kwon &lt;onacit_at_wemakeprice.com&gt;
 */
@SpringBootApplication
public class Application {

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Runs this application class with specified command line arguments.
     *
     * @param args the command line arguments.
     */
    public static void main(final String... args) {
        final ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
//        final SpringApplication application = new SpringApplication(Application.class);
//        application.setWebApplicationType(WebApplicationType.REACTIVE);
//        application.run(args);
    }
}
