#  Personal Finance Companion — Gullak App

A polished, feature-rich personal finance tracking app built with **Kotlin + Jetpack Compose**

---

## Screens & Features

### 1. Home Dashboard
- **Net balance card** with header showing total income, expenses, and savings rate
- **7-day spending bar chart** with smooth entry animations
- **Recent transactions** list (last 5) with quick-add FAB
- Personalised greeting with the user's name

### 2. Transactions
- Full transaction history **grouped by date** with per-day totals
- **Swipe-to-delete** with a red animation of delete icon
- **Search** across notes and categories
- **Filter categories** for Income / Expense / specific category
- Tap any transaction to **edit** it

### 3. Add / Edit Transaction
- Income ↔ Expense ** toggle button**
- Large bold **amount input** with decimal keyboard
- **Category grid** (emoji-based, 4 columns, context-aware by type)
- **Material 3 DatePicker** dialog
- Optional **notes** field
- Full **validation** with error messages

### 4. Goals & Challenges (Creative Feature)
- **Savings Goals**: circular progress rings, deadline countdown, add-funds dialog, completion trophy
- **No-Spend Challenge**: streak counter, duration options (7/14/21/30 days), animated linear progress
- Emoji goal icons with a picker
- Delete goals, mark challenge days, end challenge early

### 5. Insights
- **Smart insight banner** — compares this month to last month, calls out top spending category
- **Month vs. last month** comparison (income, expenses, savings rate with ↑↓ trend arrows)
- **Animated donut chart** for category breakdown with colour legend
- **7-day bar chart** with highest-day highlight and daily average
- **Savings rate gauge** with contextual label (Excellent / Great / Needs Work)

### 6. Profile & Settings
- Display name (editable)
- Currency picker (₹ $ € £ ¥ ₩ ₱ ৳)
- **Dark mode toggle** (persisted, applied app-wide instantly)
- Avatar with initial letter

---

## Architecture

```
**MVVM + Repository Pattern

UI Layer (Compose Screens)
    ↕
ViewModel (StateFlow, Hilt)
    ↕
Repository
    ↕
Room Database ←→ DataStore Preferences**
```

## 📁 Project Structure

```
com.Gullakapp/
├── data/
│   ├── local/
│   │   ├── dao/          ← TransactionDao, SavingsGoalDao, etc.
│   │   ├── entity/       ← Room entities (flat String fields for enums/dates)
│   │   ├── FinanceDatabase.kt
│   │   └── Mappers.kt    ← Entity ↔ Domain converters
│   ├── repository/
│   │   └── FinanceRepository.kt
│   └── datastore/
│       └── UserPreferencesDataStore.kt
├── domain/
│   └── model/
│       └── Models.kt     ← Transaction, SavingsGoal, NoSpendChallenge, etc.
├── ui/
│   ├── home/             ← HomeScreen + HomeViewModel
│   ├── transactions/     ← TransactionsScreen, AddEditTransactionScreen, ViewModel
│   ├── goals/            ← GoalsScreen + GoalsViewModel
│   ├── insights/         ← InsightsScreen + InsightsViewModel
│   ├── profile/          ← ProfileScreen + ProfileViewModel
│   ├── components/       ← TransactionListItem, SwipeToDelete, FinanceCard, etc.
│   ├── theme/            ← Theme.kt (colors, typography, dark mode)
│   └── Navigation.kt     ← NavHost + BottomBar + Screen sealed class
├── di/
│   └── AppModule.kt      ← Hilt database + DAO providers
├── FinanceApplication.kt ← @HiltAndroidApp
└── MainActivity.kt       ← Single activity, reads dark mode pref
```

---

##  Setup & Running

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 11+
- Android SDK 26+

### Steps
```bash
git clone <your-repo-url>
cd Gullak
# Open in Android Studio → Let Gradle sync
# Run on emulator or device (minSdk 26)
```

### Gradle dependencies (key ones)
```toml
room = "2.6.1"
hilt = "2.51.1"
compose-bom = "2024.12.01"
navigation-compose = "2.8.5"
datastore = "1.1.2"
core-splashscreen = "1.0.1"
```

---

## 💡 Assumptions Made

1. **No backend / cloud sync** — all data is local (Room + DataStore). Easy to extend with Retrofit later.
2. **Single currency display** — the currency symbol is user-configurable but no conversion is done.
3. **No authentication** — no login screen; the app is personal and single-user.
4. **Enums stored as strings** in Room — simpler than TypeConverters; safe since enum names don't change.
5. **Dates stored as ISO strings** (yyyy-MM-dd) — human-readable in DB, parsed on read.
6. **Charts are custom Canvas** — avoids an extra chart library dependency while still looking polished.
7. **No-Spend Challenge streak** is manually tapped — the app doesn't auto-detect expense-free days (that would require always-on background work).

---

##  Notable UX Touches

- **Animated bars** — bar charts animate in with `EaseOutCubic` on first load
- **Animated progress rings** — savings goal rings and savings-rate gauge animate from 0
- **Swipe to delete** — red background with delete icon revealed on swipe
- **Grouped transaction list** — transactions grouped by date with per-day net totals
- **Smart insight banner** — auto-generated text insight comparing months
- **Dark mode** — fully supported, toggled from Profile screen, persisted across restarts
- **Edge-to-edge** — `enableEdgeToEdge()` in MainActivity for modern look

---

##  Possible Extensions

-  Export transactions to CSV
-  Recurring transaction templates
-  Budget alerts (push notifications)
-  Cloud backup via Firebase
-  Biometric app lock
-  Widget for home screen balance
-  Multi-account support

**
