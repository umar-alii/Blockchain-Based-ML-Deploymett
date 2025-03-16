package ML.Deployment.ML.Deployment.repository;


import ML.Deployment.ML.Deployment.model.ModelInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelInfoRepository extends MongoRepository<ModelInfo, String> {

    List<ModelInfo> findByOwnerId(String ownerId);

    // Find models that are either owned by the user or are public
    List<ModelInfo> findByOwnerIdOrIsPublic(String ownerId, boolean isPublic);

    Optional<ModelInfo> findByIdAndOwnerId(String id, String ownerId);
}