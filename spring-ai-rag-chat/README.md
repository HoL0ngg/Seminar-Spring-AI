# Spring AI RAG Chat

A conversational AI chatbot powered by Spring AI with RAG (Retrieval-Augmented Generation) capabilities, featuring a modern web interface, chat history, and real-time messaging.

## Features

- **Real-time Chat**: WebSocket-powered instant messaging
- **RAG Integration**: Context-aware responses using document knowledge base
- **Chat History**: Persistent conversation sessions with H2 database
- **Web Interface**: Modern, responsive chat UI with Bootstrap
- **Session Management**: Create, view, and manage multiple chat sessions
- **Source Attribution**: See which documents were used for each response
- **Document Upload**: Support for PDF and various document formats
- **Knowledge Base**: Pre-loaded sample company information

## Architecture

- **Frontend**: Thymeleaf templates with Bootstrap and WebSocket
- **Backend**: Spring Boot with Spring AI integration
- **Database**: H2 in-memory database for chat persistence
- **Vector Store**: SimpleVectorStore for document embeddings
- **Real-time**: WebSocket with STOMP messaging protocol

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- OpenAI API key

## Quick Start

1. **Navigate to project**
   ```bash
   cd spring-ai-rag-chat
   ```

2. **Set OpenAI API key**
   ```bash
   export OPENAI_API_KEY=your-actual-api-key-here
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - Main page: http://localhost:8082
   - Chat interface: http://localhost:8082/chat
   - H2 Console: http://localhost:8082/h2-console

## Getting Started

### 1. Load Knowledge Base
Click "Load Knowledge Base" button in the chat interface to populate the system with sample TechCorp information.

### 2. Start Chatting
- Click "New Chat" to create a session
- Type your questions in the message input
- Get intelligent responses with source references

### 3. Manage Sessions
- View all chat sessions in the left sidebar
- Click any session to resume the conversation
- Chat history is automatically saved

## Sample Conversations

**Customer Support Questions:**
```
User: What are your support hours?
AI: TechCorp provides 24/7 technical support via chat, email, or phone...
Sources: policies-procedures
```

**Product Information:**
```
User: Tell me about CloudPlatform pricing
AI: CloudPlatform Enterprise is priced at $500/month per instance...
Sources: company-overview
```

**Troubleshooting:**
```
User: I can't log into my account
AI: For login problems, try resetting your password first...
Sources: troubleshooting-guide
```

## API Endpoints

### Chat Management
```bash
# Create new session
POST /api/chat/sessions

# Get all sessions
GET /api/chat/sessions

# Get session messages
GET /api/chat/sessions/{sessionId}/messages

# Update session title
PUT /api/chat/sessions/{sessionId}/title

# Delete session
DELETE /api/chat/sessions/{sessionId}
```

### Document Management
```bash
# Upload document
POST /api/documents/upload

# Ingest text content
POST /api/documents/ingest-text

# Load sample knowledge base
POST /api/documents/load-knowledge-base
```

### WebSocket Endpoints
```bash
# Connect to WebSocket
/ws

# Send message
/app/chat.sendMessage

# Subscribe to responses
/topic/public
```

## Configuration

### Database
The application uses H2 in-memory database by default. Access the H2 console at:
- URL: http://localhost:8082/h2-console
- JDBC URL: `jdbc:h2:mem:chatdb`
- Username: `sa`
- Password: `password`

### AI Models
```yaml
spring:
  ai:
    openai:
      chat:
        options:
          model: gpt-4
          temperature: 0.3  # Lower for more consistent responses
      embedding:
        options:
          model: text-embedding-ada-002
```

### WebSocket Configuration
Real-time messaging is configured with STOMP over WebSocket:
- Endpoint: `/ws`
- Message broker: `/topic`
- Application prefix: `/app`

## Features in Detail

### RAG Pipeline
1. **Document Ingestion**: Upload and process documents into chunks
2. **Vector Embedding**: Convert text chunks to embeddings
3. **Similarity Search**: Find relevant documents for user queries
4. **Context Integration**: Combine retrieved context with conversation history
5. **Response Generation**: Generate contextual responses using AI

### Chat Memory
- **Session Persistence**: All conversations saved to database
- **Message History**: Complete chat history with timestamps
- **Context Awareness**: AI remembers previous conversation context
- **Source Tracking**: Track which documents influenced each response

### Web Interface
- **Responsive Design**: Works on desktop and mobile devices
- **Real-time Updates**: Instant message delivery via WebSocket
- **Typing Indicators**: Visual feedback during AI response generation
- **Session Sidebar**: Easy navigation between chat sessions
- **Source Display**: Show document sources for transparency

## Sample Knowledge Base

The application includes sample data about "TechCorp":

**Company Information:**
- Products: CloudPlatform, AIAssist, DataInsights, SecureConnect
- Services: 24/7 support, documentation, training, community
- Locations: Global presence with multiple offices

**Policies & Procedures:**
- Support response times and escalation process
- Data privacy and security compliance
- Billing and pricing information

**Troubleshooting Guide:**
- Common login issues and solutions
- Performance troubleshooting steps
- API integration problems
- Billing question resolution

## Development

### Project Structure
```
src/main/java/com/example/
├── SpringAiRagChatApplication.java
├── config/
│   ├── VectorStoreConfig.java
│   └── WebSocketConfig.java
├── controller/
│   ├── ChatController.java
│   ├── DocumentController.java
│   └── WebController.java
├── model/
│   ├── ChatSession.java
│   └── ChatMessage.java
├── repository/
│   ├── ChatSessionRepository.java
│   └── ChatMessageRepository.java
└── service/
    ├── ChatService.java
    └── DocumentService.java
```

### Adding New Features
1. **Custom Document Readers**: Extend DocumentService for new file formats
2. **Advanced Vector Stores**: Replace SimpleVectorStore with Redis or Pinecone
3. **User Authentication**: Add Spring Security for multi-user support
4. **Chat Export**: Add functionality to export chat history
5. **File Management**: Add document management interface

## Testing

### Manual Testing
1. Load knowledge base: Click "Load Knowledge Base" button
2. Create new chat session
3. Ask questions like:
   - "What are TechCorp's products?"
   - "How do I reset my password?"
   - "What is your support policy?"
4. Verify responses include source references
5. Test session persistence by refreshing page

### API Testing
```bash
# Test document upload
curl -X POST http://localhost:8082/api/documents/load-knowledge-base

# Test session creation
curl -X POST http://localhost:8082/api/chat/sessions

# Test session retrieval
curl http://localhost:8082/api/chat/sessions
```

## Deployment Considerations

### Production Setup
- Replace H2 with PostgreSQL or MySQL
- Configure proper WebSocket scaling
- Add Redis for session management
- Implement user authentication
- Set up proper logging and monitoring

### Environment Variables
```bash
export OPENAI_API_KEY=your-api-key
export DATABASE_URL=your-database-url
export SPRING_PROFILES_ACTIVE=production
```

This RAG chat application demonstrates a complete conversational AI system with modern web interface, persistent chat history, and intelligent document-based responses.