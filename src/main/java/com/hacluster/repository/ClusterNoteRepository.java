package com.hacluster.repository;

import com.hacluster.model.ClusterNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClusterNoteRepository extends JpaRepository<ClusterNote, Long> {
    List<ClusterNote> findAllByOrderByCreatedAtDesc();
    List<ClusterNote> findByAuthorOrderByCreatedAtDesc(String author);
}
