package com.hacluster.controller;

import com.hacluster.config.AppConfig.InstanceInfo;
import com.hacluster.service.ClusterNoteService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.Duration;
import java.time.Instant;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final InstanceInfo instanceInfo;
    private final ClusterNoteService noteService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session, Authentication auth) {
        addInstanceInfo(model, session);

        // System metrics
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        long usedHeap = memBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long maxHeap = memBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        int heapPercent = (int) ((usedHeap * 100.0) / maxHeap);

        long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;

        model.addAttribute("username", auth.getName());
        model.addAttribute("usedHeapMb", usedHeap);
        model.addAttribute("maxHeapMb", maxHeap);
        model.addAttribute("heapPercent", heapPercent);
        model.addAttribute("uptimeSeconds", uptime);
        model.addAttribute("availableProcessors", osBean.getAvailableProcessors());
        model.addAttribute("noteCount", noteService.countAll());

        log.info("DASHBOARD accessed by user={} on instance={}", auth.getName(), instanceInfo.instanceId());
        return "pages/dashboard";
    }

    private void addInstanceInfo(Model model, HttpSession session) {
        model.addAttribute("instanceId", instanceInfo.instanceId());
        model.addAttribute("serverPort", instanceInfo.port());
        model.addAttribute("appVersion", instanceInfo.version());
        model.addAttribute("startTime", instanceInfo.startTime());
        model.addAttribute("sessionId", session.getId());
        Duration uptime = Duration.between(instanceInfo.startTime(), Instant.now());
        long uptimeSecs = uptime.getSeconds();
        String uptimeStr = String.format("%dh %dm %ds",
                uptimeSecs / 3600, (uptimeSecs % 3600) / 60, uptimeSecs % 60);
        model.addAttribute("instanceUptime", uptimeStr);
    }
}
