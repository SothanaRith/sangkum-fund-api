# Account Lockout Security Feature

## Overview
The account lockout feature protects against brute force attacks by temporarily locking user accounts after multiple failed login attempts.

## Configuration

Add these properties to your `application.properties`:

```properties
# Account Security Settings
security.max-login-attempts=5
security.lock-duration-minutes=30
```

### Settings Explained:
- **max-login-attempts**: Number of failed login attempts before account is locked (default: 5)
- **lock-duration-minutes**: Duration in minutes the account remains locked (default: 30)

## How It Works

### 1. Failed Login Tracking
- Every failed login attempt increments the user's `failedLoginAttempts` counter
- The counter is stored in the `users` table
- Users are notified of remaining attempts after each failed login

### 2. Account Locking
When the maximum attempts threshold is reached:
- Account status changes to `accountLocked = true`
- Lock timestamp is recorded in `lockTime`
- User receives message: "Your account has been locked due to too many failed login attempts"

### 3. Auto-Unlock
Accounts automatically unlock after the configured duration:
- System checks if `lockTime + duration` has passed
- No manual intervention needed for time-based unlocks
- Next login attempt will clear the lock if time has elapsed

### 4. Manual Unlock (Admin)
Administrators can manually manage locked accounts via API endpoints.

## API Endpoints

### Admin Account Management
All endpoints require `ADMIN` role authentication.

#### 1. Check Account Status
```http
GET /api/admin/accounts/{userId}/status
```

Response:
```json
{
  "userId": 1,
  "email": "user@example.com",
  "accountLocked": true,
  "failedLoginAttempts": 5,
  "lockTime": "2024-01-15T10:30:00",
  "canUnlock": true
}
```

#### 2. Unlock Account
```http
POST /api/admin/accounts/{userId}/unlock
```

Response:
```json
{
  "success": true,
  "message": "Account unlocked successfully"
}
```

#### 3. Lock Account (Manual)
```http
POST /api/admin/accounts/{userId}/lock
```

Response:
```json
{
  "success": true,
  "message": "Account locked successfully"
}
```

#### 4. Reset Failed Attempts
```http
POST /api/admin/accounts/{userId}/reset-attempts
```

Response:
```json
{
  "success": true,
  "message": "Failed login attempts reset successfully"
}
```

## Database Schema

The `users` table includes these security-related columns:

```sql
ALTER TABLE users ADD COLUMN failed_login_attempts INT DEFAULT 0;
ALTER TABLE users ADD COLUMN account_locked BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN lock_time TIMESTAMP NULL;
```

## User Flow Examples

### Scenario 1: Failed Login Attempts
1. User enters wrong password (Attempt 1)
   - Response: "Invalid credentials. You have 4 attempts remaining."
2. User enters wrong password again (Attempt 2)
   - Response: "Invalid credentials. You have 3 attempts remaining."
3. ...continues until 5 attempts
4. After 5th failed attempt:
   - Response: "Your account has been locked due to too many failed login attempts. Please try again after 30 minutes."
   - Account status: `accountLocked = true`

### Scenario 2: Successful Login Resets Counter
1. User has 3 failed attempts
2. User enters correct password
3. Failed attempts counter resets to 0
4. User logs in successfully

### Scenario 3: Auto-Unlock After Time
1. Account locked at 10:00 AM
2. User tries to login at 10:35 AM (35 minutes later)
3. System detects lock duration has passed
4. Login proceeds normally
5. Failed attempts reset to 0

### Scenario 4: Admin Manual Unlock
1. User's account is locked
2. User contacts support
3. Admin uses unlock endpoint
4. User can login immediately
5. Failed attempts reset to 0

## Security Best Practices

### For Production:
1. **Email Notifications**: Send email to user when account is locked
2. **IP Tracking**: Consider tracking IP addresses for additional security
3. **Audit Logging**: Log all lock/unlock events for security audits
4. **Rate Limiting**: Combine with rate limiting for API endpoints
5. **CAPTCHA**: Add CAPTCHA after 2-3 failed attempts

### Monitoring:
- Track number of locked accounts daily
- Alert on suspicious patterns (many locks from same IP)
- Review admin unlock activity regularly

## Error Messages

### User-Facing Messages:
- **During Attempts**: "Invalid credentials. You have X attempts remaining."
- **Account Locked**: "Your account has been locked due to too many failed login attempts. Please try again after [X] minutes."
- **Permanent Lock**: "Your account has been locked. Please contact support."

### API Error Responses:
```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Your account has been locked due to too many failed login attempts. Please try again after 30 minutes.",
  "path": "/api/auth/login"
}
```

## Integration Points

### Services:
- **AccountLockService**: Core locking logic
- **AuthService**: Integrates lock checks in login methods
- **EmailService**: Can be extended to send lock notifications

### Security Filters:
The feature integrates with:
- JWT authentication filter
- OTP verification flow
- OAuth2 login flow

## Frontend Integration

The frontend login pages now display:
- Remaining attempts after failed login
- Lock status with countdown timer
- Enhanced error messages with lock icon (🔒)
- Support contact information for locked accounts

## Testing

### Manual Testing Steps:
1. Attempt login with wrong password 5 times
2. Verify account is locked
3. Wait 30 minutes (or adjust lock duration)
4. Verify account automatically unlocks
5. Test admin unlock endpoint
6. Verify successful login resets counter

### Automated Tests:
Consider adding tests for:
- Failed attempt counter increments correctly
- Account locks at threshold
- Auto-unlock after duration
- Admin unlock functionality
- Counter resets on successful login

## Troubleshooting

### Common Issues:

**Issue**: Account won't unlock after time
- **Solution**: Check server timezone settings match database timezone

**Issue**: Counter not resetting
- **Solution**: Verify AuthService properly calls `resetFailedAttempts()`

**Issue**: Admin can't unlock account
- **Solution**: Verify admin user has `ROLE_ADMIN` authority

### Debug Queries:
```sql
-- View locked accounts
SELECT id, email, failed_login_attempts, account_locked, lock_time 
FROM users 
WHERE account_locked = true;

-- Check specific user status
SELECT id, email, failed_login_attempts, account_locked, lock_time,
       TIMESTAMPDIFF(MINUTE, lock_time, NOW()) as minutes_locked
FROM users 
WHERE email = 'user@example.com';

-- Reset specific account manually
UPDATE users 
SET account_locked = false, failed_login_attempts = 0, lock_time = NULL 
WHERE email = 'user@example.com';
```

## Future Enhancements

Consider adding:
1. Progressive delays (increase wait time after each lock)
2. Email notifications on lock/unlock
3. SMS verification for unlocking
4. Whitelist trusted IPs (skip locking for certain IPs)
5. Account recovery workflow
6. Two-factor authentication requirement after unlock
7. Admin dashboard for account security monitoring
