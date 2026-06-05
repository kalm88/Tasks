# Punchlist App — Visual Studio Setup Guide

## Prerequisites

| Tool | Version |
|------|---------|
| Visual Studio Community 2026 | with .NET MAUI workload |
| .NET SDK | 9.0 |
| Android SDK | API 26+ |

## Firebase Setup (required before first build)

1. Go to [Firebase Console](https://console.firebase.google.com) → New project → **PunchlistApp**
2. Enable **Email/Password** auth (Authentication → Sign-in method)
3. Create **Firestore Database** (start in test mode, then add rules)
4. Enable **Firebase Storage**
5. Add Android app with package name `com.punchlist.app`
6. Download `google-services.json` → place at `Platforms/Android/google-services.json`
7. (iOS) Add iOS app, download `GoogleService-Info.plist` → place at `Platforms/iOS/GoogleService-Info.plist`

## Firestore Security Rules

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
    match /projects/{projectId} {
      allow read, write: if request.auth != null;
      match /punchItems/{itemId} {
        allow read, write: if request.auth != null;
        match /comments/{commentId} {
          allow read, write: if request.auth != null;
        }
      }
    }
  }
}
```

## Open in Visual Studio

1. Open `PunchlistApp.sln` (in the root `Tasks/` folder)
2. Let NuGet restore packages
3. Set target to **Android Emulator** or a connected device
4. Press **F5** to build and run

## Project Structure

```
PunchlistApp/
├── Models/          — Domain models (PunchItem, Project, Comment, Priority, Status)
├── Services/        — Firebase wrappers (Auth, Firestore, Storage, PrintShare)
├── ViewModels/      — MVVM ViewModels (CommunityToolkit.Mvvm)
├── Views/           — XAML pages + code-behind
├── Converters/      — IValueConverter implementations
├── Resources/
│   ├── Styles/      — Colors.xaml, Styles.xaml
│   ├── Fonts/
│   └── Images/
└── Platforms/
    ├── Android/     — AndroidManifest.xml, MainActivity, MainApplication
    └── Windows/     — WinUI App entry point
```

## Features

- **Email/Password auth** — login + register
- **Projects** — create and list jobsite projects
- **Punchlist items** — title, issue description, work required, location, priority, status, assignee
- **Camera** — take photos attached to items (MediaPicker)
- **Barcode scanning** — scan UPC/EAN/QR/Code128 SKUs with ZXing.Net.MAUI
- **Comments** — threaded comments per item
- **PDF generation** — generates a US-Letter PDF report (PdfSharpCore)
- **Share PDF** — system share sheet (email, AirDrop, Drive, etc.)
- **Email share** — pre-filled mailto: with item details
- **Status workflow** — Open → In Progress → Needs Review → Complete
