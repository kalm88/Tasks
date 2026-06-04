# Android Punchlist App — Build Plan

## Overview
A Kotlin + Jetpack Compose Android app for jobsite/task issue tracking with photo capture, Firebase backend, and role-based access.

---

## Tech Stack
| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM + Clean Architecture (Use Cases) |
| DI | Hilt |
| Auth | Firebase Auth (email/password) |
| Database | Firestore (realtime) + Room (local cache) |
| Storage | Firebase Storage (photos) |
| Camera | CameraX |
| Images | Coil |
| Async | Coroutines + Flow |
| Background | WorkManager |
| Notifications | Firebase Cloud Messaging |

---

## Firestore Structure
```
users/{userId}
projects/{projectId}
  └── members/{userId}
  └── punchItems/{itemId}
        └── comments/{commentId}
```

---

## Project Structure
```
app/src/main/java/com/punchlist/app/
├── PunchlistApplication.kt
├── data/
│   ├── model/           User, Project, PunchItem, Comment, Attachment, Priority, Status, Role
│   ├── remote/          FirestoreService, StorageService
│   ├── repository/      Auth, User, Project, PunchItem, Comment, Storage (interfaces + impls)
│   └── local/           Room DB, PunchItemEntity, PunchItemDao
├── domain/usecase/
│   ├── auth/            SignIn, SignUp, SignOut
│   ├── project/         GetProjects, CreateProject, GetProjectMembers
│   ├── punchitem/       GetPunchItems, CreatePunchItem, UpdatePunchItem, UpdateStatus, GetDetail
│   ├── comment/         GetComments, AddComment
│   └── storage/         UploadPhoto
├── di/                  Firebase, Database, Repository, App modules
├── ui/
│   ├── MainActivity.kt
│   ├── navigation/      AppNavGraph, NavRoutes
│   ├── auth/            Login, Register screens + ViewModels
│   ├── project/         ProjectList, CreateProject screens + ViewModels
│   ├── feed/            PunchItemFeed + ViewModel + FeedFilterState
│   ├── create/          CreatePunchItem screen + ViewModel
│   ├── detail/          PunchItemDetail screen + ViewModel
│   ├── camera/          Camera screen + ViewModel (CameraX)
│   ├── components/      PunchItemCard, PriorityChip, StatusChip, PhotoGrid, CommentItem, FilterBar
│   └── theme/           PunchlistTheme
├── util/                Result, Extensions, ImageUtils, NetworkConnectivityObserver
├── notification/        PunchlistFirebaseMessagingService, NotificationHelper
└── worker/              PhotoUploadWorker
```

---

## Implementation Phases

### Phase 1 — Foundation + Auth ✅
- DI modules (Firebase, Database, Repository, App)
- Data models
- Auth repository + use cases
- Login / Register screens

### Phase 2 — Camera + Storage ✅
- CameraX PreviewView in Compose
- FileProvider for captured URIs
- StorageRepository → Firebase Storage upload

### Phase 3 — Projects ✅
- ProjectRepository (Firestore)
- ProjectList + CreateProject screens
- Project member management

### Phase 4 — PunchItem Core ✅
- PunchItemRepository (Firestore + Room cache)
- Feed screen with filters/search
- Create item form
- Item detail screen

### Phase 5 — Comments ✅
- CommentRepository
- Comment thread in detail screen
- Real-time Firestore listener

### Phase 6 — Offline Support (next)
- Room two-level cache
- NetworkConnectivityObserver
- PhotoUploadWorker for queued uploads
- SyncPunchItemsWorker

### Phase 7 — Notifications + Polish (next)
- FCM token refresh in onNewToken
- Cloud Function triggers for task events
- UI polish: animations, pull-to-refresh, empty states

---

## Firebase Setup (Required)
1. Go to https://console.firebase.google.com
2. Create a project
3. Add Android app with package: `com.punchlist.app`
4. Download `google-services.json` → place at `app/google-services.json`
5. Enable **Authentication** → Email/Password
6. Create **Firestore** database (start in test mode, then add security rules)
7. Enable **Storage**

### Firestore Security Rules
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
    match /projects/{projectId} {
      allow read: if request.auth.uid in get(/databases/$(database)/documents/projects/$(projectId)/members/$(request.auth.uid)).data.keys();
      allow create: if request.auth != null;
      allow update, delete: if get(/databases/$(database)/documents/projects/$(projectId)/members/$(request.auth.uid)).data.role == 'ADMIN';
      match /members/{memberId} {
        allow read, write: if request.auth.uid == memberId || get(/databases/$(database)/documents/projects/$(projectId)/members/$(request.auth.uid)).data.role in ['ADMIN', 'MANAGER'];
      }
      match /punchItems/{itemId} {
        allow read: if request.auth.uid in get(/databases/$(database)/documents/projects/$(projectId)/members).data.keys();
        allow create: if request.auth != null;
        allow update: if request.auth != null;
        match /comments/{commentId} {
          allow read, create: if request.auth != null;
        }
      }
    }
  }
}
```

---

## MVP Checklist
- [x] Login / Registration
- [x] Create / select project
- [x] Camera photo capture
- [x] Create punchlist item (with photos)
- [x] View feed with filters
- [x] Item detail screen
- [x] Comments
- [x] Change status
- [x] Mark complete + completion photo
- [ ] Role enforcement (UI gates wired to `User.role`)
- [ ] Offline queueing (WorkManager)
- [ ] Push notifications (FCM Cloud Functions)
