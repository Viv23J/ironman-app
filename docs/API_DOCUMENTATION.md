# IronMan - REST API Documentation

## Base URL
```
Development: http://localhost:8080/api/v1
Production: https://api.ironman.app/api/v1
```

## Authentication
All authenticated endpoints require JWT token in header:
```
Authorization: Bearer <jwt_token>
```

---

## API Overview

### Public Endpoints (No Auth Required)
- POST /auth/register
- POST /auth/login
- POST /auth/send-otp
- POST /auth/verify-otp
- GET /services
- GET /cloth-types
- GET /add-ons

### Customer Endpoints
- All /customers/** endpoints
- All /orders/** endpoints (own orders only)
- All /addresses/** endpoints (own addresses)
- All /payments/** endpoints (own payments)

### Delivery Partner Endpoints
- All /partners/** endpoints
- GET /assignments (assigned to them)
- PUT /assignments/{id}/status

### Admin Endpoints
- All /admin/** endpoints
- Full CRUD on all resources

---

## 1. Authentication APIs

### 1.1 Register User
**POST** `/auth/register`

**Request:**
```json
{
  "phone": "9876543210",
  "email": "user@example.com",
  "password": "SecurePass123",
  "fullName": "John Doe",
  "role": "CUSTOMER"
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "userId": 1,
    "phone": "9876543210",
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "CUSTOMER",
    "isVerified": false,
    "createdAt": "2024-01-28T10:30:00Z"
  }
}
```

---

### 1.2 Login
**POST** `/auth/login`

**Request:**
```json
{
  "phone": "9876543210",
  "password": "SecurePass123"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "phone": "9876543210",
      "fullName": "John Doe",
      "role": "CUSTOMER",
      "profileImage": null
    }
  }
}
```

---

### 1.3 Send OTP
**POST** `/auth/send-otp`

**Request:**
```json
{
  "phone": "9876543210",
  "purpose": "REGISTRATION"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "OTP sent successfully",
  "data": {
    "phone": "9876543210",
    "otpSent": true,
    "expiresIn": 300
  }
}
```

---

### 1.4 Verify OTP
**POST** `/auth/verify-otp`

**Request:**
```json
{
  "phone": "9876543210",
  "otp": "123456"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "OTP verified successfully",
  "data": {
    "verified": true,
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

---

## 2. User Profile APIs

### 2.1 Get My Profile
**GET** `/users/me`

**Headers:** Authorization required

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 1,
    "phone": "9876543210",
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "CUSTOMER",
    "profileImage": "https://storage.url/profile.jpg",
    "isActive": true,
    "isVerified": true,
    "createdAt": "2024-01-28T10:30:00Z"
  }
}
```

---

### 2.2 Update Profile
**PUT** `/users/me`

**Headers:** Authorization required

**Request:**
```json
{
  "fullName": "John Updated Doe",
  "email": "newemail@example.com"
}
```

**Response:** `200 OK`

---

### 2.3 Upload Profile Image
**POST** `/users/me/profile-image`

**Headers:** 
- Authorization required
- Content-Type: multipart/form-data

**Request:** FormData with 'image' file

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Profile image uploaded successfully",
  "data": {
    "imageUrl": "https://storage.url/profile-123.jpg"
  }
}
```

---

## 3. Address APIs

### 3.1 Get All Addresses
**GET** `/addresses`

**Headers:** Authorization required

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "label": "Home",
      "addressLine1": "123 Main Street",
      "addressLine2": "Apartment 4B",
      "landmark": "Near Central Park",
      "city": "Mumbai",
      "state": "Maharashtra",
      "pincode": "400001",
      "latitude": 19.0760,
      "longitude": 72.8777,
      "isDefault": true
    }
  ]
}
```

---

### 3.2 Add Address
**POST** `/addresses`

**Headers:** Authorization required

**Request:**
```json
{
  "label": "Office",
  "addressLine1": "456 Business Avenue",
  "addressLine2": "Floor 5",
  "landmark": "Opposite Mall",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pincode": "400002",
  "isDefault": false
}
```

**Response:** `201 Created`

---

### 3.3 Update Address
**PUT** `/addresses/{id}`

**Headers:** Authorization required

---

### 3.4 Delete Address
**DELETE** `/addresses/{id}`

**Headers:** Authorization required

**Response:** `204 No Content`

---

### 3.5 Set Default Address
**PUT** `/addresses/{id}/set-default`

**Headers:** Authorization required

**Response:** `200 OK`

---

## 4. Service Catalog APIs

### 4.1 Get All Services
**GET** `/services`

**Query Parameters:**
- `category` (optional): Filter by category

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Washing",
      "category": "LAUNDRY",
      "basePrice": 20.00,
      "description": "Professional washing service",
      "estimatedHours": 24,
      "iconUrl": "https://cdn.url/washing-icon.png",
      "isActive": true
    }
  ]
}
```

---

### 4.2 Get All Cloth Types
**GET** `/cloth-types`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Shirt",
      "category": "REGULAR",
      "priceMultiplier": 1.00,
      "description": "Regular shirts and tops"
    }
  ]
}
```

---

### 4.3 Get All Add-ons
**GET** `/add-ons`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Stain Removal",
      "description": "Advanced stain treatment",
      "price": 20.00,
      "isPerItem": true,
      "isActive": true
    }
  ]
}
```

---

### 4.4 Calculate Price
**POST** `/services/calculate-price`

**Request:**
```json
{
  "items": [
    {
      "serviceId": 1,
      "clothTypeId": 1,
      "quantity": 5
    }
  ],
  "addons": [
    {
      "addonId": 1,
      "quantity": 2
    }
  ]
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "subtotal": 100.00,
    "addonCharges": 40.00,
    "taxAmount": 25.20,
    "discountAmount": 0.00,
    "totalAmount": 165.20,
    "breakdown": {
      "items": [
        {
          "serviceName": "Washing",
          "clothTypeName": "Shirt",
          "quantity": 5,
          "unitPrice": 20.00,
          "lineTotal": 100.00
        }
      ],
      "addons": [
        {
          "name": "Stain Removal",
          "quantity": 2,
          "unitPrice": 20.00,
          "total": 40.00
        }
      ]
    }
  }
}
```

---

## 5. Order APIs

### 5.1 Get Available Slots
**GET** `/orders/available-slots`

**Query Parameters:**
- `date`: YYYY-MM-DD (required)

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "date": "2024-01-30",
    "slots": [
      {
        "slotTime": "MORNING",
        "displayTime": "9:00 AM - 1:00 PM",
        "maxCapacity": 50,
        "currentBookings": 23,
        "isAvailable": true
      },
      {
        "slotTime": "EVENING",
        "displayTime": "3:00 PM - 7:00 PM",
        "maxCapacity": 50,
        "currentBookings": 45,
        "isAvailable": true
      }
    ]
  }
}
```

---

### 5.2 Create Order
**POST** `/orders`

**Headers:** Authorization required

**Request:**
```json
{
  "pickupAddressId": 1,
  "deliveryAddressId": 1,
  "pickupSlot": "MORNING",
  "pickupDate": "2024-01-30",
  "expectedDeliveryDate": "2024-02-01",
  "items": [
    {
      "serviceId": 1,
      "clothTypeId": 1,
      "quantity": 5,
      "notes": "Gentle wash please"
    }
  ],
  "addons": [
    {
      "addonId": 1,
      "quantity": 2
    }
  ],
  "specialInstructions": "Call before pickup",
  "paymentMethod": "RAZORPAY"
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "orderId": 123,
    "orderNumber": "IM-2024-000123",
    "status": "PENDING",
    "totalAmount": 165.20,
    "razorpayOrderId": "order_123xyz",
    "pickupDate": "2024-01-30",
    "pickupSlot": "MORNING"
  }
}
```

---

### 5.3 Get My Orders
**GET** `/orders`

**Headers:** Authorization required

**Query Parameters:**
- `status` (optional): Filter by status
- `page` (default: 0)
- `size` (default: 10)
- `sort` (default: createdAt,desc)

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 123,
        "orderNumber": "IM-2024-000123",
        "status": "PENDING",
        "pickupAddress": "123 Main Street, Mumbai",
        "pickupDate": "2024-01-30",
        "pickupSlot": "MORNING",
        "totalAmount": 165.20,
        "paymentStatus": "PENDING",
        "createdAt": "2024-01-28T10:30:00Z"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "totalElements": 1,
      "totalPages": 1
    }
  }
}
```

---

### 5.4 Get Order Details
**GET** `/orders/{id}`

**Headers:** Authorization required

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 123,
    "orderNumber": "IM-2024-000123",
    "status": "PENDING",
    "customer": {
      "id": 1,
      "fullName": "John Doe",
      "phone": "9876543210"
    },
    "pickupAddress": {
      "id": 1,
      "fullAddress": "123 Main Street, Apartment 4B, Mumbai, Maharashtra - 400001"
    },
    "deliveryAddress": {
      "id": 1,
      "fullAddress": "123 Main Street, Apartment 4B, Mumbai, Maharashtra - 400001"
    },
    "pickupSlot": "MORNING",
    "pickupDate": "2024-01-30",
    "expectedDeliveryDate": "2024-02-01",
    "items": [
      {
        "serviceName": "Washing",
        "clothTypeName": "Shirt",
        "quantity": 5,
        "unitPrice": 20.00,
        "lineTotal": 100.00
      }
    ],
    "addons": [
      {
        "name": "Stain Removal",
        "quantity": 2,
        "price": 20.00,
        "total": 40.00
      }
    ],
    "pricing": {
      "subtotal": 100.00,
      "addonCharges": 40.00,
      "taxAmount": 25.20,
      "discountAmount": 0.00,
      "totalAmount": 165.20
    },
    "paymentStatus": "PENDING",
    "specialInstructions": "Call before pickup",
    "timeline": [
      {
        "status": "PENDING",
        "timestamp": "2024-01-28T10:30:00Z",
        "description": "Order created"
      }
    ],
    "createdAt": "2024-01-28T10:30:00Z"
  }
}
```

---

### 5.5 Cancel Order
**PUT** `/orders/{id}/cancel`

**Headers:** Authorization required

**Request:**
```json
{
  "reason": "Changed my mind"
}
```

**Response:** `200 OK`

---

### 5.6 Track Order
**GET** `/orders/{id}/track`

**Headers:** Authorization required

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "orderId": 123,
    "orderNumber": "IM-2024-000123",
    "currentStatus": "OUT_FOR_DELIVERY",
    "timeline": [
      {
        "status": "PENDING",
        "timestamp": "2024-01-28T10:30:00Z",
        "description": "Order placed"
      },
      {
        "status": "PICKUP_ASSIGNED",
        "timestamp": "2024-01-28T11:00:00Z",
        "description": "Pickup assigned to delivery partner"
      },
      {
        "status": "PICKED_UP",
        "timestamp": "2024-01-30T09:30:00Z",
        "description": "Clothes picked up"
      },
      {
        "status": "PROCESSING",
        "timestamp": "2024-01-30T12:00:00Z",
        "description": "In processing"
      },
      {
        "status": "OUT_FOR_DELIVERY",
        "timestamp": "2024-02-01T15:00:00Z",
        "description": "Out for delivery",
        "deliveryPartner": {
          "name": "Rahul Sharma",
          "phone": "9876543211",
          "vehicleNumber": "MH-01-AB-1234",
          "currentLocation": {
            "latitude": 19.0760,
            "longitude": 72.8777
          },
          "estimatedArrival": "2024-02-01T16:30:00Z"
        }
      }
    ]
  }
}
```

---

## 6. Payment APIs

### 6.1 Create Razorpay Order
**POST** `/payments/create-order`

**Headers:** Authorization required

**Request:**
```json
{
  "orderId": 123,
  "amount": 165.20
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "razorpayOrderId": "order_123xyz",
    "amount": 16520,
    "currency": "INR",
    "keyId": "rzp_test_xxxxx"
  }
}
```

---

### 6.2 Verify Payment
**POST** `/payments/verify`

**Headers:** Authorization required

**Request:**
```json
{
  "orderId": 123,
  "razorpayOrderId": "order_123xyz",
  "razorpayPaymentId": "pay_456abc",
  "razorpaySignature": "signature_string"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Payment verified successfully",
  "data": {
    "paymentId": 1,
    "status": "COMPLETED",
    "amount": 165.20
  }
}
```

---

### 6.3 Get Payment History
**GET** `/payments/history`

**Headers:** Authorization required

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "orderNumber": "IM-2024-000123",
      "amount": 165.20,
      "paymentMethod": "RAZORPAY_UPI",
      "status": "COMPLETED",
      "createdAt": "2024-01-28T10:30:00Z"
    }
  ]
}
```

---

## 7. Delivery Partner APIs

### 7.1 Register as Delivery Partner
**POST** `/partners/register`

**Headers:** Authorization required (must be logged in user)

**Request:**
```json
{
  "vehicleType": "BIKE",
  "vehicleNumber": "MH-01-AB-1234",
  "licenseNumber": "MH1234567890"
}
```

**Response:** `201 Created`

---

### 7.2 Upload Documents
**POST** `/partners/documents`

**Headers:** 
- Authorization required
- Content-Type: multipart/form-data

**Request:** FormData with files:
- idProof
- vehicleRC
- license
- photo

**Response:** `200 OK`

---

### 7.3 Update Availability
**PUT** `/partners/availability`

**Headers:** Authorization required

**Request:**
```json
{
  "isAvailable": true,
  "isOnline": true
}
```

**Response:** `200 OK`

---

### 7.4 Update Location
**POST** `/partners/location`

**Headers:** Authorization required

**Request:**
```json
{
  "latitude": 19.0760,
  "longitude": 72.8777,
  "accuracy": 5.5,
  "speed": 25.5,
  "heading": 180.0
}
```

**Response:** `200 OK`

---

### 7.5 Get Assigned Pickups/Deliveries
**GET** `/partners/assignments`

**Headers:** Authorization required

**Query Parameters:**
- `type`: PICKUP or DELIVERY (optional)
- `status`: ASSIGNED, ACCEPTED, IN_TRANSIT, COMPLETED (optional)

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "orderId": 123,
      "orderNumber": "IM-2024-000123",
      "assignmentType": "PICKUP",
      "status": "ASSIGNED",
      "address": {
        "fullAddress": "123 Main Street, Mumbai - 400001",
        "latitude": 19.0760,
        "longitude": 72.8777,
        "landmark": "Near Central Park"
      },
      "customerName": "John Doe",
      "customerPhone": "9876543210",
      "scheduledTime": "2024-01-30T09:00:00Z",
      "distance": 2.5,
      "assignedAt": "2024-01-28T11:00:00Z"
    }
  ]
}
```

---

### 7.6 Accept Assignment
**PUT** `/assignments/{id}/accept`

**Headers:** Authorization required

**Response:** `200 OK`

---

### 7.7 Reject Assignment
**PUT** `/assignments/{id}/reject`

**Headers:** Authorization required

**Request:**
```json
{
  "reason": "Too far from current location"
}
```

**Response:** `200 OK`

---

### 7.8 Update Assignment Status
**PUT** `/assignments/{id}/status`

**Headers:** Authorization required

**Request:**
```json
{
  "status": "IN_TRANSIT",
  "notes": "On the way",
  "latitude": 19.0760,
  "longitude": 72.8777
}
```

**Response:** `200 OK`

---

### 7.9 Complete Pickup/Delivery
**PUT** `/assignments/{id}/complete`

**Headers:** 
- Authorization required
- Content-Type: multipart/form-data

**Request:** FormData with:
- photo (required)
- signature (optional)
- notes (optional)

**Response:** `200 OK`

---

### 7.10 Get Earnings
**GET** `/partners/earnings`

**Headers:** Authorization required

**Query Parameters:**
- `period`: TODAY, WEEK, MONTH (default: TODAY)

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "period": "TODAY",
    "totalEarnings": 500.00,
    "completedAssignments": 10,
    "pickups": 5,
    "deliveries": 5,
    "breakdown": [
      {
        "date": "2024-01-30",
        "earnings": 500.00,
        "assignments": 10
      }
    ]
  }
}
```

---

## 8. Review APIs

### 8.1 Submit Review
**POST** `/reviews`

**Headers:** Authorization required

**Request:**
```json
{
  "orderId": 123,
  "serviceRating": 5,
  "qualityRating": 5,
  "deliveryRating": 4,
  "comment": "Excellent service!",
  "images": ["https://storage.url/review1.jpg"]
}
```

**Response:** `201 Created`

---

### 8.2 Get Order Reviews
**GET** `/orders/{orderId}/reviews`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "customerName": "John Doe",
      "serviceRating": 5,
      "qualityRating": 5,
      "deliveryRating": 4,
      "comment": "Excellent service!",
      "images": ["https://storage.url/review1.jpg"],
      "createdAt": "2024-02-02T10:00:00Z"
    }
  ]
}
```

---

## 9. Admin APIs

### 9.1 Dashboard Stats
**GET** `/admin/dashboard`

**Headers:** Authorization required (Admin role)

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "today": {
      "orders": 45,
      "revenue": 15000.00,
      "activePartners": 12,
      "pendingPickups": 8
    },
    "thisMonth": {
      "orders": 890,
      "revenue": 345000.00,
      "newCustomers": 67,
      "averageRating": 4.6
    },
    "recentOrders": [...],
    "topServices": [...]
  }
}
```

---

### 9.2 Get All Orders (Admin)
**GET** `/admin/orders`

**Headers:** Authorization required (Admin role)

**Query Parameters:**
- `status` (optional)
- `customerId` (optional)
- `partnerId` (optional)
- `startDate` (optional)
- `endDate` (optional)
- `page`, `size`, `sort`

**Response:** `200 OK` (Similar to customer orders but all orders)

---

### 9.3 Assign Delivery Partner
**POST** `/admin/orders/{orderId}/assign-partner`

**Headers:** Authorization required (Admin role)

**Request:**
```json
{
  "partnerId": 5,
  "assignmentType": "PICKUP"
}
```

**Response:** `200 OK`

---

### 9.4 Update Order Status
**PUT** `/admin/orders/{id}/status`

**Headers:** Authorization required (Admin role)

**Request:**
```json
{
  "status": "PROCESSING",
  "notes": "Started processing"
}
```

**Response:** `200 OK`

---

### 9.5 Manage Services
**POST** `/admin/services`
**PUT** `/admin/services/{id}`
**DELETE** `/admin/services/{id}`

**Headers:** Authorization required (Admin role)

---

### 9.6 Manage Delivery Partners
**GET** `/admin/partners`
**GET** `/admin/partners/{id}`
**PUT** `/admin/partners/{id}/approve`
**PUT** `/admin/partners/{id}/block`

**Headers:** Authorization required (Admin role)

---

### 9.7 Analytics
**GET** `/admin/analytics/revenue`
**GET** `/admin/analytics/orders`
**GET** `/admin/analytics/partners`
**GET** `/admin/analytics/customers`

**Headers:** Authorization required (Admin role)

---

## 10. Notification APIs

### 10.1 Get Notifications
**GET** `/notifications`

**Headers:** Authorization required

**Query Parameters:**
- `isRead` (optional): true/false
- `page`, `size`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Order Picked Up",
        "message": "Your order IM-2024-000123 has been picked up",
        "type": "ORDER_UPDATE",
        "isRead": false,
        "createdAt": "2024-01-30T09:30:00Z"
      }
    ],
    "unreadCount": 3,
    "totalElements": 10
  }
}
```

---

### 10.2 Mark as Read
**PUT** `/notifications/{id}/read`

**Headers:** Authorization required

**Response:** `200 OK`

---

### 10.3 Mark All as Read
**PUT** `/notifications/read-all`

**Headers:** Authorization required

**Response:** `200 OK`

---

## Error Response Format

All error responses follow this format:

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "phone",
        "message": "Phone number is required"
      }
    ]
  },
  "timestamp": "2024-01-28T10:30:00Z",
  "path": "/api/v1/auth/register"
}
```

## HTTP Status Codes

- `200 OK`: Success
- `201 Created`: Resource created successfully
- `204 No Content`: Success with no response body
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource already exists
- `422 Unprocessable Entity`: Validation error
- `500 Internal Server Error`: Server error

---

## Rate Limiting

- Standard endpoints: 100 requests/minute
- Auth endpoints: 10 requests/minute
- Location updates: 1 request/second

---

## Pagination

All list endpoints support pagination:

**Query Parameters:**
- `page`: Page number (0-indexed, default: 0)
- `size`: Items per page (default: 10, max: 100)
- `sort`: Sort field and direction (e.g., `createdAt,desc`)

**Response includes:**
```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 100,
    "totalPages": 10,
    "isFirst": true,
    "isLast": false
  }
}
```

---

## WebSocket APIs

### Real-time Location Updates
```
Connect: ws://localhost:8080/ws/location
Topic: /topic/partner/{partnerId}/location
```

### Order Status Updates
```
Connect: ws://localhost:8080/ws/orders
Topic: /topic/order/{orderId}/status
```

---

**Next Steps:**
1. Review API design
2. Implement controllers in Spring Boot
3. Add Swagger/OpenAPI documentation
4. Create Postman collection
5. Write API integration tests
