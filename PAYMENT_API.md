# Donation Payment API

## Payment Methods Supported

1. **VISA_CARD** - Instant credit/debit card payment
2. **KHQR** - Cambodia's Bakong KHQR payment system
3. **OFFLINE_QR** - Offline QR code payment (requires admin verification)

## Make a Donation

**Endpoint:** `POST /api/donations`

### Request Body Examples

#### 1. VISA Card Payment
```json
{
  "eventId": 1,
  "amount": 50.00,
  "anonymous": false,
  "paymentMethod": "VISA_CARD",
  "paymentDetails": {
    "cardNumber": "4111111111111111",
    "cardHolderName": "John Doe",
    "expiryDate": "12/25",
    "cvv": "123"
  }
}
```

#### 2. KHQR Payment
```json
{
  "eventId": 1,
  "amount": 50.00,
  "anonymous": false,
  "paymentMethod": "KHQR",
  "paymentDetails": {
    "phoneNumber": "+855123456789"
  }
}
```

#### 3. Offline QR Payment
```json
{
  "eventId": 1,
  "amount": 50.00,
  "anonymous": true,
  "paymentMethod": "OFFLINE_QR",
  "paymentDetails": {
    "notes": "Payment via bank transfer"
  }
}
```

### Response Example

#### VISA Card (Instant Success)
```json
{
  "donationId": 123,
  "status": "SUCCESS",
  "paymentMethod": "VISA_CARD",
  "transactionRef": "TXN-A1B2C3D4",
  "qrCodeData": null,
  "message": "Payment processed successfully via Visa card"
}
```

#### KHQR Payment (Pending)
```json
{
  "donationId": 124,
  "status": "PENDING",
  "paymentMethod": "KHQR",
  "transactionRef": "KHQR-TXN-E5F6G7H8",
  "qrCodeData": "PAYMENT_METHOD:KHQR|AMOUNT:50.00|TXN_REF:KHQR-TXN-E5F6G7H8|PHONE:+855123456789|TIMESTAMP:1704358800000",
  "message": "Please scan the KHQR code to complete payment"
}
```

#### Offline QR (Pending)
```json
{
  "donationId": 125,
  "status": "PENDING",
  "paymentMethod": "OFFLINE_QR",
  "transactionRef": "OFFLINE-TXN-I9J0K1L2",
  "qrCodeData": "PAYMENT_METHOD:OFFLINE_QR|AMOUNT:50.00|TXN_REF:OFFLINE-TXN-I9J0K1L2|TIMESTAMP:1704358900000",
  "message": "Please scan the QR code and complete payment offline. Payment will be verified by admin."
}
```

## Payment Status Flow

### VISA_CARD
1. Submit payment → Instant SUCCESS
2. Amount immediately added to event total

### KHQR
1. Submit payment → PENDING status
2. Receive QR code data → User scans KHQR
3. Admin/System confirms payment → SUCCESS
4. Amount added to event total

### OFFLINE_QR
1. Submit payment → PENDING status
2. Receive QR code → User completes payment offline
3. Admin verifies and confirms → SUCCESS
4. Amount added to event total

## Confirm Payment

**Endpoint:** `POST /api/donations/{id}/confirm`

Use this endpoint to confirm KHQR or OFFLINE_QR payments after verification.

## Get Donation Details

**Endpoint:** `GET /api/donations/{id}`

Returns donation details including payment status.

## Notes

- **VISA_CARD**: Currently simulated. Integrate with actual payment gateway (Stripe, PayPal, etc.) for production.
- **KHQR**: Integrate with Cambodia's Bakong KHQR API for production.
- **OFFLINE_QR**: The QR code data can be converted to actual QR image using a QR code library.
- All payments require authentication (Bearer token in Authorization header).
- Anonymous donations hide donor information but still process payment.
