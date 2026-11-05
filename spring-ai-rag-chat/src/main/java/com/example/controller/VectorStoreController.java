package com.example.controller;

import com.example.service.DocumentService;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class VectorStoreController {

    private static final int DISPLAYED_EMBEDDING_VALUES = 12;

    private final DocumentService documentService;

    public VectorStoreController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/vectors")
    public String vectors(Model model) {
        model.addAttribute("vectorCount", documentService.getIndexedDocuments().size());
        return "vectors";
    }

    @GetMapping("/api/vectors")
    @ResponseBody
    public List<VectorDocumentResponse> vectorDocuments() {
        return documentService.getIndexedDocuments().stream()
                .map(this::toResponse)
                .toList();
    }

    private VectorDocumentResponse toResponse(Document document) {
        float[] embedding = document.getEmbedding();
        List<Double> preview = embedding == null
                ? List.of()
                : java.util.stream.IntStream.range(0, Math.min(DISPLAYED_EMBEDDING_VALUES, embedding.length))
                        .mapToObj(index -> (double) embedding[index])
                        .toList();

        Map<String, String> metadata = new LinkedHashMap<>();
        document.getMetadata().forEach((key, value) -> metadata.put(key, String.valueOf(value)));

        return new VectorDocumentResponse(
                document.getId(),
                document.getContent(),
                metadata,
                embedding == null ? 0 : embedding.length,
                preview);
    }

    public record VectorDocumentResponse(
            String id,
            String content,
            Map<String, String> metadata,
            int dimensions,
            List<Double> embeddingPreview) {
    }
}
