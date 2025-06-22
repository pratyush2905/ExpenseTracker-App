# Expense Tracker App - Local Setup Guide

## Overview
This is a microservices-based expense tracker application with the following components:
- **Frontend**: React Native app
- **Auth Service**: JWT-based authentication
- **User Service**: User management
- **Expense Service**: Expense tracking
- **DS Service**: AI-powered message processing using Mistral AI
- **Kong API Gateway**: API routing and authentication
- **Kafka**: Message queuing
- **MySQL**: Database

## Updated URLs for Local Development

All frontend URLs have been updated from AWS endpoints to local Docker containers:

### Before (AWS URLs):
```
http://Expens-KongA-ChasZNdaOM4K-1208155051.ap-south-1.elb.amazonaws.com
```

### After (Local URLs):
```
http://localhost:8000
```

## Updated Files:
1. `frontend/expensetrackerapp/src/app/api/LoginService.ts`
2. `frontend/expensetrackerapp/src/app/pages/Login.tsx`
3. `frontend/expensetrackerapp/src/app/pages/SignUp.tsx`
4. `frontend/expensetrackerapp/src/app/pages/Spends.tsx`
5. `kong/custom-plugins/custom-auth/handler.lua` (Fixed header name)

## Service Ports (Docker Compose):
- **Kong API Gateway**: `8000` (external)
- **Auth Service**: `9899` (internal)
- **User Service**: `9810` (internal)
- **Expense Service**: `9821` (internal)
- **DS Service**: `8010` (internal)
- **Kafka**: `9092` (internal)
- **MySQL**: `3306` (external)

## API Endpoints:

### Auth Service (via Kong: `http://localhost:8000/auth/v1/`)
- `POST /auth/v1/signup` - User registration
- `POST /auth/v1/login` - User login
- `POST /auth/v1/refreshToken` - Refresh JWT token
- `GET /auth/v1/ping` - Validate token

### User Service (via Kong: `http://localhost:8000/user/v1/`)
- `GET /user/v1/getUser` - Get user info
- `POST /user/v1/createUpdate` - Create/update user

### Expense Service (via Kong: `http://localhost:8000/expense/v1/`)
- `GET /expense/v1/getExpense` - Get user expenses
- `POST /expense/v1/addExpense` - Add new expense

### DS Service (via Kong: `http://localhost:8000/ds/v1/`)
- `POST /ds/v1/message` - Process messages with AI

## Running the Application:

### 1. Start Docker Services:
```bash
docker-compose up -d
```

### 2. Build Frontend (if needed):
```bash
cd frontend/expensetrackerapp
npm install
# For Android:
npx react-native run-android
# For iOS:
npx react-native run-ios
```

### 3. Test the Setup:
- Kong Gateway: http://localhost:8000
- Auth Service Health: http://localhost:8000/auth/v1/health
- Expense Service Health: http://localhost:8000/expense/v1/health

## Authentication Flow:
1. User signs up/logs in via frontend
2. Auth service returns JWT token
3. Frontend stores token in AsyncStorage
4. Subsequent API calls include token in Authorization header
5. Kong validates token via custom auth plugin
6. Kong forwards requests to appropriate services with user ID

## Database Setup:
The application will automatically create the required databases:
- `authservice`
- `userservice`
- `expenseservice`

## Troubleshooting:

### Common Issues:
1. **Port conflicts**: Ensure ports 8000, 3306 are available
2. **Docker build issues**: Run `docker-compose build --no-cache`
3. **Database connection**: Wait for MySQL to fully start before other services
4. **Kafka connection**: Ensure Zookeeper starts before Kafka

### Logs:
```bash
# View all logs:
docker-compose logs -f

# View specific service logs:
docker-compose logs -f authservice
docker-compose logs -f kong-service
```

## Development Notes:
- All services use environment variables for configuration
- Kafka topics are auto-created
- JWT tokens have configurable expiration
- The DS service uses Mistral AI for message processing
- Kong handles CORS and authentication centrally 