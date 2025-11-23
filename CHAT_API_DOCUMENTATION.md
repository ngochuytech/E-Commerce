# 📱 Chat API Documentation for Frontend

## 📋 Mục lục

1. [Tổng quan](#tổng-quan)
2. [Authentication](#authentication)
3. [REST API Endpoints](#rest-api-endpoints)
4. [WebSocket Integration](#websocket-integration)
5. [Data Models](#data-models)
6. [Ví dụ Code Frontend](#ví-dụ-code-frontend)
7. [Error Handling](#error-handling)

---

## 🎯 Tổng quan

Hệ thống Chat hỗ trợ 2 phương thức giao tiếp:

- **REST API**: Tải dữ liệu lịch sử, quản lý conversation (CRUD)
- **WebSocket**: Nhắn tin real-time, typing indicator, read receipts

### Base URL
```
REST API: http://localhost:8080/api/v1/chat
WebSocket: ws://localhost:8080/ws/chat
```

---

## 🔐 Authentication

### JWT Token Required
Tất cả requests đều cần JWT token trong header:

```javascript
headers: {
  'Authorization': 'Bearer YOUR_JWT_TOKEN',
  'Content-Type': 'application/json'
}
```

### WebSocket Authentication
Khi kết nối WebSocket, gửi token trong header:

```javascript
const socket = new SockJS('http://localhost:8080/ws/chat');
const stompClient = Stomp.over(socket);

stompClient.connect(
  { Authorization: 'Bearer YOUR_JWT_TOKEN' },
  onConnected,
  onError
);
```

---

## 🌐 REST API Endpoints

### 1. 💬 Conversation Management

#### **POST** `/api/v1/chat/conversations`
Tạo cuộc trò chuyện mới

**Request Body:**
```json
{
  "recipientId": "userId_or_null",
  "storeId": "store123",
  "type": "BUYER_SELLER",
  "productId": "product123",
  "initialMessage": "Xin chào, tôi muốn hỏi về sản phẩm này"
}
```

**Response:**
```json
{
  "id": "conv123",
  "participants": [
    {
      "userId": "buyer456",
      "userName": "Nguyễn Văn A",
      "userAvatar": "https://..."
    },
    {
      "userId": "seller789",
      "userName": "Cửa hàng ABC",
      "userAvatar": "https://..."
    }
  ],
  "type": "BUYER_SELLER",
  "storeId": "store123",
  "productId": "product123",
  "productName": "iPhone 15 Pro Max",
  "lastMessage": "Xin chào, tôi muốn hỏi về sản phẩm này",
  "lastMessageTime": "2025-11-24T10:30:00",
  "unreadCount": 0,
  "status": "ACTIVE"
}
```

**Conversation Types:**
- `BUYER_SELLER`: Chat giữa người mua và cửa hàng
- `BUYER_SUPPORT`: Chat giữa người mua và admin hỗ trợ
- `SELLER_SUPPORT`: Chat giữa người bán và admin hỗ trợ

---

#### **GET** `/api/v1/chat/conversations`
Lấy danh sách conversation của user (có phân trang)

**Query Parameters:**
- `page`: Số trang (default: 0)
- `size`: Số items mỗi trang (default: 20)

**Response:**
```json
{
  "content": [
    {
      "id": "conv123",
      "participants": [...],
      "lastMessage": "Sản phẩm còn hàng không?",
      "lastMessageTime": "2025-11-24T10:30:00",
      "unreadCount": 3,
      "status": "ACTIVE"
    }
  ],
  "totalElements": 15,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

#### **GET** `/api/v1/chat/conversations/{conversationId}`
Lấy chi tiết 1 conversation

**Response:** Giống như POST `/conversations`

---

#### **GET** `/api/v1/chat/conversations/find-or-create`
Tìm hoặc tạo conversation giữa 2 user

**Query Parameters:**
- `recipientId`: ID người nhận
- `storeId`: ID cửa hàng

**Response:** Trả về conversation object

---

#### **GET** `/api/v1/chat/conversations/unread-count`
Lấy số lượng conversation chưa đọc (badge count)

**Response:**
```json
{
  "unreadCount": 5
}
```

---

#### **POST** `/api/v1/chat/conversations/{conversationId}/archive`
Lưu trữ conversation

**Response:**
```json
{
  "message": "Conversation archived successfully"
}
```

---

### 2. 💌 Message Management

#### **GET** `/api/v1/chat/conversations/{conversationId}/messages`
Lấy lịch sử tin nhắn (có phân trang)

**Query Parameters:**
- `page`: Số trang (default: 0)
- `size`: Số items mỗi trang (default: 50)

**Response:**
```json
{
  "content": [
    {
      "id": "msg123",
      "conversationId": "conv123",
      "senderId": "user456",
      "senderName": "Nguyễn Văn A",
      "senderAvatar": "https://...",
      "content": "Sản phẩm này còn hàng không?",
      "type": "TEXT",
      "attachments": [],
      "readByUserIds": ["user456"],
      "replyToMessageId": null,
      "status": "SENT",
      "sentAt": "2025-11-24T10:30:00",
      "productInfo": null
    }
  ],
  "totalElements": 45,
  "totalPages": 1,
  "size": 50,
  "number": 0
}
```

**Message Types:**
- `TEXT`: Tin nhắn văn bản
- `IMAGE`: Tin nhắn hình ảnh
- `FILE`: File đính kèm
- `SYSTEM`: Thông báo hệ thống
- `PRODUCT_LINK`: Link sản phẩm

**Message Status:**
- `SENT`: Đã gửi
- `DELIVERED`: Đã nhận
- `READ`: Đã đọc
- `DELETED`: Đã xóa

---

#### **POST** `/api/v1/chat/messages`
Gửi tin nhắn (REST - khuyến nghị dùng WebSocket)

**Request Body:**
```json
{
  "conversationId": "conv123",
  "content": "Xin chào!",
  "type": "TEXT",
  "attachments": [],
  "replyToMessageId": null
}
```

**Response:** Message object

---

#### **POST** `/api/v1/chat/messages/{messageId}/read`
Đánh dấu 1 tin nhắn đã đọc

**Response:**
```json
{
  "message": "Message marked as read"
}
```

---

#### **POST** `/api/v1/chat/conversations/{conversationId}/read`
Đánh dấu toàn bộ tin nhắn trong conversation đã đọc

**Response:**
```json
{
  "message": "Conversation marked as read"
}
```

---

#### **DELETE** `/api/v1/chat/messages/{messageId}`
Xóa tin nhắn (soft delete)

**Response:** Message object với status = DELETED

---

## 🔌 WebSocket Integration

### Connection Setup

```javascript
// 1. Import thư viện
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

// 2. Kết nối
const socket = new SockJS('http://localhost:8080/ws/chat');
const stompClient = Stomp.over(socket);

// 3. Connect với JWT token
stompClient.connect(
  { Authorization: `Bearer ${jwtToken}` },
  onConnected,
  onError
);

function onConnected() {
  console.log('WebSocket connected!');
  
  // Subscribe to receive messages
  subscribeToMessages();
}

function onError(error) {
  console.error('WebSocket error:', error);
}
```

---

### Subscribe Destinations

#### 1. **Nhận tin nhắn riêng tư** (Private Queue)

```javascript
// Mỗi user có queue riêng: /user/{userId}/queue/messages
stompClient.subscribe('/user/queue/messages', (message) => {
  const chatMessage = JSON.parse(message.body);
  console.log('New message:', chatMessage);
  
  // Update UI
  displayMessage(chatMessage);
});
```

**Message Format:**
```json
{
  "id": "msg123",
  "conversationId": "conv123",
  "senderId": "user456",
  "senderName": "Nguyễn Văn A",
  "senderAvatar": "https://...",
  "content": "Xin chào!",
  "type": "TEXT",
  "sentAt": "2025-11-24T10:30:00"
}
```

---

#### 2. **Typing Indicator**

```javascript
// Subscribe to typing events in a conversation
stompClient.subscribe(`/topic/conversation/${conversationId}/typing`, (message) => {
  const typingData = JSON.parse(message.body);
  
  if (typingData.isTyping) {
    showTypingIndicator(typingData.userName);
  } else {
    hideTypingIndicator(typingData.userName);
  }
});
```

---

### Publish Messages

#### 1. **Gửi tin nhắn**

```javascript
stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
  conversationId: 'conv123',
  content: 'Xin chào!',
  type: 'TEXT',
  attachments: [],
  replyToMessageId: null
}));
```

---

#### 2. **Gửi typing indicator**

```javascript
// User đang gõ
stompClient.send('/app/chat.typing', {}, JSON.stringify({
  conversationId: 'conv123',
  userId: 'currentUserId',
  userName: 'Nguyễn Văn A',
  isTyping: true
}));

// User ngừng gõ
stompClient.send('/app/chat.typing', {}, JSON.stringify({
  conversationId: 'conv123',
  userId: 'currentUserId',
  userName: 'Nguyễn Văn A',
  isTyping: false
}));
```

---

#### 3. **Đánh dấu đã đọc**

```javascript
// Đánh dấu 1 tin nhắn
stompClient.send('/app/chat.markRead', {}, JSON.stringify({
  messageId: 'msg123',
  conversationId: null
}));

// Đánh dấu toàn bộ conversation
stompClient.send('/app/chat.markRead', {}, JSON.stringify({
  messageId: null,
  conversationId: 'conv123'
}));
```

---

#### 4. **User presence (Online/Offline)**

```javascript
// User online
stompClient.send('/app/chat.userPresence', {}, JSON.stringify({
  userId: 'currentUserId',
  online: true,
  status: 'online'
}));

// User offline
stompClient.send('/app/chat.userPresence', {}, JSON.stringify({
  userId: 'currentUserId',
  online: false,
  status: 'offline'
}));
```

---

## 📦 Data Models

### ConversationDTO

```typescript
interface ConversationDTO {
  id: string;
  participants: ParticipantInfo[];
  type: 'BUYER_SELLER' | 'BUYER_SUPPORT' | 'SELLER_SUPPORT';
  storeId?: string;
  storeName?: string;
  storeAvatar?: string;
  productId?: string;
  productName?: string;
  productImage?: string;
  lastMessage?: string;
  lastMessageTime?: string; // ISO 8601 format
  unreadCount: number;
  status: 'ACTIVE' | 'ARCHIVED' | 'CLOSED';
}

interface ParticipantInfo {
  userId: string;
  userName: string;
  userAvatar?: string;
}
```

---

### ChatMessageDTO

```typescript
interface ChatMessageDTO {
  id: string;
  conversationId: string;
  senderId: string;
  senderName: string;
  senderAvatar?: string;
  content: string;
  type: 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM' | 'PRODUCT_LINK';
  attachments: string[];
  readByUserIds: string[];
  replyToMessageId?: string;
  status: 'SENT' | 'DELIVERED' | 'READ' | 'DELETED';
  sentAt: string; // ISO 8601 format
  productInfo?: {
    productId: string;
    productName: string;
    productImage: string;
    productPrice: number;
  };
}
```

---

### CreateConversationRequest

```typescript
interface CreateConversationRequest {
  recipientId?: string; // Null nếu type = BUYER_SELLER
  storeId?: string;
  type: 'BUYER_SELLER' | 'BUYER_SUPPORT' | 'SELLER_SUPPORT';
  productId?: string;
  initialMessage?: string;
}
```

---

### SendMessageRequest

```typescript
interface SendMessageRequest {
  conversationId: string;
  content: string;
  type: 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM' | 'PRODUCT_LINK';
  attachments?: string[];
  replyToMessageId?: string;
}
```

---

## 💻 Ví dụ Code Frontend

### React + Axios + SockJS

```javascript
import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const ChatComponent = ({ conversationId, currentUserId, jwtToken }) => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const stompClientRef = useRef(null);
  const typingTimeoutRef = useRef(null);

  // Setup axios với JWT
  const api = axios.create({
    baseURL: 'http://localhost:8080/api/v1/chat',
    headers: { Authorization: `Bearer ${jwtToken}` }
  });

  // Load lịch sử tin nhắn
  useEffect(() => {
    const loadMessages = async () => {
      try {
        const response = await api.get(
          `/conversations/${conversationId}/messages`,
          { params: { page: 0, size: 50 } }
        );
        setMessages(response.data.content.reverse());
      } catch (error) {
        console.error('Error loading messages:', error);
      }
    };
    loadMessages();
  }, [conversationId]);

  // Setup WebSocket
  useEffect(() => {
    const socket = new SockJS('http://localhost:8080/ws/chat');
    const stompClient = Stomp.over(socket);

    stompClient.connect(
      { Authorization: `Bearer ${jwtToken}` },
      () => {
        console.log('WebSocket connected');

        // Subscribe nhận tin nhắn
        stompClient.subscribe('/user/queue/messages', (message) => {
          const chatMessage = JSON.parse(message.body);
          if (chatMessage.conversationId === conversationId) {
            setMessages(prev => [...prev, chatMessage]);
          }
        });

        // Subscribe typing indicator
        stompClient.subscribe(
          `/topic/conversation/${conversationId}/typing`,
          (message) => {
            const typingData = JSON.parse(message.body);
            if (typingData.userId !== currentUserId) {
              setIsTyping(typingData.isTyping);
            }
          }
        );

        stompClientRef.current = stompClient;
      },
      (error) => {
        console.error('WebSocket error:', error);
      }
    );

    return () => {
      if (stompClient && stompClient.connected) {
        stompClient.disconnect();
      }
    };
  }, [conversationId, currentUserId, jwtToken]);

  // Gửi tin nhắn
  const handleSendMessage = () => {
    if (!newMessage.trim() || !stompClientRef.current) return;

    stompClientRef.current.send(
      '/app/chat.sendMessage',
      {},
      JSON.stringify({
        conversationId,
        content: newMessage,
        type: 'TEXT',
        attachments: [],
        replyToMessageId: null
      })
    );

    setNewMessage('');
    
    // Gửi typing = false
    stompClientRef.current.send(
      '/app/chat.typing',
      {},
      JSON.stringify({
        conversationId,
        userId: currentUserId,
        userName: 'Me',
        isTyping: false
      })
    );
  };

  // Xử lý typing indicator
  const handleInputChange = (e) => {
    setNewMessage(e.target.value);

    if (!stompClientRef.current) return;

    // Gửi typing = true
    stompClientRef.current.send(
      '/app/chat.typing',
      {},
      JSON.stringify({
        conversationId,
        userId: currentUserId,
        userName: 'Me',
        isTyping: true
      })
    );

    // Clear timeout cũ
    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }

    // Set timeout mới: sau 2s không gõ → typing = false
    typingTimeoutRef.current = setTimeout(() => {
      stompClientRef.current.send(
        '/app/chat.typing',
        {},
        JSON.stringify({
          conversationId,
          userId: currentUserId,
          userName: 'Me',
          isTyping: false
        })
      );
    }, 2000);
  };

  // Đánh dấu đã đọc khi vào conversation
  useEffect(() => {
    const markAsRead = async () => {
      try {
        await api.post(`/conversations/${conversationId}/read`);
      } catch (error) {
        console.error('Error marking as read:', error);
      }
    };
    markAsRead();
  }, [conversationId]);

  return (
    <div className="chat-container">
      {/* Message list */}
      <div className="message-list">
        {messages.map(msg => (
          <div key={msg.id} className={msg.senderId === currentUserId ? 'my-message' : 'other-message'}>
            <div className="message-avatar">
              <img src={msg.senderAvatar} alt={msg.senderName} />
            </div>
            <div className="message-content">
              <div className="message-sender">{msg.senderName}</div>
              <div className="message-text">{msg.content}</div>
              <div className="message-time">
                {new Date(msg.sentAt).toLocaleTimeString()}
              </div>
            </div>
          </div>
        ))}
        
        {/* Typing indicator */}
        {isTyping && (
          <div className="typing-indicator">
            <span>Đang gõ...</span>
          </div>
        )}
      </div>

      {/* Input area */}
      <div className="message-input">
        <input
          type="text"
          value={newMessage}
          onChange={handleInputChange}
          onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
          placeholder="Nhập tin nhắn..."
        />
        <button onClick={handleSendMessage}>Gửi</button>
      </div>
    </div>
  );
};

export default ChatComponent;
```

---

### Vue.js Example

```vue
<template>
  <div class="chat-container">
    <div class="message-list" ref="messageList">
      <div
        v-for="msg in messages"
        :key="msg.id"
        :class="msg.senderId === currentUserId ? 'my-message' : 'other-message'"
      >
        <img :src="msg.senderAvatar" :alt="msg.senderName" />
        <div>
          <div class="sender">{{ msg.senderName }}</div>
          <div class="content">{{ msg.content }}</div>
          <div class="time">{{ formatTime(msg.sentAt) }}</div>
        </div>
      </div>
      <div v-if="isTyping" class="typing-indicator">Đang gõ...</div>
    </div>

    <div class="message-input">
      <input
        v-model="newMessage"
        @input="handleTyping"
        @keyup.enter="sendMessage"
        placeholder="Nhập tin nhắn..."
      />
      <button @click="sendMessage">Gửi</button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

export default {
  props: ['conversationId', 'currentUserId', 'jwtToken'],
  data() {
    return {
      messages: [],
      newMessage: '',
      isTyping: false,
      stompClient: null,
      typingTimeout: null
    };
  },
  methods: {
    async loadMessages() {
      try {
        const response = await axios.get(
          `http://localhost:8080/api/v1/chat/conversations/${this.conversationId}/messages`,
          {
            headers: { Authorization: `Bearer ${this.jwtToken}` },
            params: { page: 0, size: 50 }
          }
        );
        this.messages = response.data.content.reverse();
      } catch (error) {
        console.error('Error loading messages:', error);
      }
    },

    connectWebSocket() {
      const socket = new SockJS('http://localhost:8080/ws/chat');
      this.stompClient = Stomp.over(socket);

      this.stompClient.connect(
        { Authorization: `Bearer ${this.jwtToken}` },
        () => {
          // Subscribe messages
          this.stompClient.subscribe('/user/queue/messages', (message) => {
            const chatMessage = JSON.parse(message.body);
            if (chatMessage.conversationId === this.conversationId) {
              this.messages.push(chatMessage);
              this.$nextTick(() => this.scrollToBottom());
            }
          });

          // Subscribe typing
          this.stompClient.subscribe(
            `/topic/conversation/${this.conversationId}/typing`,
            (message) => {
              const typingData = JSON.parse(message.body);
              if (typingData.userId !== this.currentUserId) {
                this.isTyping = typingData.isTyping;
              }
            }
          );
        }
      );
    },

    sendMessage() {
      if (!this.newMessage.trim() || !this.stompClient) return;

      this.stompClient.send(
        '/app/chat.sendMessage',
        {},
        JSON.stringify({
          conversationId: this.conversationId,
          content: this.newMessage,
          type: 'TEXT',
          attachments: [],
          replyToMessageId: null
        })
      );

      this.newMessage = '';
      this.sendTypingStatus(false);
    },

    handleTyping() {
      this.sendTypingStatus(true);

      clearTimeout(this.typingTimeout);
      this.typingTimeout = setTimeout(() => {
        this.sendTypingStatus(false);
      }, 2000);
    },

    sendTypingStatus(isTyping) {
      if (!this.stompClient) return;

      this.stompClient.send(
        '/app/chat.typing',
        {},
        JSON.stringify({
          conversationId: this.conversationId,
          userId: this.currentUserId,
          userName: 'Me',
          isTyping
        })
      );
    },

    formatTime(dateString) {
      return new Date(dateString).toLocaleTimeString();
    },

    scrollToBottom() {
      const messageList = this.$refs.messageList;
      if (messageList) {
        messageList.scrollTop = messageList.scrollHeight;
      }
    }
  },

  mounted() {
    this.loadMessages();
    this.connectWebSocket();
  },

  beforeUnmount() {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.disconnect();
    }
  }
};
</script>
```

---

## ⚠️ Error Handling

### HTTP Error Codes

| Code | Meaning | Solution |
|------|---------|----------|
| **400** | Bad Request | Kiểm tra request body format |
| **401** | Unauthorized | JWT token hết hạn/không hợp lệ → Refresh token |
| **403** | Forbidden | User không có quyền truy cập conversation này |
| **404** | Not Found | Conversation/Message không tồn tại |
| **500** | Server Error | Lỗi server → Retry hoặc liên hệ admin |

---

### WebSocket Error Handling

```javascript
stompClient.connect(
  { Authorization: `Bearer ${jwtToken}` },
  onConnected,
  (error) => {
    console.error('WebSocket connection error:', error);
    
    // Retry after 5 seconds
    setTimeout(() => {
      console.log('Retrying WebSocket connection...');
      connectWebSocket();
    }, 5000);
  }
);

// Xử lý disconnect
socket.onclose = () => {
  console.log('WebSocket disconnected');
  // Implement reconnection logic
};
```

---

## 🔥 Best Practices

### 1. **Pagination**
- Load tin nhắn theo batch (50-100 messages/lần)
- Implement infinite scroll khi user scroll lên

### 2. **Optimistic UI**
- Hiển thị tin nhắn ngay khi user gửi (không đợi server)
- Update lại khi nhận confirmation từ server

### 3. **Typing Indicator**
- Debounce 300ms khi user gõ
- Tự động tắt sau 2-3 giây không activity

### 4. **Read Receipts**
- Đánh dấu đã đọc khi user vào conversation
- Update UI real-time khi người khác đọc tin nhắn

### 5. **Connection Management**
- Auto-reconnect khi WebSocket bị disconnect
- Hiển thị status "Connecting..." cho user

### 6. **Performance**
- Virtualize message list khi có > 100 messages
- Lazy load images/attachments

### 7. **Security**
- Validate JWT token trước khi connect WebSocket
- Không lưu sensitive data trong localStorage

---

## 📚 Tài liệu tham khảo

- [SockJS Documentation](https://github.com/sockjs/sockjs-client)
- [STOMP Protocol](https://stomp.github.io/)
- [Spring WebSocket Guide](https://spring.io/guides/gs/messaging-stomp-websocket/)


**Last Updated:** November 24, 2025  
**API Version:** 1.0
