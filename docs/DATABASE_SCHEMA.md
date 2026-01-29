# IronMan - Database Schema Design

## Overview
PostgreSQL database with 15+ tables to manage users, orders, services, delivery partners, and payments.

---

## Entity Relationship Diagram (ERD)

```
Users (1) ----< (M) Addresses
Users (1) ----< (M) Orders
Users (1) ----< (M) Reviews
Users (1) ----< (1) DeliveryPartners

Orders (1) ----< (M) OrderItems
Orders (1) ----< (M) OrderAddOns
Orders (1) ----< (M) Assignments
Orders (1) ----< (1) Payments
Orders (M) ----< (1) Addresses (pickup)
Orders (M) ----< (1) Addresses (delivery)

Services (1) ----< (M) OrderItems
ClothTypes (1) ----< (M) OrderItems
AddOns (1) ----< (M) OrderAddOns

DeliveryPartners (1) ----< (M) Assignments
DeliveryPartners (1) ----< (M) LocationTracking
DeliveryPartners (1) ----< (M) Reviews
```

---

## Table Definitions

### 1. users
User accounts for all user types (Customer, Delivery Partner, Admin)

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('CUSTOMER', 'DELIVERY_PARTNER', 'ADMIN', 'SHOP_STAFF')),
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    profile_image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
```

---

### 2. addresses
Customer delivery/pickup addresses with geolocation

```sql
CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label VARCHAR(50) NOT NULL, -- 'Home', 'Work', 'Other'
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    landmark VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_addresses_user_id ON addresses(user_id);
CREATE INDEX idx_addresses_location ON addresses(latitude, longitude);
```

---

### 3. services
Available laundry services

```sql
CREATE TABLE services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE, -- 'Washing', 'Ironing', 'Dry Cleaning', 'Shoe Cleaning'
    category VARCHAR(50) NOT NULL, -- 'LAUNDRY', 'SPECIALTY', 'REPAIR'
    base_price DECIMAL(10, 2) NOT NULL,
    description TEXT,
    estimated_hours INTEGER NOT NULL, -- Time to complete
    icon_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_services_category ON services(category);
```

**Initial Data:**
```sql
INSERT INTO services (name, category, base_price, estimated_hours) VALUES
('Washing', 'LAUNDRY', 20.00, 24),
('Ironing', 'LAUNDRY', 15.00, 12),
('Dry Cleaning', 'SPECIALTY', 100.00, 48),
('Shoe Cleaning', 'SPECIALTY', 75.00, 24),
('Steam Press', 'LAUNDRY', 25.00, 12);
```

---

### 4. cloth_types
Different types of clothing with pricing multipliers

```sql
CREATE TABLE cloth_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL, -- 'REGULAR', 'DELICATE', 'ETHNIC', 'FORMAL'
    price_multiplier DECIMAL(3, 2) DEFAULT 1.00, -- Multiply with base service price
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cloth_types_category ON cloth_types(category);
```

**Initial Data:**
```sql
INSERT INTO cloth_types (name, category, price_multiplier) VALUES
('Shirt', 'REGULAR', 1.00),
('T-Shirt', 'REGULAR', 0.80),
('Pants', 'REGULAR', 1.20),
('Jeans', 'REGULAR', 1.30),
('Saree', 'ETHNIC', 2.50),
('Lehenga', 'ETHNIC', 3.00),
('Sherwani', 'ETHNIC', 3.50),
('Suit', 'FORMAL', 2.80),
('Blazer', 'FORMAL', 2.50),
('Dress', 'REGULAR', 1.80),
('Dupatta', 'ETHNIC', 1.50),
('Kurta', 'ETHNIC', 1.60);
```

---

### 5. add_ons
Additional services customers can add

```sql
CREATE TABLE add_ons (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    is_per_item BOOLEAN DEFAULT TRUE, -- True: price per item, False: flat price
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Initial Data:**
```sql
INSERT INTO add_ons (name, description, price, is_per_item) VALUES
('Stain Removal', 'Advanced stain treatment', 20.00, TRUE),
('Express Service', 'Get clothes within 24 hours', 50.00, FALSE),
('Premium Packaging', 'Special packaging for events', 30.00, FALSE),
('Fabric Softener', 'Extra soft finish', 15.00, FALSE),
('Odor Removal', 'Special treatment for odors', 25.00, TRUE);
```

---

### 6. orders
Main order table

```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL, -- Format: IM-2024-001234
    customer_id BIGINT NOT NULL REFERENCES users(id),
    
    -- Status tracking
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    -- Status flow: PENDING → PICKUP_ASSIGNED → PICKED_UP → PROCESSING → 
    --              QUALITY_CHECK → READY_FOR_DELIVERY → OUT_FOR_DELIVERY → DELIVERED → COMPLETED
    -- Can also be: CANCELLED, REFUNDED
    
    -- Addresses
    pickup_address_id BIGINT NOT NULL REFERENCES addresses(id),
    delivery_address_id BIGINT NOT NULL REFERENCES addresses(id),
    
    -- Scheduling
    pickup_slot VARCHAR(20) NOT NULL, -- 'MORNING' (9AM-1PM), 'EVENING' (3PM-7PM)
    pickup_date DATE NOT NULL,
    expected_delivery_date DATE NOT NULL,
    actual_pickup_time TIMESTAMP,
    actual_delivery_time TIMESTAMP,
    
    -- Pricing
    subtotal DECIMAL(10, 2) NOT NULL,
    addon_charges DECIMAL(10, 2) DEFAULT 0.00,
    tax_amount DECIMAL(10, 2) DEFAULT 0.00,
    discount_amount DECIMAL(10, 2) DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,
    
    -- Payment
    payment_method VARCHAR(20), -- 'RAZORPAY', 'COD'
    payment_status VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'PAID', 'REFUNDED'
    
    -- Special instructions
    special_instructions TEXT,
    
    -- Tracking
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_orders_pickup_date ON orders(pickup_date);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
```

---

### 7. order_items
Individual items in an order

```sql
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    service_id BIGINT NOT NULL REFERENCES services(id),
    cloth_type_id BIGINT NOT NULL REFERENCES cloth_types(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL, -- service.base_price * cloth_type.price_multiplier
    line_total DECIMAL(10, 2) NOT NULL, -- unit_price * quantity
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_service_id ON order_items(service_id);
```

---

### 8. order_addons
Add-ons selected for an order

```sql
CREATE TABLE order_addons (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    addon_id BIGINT NOT NULL REFERENCES add_ons(id),
    quantity INTEGER DEFAULT 1,
    price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_addons_order_id ON order_addons(order_id);
```

---

### 9. delivery_partners
Delivery partner profiles

```sql
CREATE TABLE delivery_partners (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vehicle_type VARCHAR(30) NOT NULL, -- 'BIKE', 'SCOOTER', 'BICYCLE', 'VAN'
    vehicle_number VARCHAR(20) UNIQUE NOT NULL,
    license_number VARCHAR(30) UNIQUE,
    
    -- Current status
    is_available BOOLEAN DEFAULT FALSE,
    is_online BOOLEAN DEFAULT FALSE,
    current_latitude DECIMAL(10, 8),
    current_longitude DECIMAL(11, 8),
    last_location_update TIMESTAMP,
    
    -- Performance metrics
    rating DECIMAL(3, 2) DEFAULT 5.00 CHECK (rating >= 0 AND rating <= 5),
    total_deliveries INTEGER DEFAULT 0,
    successful_deliveries INTEGER DEFAULT 0,
    cancelled_deliveries INTEGER DEFAULT 0,
    
    -- Documents
    id_proof_url VARCHAR(255),
    vehicle_rc_url VARCHAR(255),
    license_url VARCHAR(255),
    photo_url VARCHAR(255),
    
    -- Approval
    is_approved BOOLEAN DEFAULT FALSE,
    approved_at TIMESTAMP,
    approved_by BIGINT REFERENCES users(id),
    
    -- Tracking
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_delivery_partners_user_id ON delivery_partners(user_id);
CREATE INDEX idx_delivery_partners_availability ON delivery_partners(is_available, is_online);
CREATE INDEX idx_delivery_partners_location ON delivery_partners(current_latitude, current_longitude);
```

---

### 10. assignments
Pickup/Delivery assignments to delivery partners

```sql
CREATE TABLE assignments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    partner_id BIGINT NOT NULL REFERENCES delivery_partners(id),
    assignment_type VARCHAR(20) NOT NULL, -- 'PICKUP', 'DELIVERY'
    status VARCHAR(30) NOT NULL DEFAULT 'ASSIGNED',
    -- Status flow: ASSIGNED → ACCEPTED → IN_TRANSIT → COMPLETED → FAILED
    
    -- Timing
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    
    -- Location tracking
    pickup_latitude DECIMAL(10, 8),
    pickup_longitude DECIMAL(11, 8),
    delivery_latitude DECIMAL(10, 8),
    delivery_longitude DECIMAL(11, 8),
    
    -- Proof
    pickup_photo_url VARCHAR(255),
    delivery_photo_url VARCHAR(255),
    signature_url VARCHAR(255),
    
    -- Notes
    notes TEXT,
    failure_reason TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_assignments_order_id ON assignments(order_id);
CREATE INDEX idx_assignments_partner_id ON assignments(partner_id);
CREATE INDEX idx_assignments_status ON assignments(status);
CREATE INDEX idx_assignments_type ON assignments(assignment_type);
```

---

### 11. location_tracking
Real-time location tracking for delivery partners

```sql
CREATE TABLE location_tracking (
    id BIGSERIAL PRIMARY KEY,
    partner_id BIGINT NOT NULL REFERENCES delivery_partners(id),
    assignment_id BIGINT REFERENCES assignments(id),
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    accuracy DECIMAL(10, 2), -- in meters
    speed DECIMAL(10, 2), -- km/h
    heading DECIMAL(5, 2), -- degrees (0-360)
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_location_tracking_partner_id ON location_tracking(partner_id);
CREATE INDEX idx_location_tracking_assignment_id ON location_tracking(assignment_id);
CREATE INDEX idx_location_tracking_recorded_at ON location_tracking(recorded_at);
```

---

### 12. payments
Payment transactions

```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL, -- 'RAZORPAY_UPI', 'RAZORPAY_CARD', 'RAZORPAY_NETBANKING', 'COD'
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- Status: PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED
    
    -- Razorpay integration
    razorpay_order_id VARCHAR(100),
    razorpay_payment_id VARCHAR(100),
    razorpay_signature VARCHAR(255),
    
    -- Metadata
    payment_details JSONB, -- Store additional payment info
    error_message TEXT,
    
    -- Refund info
    refund_amount DECIMAL(10, 2),
    refund_reason TEXT,
    refunded_at TIMESTAMP,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_razorpay_order_id ON payments(razorpay_order_id);
```

---

### 13. reviews
Customer reviews for orders and delivery partners

```sql
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    customer_id BIGINT NOT NULL REFERENCES users(id),
    partner_id BIGINT REFERENCES delivery_partners(id),
    
    -- Ratings (1-5)
    service_rating INTEGER NOT NULL CHECK (service_rating >= 1 AND service_rating <= 5),
    quality_rating INTEGER NOT NULL CHECK (quality_rating >= 1 AND quality_rating <= 5),
    delivery_rating INTEGER CHECK (delivery_rating >= 1 AND delivery_rating <= 5),
    
    -- Feedback
    comment TEXT,
    
    -- Media
    images JSONB, -- Array of image URLs
    
    -- Response
    admin_response TEXT,
    responded_at TIMESTAMP,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(order_id, customer_id) -- One review per order per customer
);

CREATE INDEX idx_reviews_order_id ON reviews(order_id);
CREATE INDEX idx_reviews_customer_id ON reviews(customer_id);
CREATE INDEX idx_reviews_partner_id ON reviews(partner_id);
```

---

### 14. notifications
User notifications

```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL, -- 'ORDER_UPDATE', 'PAYMENT', 'PROMOTION', 'SYSTEM'
    related_entity_type VARCHAR(50), -- 'ORDER', 'PAYMENT', 'ASSIGNMENT'
    related_entity_id BIGINT,
    
    -- Delivery
    is_read BOOLEAN DEFAULT FALSE,
    is_sent BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP,
    read_at TIMESTAMP,
    
    -- Channels
    push_sent BOOLEAN DEFAULT FALSE,
    email_sent BOOLEAN DEFAULT FALSE,
    sms_sent BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
```

---

### 15. slots
Available booking slots

```sql
CREATE TABLE slots (
    id BIGSERIAL PRIMARY KEY,
    slot_date DATE NOT NULL,
    slot_time VARCHAR(20) NOT NULL, -- 'MORNING', 'EVENING'
    max_capacity INTEGER NOT NULL DEFAULT 50,
    current_bookings INTEGER DEFAULT 0,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(slot_date, slot_time)
);

CREATE INDEX idx_slots_date ON slots(slot_date);
CREATE INDEX idx_slots_available ON slots(is_available);
```

---

## Database Functions & Triggers

### Auto-update timestamp trigger

```sql
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply to relevant tables
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_delivery_partners_updated_at BEFORE UPDATE ON delivery_partners
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_assignments_updated_at BEFORE UPDATE ON assignments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

### Generate order number

```sql
CREATE OR REPLACE FUNCTION generate_order_number()
RETURNS TRIGGER AS $$
BEGIN
    NEW.order_number := 'IM-' || TO_CHAR(CURRENT_DATE, 'YYYY') || '-' || 
                        LPAD(nextval('order_number_seq')::TEXT, 6, '0');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE SEQUENCE order_number_seq START 1;

CREATE TRIGGER set_order_number BEFORE INSERT ON orders
    FOR EACH ROW EXECUTE FUNCTION generate_order_number();
```

---

## Indexes for Performance

```sql
-- Composite indexes for common queries
CREATE INDEX idx_orders_customer_status ON orders(customer_id, status);
CREATE INDEX idx_orders_date_status ON orders(pickup_date, status);
CREATE INDEX idx_assignments_partner_status ON assignments(partner_id, status);
CREATE INDEX idx_delivery_partners_online ON delivery_partners(is_online, is_available, is_approved);
```

---

## Sample Queries

### Get active orders for a customer
```sql
SELECT o.*, a.address_line1 as pickup_address
FROM orders o
JOIN addresses a ON o.pickup_address_id = a.id
WHERE o.customer_id = ? AND o.status NOT IN ('COMPLETED', 'CANCELLED')
ORDER BY o.created_at DESC;
```

### Find nearest available delivery partners
```sql
SELECT dp.*, u.full_name, u.phone,
       (6371 * acos(cos(radians(?)) * cos(radians(dp.current_latitude)) * 
        cos(radians(dp.current_longitude) - radians(?)) + 
        sin(radians(?)) * sin(radians(dp.current_latitude)))) AS distance
FROM delivery_partners dp
JOIN users u ON dp.user_id = u.id
WHERE dp.is_available = TRUE AND dp.is_online = TRUE AND dp.is_approved = TRUE
ORDER BY distance
LIMIT 10;
```

### Calculate partner earnings
```sql
SELECT 
    dp.id,
    u.full_name,
    COUNT(a.id) as total_assignments,
    COUNT(CASE WHEN a.status = 'COMPLETED' THEN 1 END) as completed_assignments,
    SUM(CASE WHEN a.status = 'COMPLETED' THEN 50 ELSE 0 END) as total_earnings
FROM delivery_partners dp
JOIN users u ON dp.user_id = u.id
LEFT JOIN assignments a ON dp.id = a.partner_id
WHERE a.created_at >= DATE_TRUNC('month', CURRENT_DATE)
GROUP BY dp.id, u.full_name;
```

---

## Data Retention Policies

- **location_tracking**: Keep last 30 days, archive older data
- **notifications**: Keep last 90 days
- **Soft delete** orders: Mark as deleted but retain for 1 year
- **Audit logs**: Consider adding audit_logs table for compliance

---

This schema supports:
- ✅ Multi-user system (Customer, Partner, Admin, Shop)
- ✅ Complex order management with status tracking
- ✅ Real-time location tracking
- ✅ Payment integration
- ✅ Reviews and ratings
- ✅ Notification system
- ✅ Scalability for multi-shop expansion

---

**Next Steps:**
1. Review and approve schema
2. Create migration scripts
3. Set up test data
4. Build repository layer in Spring Boot
