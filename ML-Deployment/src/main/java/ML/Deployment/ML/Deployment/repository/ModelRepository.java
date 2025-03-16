package ML.Deployment.ML.Deployment.repository;

import ML.Deployment.ML.Deployment.model.Model;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRepository extends MongoRepository<Model, String> {

    // Find models by the user who owns them
    List<Model> findByOwnerId(String ownerId);

    // Optional: Find public models
    List<Model> findByIsPublic(boolean isPublic);

    // Optional: Count models by owner
    int countByOwnerId(String ownerId);
}