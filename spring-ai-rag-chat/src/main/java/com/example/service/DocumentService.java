package com.example.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;

    public DocumentService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.textSplitter = new TokenTextSplitter();
    }

    public void ingestPdfDocument(Resource pdfResource) {
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                        .withNumberOfBottomTextLinesToDelete(3)
                        .withNumberOfTopPagesToSkipBeforeDelete(1)
                        .build())
                .withPagesPerDocument(1)
                .build();

        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource, config);
        List<Document> documents = pdfReader.get();
        
        List<Document> splitDocuments = textSplitter.apply(documents);
        vectorStore.add(splitDocuments);
    }

    public void ingestDocument(Resource resource) {
        TikaDocumentReader tikaReader = new TikaDocumentReader(resource);
        List<Document> documents = tikaReader.get();
        
        List<Document> splitDocuments = textSplitter.apply(documents);
        vectorStore.add(splitDocuments);
    }

    public void ingestTextContent(String content, String source) {
        Document document = new Document(content);
        document.getMetadata().put("source", source);
        
        List<Document> splitDocuments = textSplitter.apply(List.of(document));
        vectorStore.add(splitDocuments);
    }

    public List<Document> searchSimilarDocuments(String query, int topK) {
        return vectorStore.similaritySearch(SearchRequest.query(query).withTopK(topK));
    }

    public void loadSampleKnowledgeBase() {
        String companyInfo = """
            TechCorp Knowledge Base
            
            Company Overview:
            TechCorp is a leading technology company specializing in AI solutions, cloud computing, 
            and enterprise software. Founded in 2010, we serve over 10,000 customers worldwide.
            
            Products and Services:
            1. CloudPlatform - Enterprise cloud infrastructure
            2. AIAssist - Machine learning platform
            3. DataInsights - Business intelligence suite
            4. SecureConnect - Cybersecurity solutions
            
            Support Information:
            - Technical Support: Available 24/7 via chat, email, or phone
            - Documentation: Comprehensive guides available at docs.techcorp.com
            - Training: Online courses and certification programs
            - Community: Active developer forum with 50,000+ members
            """;

        String policies = """
            TechCorp Policies and Procedures
            
            Customer Support Policy:
            - Response time: 4 hours for critical issues, 24 hours for standard
            - Escalation process: Level 1 → Level 2 → Engineering team
            - SLA: 99.9% uptime guarantee with compensation for downtime
            
            Data Privacy:
            - GDPR and CCPA compliant
            - Data encryption at rest and in transit
            - Regular security audits and penetration testing
            - Customer data never shared with third parties
            
            Billing and Pricing:
            - Monthly and annual billing options
            - 30-day free trial for all products
            - Volume discounts available for enterprise customers
            - No hidden fees or setup costs
            """;

        String troubleshooting = """
            Common Issues and Solutions
            
            Login Problems:
            Q: Can't log into my account
            A: Try resetting your password. If that doesn't work, check if your account is locked 
            due to multiple failed attempts. Contact support if the issue persists.
            
            Performance Issues:
            Q: Application is running slowly
            A: Check your internet connection first. Clear browser cache and cookies. 
            If using CloudPlatform, verify your instance size meets your workload requirements.
            
            Integration Problems:
            Q: API calls are failing
            A: Verify your API key is correct and has proper permissions. Check rate limits 
            and ensure you're using the correct endpoint URLs. Review our API documentation.
            
            Billing Questions:
            Q: Unexpected charges on my bill
            A: Review your usage dashboard for detailed breakdown. Additional charges may be 
            due to overage fees or add-on services. Contact billing support for clarification.
            """;

        ingestTextContent(companyInfo, "company-overview");
        ingestTextContent(policies, "policies-procedures");
        ingestTextContent(troubleshooting, "troubleshooting-guide");
    }
}
