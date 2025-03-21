package ML.Deployment.ML.Deployment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class MlDeploymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(MlDeploymentApplication.class, args);
	}

}
