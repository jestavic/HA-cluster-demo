package com.hacluster.controller;

import com.hacluster.config.AppConfig.InstanceInfo;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final InstanceInfo instanceInfo;

    @GetMapping({"/", "/home"})
    public String home(Model model, HttpSession session) {
        addInstanceInfo(model, session);
        log.info("HOME page served by instance={} port={}", instanceInfo.instanceId(), instanceInfo.port());
        return "pages/home";
    }

    @GetMapping("/architecture")
    public String architecture(Model model, HttpSession session) {
        addInstanceInfo(model, session);
        return "pages/architecture";
    }

    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        addInstanceInfo(model, session);
        return "pages/login";
    }

    @GetMapping("/cluster-status")
    public String clusterStatus(Model model, HttpSession session) {
        addInstanceInfo(model, session);
        return "pages/cluster-status";
    }

    @GetMapping("/monitor")
    public String monitor(Model model, HttpSession session) {
        addInstanceInfo(model, session);
        return "pages/monitor";
    }

    @GetMapping("/simulate")
    public String simulate(Model model, HttpSession session) {
        addInstanceInfo(model, session);
        return "pages/simulate";
    }

    @GetMapping("/failover")
    public String failover(Model model, HttpSession session) {
        addInstanceInfo(model, session);
        return "pages/failover";
    }

    private void addInstanceInfo(Model model, HttpSession session) {
        model.addAttribute("instanceId", instanceInfo.instanceId());
        model.addAttribute("serverPort", instanceInfo.port());
        model.addAttribute("appVersion", instanceInfo.version());
        model.addAttribute("startTime", instanceInfo.startTime());
        model.addAttribute("sessionId", session.getId());
    }
}
