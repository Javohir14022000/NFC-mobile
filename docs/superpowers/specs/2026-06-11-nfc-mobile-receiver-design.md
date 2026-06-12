# NFC Mobile Receiver — Design Spec
**Date:** 2026-06-11
**Status:** Approved
**Project:** NFCmobile (Telefon tomon)

## Overview

Telefon Android ilovasi. HCE (Host Card Emulation) orqali terminal APDU buyruqlarini qabul qiladi. Terminaldan kelgan `merchantId`, `terminalId`, `amount` ma'lumotlarini ekranda ko'rsatadi.

## Protocol

Custom APDU over HCE (ISO 14443-4):
- **AID:** `F0010203040506` (terminal bilan bir xil)
- SELECT AID → `9000 OK` javob beradi
- PUT DATA (JSON) → parse qilib UI ni yangilaydi → `9000 OK`

## Architecture

```
NFCmobile/
├── hce/
│   └── CardEmulationService.kt  ← HostApduService, APDU qabul + parse
├── model/
│   └── ReceivedData.kt          ← merchantId, terminalId, amount, receivedAt
├── ui/
│   └── ReceivedScreen.kt        ← qabul qilingan ma'lumot + vaqt
└── MainActivity.kt              ← HCE service boshlanishini tekshiradi
```

## Data Flow

```
[Terminal telefonni tekkizadi]
    → HostApduService.processCommandApdu() chaqiriladi
    → SELECT AID → 9000 OK javob
    → PUT DATA → JSON parse → ReceivedData
    → SharedFlow orqali MainActivity ga uzatiladi
    → ReceivedScreen yangilanadi
```

## HCE Service

`HostApduService` extend qilinadi:
- `processCommandApdu(apdu, extras)` — APDU baytlarini tahlil qiladi
- SELECT AID bo'lsa → `9000` qaytaradi
- PUT DATA bo'lsa → JSON parse, `SharedFlow.emit(data)` → `9000` qaytaradi

## UI

Bitta ekran, ikki holat:
1. **Kutish** — "Terminal ulanishini kutmoqda..."
2. **Ma'lumot** — qabul qilingan maydonlar + vaqt + "Tozalash" tugmasi

```
┌─────────────────────────┐
│  Qabul qilindi          │
├─────────────────────────┤
│ Merchant: M001          │
│ Terminal: T001          │
│ Summa:    50,000 UZS    │
│ Vaqt:     14:32:05      │
├─────────────────────────┤
│      [Tozalash]         │
└─────────────────────────┘
```

## AndroidManifest

```xml
<uses-permission android:name="android.permission.NFC" />
<uses-feature android:name="android.hardware.nfc" android:required="true" />

<service android:name=".hce.CardEmulationService"
    android:exported="true"
    android:permission="android.permission.BIND_NFC_SERVICE">
    <intent-filter>
        <action android:name="android.nfc.cardemulation.action.HOST_APDU_SERVICE"/>
    </intent-filter>
    <meta-data android:name="android.nfc.cardemulation.host_apdu_service"
        android:resource="@xml/apdu_service" />
</service>
```

## apdu_service.xml

```xml
<host-apdu-service>
    <aid-group category="other">
        <aid-filter android:name="F0010203040506"/>
    </aid-group>
</host-apdu-service>
```

## Error Handling

| Holat | Sabab | UI |
|-------|-------|-----|
| NFC o'chiq | adapter disabled | "NFC yoqing" |
| JSON parse xato | noto'g'ri format | "Noto'g'ri ma'lumot" |
| HCE qo'llab-quvvatlanmaydi | qurilma | "Qurilma HCE ni qo'llab-quvvatlamaydi" |
