package com.example.secretGuard.controller;



import com.example.secretGuard.dto.CreateScanRequest;
import com.example.secretGuard.entity.Scan;
import com.example.secretGuard.service.ScanService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//@RestController
@RequestMapping("/api/scans")
public class ScanController {

    private final ScanService scanService;

    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @PostMapping
    public ResponseEntity<?> createScan(
            @RequestBody CreateScanRequest request) {

        try {
            Scan scan = scanService.createScan(
                    request.getRepositoryName(),
                    request.getSourceType()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(scan);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", e.getMessage())
            );
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Scan>> getAllScans() {
        return ResponseEntity.ok(scanService.getAllScans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Scan> getScanById(@PathVariable Long id) {

        return scanService.getScanById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}