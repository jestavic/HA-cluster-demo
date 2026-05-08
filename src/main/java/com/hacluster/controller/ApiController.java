package com.hacluster.controller;

import com.hacluster.config.AppConfig.InstanceInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ApiController {

    private final InstanceInfo instanceInfo;
    private final Random random = new Random();

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health(HttpServletRequest request) {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("instanceId", instanceInfo.instanceId());
        health.put("port", instanceInfo.port());
        health.put("version", instanceInfo.version());
        health.put("timestamp", Instant.now().toString());

        Duration uptime = Duration.between(instanceInfo.startTime(), Instant.now());
        health.put("uptimeSeconds", uptime.getSeconds());

        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        long usedMb = mem.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long maxMb = mem.getHeapMemoryUsage().getMax() / (1024 * 1024);
        health.put("heapUsedMb", usedMb);
        health.put("heapMaxMb", maxMb);
        health.put("heapPercent", (int) ((usedMb * 100.0) / maxMb));

        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        health.put("processors", os.getAvailableProcessors());
        health.put("loadAverage", os.getSystemLoadAverage());

        log.debug("Health check on instance={}", instanceInfo.instanceId());
        return ResponseEntity.ok(health);
    }

    @GetMapping("/simulate")
    public ResponseEntity<Map<String, Object>> simulate(HttpServletRequest request, HttpSession session) {
        // Simulate a load-balanced request
        int simulatedLatencyMs = 50 + random.nextInt(200);

        try {
            Thread.sleep(simulatedLatencyMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("instanceId", instanceInfo.instanceId());
        result.put("port", instanceInfo.port());
        result.put("sessionId", session.getId());
        result.put("clientIp", getClientIp(request));
        result.put("timestamp", Instant.now().toString());
        result.put("simulatedLatencyMs", simulatedLatencyMs);
        result.put("status", "REQUEST_PROCESSED");

        log.info("SIMULATE request processed by instance={} latency={}ms", instanceInfo.instanceId(), simulatedLatencyMs);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/instance-info")
    public ResponseEntity<Map<String, Object>> instanceInfo(HttpSession session) {
        Map<String, Object> info = new HashMap<>();
        info.put("instanceId", instanceInfo.instanceId());
        info.put("port", instanceInfo.port());
        info.put("version", instanceInfo.version());
        info.put("startTime", instanceInfo.startTime().toString());
        info.put("sessionId", session.getId());

        Duration uptime = Duration.between(instanceInfo.startTime(), Instant.now());
        long uptimeSecs = uptime.getSeconds();
        info.put("uptimeFormatted", String.format("%dh %dm %ds",
                uptimeSecs / 3600, (uptimeSecs % 3600) / 60, uptimeSecs % 60));

        return ResponseEntity.ok(info);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwarded = request.getHeader("X-Forwarded-For");
        if (xForwarded != null && !xForwarded.isEmpty()) {
            return xForwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
