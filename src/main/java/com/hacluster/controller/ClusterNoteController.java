package com.hacluster.controller;

import com.hacluster.config.AppConfig.InstanceInfo;
import com.hacluster.model.ClusterNote;
import com.hacluster.service.ClusterNoteService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/notes")
@RequiredArgsConstructor
@Slf4j
public class ClusterNoteController {

    private final ClusterNoteService noteService;
    private final InstanceInfo instanceInfo;

    @GetMapping
    public String listNotes(Model model, HttpSession session, Authentication auth) {
        addInstanceInfo(model, session);
        model.addAttribute("notes", noteService.findAll());
        model.addAttribute("username", auth.getName());
        model.addAttribute("newNote", new ClusterNote());
        log.info("NOTES list accessed by user={} on instance={}", auth.getName(), instanceInfo.instanceId());
        return "pages/notes";
    }

    @PostMapping("/create")
    public String createNote(@Valid @ModelAttribute("newNote") ClusterNote note,
                              BindingResult result,
                              Model model,
                              HttpSession session,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            addInstanceInfo(model, session);
            model.addAttribute("notes", noteService.findAll());
            model.addAttribute("username", auth.getName());
            return "pages/notes";
        }

        note.setAuthor(auth.getName());
        note.setNodeTag(instanceInfo.instanceId());
        noteService.save(note);

        redirectAttributes.addFlashAttribute("successMessage",
                "Note created successfully by " + instanceInfo.instanceId());
        log.info("Note CREATED by user={} on instance={}", auth.getName(), instanceInfo.instanceId());
        return "redirect:/notes";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, HttpSession session, Authentication auth) {
        Optional<ClusterNote> note = noteService.findById(id);
        if (note.isEmpty()) {
            return "redirect:/notes";
        }
        addInstanceInfo(model, session);
        model.addAttribute("note", note.get());
        model.addAttribute("username", auth.getName());
        return "pages/note-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateNote(@PathVariable Long id,
                              @Valid @ModelAttribute("note") ClusterNote note,
                              BindingResult result,
                              Model model,
                              HttpSession session,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            addInstanceInfo(model, session);
            model.addAttribute("username", auth.getName());
            return "pages/note-edit";
        }

        Optional<ClusterNote> existing = noteService.findById(id);
        if (existing.isEmpty()) {
            return "redirect:/notes";
        }

        ClusterNote toUpdate = existing.get();
        toUpdate.setTitle(note.getTitle());
        toUpdate.setContent(note.getContent());
        toUpdate.setNodeTag(instanceInfo.instanceId()); // updated by this instance
        noteService.save(toUpdate);

        redirectAttributes.addFlashAttribute("successMessage",
                "Note updated successfully by " + instanceInfo.instanceId());
        return "redirect:/notes";
    }

    @PostMapping("/delete/{id}")
    public String deleteNote(@PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        noteService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Note deleted successfully.");
        return "redirect:/notes";
    }

    private void addInstanceInfo(Model model, HttpSession session) {
        model.addAttribute("instanceId", instanceInfo.instanceId());
        model.addAttribute("serverPort", instanceInfo.port());
        model.addAttribute("sessionId", session.getId());
    }
}
