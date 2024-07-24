package aroundtheeurope.identityservice;

import aroundtheeurope.identityservice.SideFunctions.EnvPropertyLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class IdentityServiceApplication {

    static{
        EnvPropertyLoader.loadProperties();
    }

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }

}
