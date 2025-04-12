package ML.Deployment.ML.Deployment.repository;

import ML.Deployment.ML.Deployment.model.Model;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ModelRepository extends MongoRepository<Model, String> {
    List<Model> findByOwnerId(String ownerId);
}