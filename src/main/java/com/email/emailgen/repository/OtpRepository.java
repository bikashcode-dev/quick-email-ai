package com.email.emailgen.repository;

import com.email.emailgen.model.OtpDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OtpRepository extends MongoRepository<OtpDocument, String> {
    Optional<OtpDocument> findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(String email);
    List<OtpDocument> findByEmailAndIsUsedFalse(String email);
}
