# IronMan - Tech Stack & Setup Guide

## Technology Stack

### Backend
- **Framework:** Spring Boot 3.2.2
- **Language:** Java 17
- **Build Tool:** Maven 3.9.6
- **Database:** PostgreSQL 16
- **Cache:** Redis 5.0
- **Security:** Spring Security + JWT
- **API Documentation:** Swagger/OpenAPI 3.0

### Frontend
- **Framework:** React 18.2
- **Build Tool:** Vite 5.0
- **Language:** JavaScript (ES6+)
- **Styling:** Tailwind CSS 3.4
- **HTTP Client:** Axios 1.6
- **Routing:** React Router 6.21
- **State Management:** React Context API
- **Maps:** Leaflet.js 1.9
- **Charts:** Recharts 2.10

### External Services
- **Payment:** Razorpay
- **Push Notifications:** Firebase Cloud Messaging
- **Storage:** AWS S3 / Cloudinary
- **Maps:** OpenStreetMap + Nominatim
- **Email:** SMTP (Gmail/SendGrid)

### Deployment
- **Backend:** Railway / Render / AWS EC2
- **Frontend:** Vercel / Netlify
- **Database:** Railway PostgreSQL / AWS RDS
- **CI/CD:** GitHub Actions

---

## Backend Dependencies (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.2</version>
        <relativePath/>
    </parent>
    
    <groupId>com.ironman</groupId>
    <artifactId>ironman-backend</artifactId>
    <version>1.0.0</version>
    <name>ironman-backend</name>
    <description>IronMan Laundry Service Backend</description>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.3</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Razorpay -->
        <dependency>
            <groupId>com.razorpay</groupId>
            <artifactId>razorpay-java</artifactId>
            <version>1.4.3</version>
        </dependency>
        
        <!-- Firebase Admin SDK -->
        <dependency>
            <groupId>com.google.firebase</groupId>
            <artifactId>firebase-admin</artifactId>
            <version>9.2.0</version>
        </dependency>
        
        <!-- AWS S3 -->
        <dependency>
            <groupId>com.amazonaws</groupId>
            <artifactId>aws-java-sdk-s3</artifactId>
            <version>1.12.565</version>
        </dependency>
        
        <!-- Cloudinary (Alternative to S3) -->
        <dependency>
            <groupId>com.cloudinary</groupId>
            <artifactId>cloudinary-http44</artifactId>
            <version>1.36.0</version>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- MapStruct (for DTO mapping) -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>1.5.5.Final</version>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct-processor</artifactId>
            <version>1.5.5.Final</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- Swagger/OpenAPI -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.3.0</version>
        </dependency>
        
        <!-- Apache Commons -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- Dev Tools -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## Backend Configuration (application.yml)

```yaml
spring:
  application:
    name: ironman-backend
    
  datasource:
    url: jdbc:postgresql://localhost:5432/ironman_db
    username: postgres
    password: ${DB_PASSWORD:your_password}
    driver-class-name: org.postgresql.Driver
    
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        
  data:
    redis:
      host: localhost
      port: 6379
      password: ${REDIS_PASSWORD:}
      timeout: 60000
      
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
      
server:
  port: 8080
  error:
    include-message: always
    include-stacktrace: on_param
    
# JWT Configuration
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-change-this-in-production}
  expiration: 86400000 # 24 hours in milliseconds
  refresh-expiration: 604800000 # 7 days
  
# Razorpay Configuration
razorpay:
  key-id: ${RAZORPAY_KEY_ID}
  key-secret: ${RAZORPAY_KEY_SECRET}
  
# Firebase Configuration
firebase:
  service-account-path: ${FIREBASE_SERVICE_ACCOUNT_PATH:classpath:firebase-service-account.json}
  
# AWS S3 Configuration (if using S3)
aws:
  s3:
    access-key: ${AWS_ACCESS_KEY}
    secret-key: ${AWS_SECRET_KEY}
    bucket-name: ${AWS_S3_BUCKET}
    region: ap-south-1
    
# Cloudinary Configuration (if using Cloudinary)
cloudinary:
  cloud-name: ${CLOUDINARY_CLOUD_NAME}
  api-key: ${CLOUDINARY_API_KEY}
  api-secret: ${CLOUDINARY_API_SECRET}
  
# Application Configuration
app:
  slot:
    morning:
      start: 09:00
      end: 13:00
    evening:
      start: 15:00
      end: 19:00
  location:
    tracking-interval: 30 # seconds
    
# CORS Configuration
cors:
  allowed-origins: http://localhost:5173,http://localhost:3000
  allowed-methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
  allowed-headers: "*"
  allow-credentials: true
  
# Logging
logging:
  level:
    root: INFO
    com.ironman: DEBUG
    org.springframework.web: INFO
    org.hibernate: INFO
    
# Swagger/OpenAPI
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
```

---

## Frontend Dependencies (package.json)

```json
{
  "name": "ironman-frontend",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext js,jsx --report-unused-disable-directives --max-warnings 0"
  },
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-router-dom": "^6.21.0",
    "axios": "^1.6.5",
    "leaflet": "^1.9.4",
    "react-leaflet": "^4.2.1",
    "recharts": "^2.10.3",
    "react-hot-toast": "^2.4.1",
    "lucide-react": "^0.307.0",
    "date-fns": "^3.0.6",
    "clsx": "^2.1.0"
  },
  "devDependencies": {
    "@types/react": "^18.2.43",
    "@types/react-dom": "^18.2.17",
    "@vitejs/plugin-react": "^4.2.1",
    "vite": "^5.0.8",
    "eslint": "^8.55.0",
    "eslint-plugin-react": "^7.33.2",
    "eslint-plugin-react-hooks": "^4.6.0",
    "eslint-plugin-react-refresh": "^0.4.5",
    "tailwindcss": "^3.4.0",
    "autoprefixer": "^10.4.16",
    "postcss": "^8.4.32"
  }
}
```

---

## Frontend Configuration

### vite.config.js
```javascript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    sourcemap: false
  }
})
```

### tailwind.config.js
```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f0f9ff',
          100: '#e0f2fe',
          500: '#0ea5e9',
          600: '#0284c7',
          700: '#0369a1',
        },
        secondary: {
          500: '#8b5cf6',
          600: '#7c3aed',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
```

### .env.example (Frontend)
```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_RAZORPAY_KEY_ID=rzp_test_xxxxx
VITE_FIREBASE_API_KEY=AIzaSyXXXXXX
VITE_FIREBASE_AUTH_DOMAIN=ironman-xxxxx.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=ironman-xxxxx
VITE_FIREBASE_STORAGE_BUCKET=ironman-xxxxx.appspot.com
VITE_FIREBASE_MESSAGING_SENDER_ID=123456789
VITE_FIREBASE_APP_ID=1:123456789:web:xxxxx
```

---

## Project Structure

### Backend Structure
```
ironman-backend/
├── src/
│   ├── main/
│   │   ├── java/com/ironman/
│   │   │   ├── IronmanApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtConfig.java
│   │   │   │   ├── RedisConfig.java
│   │   │   │   ├── WebSocketConfig.java
│   │   │   │   ├── SwaggerConfig.java
│   │   │   │   └── CorsConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── AddressController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   ├── ServiceController.java
│   │   │   │   ├── DeliveryPartnerController.java
│   │   │   │   ├── AssignmentController.java
│   │   │   │   ├── ReviewController.java
│   │   │   │   ├── NotificationController.java
│   │   │   │   └── AdminController.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   ├── PaymentService.java
│   │   │   │   ├── PricingService.java
│   │   │   │   ├── SlotService.java
│   │   │   │   ├── DeliveryPartnerService.java
│   │   │   │   ├── AssignmentService.java
│   │   │   │   ├── LocationTrackingService.java
│   │   │   │   ├── NotificationService.java
│   │   │   │   ├── FileStorageService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   └── ReviewService.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── AddressRepository.java
│   │   │   │   ├── ServiceRepository.java
│   │   │   │   ├── ClothTypeRepository.java
│   │   │   │   ├── AddOnRepository.java
│   │   │   │   ├── DeliveryPartnerRepository.java
│   │   │   │   ├── AssignmentRepository.java
│   │   │   │   ├── PaymentRepository.java
│   │   │   │   ├── ReviewRepository.java
│   │   │   │   ├── NotificationRepository.java
│   │   │   │   └── LocationTrackingRepository.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   ├── Address.java
│   │   │   │   ├── Service.java
│   │   │   │   ├── ClothType.java
│   │   │   │   ├── AddOn.java
│   │   │   │   ├── DeliveryPartner.java
│   │   │   │   ├── Assignment.java
│   │   │   │   ├── Payment.java
│   │   │   │   ├── Review.java
│   │   │   │   ├── Notification.java
│   │   │   │   ├── LocationTracking.java
│   │   │   │   └── Slot.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   ├── CreateOrderRequest.java
│   │   │   │   │   └── ...
│   │   │   │   └── response/
│   │   │   │       ├── AuthResponse.java
│   │   │   │       ├── OrderResponse.java
│   │   │   │       ├── ApiResponse.java
│   │   │   │       └── ...
│   │   │   ├── security/
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── BadRequestException.java
│   │   │   │   └── UnauthorizedException.java
│   │   │   └── util/
│   │   │       ├── GeoLocationUtil.java
│   │   │       ├── DateTimeUtil.java
│   │   │       └── ValidationUtil.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── firebase-service-account.json
│   └── test/
│       └── java/com/ironman/
│           ├── controller/
│           ├── service/
│           └── repository/
├── pom.xml
└── README.md
```

### Frontend Structure
```
ironman-frontend/
├── public/
│   └── favicon.ico
├── src/
│   ├── App.jsx
│   ├── main.jsx
│   ├── index.css
│   ├── components/
│   │   ├── common/
│   │   │   ├── Button.jsx
│   │   │   ├── Input.jsx
│   │   │   ├── Card.jsx
│   │   │   ├── Modal.jsx
│   │   │   ├── Loader.jsx
│   │   │   ├── Navbar.jsx
│   │   │   └── Footer.jsx
│   │   ├── customer/
│   │   │   ├── ServiceCard.jsx
│   │   │   ├── BookingForm.jsx
│   │   │   ├── OrderCard.jsx
│   │   │   └── OrderTracking.jsx
│   │   ├── partner/
│   │   │   ├── AssignmentCard.jsx
│   │   │   ├── LocationTracker.jsx
│   │   │   └── EarningsCard.jsx
│   │   └── admin/
│   │       ├── StatsCard.jsx
│   │       ├── OrderTable.jsx
│   │       └── PartnerTable.jsx
│   ├── pages/
│   │   ├── auth/
│   │   │   ├── Login.jsx
│   │   │   └── Register.jsx
│   │   ├── customer/
│   │   │   ├── Home.jsx
│   │   │   ├── Services.jsx
│   │   │   ├── Booking.jsx
│   │   │   ├── Orders.jsx
│   │   │   ├── OrderDetails.jsx
│   │   │   ├── Profile.jsx
│   │   │   └── Addresses.jsx
│   │   ├── partner/
│   │   │   ├── Dashboard.jsx
│   │   │   ├── Assignments.jsx
│   │   │   ├── AssignmentDetails.jsx
│   │   │   └── Earnings.jsx
│   │   └── admin/
│   │       ├── Dashboard.jsx
│   │       ├── Orders.jsx
│   │       ├── Partners.jsx
│   │       ├── Services.jsx
│   │       └── Analytics.jsx
│   ├── context/
│   │   ├── AuthContext.jsx
│   │   └── CartContext.jsx
│   ├── services/
│   │   ├── api.js
│   │   ├── authService.js
│   │   ├── orderService.js
│   │   ├── paymentService.js
│   │   ├── partnerService.js
│   │   └── adminService.js
│   ├── utils/
│   │   ├── constants.js
│   │   ├── validators.js
│   │   ├── formatters.js
│   │   └── helpers.js
│   ├── hooks/
│   │   ├── useAuth.js
│   │   ├── useOrders.js
│   │   └── useLocation.js
│   └── assets/
│       ├── images/
│       └── icons/
├── .env
├── .env.example
├── package.json
├── vite.config.js
├── tailwind.config.js
├── postcss.config.js
└── README.md
```

---

## Setup Instructions

### 1. Initial Setup (Day 1 Morning)

#### Backend Setup
```bash
# Navigate to backend folder
cd ironman-app/backend

# Create Spring Boot project using Spring Initializr (web interface)
# OR use command line:
curl https://start.spring.io/starter.zip \
  -d dependencies=web,data-jpa,security,validation,postgresql,lombok,devtools \
  -d javaVersion=17 \
  -d bootVersion=3.2.2 \
  -d groupId=com.ironman \
  -d artifactId=ironman-backend \
  -d name=IronMan \
  -d packageName=com.ironman \
  -d type=maven-project \
  -o ironman-backend.zip

unzip ironman-backend.zip
rm ironman-backend.zip

# Update pom.xml with all dependencies from above
# Create application.yml from the template above
# Create package structure
```

#### Frontend Setup
```bash
# Navigate to frontend folder
cd ironman-app/frontend

# Create Vite + React project
npm create vite@latest . -- --template react

# Install dependencies
npm install react-router-dom axios leaflet react-leaflet recharts react-hot-toast lucide-react date-fns clsx

# Install dev dependencies
npm install -D tailwindcss autoprefixer postcss

# Initialize Tailwind
npx tailwindcss init -p

# Update configurations from templates above
# Create folder structure
```

### 2. Database Setup
```bash
# Start PostgreSQL
# Open pgAdmin or psql

# Create database
CREATE DATABASE ironman_db;

# Connect to database
\c ironman_db

# Run all CREATE TABLE scripts from DATABASE_SCHEMA.md
# Insert initial data (services, cloth types, add-ons)
```

### 3. Redis Setup
```bash
# Start Redis server
redis-server

# Verify Redis is running
redis-cli ping
# Should return: PONG
```

### 4. Environment Variables

Create `.env` files:

**Backend:** `src/main/resources/.env`
```
DB_PASSWORD=your_postgres_password
REDIS_PASSWORD=
JWT_SECRET=your-super-secret-256-bit-key-change-in-production
RAZORPAY_KEY_ID=rzp_test_xxxxx
RAZORPAY_KEY_SECRET=your_razorpay_secret
FIREBASE_SERVICE_ACCOUNT_PATH=classpath:firebase-service-account.json
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

**Frontend:** `.env`
```
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_RAZORPAY_KEY_ID=rzp_test_xxxxx
VITE_FIREBASE_API_KEY=AIzaSyXXXXXX
VITE_FIREBASE_AUTH_DOMAIN=ironman-xxxxx.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=ironman-xxxxx
VITE_FIREBASE_STORAGE_BUCKET=ironman-xxxxx.appspot.com
VITE_FIREBASE_MESSAGING_SENDER_ID=123456789
VITE_FIREBASE_APP_ID=1:123456789:web:xxxxx
```

### 5. Running the Application

#### Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run

# OR in IntelliJ: Right-click IronmanApplication.java → Run
```

#### Frontend
```bash
cd frontend
npm run dev

# Opens at http://localhost:5173
```

### 6. Verify Setup
- Backend API: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/swagger-ui.html
- Frontend: http://localhost:5173

---

## Development Tools

### Required
- **Java 17 JDK**
- **Node.js 18+**
- **PostgreSQL 16**
- **Redis 5+**
- **IntelliJ IDEA** (or VS Code)
- **Postman**
- **Git**

### Recommended
- **pgAdmin** (PostgreSQL GUI)
- **Redis Insight** (Redis GUI)
- **Postman** (API testing)
- **GitHub Desktop** (Git GUI)

---

## Useful Commands

### Maven
```bash
mvn clean install          # Build project
mvn spring-boot:run        # Run application
mvn test                   # Run tests
mvn clean                  # Clean build files
```

### NPM
```bash
npm install                # Install dependencies
npm run dev                # Start dev server
npm run build              # Build for production
npm run preview            # Preview production build
```

### Git
```bash
git status                 # Check status
git add .                  # Stage all changes
git commit -m "message"    # Commit
git push origin main       # Push to GitHub
git pull origin main       # Pull latest changes
```

### PostgreSQL
```bash
psql -U postgres           # Connect to PostgreSQL
\l                         # List databases
\c ironman_db              # Connect to database
\dt                        # List tables
\d table_name              # Describe table
```

### Redis
```bash
redis-cli                  # Connect to Redis
PING                       # Test connection
KEYS *                     # List all keys
GET key                    # Get value
DEL key                    # Delete key
FLUSHDB                    # Clear current database
```

---

This completes your tech stack setup! Follow the DEVELOPMENT_PLAN.md for daily tasks. 🚀
