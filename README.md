# 💬 Chat App

A secure, end-to-end encrypted desktop chat application built with JavaFX. The app supports user registration with OTP verification, encrypted messaging using public/private key cryptography, friend management, and account settings — all through a clean multi-panel UI.

---
## Screenshot
<img src='/dashboard.png'>
---

## 📋 Table of Contents

- [Features](#features)
- [UI Sections](#ui-sections)
- [Architecture & Function Flow](#architecture--function-flow)
  - [SignUp Flow](#signup-flow)
  - [Login Flow](#login-flow)
  - [Forgot Password Flow](#forgot-password-flow)
  - [Main Chat Flow](#main-chat-flow)
- [Encryption & Security](#encryption--security)
- [Database Operations](#database-operations)
- [Friend List Management](#friend-list-management)
- [Message Send & Receive](#message-send--receive)
- [Settings](#settings)
- [Project Structure (UI Tree)](#project-structure-ui-tree)

---

## ✨ Features

- **OTP-based Registration** — Mobile number verified via OTP before account creation
- **Secure Login** — Password hashing with bcrypt/SHA; local JKS key store
- **End-to-End Encrypted Messaging** — RSA public/private key encryption per user
- **Forgot Password** — OTP-verified password reset flow
- **Friend List** — Add and remove friends with real-time chat list updates
- **Account Settings** — Change display name, password, and profile photo; delete account
- **Multi-panel JavaFX UI** — Split-pane layout with left sidebar, chat area, and right info panel

---

## 🖥️ UI Sections

### 1. SignUp
Dual-pane layout (`HBox` root) with a decorative left panel and a right form panel containing:
- Name, Mobile, Password, Confirm Password fields
- Validation warnings
- Navigate to Login button

### 2. SignUp OTP Verification
After registration form submission, a 4-digit OTP panel is shown:
- Countdown timer label
- 4 individual `TextField` inputs for OTP digits
- Verify / Resend options

### 3. Login
Standard login form:
- Mobile number and Password fields
- Forgot Password link
- Navigate to SignUp button

### 4. Forgot Password
Three-step sub-flow:
1. **Mobile Input** — Enter registered mobile number
2. **OTP Input** — 4-digit OTP with countdown timer
3. **New Password Input** — Enter and confirm new password

### 5. Main Chat
Three-column layout:
- **Left column** — Profile picture, username, search bar, scrollable chat person list
- **Middle column** — Active chat area (scrollable message bubbles), message text area, send button, attach/emoji controls
- **Right column** — Info panel, encryption label, options menu

### 6. Settings
Settings menu with four options:
- Change Account Name
- Change Account Password
- Change Profile Photo
- Delete Account

---

## ⚙️ Architecture & Function Flow

### SignUp Flow

```
User fills Name, Mobile, Password, Confirm Password
        │
        ▼
Validation
  ├─ All fields non-empty
  ├─ Mobile ≥ 10 digits
  └─ Password length > 5 & both password fields match
        │
        ▼
OTP Generate Function(mobile) → Response Code
        │
        ▼
Is Mobile already in Database?
  ├─ YES → Show warning
  └─ NO  → Send OTP
              │
              ▼
        User enters OTP
              │
              ▼
        Get Verification ID(JSON) → Verification ID
              │
              ▼
        Verify OTP(mobile, OTP, verificationID) → Response Code 200
              │
              ▼
        JKS File Generate(password) → 1 or 0
        Setup Code Function → Unlock JKS, store private key
        Load Public Key → Public Key
        Convert Public Key to Base64 → Modified Public Key
        Hash Password(password) → Hash
        Data Insert in DB(name, mobile, verified, hash, modifiedPublicKey)
        Create JSON File + Create TXT File
              │
              ▼
        Load Login Scene
```

### Login Flow

```
User enters Mobile + Password
        │
        ▼
Validation
  ├─ All fields non-empty
  └─ Mobile ≥ 10 digits
        │
        ▼
Data Read Function(mobile, password)
  └─ Stores Hash from DB
        │
        ▼
Setup Code Function → Unlock JKS, store private key
Check Password Function(plainPassword, hash) → true / false
        │
        ▼
Load Main Scene
```

### Forgot Password Flow

```
User enters registered Mobile
        │
        ▼
Validation (non-empty, ≥ 10 digits)
        │
        ▼
Data Exist Function(mobile) → 1 or 0
        │
        ▼
OTP Generate Function(mobile) → Response Code
        │
        ▼
Get Verification ID(JSON) → Verification ID
        │
        ▼
User enters OTP
        │
        ▼
Verify OTP(mobile, OTP, verificationID) → Response Code 200
        │
        ▼
User enters New Password + Confirm Password
Validation (non-empty, length > 5, both fields match)
        │
        ▼
Hash Password(password) → Hash
Database Update Password(hash, mobile) → 1 or 0
Delete old JKS File(filePath) → 1 or 0
JKS File Generate(password) → 1 or 0
Setup Code Function → Unlock JKS, store private key
Load Public Key → Public Key
Convert Public Key to Base64 → Modified Public Key
In Database Update Public Key(modifiedKey, mobile) → 1 or 0
        │
        ▼
Load Login Scene
```

### Main Chat Flow

```
Compile Time:
JSON Read Function → ArrayNode
Chat Person Function(vbox, resultset, vbox, arraylist, hbox, contextMenuItem)
  └─ Add Person Function(vbox, name, id, url, arraylist, hbox, contextMenuItem, runtimeFlag)
  └─ Update ChatList Function(root, name, mobile, runtimeFlag)

Runtime:
Main Function(Stage) → Scene
```

---

## 🔐 Encryption & Security

| Function | Input | Output |
|---|---|---|
| `Hash Password` | Plain password | Hashed password |
| `Check Password` | Plain password + Hash | true / false |
| `JKS File Generate` | Password | 1 (success) / 0 (failure) |
| `Setup Code` | — | Unlocks JKS, stores private key |
| `Load Public Key` | — | Public key |
| `Convert Public Key to Base64` | Public key | Modified (Base64) public key |
| `Encrypt` | Message + receiver's modified public key | Encrypted message |
| `Decrypt` | Encrypted message + private key | Original message |

All messages are encrypted with the **receiver's public key** before sending, and decrypted by the **receiver's private key** — ensuring true end-to-end encryption.

---

## 🗄️ Database Operations

| Function | Input | Output |
|---|---|---|
| `Data Insert in Database` | name, mobile, verified, password, modifiedPublicKey | 1 / 0 |
| `Data Read Function` | mobile, password | Stored Hash |
| `Data Exist Function` | mobile | 1 / 0 |
| `Database Update Password` | hash, mobile | 1 / 0 |
| `In Database Update Public Key` | modifiedKey, mobile | 1 / 0 |

---

## 👥 Friend List Management

### Adding a Friend
Event listener on the chat person list triggers:
```
Chat Person Function → Add Person Function → Update ChatList Function
```

### Removing a Friend
User clicks the three-dot menu on a contact → selects Delete:
```
Three Dot Delete Option → Event Listener
  └─ Chat Person Delete Function(vbox, mobile, hbox)
  └─ Remove Person Function(mobile)
  └─ Update File (remove from local storage)
  └─ Removing Checkmark in UI
```

---

## 📨 Message Send & Receive

### Sending a Message
```
User clicks Send button
        │
        ▼
Encrypt Function(message, receiverModifiedPublicKey) → Encrypted Message
        │
        ▼
Send Msg Function(mobile, message, publicKey) → Encoded message
        │
        ▼
Sender Mail Function(dataOutputStream, message, socket)
        │
        ▼
Send Message GUI Function(vbox, message, encryption, isText)
  └─ Renders outgoing message bubble in chat area
```

### Receiving a Message
```
Receiving Thread (listens to Server)
        │
        ▼
Message Route Function(originalMessage, encryptedMessage, isTextMsg)
  └─ Decrypt Function(encryptedMsg, privateKey) → Original Message
        │
        ▼
Receiver Message Function(vbox, message, img, scrollPane, encryptionMsg, isTextMsg)
  └─ Renders incoming message bubble in chat area
```

---

## ⚙️ Settings

| Setting | UI Components |
|---|---|
| Change Account Name | `heading` label, `currentName` label, `tf1` TextField, `saveBtn` |
| Change Account Password | `heading` label, `pf1/pf2/pf3` PasswordFields, `warningText`, `saveBtn` |
| Change Profile Photo | Triggered from settings menu |
| Delete Account | Triggered from settings menu |

---

## 🗂️ Project Structure (UI Tree)

```
SignUp
└── HBox (root)
    ├── VBox (left)
    └── VBox (right)
        ├── Labels, TextFields, PasswordFields
        └── Buttons (signUp, signIn)

SignUp OTP
└── HBox (root)
    ├── VBox (left)
    └── VBox (right)
        └── BorderPane (otpParent)
            └── VBox (box)
                ├── Labels (verifyLabel, timeLabel, infoText)
                ├── HBox (otpBox) → 4x TextFields
                └── Button (verifyBtn) / Label (resendLabel)

Login
└── HBox (root)
    ├── VBox (left)
    └── VBox (right)
        ├── Labels, TextFields
        ├── Label (forgotPassword)
        └── Buttons (signIn, signUp)

Main Chat
└── HBox (root)
    ├── VBox (vboxLeft)
    │   ├── VBox (imgContainer)
    │   ├── VBox (profileNameBox) → Label (profileName)
    │   ├── HBox (i1, i2, i3)
    │   └── VBox (searchbarBox) → TextField (searchBar)
    ├── VBox (vboxMid)
    │   ├── ScrollPane (scrollPane2)
    │   ├── VBox (midLowerBox) → VBox (chatPerson)
    │   ├── StackPane (personName)
    │   │   ├── Rectangle
    │   │   └── Label (nameLabel, encryptionLabel)
    │   ├── ScrollPane (scrollPane1)
    │   │   └── VBox (chatArea) → HBox (msgSide) → StackPane (bubble) → Label
    │   └── HBox (msgBox)
    │       ├── TextArea (msg)
    │       ├── Button (sendBtn)
    │       ├── BorderPane (attachBox)
    │       └── BorderPane (emojiBox)
    └── VBox (vboxRight)
        └── VBox (menuContainer)

Settings
└── HBox (root)
    └── VBox (settingMid)
        ├── VBox (accountNameBox)
        ├── VBox (passwordChangeBox)
        ├── VBox (photoChangeBox)
        └── VBox (deleteAccountBox)
```

---

## 📄 License

This project is for educational/personal use. See `LICENSE` for details.
