-- Indexes for Order table (most queried)
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_order_number ON orders(order_number);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_payment_status ON orders(payment_status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_orders_pickup_date ON orders(pickup_date);

-- Indexes for User table
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Indexes for Delivery Partner table
CREATE INDEX IF NOT EXISTS idx_partners_status ON delivery_partners(status);
CREATE INDEX IF NOT EXISTS idx_partners_available ON delivery_partners(is_available);
CREATE INDEX IF NOT EXISTS idx_partners_user_id ON delivery_partners(user_id);

-- Indexes for Assignment table
CREATE INDEX IF NOT EXISTS idx_assignments_order_id ON assignments(order_id);
CREATE INDEX IF NOT EXISTS idx_assignments_partner_id ON assignments(partner_id);
CREATE INDEX IF NOT EXISTS idx_assignments_status ON assignments(status);

-- Indexes for Payment table
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_razorpay_order_id ON payments(razorpay_order_id);

-- Indexes for Coupon table
CREATE INDEX IF NOT EXISTS idx_coupons_code ON coupons(code);
CREATE INDEX IF NOT EXISTS idx_coupons_active ON coupons(is_active);

-- Indexes for Review table
CREATE INDEX IF NOT EXISTS idx_reviews_order_id ON reviews(order_id);
CREATE INDEX IF NOT EXISTS idx_reviews_customer_id ON reviews(customer_id);
CREATE INDEX IF NOT EXISTS idx_reviews_partner_id ON reviews(partner_id);

-- Indexes for Notification table
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_read ON notifications(is_read);

-- Indexes for SMS Logs table
CREATE INDEX IF NOT EXISTS idx_sms_logs_phone ON sms_logs(phone_number);
CREATE INDEX IF NOT EXISTS idx_sms_logs_order_id ON sms_logs(order_id);

-- Indexes for Order Status History
CREATE INDEX IF NOT EXISTS idx_status_history_order_id ON order_status_history(order_id);
CREATE INDEX IF NOT EXISTS idx_status_history_changed_at ON order_status_history(changed_at);

-- Composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_orders_customer_status ON orders(customer_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_status_created ON orders(status, created_at);