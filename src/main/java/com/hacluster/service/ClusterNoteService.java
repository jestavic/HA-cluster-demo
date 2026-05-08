package com.hacluster.service;

import com.hacluster.model.ClusterNote;
import com.hacluster.repository.ClusterNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClusterNoteService {

    private final ClusterNoteRepository noteRepository;

    public List<ClusterNote> findAll() {
        return noteRepository.findAllByOrderByCreatedAtDesc();
    }

    @SuppressWarnings("null")
    public Optional<ClusterNote> findById(Long id) {
        return noteRepository.findById(id);
    }

    @Transactional
    @SuppressWarnings("null")
    public ClusterNote save(ClusterNote note) {
        ClusterNote saved = noteRepository.save(note);
        log.info("Note saved [id={}, title='{}', nodeTag={}, author={}]",
                saved.getId(), saved.getTitle(), saved.getNodeTag(), saved.getAuthor());
        return saved;
    }

    @Transactional
    @SuppressWarnings("null")
    public void deleteById(Long id) {
        log.info("Note deleted [id={}]", id);
        noteRepository.deleteById(id);
    }

    public long countAll() {
        return noteRepository.count();
    }
}
