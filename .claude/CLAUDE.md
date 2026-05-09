# CLAUDE.md — FitFlow Android Project

## Tổng quan dự án

FitFlow là ứng dụng Android mentor người dùng tập thể dục tại nhà, lấy cảm hứng từ app **"6 Pack in 30 Days"**. Ứng dụng xây dựng bằng **Kotlin + Jetpack Compose**, theo kiến trúc **MVVM**, hỗ trợ dark/light theme.

**Package**: `com.example.fitflow`
**Min SDK**: 26 (Android 8.0) | **Target SDK**: 35 | **Compile SDK**: 36

---

## Cấu trúc dự án

```
app/src/main/java/com/example/fitflow/
├── MainActivity.kt              # Entry point, NavHost, navigation graph
├── FitFlowApplication.kt        # Application class, khởi tạo UserPreferences
├── data/
│   ├── UserPreferences.kt       # SharedPreferences wrapper (lưu profile, onboarding, completed days)
│   └── model/
│       ├── DayPlan.kt           # Data class: dayNumber, isRest, exercises
│       ├── Exercise.kt          # Data class: name, sets, reps, kcal
│       └── UserProfile.kt       # Data class + BmiCategory enum
├── domain/
│   ├── BmiCalculator.kt         # calculateBmi(), getBmiCategory()
│   └── WorkoutPlanGenerator.kt  # Object WorkoutPlangenerator — sinh 30-day plan theo BMI
├── viewmodel/
│   └── UserViewModel.kt         # UserViewModel + Factory, quản lý state: profile, workoutPlan, completedDays
└── ui/
    ├── theme/
    │   ├── Color.kt             # Bảng màu: BackgroundDark, CardDark, AccentNeon, SecondaryBlue, TextDim...
    │   ├── Theme.kt             # FitflowTheme — dark/light scheme, status bar color
    │   └── Type.kt              # Typography (chỉ có bodyLarge, phần còn lại đang comment)
    ├── components/
    │   └── BottomNavbar.kt      # Bottom navigation: Home, Plan, (+), Library, Me
    └── screens/
        ├── OnboardingScreen.kt       # Nhập height/weight → tính BMI → suggested goal
        ├── WorkoutSetupScreen.kt     # Chọn equipment, frequency → finalize
        ├── DashboardScreen.kt        # Trang chủ: streak, health metrics (steps, water)
        ├── PlannerScreen.kt          # Lịch 30 ngày chia theo tuần, LazyColumn
        ├── WorkoutDayDetailScreen.kt # Chi tiết ngày tập: danh sách exercise, nút DONE, rest timer có chọn duration
        ├── WorkoutSessionScreen.kt   # Timer-based session (đã tích hợp route workout_session/{dayNumber})
        ├── LoadingScreen.kt          # "Picking the best exercises for you" — animated progress bar 2.5s
        ├── LibraryScreen.kt          # Thư viện bài tập (hardcoded 2 exercises)
        └── ProfileScreen.kt         # Hồ sơ người dùng: stats, re-calibrate
```

**Resources:**
```
app/src/main/res/
├── values/
│   ├── strings.xml     # String resources cho Dashboard
│   ├── colors.xml      # XML color definitions
│   └── themes.xml      # Theme.Fitflow (Material Light NoActionBar)
└── drawable/           # ic_launcher vectors
```

---

## Design System — "Hyper Energy" Theme

### Bảng màu chính
| Token          | Hex         | Vai trò                              |
|----------------|-------------|--------------------------------------|
| `AccentNeon`   | `#FF5F07`   | Primary — actions, CTA, highlights   |
| `SecondaryBlue`| `#00E5FF`   | Secondary — metrics, recovery states |
| `BackgroundDark`| `#0A0B10`  | Nền dark mode                        |
| `CardDark`     | `#161821`   | Surface/card dark mode               |
| `TextDim`      | `#F0F0F5`   | Text chính trên nền tối              |
| `BackgroundLight`| `#F2F2F7` | Nền light mode                       |
| `CardLight`    | `#FFFFFF`   | Surface/card light mode              |
| `TextDarkMode` | `#1A1A2E`   | Text chính trên nền sáng             |

### Opacity tokens
- `White40`, `White20`, `White10`, `White05` — cho dark mode
- `Black40`, `Black20`, `Black10`, `Black05` — cho light mode

### Typography
- Phong cách: **Bold, Black, Italic** — tạo cảm giác năng lượng cao
- Label nhỏ: `UPPERCASE`, `letterSpacing = 2-3.sp`, `fontWeight = Black`
- Giá trị lớn: `fontSize 28-52.sp`, `fontWeight = Black`, `fontStyle = Italic`

### Shape system
- Card lớn: `RoundedCornerShape(32.dp)`
- Card nhỏ: `RoundedCornerShape(24.dp)`
- Button: `RoundedCornerShape(16-24.dp)`
- Badge/tag: `RoundedCornerShape(8.dp)` hoặc `CircleShape`
- Border: luôn `1.dp` với `color.copy(alpha = 0.05f-0.4f)`

---

## Navigation Flow

```
onboarding → workout_setup → loading (2.5s) → dashboard (root)
                                                   ├── planner → day_detail/{dayNumber} → workout_session/{dayNumber}
                                                   ├── library
                                                   └── profile → (re-calibrate) → onboarding
```

**Bottom navbar** hiển thị ở: `dashboard`, `planner`, `library`, `profile`
**Ẩn navbar** ở: `onboarding`, `workout_setup`, `loading`, `day_detail/{dayNumber}`, `workout_session/{dayNumber}`

---

## Các vấn đề đã biết (Known Issues)

### ~~1. Trang chủ (Dashboard) cuộn không hợp lý~~ ✅ ĐÃ SỬA
- ~~`DashboardScreen` dùng `Column` thường thay vì `LazyColumn`~~
- **Đã sửa**: Thêm `Modifier.verticalScroll(rememberScrollState())` vào root Column của `DashboardScreen`
- ~~Còn thiếu: Chưa thêm các sections mới~~ → ✅ Đã thêm Calendar + Workouts summary (2026-05-09)

### ~~2. Thiếu nút Finish trong workout flow~~ ✅ ĐÃ SỬA
- ~~Tự động gọi `onDayComplete()` khi bài cuối xong~~
- **Đã sửa**: 
  - `DayCompleteContent` nhận `onFinish` và `onBack` riêng biệt
  - Nút **"FINISH WORKOUT"** (primary, full-width) → gọi `onDayComplete()` + `onBack()`
  - Nút **"BACK WITHOUT SAVING"** (text button) → chỉ navigate về, không mark complete
  - Dùng `dayFinished` state để tránh gọi `onDayComplete()` nhiều lần
  - `RestDayContent` cũng được thêm nút "BACK TO PLAN" (secondary color)

### ~~3. Chiều cao giao diện bị fix cứng (hardcoded)~~ ✅ ĐÃ SỬA
- ~~`StreakSummarySection`: `.height(160.dp)`~~
- ~~`MetricCard`: `.height(80.dp)`~~
- **Đã sửa**: Xóa toàn bộ fixed height, card tự co giãn theo nội dung qua `spacedBy` và `padding`

### ~~4. Giao diện chưa đồng nhất giữa các màn hình~~ ✅ ĐÃ SỬA
- ~~`DashboardScreen` và `PlannerScreen` dùng màu trực tiếp~~
- **Đã sửa**: Tất cả screens đã dùng `MaterialTheme.colorScheme` — bao gồm `WorkoutSessionScreen` (2026-05-09)
- ⚠️ Còn vi phạm strings: Một số strings vẫn hardcode trong PlannerScreen và WorkoutDayDetailScreen

### ~~5. Import & code hygiene trong MainActivity.kt~~ ✅ ĐÃ SỬA
- ~~Commented import thừa L34, fully-qualified names thừa ở L114/L123, indentation sai L122~~
- **Đã sửa** (2026-05-08): Xóa commented import, xóa commented route, dùng short names đã import, fix indent `composable("workout_setup")`

### ~~6. Xây dựng kế hoạch nên dựa trên mục tiêu, không phải chỉ số BMI~~ ✅ ĐÃ SỬA
- ~~Hiện tại `WorkoutPlanGenerator` dùng `BmiCategory` để chọn pool bài tập~~
- **Đã sửa** (2026-05-08): `FitnessGoal` enum thêm vào `UserProfile`, `WorkoutPlanGenerator` có 4 goal-based exercise pools, `WorkoutSetupScreen` có section chọn goal, `UserPreferences.saveGoal()` persist goal
- ⚠️ Còn thiếu: Onboarding chưa có step chọn target weight / birth year

### ~~7. WorkoutSessionScreen chưa tích hợp~~ ✅ ĐÃ SỬA
- ~~File tồn tại nhưng không có route trong `NavHost`~~
- **Đã sửa** (2026-05-09): Route `workout_session/{dayNumber}` thêm vào NavHost. Nút "START TIMED SESSION" trong `WorkoutDayDetailScreen`. Exercises convert từ `DayPlan` với `durationSec = reps * 3`
- ~~Vẫn dùng hardcoded colors từ `theme.*`~~ → ✅ Đã chuyển sang `MaterialTheme.colorScheme` (2026-05-09)

### ~~8. Indentation không nhất quán~~ ✅ ĐÃ SỬA
- ~~`MainActivity.kt` line 123: `composable("workout_setup")` bị thụt lề sai (dùng tab thay vì spaces)~~
- **Đã sửa** (2026-05-08): Căn về đúng indent cùng cấp với các `composable()` khác

---

## Design References (folder `/design/`)

10 ảnh chụp từ app "6 Pack in 30 Days" gốc, mô tả các flow chính:

| File | Nội dung |
|------|----------|
| `e1baaca6` | **Training tab** — Challenge title, stages, day list (Day 1 START, Day 2-4 với kcal/min) |
| `1deaf581` | **Daily tab (top)** — Check-in Record, Calendar tuần, Workouts section (0/7 min) |
| `9012477c` | **Daily tab (scroll)** — Start Workout button, Step/Water cards, Today's Weight |
| `94ab941e` | **Daily tab (full scroll)** — Weight tracking, Step/Water detail cards |
| `c51a0197` | **Me tab (top)** — Welcome, stats (Workouts/Kcal/Minutes), Special Offer, Awards |
| `e1baaca6` | **Me tab (scroll)** — Duration/Calories charts, Weight tracking graph |
| `24d90e7a` | **Onboarding** — "What's your height?" với ruler picker, cm/ft toggle |
| `42ec5c9f` | **Onboarding** — "What's your birth year?" với wheel picker |
| `85afb086` | **Onboarding** — "What's your target weight?" với ruler, BMI display, challenging goal card |
| `c77e56c5` | **Onboarding** — "What's your current weight?" với ruler, Current BMI card |
| `c6cbc298` | **Loading** — "Picking the best exercises for you" với progress animation |

### Nguyên tắc áp dụng design
1. **Giữ nguyên** những gì đã phát triển trong dự án (theme, color system, typography style)
2. **Phát triển đồng bộ** các chi tiết chưa hợp lý theo hướng design reference
3. **Taskbar chỉ chứa** các chức năng đã phát triển (Home, Plan, Library, Me) — KHÔNG thêm tab chưa có
4. Bottom navbar hiện tại: `Home | Plan | Library | Me` — giữ nguyên layout này

---

## Quy tắc phát triển (Development Rules)

### Code Style
- **Ngôn ngữ**: Kotlin, Jetpack Compose declarative UI
- **Kiến trúc**: MVVM — ViewModel xử lý logic, Screen chỉ render UI
- **State management**: `StateFlow` + `collectAsState()` trong Compose
- **Persistence**: `SharedPreferences` qua `UserPreferences` class
- **Navigation**: `navigation-compose` với string routes

### Compose Best Practices
- Dùng `LazyColumn` / `LazyVerticalGrid` cho danh sách có scroll
- KHÔNG hardcode chiều cao/rộng — dùng `weight()`, `fillMaxWidth()`, `wrapContentHeight()`
- Dùng `MaterialTheme.colorScheme` thay vì reference trực tiếp color tokens (để support dark/light theme)
- Tất cả text hiển thị nên dùng `stringResource(R.string.xxx)` thay vì hardcode string
- Padding: dùng `Modifier.padding()` nhất quán, tránh nested padding

### Khi thêm màn hình mới
1. Tạo file trong `ui/screens/`
2. Thêm route vào `NavHost` trong `MainActivity.kt`
3. Nếu cần hiện trong bottom nav → cập nhật `BottomNavbar.kt`
4. Nếu cần ẩn bottom nav → thêm route vào `hideNav` condition trong `MainActivity.kt`
5. Dùng đúng color scheme từ `MaterialTheme` — KHÔNG import trực tiếp từ `Color.kt`

### Khi sửa giao diện
1. Tham khảo design trong `/design/` folder
2. Giữ nguyên "Hyper Energy" aesthetic: bold fonts, italic, uppercase labels, neon accents
3. Đảm bảo hoạt động cả dark mode và light mode
4. Test responsive trên nhiều kích thước màn hình — không fix cứng chiều cao

---

## Build & Run

```bash
# Sync Gradle
./gradlew build

# Run on device/emulator
./gradlew installDebug

# Clean build
./gradlew clean build
```

**IDE**: Android Studio (Brave Badger+)
**JDK Target**: 17

### Dependencies đáng chú ý
- `androidx.navigation:navigation-compose:2.7.7` — Navigation
- `androidx.compose.material:material-icons-extended` — Icons
- `com.google.ai.client.generativeai:generativeai:0.2.2` — Gemini AI (có trong deps nhưng chưa sử dụng)
- `androidx.room:room-ktx` — Room (có trong deps nhưng chưa sử dụng)

---

## Trạng thái phát triển hiện tại

### Tổng quan theo màn hình

| Màn hình | Theme | Layout | Navigation | Data | Tổng thể |
|----------|-------|--------|-----------|------|----------|
| Onboarding | ✅ MaterialTheme | ⚠️ Slider fixed-range | ✅ OK | ⚠️ Thiếu birth year, target weight | ⚠️ Cần mở rộng |
| Workout Setup | ✅ MaterialTheme | ✅ Scroll | ✅ OK | ✅ Goal + equipment + frequency | ✅ Hoạt động |
| Loading | ✅ MaterialTheme | ✅ Center column | ✅ Popbackstack clean | ✅ Fake 2.5s delay | ✅ Hoạt động |
| Dashboard | ✅ MaterialTheme | ✅ Responsive + scroll | ✅ OK | ✅ Calendar + Workouts từ ViewModel, steps/water local | ✅ Hoạt động |
| Planner | ✅ MaterialTheme | ✅ LazyColumn | ✅ OK | ✅ ViewModel | ✅ Hoạt động |
| Day Detail | ✅ MaterialTheme | ✅ LazyColumn | ✅ OK + onStartSession | ✅ ViewModel | ✅ Hoạt động |
| Workout Session | ✅ MaterialTheme | ✅ Column | ✅ Route workout_session/{dayNumber} | ✅ TimedExercise từ DayPlan | ✅ Tích hợp |
| Library | ✅ MaterialTheme | ✅ OK | ✅ OK | ❌ 2 exercises hardcoded | ⚠️ Cần phát triển |
| Profile | ✅ MaterialTheme | ✅ OK | ✅ OK | ❌ Stats hardcoded | ⚠️ Cần kết nối ViewModel |

### Trạng thái theo component hệ thống

| Component | File | Trạng thái |
|-----------|------|-----------|
| Theme (Color.kt) | `ui/theme/Color.kt` | ✅ Định nghĩa đầy đủ. Screens đã chuyển sang MaterialTheme |
| Theme (Theme.kt) | `ui/theme/Theme.kt` | ✅ Dark/Light scheme hoạt động |
| Theme (Type.kt) | `ui/theme/Type.kt` | ⚠️ Chỉ có `bodyLarge`, phần còn lại bị comment |
| BottomNavbar | `ui/components/BottomNavbar.kt` | ✅ Home, Plan, Library, Me — hoạt động |
| Navigation | `MainActivity.kt` | ✅ 8 routes, imports sạch, hideNav đầy đủ (2026-05-09) |
| UserPreferences | `data/UserPreferences.kt` | ✅ Lưu profile, onboarding status, completed days, goal |
| UserViewModel | `viewmodel/UserViewModel.kt` | ✅ StateFlow cho workoutPlan, completedDays, profile; saveGoal() |
| WorkoutPlanGenerator | `domain/WorkoutPlanGenerator.kt` | ✅ 4 goal-based pools (WEIGHT_LOSS/MUSCLE_GAIN/ENDURANCE/MAINTENANCE) |
| strings.xml | `res/values/strings.xml` | ⚠️ Chỉ có strings cho Dashboard, còn thiếu Planner + WorkoutDayDetail |

---

## Những gì đã hoàn thành (Phiên 2026-05-09)

### Sprint 2 — Feature Enhancement — ✅ HOÀN THÀNH

1. **Goal-based Workout Plan** (tiếp nối từ phiên 05-08):
   - `FitnessGoal` enum (`WEIGHT_LOSS`, `MUSCLE_GAIN`, `ENDURANCE`, `MAINTENANCE`) thêm vào `UserProfile.kt`
   - `WorkoutPlanGenerator` refactor sang 4 exercise pools theo goal (mỗi pool 9 bài)
   - `WorkoutSetupScreen` thêm section "FITNESS GOAL" với 4 `EquipmentItem` chips
   - `UserPreferences.saveGoal()` + `getUserProfile()` persist và load goal, backward-compatible (try/catch default WEIGHT_LOSS)
   - `UserViewModel.saveGoal()` kích hoạt lại `loadUserProfile()` → regenerate plan

2. **LoadingScreen**:
   - File mới `LoadingScreen.kt` — delay 2.5s + `animateFloatAsState` progress bar
   - Route `loading` chèn giữa `workout_setup` → `dashboard`, popUpTo(0) để clear backstack
   - hideNav bao gồm `loading`

3. **Calendar section trong Dashboard**:
   - `WeeklyCalendarSection()` — 7 cột S M T W T F S, primary circle highlight cho ngày hôm nay
   - Header tháng/năm với `ChevronLeft` / `ChevronRight` navigate giữa các tuần
   - Dùng `java.time.LocalDate` (API 26+), `weekOffset` state để chuyển tuần

4. **Workouts summary section trong Dashboard**:
   - `WorkoutsSummarySection()` — 2 card: "X / Y DAYS COMPLETED" (primary) + "X KCAL BURNED" (secondary)
   - Tính `totalKcal` từ `workoutPlan.filter { in completedDays }.flatMap { exercises }.sumOf { kcal }`
   - Nút "START A WORKOUT" (dark full-width) → navigate `planner` tab
   - `DashboardScreen` nhận `completedDays`, `workoutPlan`, `onStartWorkout` từ `MainActivity`

5. **Custom Rest Timer**:
   - `RestTimerDialog` có 4 `FilterChip` (30s / 60s / 90s / 120s) tại đầu dialog
   - `selectedDuration` state + `LaunchedEffect(selectedDuration)` → chọn chip mới = reset + đếm lại
   - Progress bar `fillMaxWidth(secondsLeft / selectedDuration.toFloat())` — không còn hardcoded `/60f`

6. **WorkoutSessionScreen tích hợp**:
   - Fix toàn bộ hardcoded `theme.*` colors → `MaterialTheme.colorScheme` (xóa `import com.example.fitflow.ui.theme.*`)
   - `CircularProgressIndicator` và `LinearProgressIndicator` dùng lambda form `progress = { value }`
   - `WorkoutDayDetailScreen` thêm `onStartSession: () -> Unit = {}` parameter
   - Nút "START TIMED SESSION" (primary, full-width) trong `WorkoutContent` — trước summary chips
   - Route `workout_session/{dayNumber}` trong NavHost, exercises convert: `durationSec = ex.reps * 3`
   - `onFinish` trong session screen gọi `viewModel.markDayComplete()` + `popBackStack()`
   - `workout_session` thêm vào `hideNav`

---

## Những gì đã hoàn thành (Phiên 2026-05-08)

### Code Hygiene — ✅ HOÀN THÀNH

1. **Dọn dẹp toàn bộ `MainActivity.kt`** (P1.1):
   - Xóa blank line thừa giữa import block
   - Xóa `//import com.example.fitflow.ui.screens.DashboardScreen` (duplicate commented)
   - Xóa `//composable("library") { LibraryScreen() }` (duplicate commented route)
   - Thay fully-qualified `OnboardingScreen` và `WorkoutSetupScreen` bằng short name (imports đã có sẵn)
   - Fix indentation sai `composable("workout_setup")` về cùng cấp với các route khác

2. **Fix Kotlin style lỗi trong `BmiCalculator.kt`**:
   - Xóa 3 từ khóa `return` thừa bên trong nhánh `when` (`return when { ... -> return X }` → `return when { ... -> X }`)

3. **Giải quyết xung đột tên class trong `WorkoutSessionScreen.kt`**:
   - Đổi tên `data class Exercise` → `data class TimedExercise` để tránh xung đột với `data.model.Exercise`
   - Cập nhật `sampleExercises()` dùng `TimedExercise`

4. **Khảo sát toàn dự án tìm dead code** — kết quả:
   - `onClick = {}` trong các màn hình: KHÔNG phải dead code — là required parameter của `IconButton`/`Card`
   - `WorkoutSessionScreen.kt`: giữ nguyên file, đã xử lý class naming conflict

---

## Những gì đã hoàn thành (Phiên 2026-05-07)

### Sprint 1: UI Consistency — ✅ HOÀN THÀNH

1. **Đồng bộ color tokens** cho 3 screens chính:
   - `DashboardScreen.kt` — toàn bộ hardcoded colors → MaterialTheme
   - `PlannerScreen.kt` — toàn bộ hardcoded colors → MaterialTheme  
   - `WorkoutDayDetailScreen.kt` — toàn bộ hardcoded colors → MaterialTheme

2. **Fix responsive layout**:
   - Xóa `.height(160.dp)` trên StreakSummarySection → tự co giãn
   - Xóa `.height(80.dp)` trên MetricCard → tự co giãn
   - Thêm `verticalScroll(rememberScrollState())` cho DashboardScreen

3. **Fix Finish workflow** (UX quan trọng):
   - Tách `DayCompleteContent` thành 2 nút: **FINISH WORKOUT** + **BACK WITHOUT SAVING**
   - Thêm `dayFinished` state chống double-call
   - Bỏ auto-call `onDayComplete()` — user phải chủ động bấm FINISH
   - Thêm nút "BACK TO PLAN" cho RestDayContent

4. **Fix critical navigation bug**:
   - Phát hiện double `popBackStack()`: MainActivity.onDayComplete gọi pop + DayCompleteContent.onFinish cũng gọi onBack (pop)
   - Sửa: `onDayComplete` trong MainActivity chỉ gọi `viewModel.markDayComplete()`, **không** gọi `popBackStack()`

5. **Clean up MainActivity.kt**:
   - Xóa 2 commented imports sai package (`com.fitflow`, duplicate `LibraryScreen`)

6. **Hệ thống hóa quy trình phát triển**:
   - Tạo `CLAUDE.md` — source of truth cho architecture, design system, rules
   - Tạo `.claude/agents/QA-Testcode.md` — code review agent (static analysis only)
   - Tạo `.claude/agents/specify.md` — business analyst agent
   - Tạo Notion task board với 35 tasks chia 3 Sprint

---

## Bước tiếp theo (Priority Order)

### 🔴 Priority 1 — Sửa ngay

| # | Task | File(s) | Chi tiết |
|---|------|---------|----------|
| P1.2 | Di chuyển hardcoded strings | `PlannerScreen.kt`, `WorkoutDayDetailScreen.kt`, `strings.xml` | ~17 strings cần vào `strings.xml` để tuân thủ i18n convention |

### 🟠 Priority 2 — Sprint 3 (Enhancement)

| # | Task | Mô tả |
|---|------|-------|
| P2.1 | **Kết nối Profile → ViewModel** | ProfileScreen lấy stats thực (weight, height, workouts completed, total kcal) từ UserViewModel thay vì hardcode |
| P2.2 | **Phát triển Library** | Thêm exercise pools đầy đủ từ `WorkoutPlanGenerator` (4 pools × 9 bài), hiện theo category với filter |
| P2.3 | **Check-in Record section** | Thêm calendar heatmap hoặc streak indicator theo design `1deaf581` — biểu diễn completedDays |
| P2.4 | **Weight tracking card** | Thêm card theo dõi cân nặng vào Dashboard theo design `9012477c` — cần thêm field weight history vào UserPreferences |

### 🟡 Priority 3 — Sprint 4 (Polish)

| # | Task | Mô tả |
|---|------|-------|
| P3.1 | **Cải thiện Onboarding** | Multi-step flow: height → birth year → current weight → target weight. Ruler/wheel picker thay vì slider — theo design `24d90e7a`, `85afb086` |
| P3.2 | **Typography system** | Mở rộng `Type.kt` — thêm full typography set (headline, title, label styles) thay vì chỉ `bodyLarge` |
| P3.3 | **WorkoutSession UX** | Thêm rest timer giữa các bài trong WorkoutSessionScreen (hiện chỉ có trong WorkoutDayDetailScreen) |

---

## Quyết định quan trọng & Lý do

### QĐ-1: Dùng `verticalScroll` thay vì `LazyColumn` cho Dashboard
- **Quyết định**: Dashboard dùng `Column + verticalScroll` thay vì `LazyColumn`
- **Lý do**: Dashboard có ít section cố định (header, streak, metrics) — chưa đủ phức tạp để cần lazy loading. `verticalScroll` đơn giản hơn và cho phép giữ cấu trúc composable hiện tại. Khi thêm sections mới (Calendar, Weight chart...), nếu số lượng items tăng đáng kể → chuyển sang `LazyColumn`

### QĐ-2: Tách Finish thành 2 nút riêng biệt
- **Quyết định**: `DayCompleteContent` có 2 nút: **FINISH WORKOUT** (lưu + quay về) và **BACK WITHOUT SAVING** (chỉ quay về)
- **Lý do**: Feedback ban đầu là "không thấy nút Finish". Giải pháp auto-finish khi bài cuối xong không rõ ràng cho UX. 2 nút tách biệt cho user quyền chọn: hoàn thành thật sự hay chỉ xem thử. Ngăn trường hợp vô tình mark complete

### QĐ-3: `onDayComplete` chỉ lưu dữ liệu, KHÔNG navigate
- **Quyết định**: Trong `MainActivity.kt`, callback `onDayComplete` chỉ gọi `viewModel.markDayComplete()`. Navigation do `WorkoutDayDetailScreen` tự xử lý qua `onBack()`
- **Lý do**: QA phát hiện bug double `popBackStack()` — cả caller (MainActivity) và callee (DayCompleteContent) đều gọi popBackStack(). Nguyên tắc: **screen sở hữu navigation của mình** — callback chỉ nên xử lý business logic

### QĐ-4: QA Agent chỉ review code tĩnh, không build/test
- **Quyết định**: `.claude/agents/QA-Testcode.md` chỉ làm static code review (7 checklists), không yêu cầu build, lint, hay chạy trên thiết bị
- **Lý do**: Dự án mobile không có môi trường CI/CD tích hợp sẵn cho agent. Build yêu cầu JDK + Android SDK đúng version. Agent tập trung vào giá trị cao nhất: kiểm tra logic code có đúng yêu cầu task, có tuân thủ CLAUDE.md, có gây side-effect không

### QĐ-5: Giữ nguyên color tokens trong Color.kt
- **Quyết định**: KHÔNG xóa `BackgroundDark`, `AccentNeon`... trong `Color.kt`. Chỉ thay đổi cách screens reference chúng
- **Lý do**: `Color.kt` là nơi khai báo raw color values, được `Theme.kt` sử dụng để xây dựng `darkColorScheme()` / `lightColorScheme()`. Screens reference qua `MaterialTheme.colorScheme` — đây là cách đúng của Material3. Xóa tokens trong Color.kt sẽ break Theme.kt

### QĐ-7: Không xóa `onClick = {}` trong các màn hình
- **Quyết định**: Giữ nguyên các `onClick = {}` rỗng ở DashboardScreen, PlannerScreen, LibraryScreen, ProfileScreen
- **Lý do**: `onClick` là **required parameter** của `IconButton` và overload clickable của `Card` trong Jetpack Compose — xóa sẽ compile error. Đây là placeholder cho tính năng sẽ implement sau, không phải dead code

### QĐ-8: Đổi tên class thay vì xóa trong WorkoutSessionScreen
- **Quyết định**: Đổi tên `data class Exercise` → `TimedExercise` trong `WorkoutSessionScreen.kt`, không xóa file
- **Lý do**: File chưa được tích hợp nhưng chứa logic timer-based session hoàn chỉnh — xóa sẽ mất công sức đã làm. Đổi tên giải quyết xung đột namespace với `data.model.Exercise` mà không phá vỡ logic nội bộ. File sẽ được tích hợp ở P2.4

### QĐ-9: `DashboardScreen` nhận data qua parameters, không gọi `viewModel()` trực tiếp
- **Quyết định**: `DashboardScreen(completedDays, workoutPlan, onStartWorkout)` — data truyền từ `MainActivity`
- **Lý do**: Nhất quán với kiến trúc MVVM của dự án — ViewModel sống ở `MainActivity`, screens chỉ nhận data thuần túy qua params. Dễ test (preview không cần ViewModel). Tránh tạo thêm ViewModel instance trong screen.

### QĐ-10: Duration picker bên trong `RestTimerDialog`, không phải bước riêng
- **Quyết định**: 4 `FilterChip` (30s/60s/90s/120s) đặt ngay trong dialog, timer chạy song song với khả năng đổi
- **Lý do**: UX đơn giản hơn — không cần thêm bước "chọn trước, rồi bắt đầu". `LaunchedEffect(selectedDuration)` reset timer tự nhiên. User vẫn thấy countdown trong khi chọn.

### QĐ-11: `TimedExercise.durationSec = reps * 3`
- **Quyết định**: Convert `Exercise` → `TimedExercise` với công thức `durationSec = ex.reps * 3` (~3 giây/rep)
- **Lý do**: `Exercise` model không có field `durationSec`. Convention 3 giây/rep là ước tính hợp lý cho bodyweight exercises. Nếu muốn chính xác hơn sau này, thêm field `durationSec` vào `Exercise` data class.

### QĐ-12: `WeeklyCalendarSection` dùng `java.time` (API 26+), không dùng Calendar cũ
- **Quyết định**: `LocalDate`, `DateTimeFormatter`, `DayOfWeek` từ `java.time`
- **Lý do**: Min SDK = 26 (Android 8.0) nên `java.time` khả dụng không cần desugaring. API hiện đại hơn `java.util.Calendar`, code gọn và immutable.

### QĐ-6: Strings hardcode chấp nhận tạm thời
- **Quyết định**: PlannerScreen và WorkoutDayDetailScreen còn ~17 strings hardcode, chưa chuyển sang `strings.xml`
- **Lý do**: Sprint 1 ưu tiên sửa color consistency + UX bugs trước. Strings hardcode không gây crash hay sai logic — chỉ vi phạm convention i18n. Được plan vào Priority 1.2 để sửa sớm nhất ở phiên tiếp theo

---

## Agent System

Dự án sử dụng hệ thống multi-agent trong `.claude/agents/`:

| Agent | File | Vai trò |
|-------|------|---------|
| **QA Code Reviewer** | `agents/QA-Testcode.md` | Review code tĩnh theo 7 checklists (Task Compliance, Color/Theme, Layout, Navigation, Hygiene, Design System, Data Flow). KHÔNG build/test |
| **Business Analyst** | `agents/specify.md` | Phân tích yêu cầu mơ hồ, đặt câu hỏi nghiệp vụ, tự generate/cập nhật CLAUDE.md |

**Quy trình**: Agent chính code → QA agent review → Agent chính sửa bugs → Cập nhật CLAUDE.md

---

## Changelog

### 2026-05-09 — Feature Sprint

#### DashboardScreen.kt
- ✅ Thêm `WeeklyCalendarSection()` — 7 cột S M T W T F S, `CircleShape` highlight ngày hôm nay, điều hướng tuần bằng `ChevronLeft`/`ChevronRight`
- ✅ Thêm `WorkoutsSummarySection()` — 2 card (COMPLETED DAYS + KCAL BURNED), nút "START A WORKOUT"
- ✅ Thêm params `completedDays: Set<Int>`, `workoutPlan: List<DayPlan>`, `onStartWorkout: () -> Unit`
- ✅ Import thêm: `CircleShape`, `ChevronLeft`, `ChevronRight`, `DayPlan`, `LocalDate`, `DateTimeFormatter`

#### WorkoutDayDetailScreen.kt
- ✅ `RestTimerDialog` thêm `initialSeconds: Int = 60` param
- ✅ Thêm `selectedDuration` state + 4 `FilterChip` (30s/60s/90s/120s) trong dialog
- ✅ `LaunchedEffect(selectedDuration)` — đổi chip reset timer về duration mới
- ✅ Progress bar `fillMaxWidth(secondsLeft / selectedDuration.toFloat())` — không còn `/60f` cứng
- ✅ `WorkoutDayDetailScreen` thêm `onStartSession: () -> Unit = {}` param
- ✅ `WorkoutContent` thêm `onStartSession` param + nút "START TIMED SESSION" (primary, full-width, heightIn 48dp)

#### WorkoutSessionScreen.kt
- ✅ Xóa `import com.example.fitflow.ui.theme.*` và `import com.example.fitflow.data.model.Exercise` (unused)
- ✅ Thay tất cả `BackgroundDark`, `TextDim`, `AccentNeon`, `White05/10/20/40` → `MaterialTheme.colorScheme.*`
- ✅ `CircularProgressIndicator` và `LinearProgressIndicator` dùng lambda form `progress = { value }`
- ✅ TopAppBar title đổi thành "WORKOUT SESSION" uppercase với fontWeight Black

#### MainActivity.kt
- ✅ Thêm imports `WorkoutSessionScreen`, `TimedExercise`
- ✅ `hideNav` thêm `|| (currentRoute?.startsWith("workout_session") == true)`
- ✅ `day_detail` route: thêm `onStartSession = { navController.navigate("workout_session/$dayNumber") }`
- ✅ Route `workout_session/{dayNumber}`: convert `DayPlan.exercises` → `List<TimedExercise>`, `onFinish` gọi `markDayComplete` + `popBackStack`
- ✅ `dashboard` route: kết nối ViewModel — `DashboardScreen(completedDays, workoutPlan, onStartWorkout)`

#### (từ phiên 05-08, ghi lại đầy đủ)
- ✅ `UserProfile.kt`: thêm `FitnessGoal` enum + `goal: FitnessGoal = WEIGHT_LOSS` field
- ✅ `UserPreferences.kt`: thêm `KEY_GOAL`, `saveGoal()`, update `getUserProfile()` đọc goal với try/catch
- ✅ `WorkoutPlanGenerator.kt`: refactor sang `generatePlan(goal: FitnessGoal)`, 4 pools × 9 exercises, 2 rest patterns
- ✅ `UserViewModel.kt`: `saveGoal()` method, `loadUserProfile()` dùng `profile.goal`
- ✅ `WorkoutSetupScreen.kt`: thêm "FITNESS GOAL" section 4 chips, `onComplete: (FitnessGoal) -> Unit`, `verticalScroll`
- ✅ `LoadingScreen.kt`: tạo mới — `LaunchedEffect` delay 2.5s, `animateFloatAsState` progress bar tween 2000ms
- ✅ Tất cả hardcoded `.height(X.dp)` trên buttons → `.heightIn(min=X.dp)` (5 chỗ trong 3 files)

---

### 2026-05-07 — Phiên làm việc chính

#### DashboardScreen.kt
- ✅ Thay toàn bộ hardcoded color tokens → `MaterialTheme.colorScheme`
  - `BackgroundDark` → `.background`, `CardDark` → `.surface`, `AccentNeon` → `.primary`
  - `SecondaryBlue` → `.secondary`, `TextDim` → `.onBackground`, `White05/10/40` → `.onBackground.copy(alpha=x)`
- ✅ Xóa `import com.example.fitflow.ui.theme.*`
- ✅ Xóa hardcoded `.height(160.dp)` trên StreakSummarySection → tự co giãn
- ✅ Xóa hardcoded `.height(80.dp)` trên MetricCard → tự co giãn
- ✅ Thêm `Modifier.verticalScroll(rememberScrollState())` vào root Column

#### PlannerScreen.kt
- ✅ Thay toàn bộ hardcoded color tokens → `MaterialTheme.colorScheme` (PlannerScreen, DayPlanItem, ExerciseTag)
- ✅ `"NEW CYCLE"` card: `AccentNeon` → `colorScheme.primary`, `BackgroundDark` → `colorScheme.onPrimary`
- ✅ Xóa `import com.example.fitflow.ui.theme.*` và `import ...Color`

#### WorkoutDayDetailScreen.kt
- ✅ Thay toàn bộ hardcoded color tokens → `MaterialTheme.colorScheme` trong tất cả composables
- ✅ Tách nút Finish: `DayCompleteContent` nhận `onFinish` (mark complete) và `onBack` (navigate only)
- ✅ Thêm nút **"FINISH WORKOUT"** (primary, full-width 56dp)
- ✅ Thêm nút **"BACK WITHOUT SAVING"** (TextButton)
- ✅ Thêm `dayFinished` state chống double-call `onDayComplete()`
- ✅ Bỏ auto-call `onDayComplete()` — user phải bấm FINISH
- ✅ `RestDayContent` thêm nút "BACK TO PLAN" (secondary color)

#### MainActivity.kt
- ✅ Fix double `popBackStack()` bug: `onDayComplete` chỉ gọi `viewModel.markDayComplete()`
- ✅ Xóa 2 commented imports sai package (L24, L32 cũ)
- ⚠️ ~~Còn lại: 1 commented import L34, indent sai L122~~ → ✅ Đã xử lý hoàn toàn (2026-05-08)

#### Hệ thống
- ✅ Tạo `CLAUDE.md` — source of truth
- ✅ Tạo `.claude/agents/QA-Testcode.md` — code review agent (v2: static-only)
- ✅ Tạo `.claude/agents/specify.md` — business analyst agent
- ✅ Tạo Notion task board (35 tasks × 3 sprints)
- ✅ Chạy QA review phiên đầu tiên → phát hiện + sửa Bug Critical (double pop)

### 2026-05-08 — Code Hygiene Sprint

#### MainActivity.kt
- ✅ Xóa blank line thừa trong import block (giữa WorkoutSetupScreen và PlannerScreen)
- ✅ Xóa `//import com.example.fitflow.ui.screens.DashboardScreen` (L34 cũ)
- ✅ Xóa `//composable("library") { LibraryScreen() }` (duplicate commented route)
- ✅ Thay `com.example.fitflow.ui.screens.OnboardingScreen(...)` → `OnboardingScreen(...)` (import có sẵn)
- ✅ Thay `com.example.fitflow.ui.screens.WorkoutSetupScreen(...)` → `WorkoutSetupScreen(...)` (import có sẵn)
- ✅ Fix indentation `composable("workout_setup")` về cùng cấp 24 spaces

#### BmiCalculator.kt
- ✅ Xóa 3 `return` thừa trong nhánh `when`: `-> return X` → `-> X`

#### WorkoutSessionScreen.kt
- ✅ Đổi tên `data class Exercise` → `data class TimedExercise`
- ✅ Cập nhật `sampleExercises()` dùng `TimedExercise`

#### Khảo sát dead code toàn dự án
- ✅ Xác nhận `onClick = {}` là required parameter — không phải dead code
- ✅ Xác nhận không còn commented-out code blocks trong codebase
- ✅ Xác nhận không còn duplicate import hay sai package nào
