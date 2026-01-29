# IronMan - Credentials & Configuration

**⚠️ IMPORTANT: Keep this file PRIVATE! Add to .gitignore!**

---

## Firebase Configuration

```javascript
const firebaseConfig = {
  apiKey: "AIzaSyDd6uirlJZuKnBwGhop2OQIgUzfWOx3qrw",
  authDomain: "ironman-7f027.firebaseapp.com",
  projectId: "ironman-7f027",
  storageBucket: "ironman-7f027.firebasestorage.app",
  messagingSenderId: "702940926826",
  appId: "1:702940926826:web:6a40e4b39374eeb4574b13"
};
```

**Firebase Service Account JSON:**
- Location: `backend/src/main/resources/firebase-service-account.json`
- Download from: Firebase Console → Project Settings → Service Accounts → Generate Private Key

---

## Razorpay (Payment Gateway)

**Test Mode Keys:**
```
Key ID: [Your Razorpay Key ID - starts with rzp_test_]
Key Secret: [Your Razorpay Key Secret]
```

**Get Keys From:** https://dashboard.razorpay.com/app/keys

**Note:** Use Test Mode during development. Switch to Live Mode only when deploying to production.

---

## Database (PostgreSQL)

**Local Development:**
```
Host: localhost
Port: 5432
Database: ironman_db
Username: postgres
Password: [Your PostgreSQL password]

JDBC URL: jdbc:postgresql://localhost:5432/ironman_db
```

**Production (After Deployment):**
```
Host: [Railway/AWS RDS host]
Port: 5432
Database: ironman_db
Username: [Provided by hosting]
Password: [Provided by hosting]
Connection String: [Full connection URL]
```

---

## Redis (Cache & Sessions)

**Local Development:**
```
Host: localhost
Port: 6379
Password: (none - no password for local)
Connection URL: redis://localhost:6379
```

**Production:**
```
Host: [Railway/Cloud provider]
Port: 6379
Password: [Provided by hosting]
Connection URL: redis://:[password]@[host]:[port]
```

---

## Maps API

**Provider:** OpenStreetMap (No account needed!)

**Services:**
- **Map Tiles:** OpenStreetMap (https://tile.openstreetmap.org/{z}/{x}/{y}.png)
- **Geocoding:** Nominatim API (https://nominatim.openstreetmap.org/)
- **Routing:** OSRM (http://router.project-osrm.org/)

**Note:** No API key required. Free for reasonable use.

**Alternative (If needed):**
If you decide to use Mapbox later:
```
Access Token: [Your Mapbox token - starts with pk.]
```

---

## Email Service (Gmail SMTP)

**Configuration:**
```
Host: smtp.gmail.com
Port: 587
Username: [Your Gmail address]
Password: [Gmail App Password - NOT your regular password!]
```

**How to get Gmail App Password:**
1. Go to: https://myaccount.google.com/security
2. Enable 2-Step Verification
3. Go to: https://myaccount.google.com/apppasswords
4. Create new app password for "Mail"
5. Copy the 16-character password

---

## File Storage

**Option 1: Cloudinary (Recommended - Easier)**
```
Cloud Name: [Your Cloudinary cloud name]
API Key: [Your API key]
API Secret: [Your API secret]
```

**Sign up:** https://cloudinary.com/users/register/free

**Option 2: AWS S3**
```
Access Key ID: [Your AWS access key]
Secret Access Key: [Your AWS secret key]
Bucket Name: ironman-files
Region: ap-south-1 (Mumbai)
```

---

## JWT Secret

**Development:**
```
JWT_SECRET: your-super-secret-256-bit-key-for-development-only-change-in-production
```

**Production:**
```
JWT_SECRET: [Generate a strong 256-bit random key]
```

**Generate strong key:**
```bash
# Using Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"

# Using OpenSSL
openssl rand -hex 32
```

---

## Environment Variables Files

### Backend (.env or application.yml)

```yaml
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ironman_db
DB_USERNAME=postgres
DB_PASSWORD=[your_password]

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT
JWT_SECRET=[your_jwt_secret]
JWT_EXPIRATION=86400000

# Razorpay
RAZORPAY_KEY_ID=[your_key_id]
RAZORPAY_KEY_SECRET=[your_key_secret]

# Firebase
FIREBASE_SERVICE_ACCOUNT_PATH=classpath:firebase-service-account.json

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=[your_email@gmail.com]
MAIL_PASSWORD=[your_app_password]

# Storage (Choose one)
# Cloudinary
CLOUDINARY_CLOUD_NAME=[your_cloud_name]
CLOUDINARY_API_KEY=[your_api_key]
CLOUDINARY_API_SECRET=[your_api_secret]

# OR AWS S3
AWS_ACCESS_KEY=[your_access_key]
AWS_SECRET_KEY=[your_secret_key]
AWS_S3_BUCKET=ironman-files
AWS_REGION=ap-south-1
```

### Frontend (.env)

```env
# API
VITE_API_BASE_URL=http://localhost:8080/api/v1

# Razorpay
VITE_RAZORPAY_KEY_ID=[your_razorpay_key_id]

# Firebase
VITE_FIREBASE_API_KEY=AIzaSyDd6uirlJZuKnBwGhop2OQIgUzfWOx3qrw
VITE_FIREBASE_AUTH_DOMAIN=ironman-7f027.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=ironman-7f027
VITE_FIREBASE_STORAGE_BUCKET=ironman-7f027.firebasestorage.app
VITE_FIREBASE_MESSAGING_SENDER_ID=702940926826
VITE_FIREBASE_APP_ID=1:702940926826:web:6a40e4b39374eeb4574b13

# Maps (Optional - only if using Mapbox)
VITE_MAPBOX_ACCESS_TOKEN=[your_mapbox_token]
```

---

## Deployment Credentials (Add when deploying)

### Railway (Recommended for backend + database)

**Account:** [Your Railway account email]
**Projects:**
- Backend: [Project URL]
- Database: [Connection string provided by Railway]

### Vercel/Netlify (For frontend)

**Account:** [Your Vercel/Netlify account]
**Project:** [Your project URL]
**Environment Variables:** Set all VITE_* variables in dashboard

---

## GitHub Repository

**Repository:** https://github.com/[YourUsername]/ironman-app

**Access:**
- Personal Access Token: [Your GitHub PAT]
- SSH Key: [If using SSH]

---

## Security Checklist

**Before Deployment:**
- [ ] Change all default passwords
- [ ] Generate new JWT secret (256-bit)
- [ ] Use environment variables, not hardcoded values
- [ ] Add .env files to .gitignore
- [ ] Enable CORS only for your domains
- [ ] Use HTTPS in production
- [ ] Enable rate limiting
- [ ] Review all API permissions

**In Production:**
- [ ] Use strong database password
- [ ] Enable Redis password
- [ ] Restrict database access to specific IPs
- [ ] Use separate keys for test/live Razorpay
- [ ] Monitor API usage
- [ ] Set up alerts for unusual activity
- [ ] Regular security audits

---

## Quick Reference - What to Copy Where

**When setting up backend:**
1. Copy Razorpay keys to `application.yml`
2. Copy database password to `application.yml`
3. Download Firebase JSON and place in `src/main/resources/`
4. Generate JWT secret and add to `application.yml`
5. Add Gmail credentials to `application.yml`

**When setting up frontend:**
1. Copy Firebase config to `.env` (with VITE_ prefix)
2. Copy Razorpay Key ID to `.env`
3. Set API base URL in `.env`

**Don't forget:**
- Add `.env` to `.gitignore`
- Never commit secrets to GitHub
- Use different keys for development and production

---

## Troubleshooting

**Can't connect to database:**
- Check PostgreSQL is running: `pg_isready`
- Verify password is correct
- Check port 5432 is not blocked

**Can't connect to Redis:**
- Check Redis is running: `redis-cli ping`
- Verify connection string
- Check port 6379 is not blocked

**Razorpay not working:**
- Verify using Test Mode keys (rzp_test_)
- Check keys are correctly copied
- Ensure no extra spaces in credentials

**Firebase errors:**
- Verify service account JSON is in correct location
- Check file name matches configuration
- Ensure JSON file is valid (use a JSON validator)

---

## Notes

- All credentials above are for **DEVELOPMENT ONLY**
- Generate new credentials for **PRODUCTION**
- Never share this file publicly
- Keep backups of this file securely (encrypted)
- Update credentials when they expire or are compromised

---

**Last Updated:** [Date you fill this in]
**Next Review:** [Set a reminder to review/rotate credentials]
