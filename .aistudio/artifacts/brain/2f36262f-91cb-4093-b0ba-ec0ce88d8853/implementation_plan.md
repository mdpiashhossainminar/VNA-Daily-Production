# VNA Daily Production - Architecture & Implementation Plan

A specialized warehouse mobile application for VNA (Very Narrow Aisle) operators. It digitizes the physical paper shift checklist, provides rapid QR code plate scanning (live camera and gallery photos), tracks daily production targets in real time, and prints an exact replica of the official A4 physical worksheet.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The workspace is configured as a native Android project with Jetpack Compose. We are implementing a **100% native Android solution** that delivers maximum camera performance, instant QR decoding, seamless hardware haptic/audio feedback, and pixel-accurate A4 PDF generation.

- **App Architecture**: Native Android (Kotlin + Jetpack Compose + CameraX + Google ML Kit Barcode Scanning + Android Print Framework).
- **Worksheet Printing**: Uses Android's native `PrintManager` and vector-rendered A4 `PdfDocument` to guarantee 1:1 visual fidelity with the attached physical sheet (both on physical printers and when saved as PDF).
- **Data Persistence**: Offline-first local storage ensuring all shift info, checklist toggles, plate scans, and counts persist across app restarts without data loss.

---

## 1. Overview & Core Concept

- **What It Does**: Replaces the warehouse operator's physical paper sheet with a streamlined mobile app. Operators complete pre-shift safety checks, scan pallet/box QR codes across 4 operational modes, monitor live production counts against target benchmarks (190 total), and generate an identical print-ready A4 document at shift completion.
- **Target Audience**: Warehouse VNA operators working in fast-paced industrial environments, requiring large touch controls, high-contrast dark theme, and zero-friction repetitive scanning.
- **Key Value**: Eliminates manual handwriting, prevents miscounted tallies, supports multiple identical plate scans without false blocking, and produces standard archival paper sheets instantly.

---

## 2. User Experience & Visual Design

### Visual Identity & Theme
- **Color Palette**:
  - Background: Deep industrial navy (`#0D131A` / `#12161B`)
  - Surface Cards: Elevated dark charcoal-slate (`#1B232C` / `#1F2937`) with subtle border `#2D3748`
  - Primary Accent & Selected States: Industrial Hazard Yellow (`#F2B400`)
  - Status Success (OK, Clear, Yes, Done): Vibrant Emerald Green (`#22C55E`)
  - Typography: Noto Sans Bengali & Roboto / Orbitron for VNA branding (`#F3F4F6` primary text, `#94A3B8` secondary)
- **Design Rules**:
  - Strictly **no emojis** anywhere in the interface.
  - Exactly **3 text-only bottom tabs** without icons: **"শুরু"**, **"স্ক্যান"**, **"হিসাব ও প্রিন্ট"** (with an active yellow indicator line on top).
  - No extraneous subtitle text; terminology strictly uses **"operator"** (never "driver").
  - Top header displays minimal yellow rounded-square **"VNA"** logo with title, and a vertical three-dot menu button in the top right.
  - Three-dot menu contains **"অ্যাবাউট"**, opening a modal with the VNA logo, app title, developer credits (`This app develop by Piash Hossain.`), and clickable email contact (`mdpiashhossain2024@gmail.com`).

### Screen & Flow Layouts

```
┌─────────────────────────────────────────────────────────┐
│ [VNA] VNA Daily Production                          [⋮] │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  [ TAB 1: শুরু ]                                        │
│  ┌───────────────────────────────────────────────────┐  │
│  │ শিফট তথ্য: নাম, পদবী, আইডি, সুপারভাইজার, তারিখ    │  │
│  │ শিফট নির্বাচন: [A (Morning)] [B] [C] [G]         │  │
│  └───────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────┐  │
│  │ কাজ শুরুর চেকলিস্ট (1-8, 11, 13, 14)               │  │
│  │ 1. VNA মেশিন চালুর পরিদর্শন   [ OK ] [ Not OK ]   │  │
│  │ 5. Ailes / Rack বাধা পরীক্ষা [ Clear ] [ Issue ]  │  │
│  │ 6. Rack/Box লোকেশন যাচাই      [ Yes ] [ No ]      │  │
│  │ 11. খালি লোকেশন আপডেট         [ Done ] [ Pending ]│  │
│  │ 13. দুর্ঘটনা রিপোর্ট          [ No ] [ Yes ]      │  │
│  └───────────────────────────────────────────────────┘  │
│                                                         │
├─────────────────────────────────────────────────────────┤
│  ═════ [ শুরু ] ═══════ [ স্ক্যান ] ═════ [ হিসাব ও প্রিন্ট ] ══ │
└─────────────────────────────────────────────────────────┘
```

#### Tab 1: শুরু (Shift Info & Pre-Operation Checklist)
- **Card "শিফট তথ্য"**: Inputs for Name, Designation, ID Number, Shift Supervisor, Date picker (defaults to today), and 4 segmented buttons for Shift: `A (Morning)`, `B (Evening)`, `C (Night)`, `G (General)`.
- **Card "কাজ শুরুর চেকলিস্ট"**: Checkpoints 1–8, 11, 13, 14 matching the Bengali text from the physical sheet. Positive selections highlighted in green (`#22C55E`), alternate choices in yellow (`#F2B400`).

#### Tab 2: স্ক্যান (Plate Scanning & Live Counts)
- **Card "কী কাজ করছেন?"**: 2x2 grid of operational categories:
  - `লোড` (Plate Load)
  - `আনলোড` (Plate Unload)
  - `স্টক চেক` (Stock Check Physically)
  - `হাউসকিপিং` (Housekeeping / Segregation)
- **Scanning Actions**:
  - `ক্যামেরা চালু`: Continuous live back-camera QR scanner with CameraX + ML Kit; beeps via `ToneGenerator` and vibrates on each scan. 1-second debouncing prevents accidental physical duplicate triggers while allowing deliberate duplicates.
  - `ছবি থেকে স্ক্যান`: Native system photo picker to scan one or multiple photos from gallery/camera.
  - Manual plate entry: Text field with quick `যোগ` (Add) button.
  - **Rule**: Duplicate plate numbers are explicitly allowed in any category.
- **Card "কাউন্ট ও তালিকা"**: 4 count metric cards (Load, Unload, Stock Check, Housekeeping), followed by the list of entries for the active category with item count and tap-to-delete confirmation.

#### Tab 3: হিসাব ও প্রিন্ট (Target Tally & Exact A4 Print)
- **Card "শিফট শেষের হিসাব"**: Live dynamic target vs. achieved table:
  - Plate Load: Target 85
  - Plate Unload: Target 85
  - Stock Check Physically: Target 10
  - Housekeeping / Segregation: Target 10
  - Total Target: 190 | Live Achieved Total
- **Card Inputs**:
  - ক্ষতিগ্রস্ত প্লেট (Qty) (Checkpoint 10)
  - ভুল লোকেশনে থাকা (Qty) (Checkpoint 9)
  - VNA ডাউনটাইম (min) (Checkpoint 12)
  - টার্গেট অর্জন না করার কারণ (Multiline text)
- **Action Buttons**:
  - `প্রিন্ট`: Launches the print preview and generates the exact A4 worksheet matching the physical paper form.
  - `নতুন শিট`: Prompts confirmation before clearing current shift data for a fresh start.

---

## 3. Key Product Decisions & Trade-Offs

1. **Native ML Kit Barcode Scanning over Webview Scanner**:
   - *Chosen Approach*: Android CameraX with Google ML Kit Barcode Scanning API.
   - *Why*: Instant sub-second decoding, flawless hardware torch/autofocus control, and reliable offline operation in low-connectivity warehouse aisles.
2. **Exact A4 Vector Print Engine**:
   - *Chosen Approach*: Android `PrintDocumentAdapter` + `PdfDocument` rendering an exact pixel-grid replica of the paper sheet.
   - *Why*: Web `@media print` in mobile browsers often clips borders, shifts table alignments, or renders blurry text. A native PDF canvas guarantees crisp black borders, exact 60-box columns (SL 1–30 and 31–60), and proper pagination for scans exceeding 60 items.
3. **Local SharedPreferences / Room Persistence**:
   - *Chosen Approach*: Structured local persistence for active shift state, operator preferences, and plate history.
   - *Why*: Prevents loss of scanned plates if the app is minimized, rotated, or if battery saver triggers background kill.

---

## 4. Technical Architecture & Data Strategy

### System & Component Diagram

```
┌────────────────────────────────────────────────────────┐
│                      MainActivity                      │
│             (Edge-to-edge, Navy M3 Theme)              │
├────────────────────────────────────────────────────────┤
│                      Top App Bar                       │
│  - VNA Logo (Yellow Orbitron)                          │
│  - App Title ("VNA Daily Production")                  │
│  - Three-dot Menu -> About Dialog                      │
├────────────────────────────────────────────────────────┤
│                       Main Content                     │
│  ┌─────────────────┬──────────────────┬─────────────┐  │
│  │ Tab 1: শুরু     │ Tab 2: স্ক্যান    │ Tab 3: হিসাব│  │
│  │ - Shift Profile │ - Mode Selector  │ - Tally Grid│  │
│  │ - 10 Checkpoints│ - CameraX Scanner│ - Qty/Reason│  │
│  │                 │ - Gallery Picker │ - A4 Print  │  │
│  │                 │ - Scan List      │ - Reset     │  │
│  └─────────────────┴──────────────────┴─────────────┘  │
├────────────────────────────────────────────────────────┤
│         Bottom Navigation (3 Text Tabs, Yellow Bar)     │
└──────────────────────────┬─────────────────────────────┘
                           │
             ┌─────────────┴─────────────┐
             ▼                           ▼
 ┌───────────────────────┐   ┌───────────────────────┐
 │   VnaViewModel & Repo │   │   A4 Print Generator  │
 │ - ShiftState Flow     │   │ - Exact Paper Layout  │
 │ - ScannedPlates List  │   │ - Checkbox Glyphs     │
 │ - Target Computations │   │ - Multi-page 60-box   │
 │ - Beep / Haptic Util  │   │ - Category Summaries  │
 └───────────────────────┘   └───────────────────────┘
```

### Exact Physical Sheet Reproduction Specifications

The generated A4 Print/PDF document strictly adheres to the layout photographed in `IMG_20261103_004644_61c7f6be.jpg`:
- **Dimensions**: Standard A4 Portrait (595 x 842 pt).
- **Styling**: Pure white background, solid black borders (`1.5pt` outer, `1pt` inner), clean black typography.
- **Left Column**:
  1. Header meta block: Operator Name, Designation, ID Number, Shift.
  2. Table: `Daily VNA Operator Work Status / Checklist` (Columns: SL, Activity / Check Point, Daily Status) with all 14 checklist rows and checkbox markers (`☑ OK  ☐ Not OK`, `☑ Clear  ☐ Issue`, `☑ Yes  ☐ No`, `☑ Done  ☐ Pending`, `☐ No  ☑ Yes`, `Qty: ___`, `___ min`).
  3. Production table: Category, Target (85, 85, 10, 10, Total 190), Achieved.
  4. Reason block: `Reason For not Achieving Target (টার্গেট অর্জন না করার কারণ)`.
  5. Signature block: Shift Supervisor Name, Date (DD.MM.YY), Signature space.
- **Right Column**:
  - Table: `Plate Load / Unload / Stock Check / Housekeeping Status`.
  - 4 sub-columns: `SL # | Box Number | SL # | Box Number`.
  - Slots 1–30 on left, 31–60 on right.
  - Overflow handling: Scans 61+ continue onto Page 2 in the same 60-box format, followed by a category-by-category breakdown table.
