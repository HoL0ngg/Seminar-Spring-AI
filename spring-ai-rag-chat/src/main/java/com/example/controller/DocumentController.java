package com.example.controller;

import com.example.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            Path tempFile = Files.createTempFile("upload-", file.getOriginalFilename());
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            org.springframework.core.io.Resource resource = 
                new org.springframework.core.io.FileSystemResource(tempFile.toFile());
            
            if (file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                documentService.ingestPdfDocument(resource);
            } else {
                documentService.ingestDocument(resource);
            }
            
            Files.deleteIfExists(tempFile);
            
            return ResponseEntity.ok("Document uploaded and processed successfully: " + file.getOriginalFilename());
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to upload document: " + e.getMessage());
        }
    }

    @PostMapping("/ingest-text")
    public ResponseEntity<String> ingestText(@RequestBody TextIngestRequest request) {
        documentService.ingestTextContent(request.content(), request.source());
        return ResponseEntity.ok("Text content ingested successfully");
    }

    @PostMapping("/load-knowledge-base")
    public ResponseEntity<String> loadKnowledgeBase() {
        documentService.loadSampleKnowledgeBase();
        return ResponseEntity.ok("Knowledge base loaded successfully");
    }

    public record TextIngestRequest(String content, String source) {}
}