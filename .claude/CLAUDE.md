# CLAUDE.md — FitFlow Android Project

## Tổng quan dự án

FitFlow là ứng dụng Android mentor người dùng tập thể dục tại nhà, lấy cảm hứng từ app **"6 Pack in 30 Days"**. Ứng dụng xây dựng bằng **Kotlin + Jetpack Compose**, theo kiến trúc **MVVM**, hỗ trợ dark/light theme.

**Package**: `com.example.fitflow`
**Min SDK**: 26 (Android 8.0) | **Target SDK**: 35 | **Compile SDK**: 35

---

## Cấu trúc dự án

```
app/src/main/java/com/example/fitflow/
├── MainActivity.kt              # Entry point, NavHost, navigation graph
├── FitFlowApplication.kt        # Application class, khởi tạo UserPreferences + global Coil imageLoader
├── data/
│   ├── UserPreferences.kt       # SharedPreferences wrapper (lưu profile, onboarding, completed days)
│   └── model/
│       ├── DayPlan.kt           # Data class: dayNumber, isRest, workoutExercises
│       ├── WorkoutExercise.kt   # Data class: category, name, sets, reps, kcal, durationSec, gifFileName
│       ├── Exercise.kt          # Legacy model (còn tồn tại trong codebase)
│       └── UserProfile.kt       # Data class + FitnessGoal enum
├── domain/
│   ├── BmiCalculator.kt         # calculateBmi(), getBmiCategory()
│   └── WorkoutPlanGenerator.kt  # Object WorkoutPlangenerator — sinh 30-day plan theo FitnessGoal
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
        ├── OnboardingScreen.kt       # Nhập height/weight/targetWeight → tính BMI → suggested goal
        ├── WorkoutSetupScreen.kt     # Chọn equipment, frequency → finalize
        ├── DashboardScreen.kt        # Trang chủ: LazyColumn, TodaysWeight, streak, Calendar, Workouts, health metrics
        ├── PlannerScreen.kt          # Lịch 30 ngày chia theo tuần, LazyColumn
        ├── WorkoutDayDetailScreen.kt # Chi tiết ngày tập: danh sách exercise, nút DONE, rest timer có chọn duration
            ├── WorkoutSessionScreen.kt   # 3-phase session: PREPARING/EXERCISING/RESTING + GIF preview
        ├── LoadingScreen.kt          # "Picking the best exercises for you" — animated progress bar 2.5s
            ├── LibraryScreen.kt          # Thư viện bài tập (đã chuyển sang luồng data qua ViewModel/JSON)
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
**Ẩn navbar** ở: `onboarding`, `workout_setup`, `loading`, `day_detail/{dayNumber}`, `workout_session/{dayNumber}`, `edit_plan/{dayNumber}`, `workout_settings`

---

## Các vấn đề đã biết (Known Issues)

### ~~1. Trang chủ (Dashboard) cuộn không hợp lý~~ ✅ ĐÃ SỬA
- ~~`DashboardScreen` dùng `Column` thường thay vì `LazyColumn`~~
- **Đã sửa (lần 1, 05-07)**: Thêm `Modifier.verticalScroll(rememberScrollState())` vào root Column
- **Đã sửa (lần 2, 05-09)**: Chuyển hoàn toàn sang `LazyColumn` — `verticalScroll` bị xóa, mỗi section là một `item {}`
- ✅ Đã thêm Calendar + Workouts summary + TodaysWeight section

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
- ✅ Đã hoàn tất migrate text người dùng trong `DashboardScreen`, `PlannerScreen`, `WorkoutDayDetailScreen` sang `strings.xml` (2026-06-04)

### ~~5. Import & code hygiene trong MainActivity.kt~~ ✅ ĐÃ SỬA
- ~~Commented import thừa L34, fully-qualified names thừa ở L114/L123, indentation sai L122~~
- **Đã sửa** (2026-05-08): Xóa commented import, xóa commented route, dùng short names đã import, fix indent `composable("workout_setup")`

### ~~6. Xây dựng kế hoạch nên dựa trên mục tiêu, không phải chỉ số BMI~~ ✅ ĐÃ SỬA
- ~~Hiện tại `WorkoutPlanGenerator` dùng `BmiCategory` để chọn pool bài tập~~
- **Đã sửa** (2026-05-08): `FitnessGoal` enum thêm vào `UserProfile`, `WorkoutPlanGenerator` có 4 goal-based exercise pools, `WorkoutSetupScreen` có section chọn goal, `UserPreferences.saveGoal()` persist goal
- ✅ Đã bổ sung step onboarding cho target weight và birth year (2026-05-23, refactor flow)

### ~~7. WorkoutSessionScreen chưa tích hợp~~ ✅ ĐÃ SỬA
- ~~File tồn tại nhưng không có route trong `NavHost`~~
- **Đã sửa** (2026-05-09): Route `workout_session/{dayNumber}` thêm vào NavHost. Nút "START TIMED SESSION" trong `WorkoutDayDetailScreen`.
- ~~`TimedExercise` class riêng~~ → ✅ Đã xóa, `WorkoutSessionScreen` dùng `Exercise` trực tiếp (2026-05-09, commit 183b059)
- ~~Vẫn dùng hardcoded colors từ `theme.*`~~ → ✅ Đã chuyển sang `MaterialTheme.colorScheme` (2026-05-09)

### ~~9. HealthMetricsSection — steps/water hardcoded về "0"~~ ✅ ĐÃ SỬA (2026-05-23)
- ~~`MetricHorizontalCard` hiển thị giá trị `"0"` cứng, không lấy từ state~~
- **Đã sửa**:
   - Dashboard nối state thật từ `UserViewModel.todayHealthMetrics`
   - Nút water actions hoạt động thật: `+250ml`, `+500ml`, `SET GOAL`
   - Steps hỗ trợ 2 nguồn: `SENSOR` (live) và `MANUAL` (fallback)
   - Thêm status message rõ trạng thái: permission off / sensor unavailable / live tracking

### ~~10. WorkoutPlanGenerator có test override cho Day 1~~ ✅ ĐÃ SỬA (2026-05-24)
- ~~`WorkoutPlangenerator.generatePlan()` hardcode Day 1 thành 2 bài test với `localGifs`~~
- **Đã sửa**:
   - Bỏ nhánh test override Day 1, quay về flow chọn bài theo pool/goal chuẩn
   - Đồng bộ schema GIF sang `gifFileName` và map dữ liệu GIF từ repository trong lúc generate plan

### ~~11. Onboarding flow chưa khớp reference về chức năng/interaction~~ ✅ ĐÃ SỬA CƠ BẢN (2026-05-23)
- ~~Flow onboarding còn lệch lớn so với reference (ruler/wheel semantics)~~
- **Đã sửa**:
   - Refactor onboarding thành flow 4 bước: height, birth year, current weight, target weight
   - Giữ visual language Hyper Energy của dự án
- ⚠️ Còn cần hardening: tinh chỉnh inertia/validation edge cases để parity interaction sát hơn

### ~~12. Profile chưa có Weight chart + Record flow hoàn chỉnh~~ ✅ ĐÃ SỬA (2026-05-23)
- ~~Thiếu luồng record cân nặng + biểu đồ lịch sử~~
- **Đã sửa**:
   - `UserPreferences` có persistence `weightHistory`
   - `UserViewModel` có action `recordWeight(...)` + stream history
   - `ProfileScreen` có form record + chart lịch sử cân nặng
   - Bổ sung thêm chart tuần cho `STEPS` và `WATER` dựa trên `healthMetricsHistory`

### 15. Tracking lifecycle cần harden thêm theo app lifecycle thực tế ⚠️ CẦN THEO DÕI
- **Vấn đề**: Đã thêm `onResume/onStop` để start/stop tracking, nhưng chưa có chiến lược background tracking hoặc WorkManager
- **Ảnh hưởng**: Nếu yêu cầu tracking liên tục khi app background sẽ chưa đáp ứng
- **Hiện trạng**: Đã harden foreground lifecycle + tách rõ sensor availability/tracking active để tránh trạng thái UI sai (2026-06-04)
- **Cần làm**: Nếu cần background tracking thật thì thiết kế thêm service/worker + battery policy

### ~~16. Hygiene repo: file lỗi IDE bị commit nhầm~~ ✅ ĐÃ SỬA (2026-06-04)
- ~~**Vấn đề**: Các file lỗi IDE `.kotlin/errors/errors-1779632416724.log` (commit `c61b557`) và `.kotlin/errors/errors-1780040377201.log` (commit `8ec0988`) đã bị add vào repo~~
- **Đã sửa**:
   - Xóa 2 file log khỏi repo
   - Thêm ignore rule `.kotlin/errors/` để tránh tái diễn

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

### Quy tắc làm việc với AI Agent
- Sau khi hoàn thành code, **bắt buộc** chạy subagent (ưu tiên `QA-Testcode`) để review lại toàn bộ thay đổi so với yêu cầu ban đầu.
- Chỉ xem task là hoàn tất khi đã đối chiếu kết quả review và xử lý các điểm lệch quan trọng (nếu có).
- Nếu yêu cầu có điểm mơ hồ, thiếu dữ liệu, hoặc có nhiều cách hiểu, **bắt buộc dùng subagent `specify` để tạo câu hỏi làm rõ và hỏi lại người dùng trước khi làm**.
- Không tự suy đoán yêu cầu nghiệp vụ và không tự quyết định các thay đổi chưa được người dùng xác nhận.

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
- `androidx.compose.material:material-icons-extended` — Icons (Material)
- `com.composables:icons-lucide:1.1.0` — Lucide icons (thêm mới, dùng trong MetricHorizontalCard)
- `io.coil-kt:coil-compose:2.6.0` + `io.coil-kt:coil-gif:2.6.0` — render GIF cho WorkoutSessionScreen
- `com.google.ai.client.generativeai:generativeai:0.2.2` — Gemini AI (có trong deps nhưng chưa sử dụng)
- `androidx.room:room-ktx:2.6.1` — Room (có trong deps nhưng chưa sử dụng)

### Versions (sau downgrade commit 773f247)
- AGP: `8.7.3` | Kotlin: `2.0.21` | compileSdk: `35`
- coreKtx: `1.13.1` | lifecycleRuntime: `2.8.6` | activityCompose: `1.9.3` | composeBom: `2024.10.00`

---

## Trạng thái phát triển hiện tại

### Tổng quan theo màn hình

| Màn hình | Theme | Layout | Navigation | Data | Tổng thể |
|----------|-------|--------|-----------|------|----------|
| Onboarding | ✅ MaterialTheme | ✅ Multi-step flow | ✅ OK | ✅ Đã refactor 4-step (height/birth year/current weight/target weight) theo hướng parity reference | ✅ Hoạt động |
| Workout Setup | ✅ MaterialTheme | ✅ Scroll | ✅ OK | ✅ Goal + equipment + frequency | ✅ Hoạt động |
| Loading | ✅ MaterialTheme | ✅ Center column | ✅ Popbackstack clean | ✅ Fake 2.5s delay | ✅ Hoạt động |
| Dashboard | ✅ MaterialTheme | ✅ LazyColumn | ✅ OK | ✅ TodaysWeight + Calendar + Workouts + Steps/Water realtime từ ViewModel + permission/sensor fallback | ✅ Hoạt động |
| Planner | ✅ MaterialTheme | ✅ LazyColumn | ✅ OK | ✅ ViewModel | ✅ Hoạt động |
| Day Detail | ✅ MaterialTheme | ✅ LazyColumn | ✅ OK + onStartSession + onEditPlan + onWorkoutSettingsClick | ✅ ViewModel | ✅ Hoạt động |
| Edit Plan | ✅ MaterialTheme (partial token usage) | ✅ LazyColumn + reorder controls | ✅ Route `edit_plan/{dayNumber}` | ✅ Persist custom day plan vào SharedPreferences | ✅ Tích hợp |
| Workout Settings | ✅ MaterialTheme (partial token usage) | ✅ LazyColumn + section cards | ✅ Route `workout_settings` | ✅ Persist settings (music/coach/timer) qua UserPreferences | ✅ Tích hợp |
| Workout Session | ✅ MaterialTheme | ✅ Column | ✅ Route workout_session/{dayNumber} | ✅ Exercise (có category) từ DayPlan | ✅ Tích hợp |
| Library | ✅ MaterialTheme | ✅ OK | ✅ OK | ❌ 2 exercises hardcoded | ⚠️ Cần phát triển |
| Profile | ✅ MaterialTheme | ✅ OK | ✅ OK | ✅ Weight record/history chart + weekly Steps/Water charts (vẫn còn một phần stats placeholder) | ⚠️ Gần hoàn chỉnh MVP |

### Trạng thái theo component hệ thống

| Component | File | Trạng thái |
|-----------|------|-----------|
| Theme (Color.kt) | `ui/theme/Color.kt` | ✅ Định nghĩa đầy đủ. Screens đã chuyển sang MaterialTheme |
| Theme (Theme.kt) | `ui/theme/Theme.kt` | ✅ Dark/Light scheme hoạt động |
| Theme (Type.kt) | `ui/theme/Type.kt` | ⚠️ Chỉ có `bodyLarge`, phần còn lại bị comment |
| BottomNavbar | `ui/components/BottomNavbar.kt` | ✅ Home, Plan, Library, Me — hoạt động |
| Navigation | `MainActivity.kt` | ✅ 11 routes + wiring health metrics + ACTIVITY_RECOGNITION permission + lifecycle start/stop tracking |
| UserPreferences | `data/UserPreferences.kt` | ✅ Lưu profile + goal + completed days + weightHistory + healthMetricsHistory + custom day plan + workout settings |
| UserViewModel | `viewmodel/UserViewModel.kt` | ✅ StateFlow đầy đủ cho profile/plan/completedDays/weightHistory/todayHealthMetrics/healthMetricsHistory + step tracking + `updateDayPlan(...)` |
| WorkoutPlannerViewModel | `viewmodel/WorkoutPlannerViewModel.kt` | ✅ Quản lý edit mode/reorder/adjust/replace cho kế hoạch ngày |
| WorkoutSettingsViewModel | `viewmodel/WorkoutSettingsViewModel.kt` | ✅ Quản lý state background music/voice guide/timer + playback mock |
| StepCounterManager | `data/StepCounterManager.kt` | ✅ Bọc `SensorManager` (TYPE_STEP_COUNTER / TYPE_STEP_DETECTOR) với callback listener |
| WorkoutPlanGenerator | `domain/WorkoutPlanGenerator.kt` | ✅ WEIGHT_LOSS dùng `JefitFatToFitPlan` + các goal còn lại dùng pool theo `FitnessGoal`; map GIF runtime qua repository |
| JefitFatToFitPlan | `domain/JefitFatToFitPlan.kt` | ✅ Month-1 plan 30 ngày có metadata `title/difficulty/muscleGroup` |
| strings.xml | `res/values/strings.xml` | ✅ Đã migrate text người dùng mới cho Dashboard/Planner/WorkoutDayDetail + health metrics/loading states |

---

## Những gì đã hoàn thành (Sprint 3 — 2026-05-09, commits 183b059→941263f)

### Sprint 3 — Data Model & UI Polish — ✅ HOÀN THÀNH

1. **Exercise model hoàn chỉnh**:
   - Thêm `category: String` vào `Exercise` data class — giải quyết xung đột namespace với `TimedExercise`, mở đường cho Library filter
   - `durationSec` non-nullable — contract rõ ràng hơn
   - `WorkoutPlanGenerator` cập nhật tất cả 36 exercises với đúng category
   - `TimedExercise` bị xóa hoàn toàn, `WorkoutSessionScreen` dùng `Exercise` trực tiếp

2. **Target Weight trong Onboarding + Dashboard**:
   - `UserProfile.targetWeight: Float` — lưu/load qua `UserPreferences`
   - `OnboardingScreen` có slider TARGET WEIGHT (range 30–150kg, secondary color)
   - `TodaysWeightSection` trên Dashboard — current weight, "X kg to goal", emoji motivational

3. **Dashboard chuyển sang LazyColumn** — 6 sections, mỗi section là `item {}`

4. **HealthMetricsSection redesign** — `MetricHorizontalCard` với Lucide icons, layout 2 cột ngang

5. **Header standardization** — tất cả 7 screens đổi sang pattern "label phụ + tên 1 từ 28sp"

6. **Build toolchain downgrade** — compileSdk 35, AGP 8.7.3, Kotlin 2.0.21 (stability fix)

---

## Những gì đã hoàn thành (Sprint 2 — 2026-05-09, commit d227222)

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
| P1.1 | **Hardening onboarding parity** | `OnboardingScreen.kt` | Fine-tune behavior (ruler inertia, validation edge cases) để parity gần hơn reference |
| P1.2 | **Health tracking test matrix** | `UserPreferences.kt`, `UserViewModel.kt`, `MainActivity.kt` | Kiểm thử các case đổi ngày, deny permission, sensor unavailable, process recreate |
| P1.3 | **Process recreate regression pass cho Edit Plan & Workout Settings** | `EditPlanScreen.kt`, `WorkoutSettingsScreen.kt`, `WorkoutPlannerViewModel.kt`, `WorkoutSettingsViewModel.kt` | Sau khi đã fix stale event/playback cleanup, cần kiểm thử thêm case kill/recreate process |

### 🟠 Priority 2 — Sprint 4 (Enhancement)

| # | Task | Mô tả |
|---|------|-------|
| P2.1 | **Planner parity theo Training reference** | Cải thiện semantics START/current day, thông tin kcal/min và thứ tự hiển thị gần flow reference |
| P2.2 | **Phát triển Library** | Thêm exercise pools đầy đủ từ `WorkoutPlanGenerator` (4 pools × 9 bài), hiện theo category với filter |
| P2.3 | **Check-in Record section** | Thêm calendar heatmap hoặc streak indicator theo design `1deaf581` — biểu diễn completedDays |
| P2.4 | **Harden health/weight tracking data model** | Chuẩn hóa schema history (timestamp/source/device), xem xét chuyển từ SharedPreferences sang Room |

### 🟡 Priority 3 — Sprint 5 (Polish)

| # | Task | Mô tả |
|---|------|-------|
| P3.1 | **Premium & Offer modules** | Special Offer/Awards/upsell placement trong Profile sau khi core build ổn định |
| P3.2 | **Typography system** | Mở rộng `Type.kt` — thêm full typography set (headline, title, label styles) thay vì chỉ `bodyLarge` |
| P3.3 | **WorkoutSession UX** | Thêm rest timer giữa các bài trong WorkoutSessionScreen (hiện chỉ có trong WorkoutDayDetailScreen) |

---

## Open Quest Decisions (2026-05-23)

1. **Design parity strategy**
   - Bám sát design gốc về **chức năng** và **flow**
   - Giữ nguyên visual language hiện tại theo phong cách **Hyper Energy**

2. **Onboarding direction**
   - Onboarding phải đi theo flow reference (ưu tiên parity hành vi trước)

3. **Build strategy cho MVP**
   - ✅ Đã hoàn thành **Weight chart + Record** + **Steps/Water tracking MVP**
   - Tiếp theo ưu tiên hardening (i18n + test matrix + release cleanup) trước khi mở rộng premium/offer

4. **Health tracking strategy**
   - MVP dùng mô hình `sensor-first` với fallback manual
   - Tracking hiện định hướng foreground-lifecycle, chưa mở background tracking để tránh scope creep

---

## Quyết định quan trọng & Lý do

### ~~QĐ-1: Dùng `verticalScroll` thay vì `LazyColumn` cho Dashboard~~ ĐÃ ĐẢO NGƯỢC (2026-05-09)
- **Quyết định ban đầu**: `Column + verticalScroll` cho Dashboard
- **Đảo ngược**: Commit 773f247 chuyển hoàn toàn sang `LazyColumn` — mỗi section là một `item {}`
- **Lý do đảo ngược**: Dashboard đã có 6+ sections (Header, TodaysWeight, Streak, Calendar, WorkoutsSummary, HealthMetrics) — đủ điều kiện dùng `LazyColumn`. Dễ thêm sections mới mà không cần wrap lại

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

### ~~QĐ-8: Đổi tên class thay vì xóa trong WorkoutSessionScreen~~ ĐÃ ĐẢO NGƯỢC (2026-05-09)
- **Quyết định ban đầu**: Đổi `data class Exercise` → `TimedExercise` để tránh xung đột namespace
- **Đảo ngược**: Commit 183b059 xóa hoàn toàn `TimedExercise`, thêm field `category: String` vào `data.model.Exercise` và dùng trực tiếp trong `WorkoutSessionScreen`
- **Lý do đảo ngược**: Thêm `category` vào model gốc giải quyết cả hai vấn đề — không cần class trung gian, `WorkoutPlanGenerator` tự sinh exercises có category, `Library` có thể filter theo category

### QĐ-9: `DashboardScreen` nhận data qua parameters, không gọi `viewModel()` trực tiếp
- **Quyết định**: `DashboardScreen(completedDays, workoutPlan, onStartWorkout)` — data truyền từ `MainActivity`
- **Lý do**: Nhất quán với kiến trúc MVVM của dự án — ViewModel sống ở `MainActivity`, screens chỉ nhận data thuần túy qua params. Dễ test (preview không cần ViewModel). Tránh tạo thêm ViewModel instance trong screen.

### QĐ-10: Duration picker bên trong `RestTimerDialog`, không phải bước riêng
- **Quyết định**: 4 `FilterChip` (30s/60s/90s/120s) đặt ngay trong dialog, timer chạy song song với khả năng đổi
- **Lý do**: UX đơn giản hơn — không cần thêm bước "chọn trước, rồi bắt đầu". `LaunchedEffect(selectedDuration)` reset timer tự nhiên. User vẫn thấy countdown trong khi chọn.

### ~~QĐ-11: `TimedExercise.durationSec = reps * 3`~~ ĐÃ THAY THẾ (2026-05-09)
- **Quyết định cũ**: Convert `Exercise` → `TimedExercise` với `durationSec = reps * 3`
- **Thay thế**: `durationSec: Int` đã là field của `Exercise` (non-nullable), `WorkoutPlanGenerator` hardcode 60s cho tất cả exercises. Công thức `reps * 3` giữ lại trong `workout_session` route của `MainActivity` khi convert từ `DayPlan`

### QĐ-12: `WeeklyCalendarSection` dùng `java.time` (API 26+), không dùng Calendar cũ
- **Quyết định**: `LocalDate`, `DateTimeFormatter`, `DayOfWeek` từ `java.time`
- **Lý do**: Min SDK = 26 (Android 8.0) nên `java.time` khả dụng không cần desugaring. API hiện đại hơn `java.util.Calendar`, code gọn và immutable.

### QĐ-13: Header pattern thống nhất — label phụ + tên 1 từ
- **Quyết định**: Tất cả screens dùng cùng pattern: label phụ nhỏ (10sp, onBackground 40%, letterSpacing 3sp, Black) ở trên, tên màn hình 1 từ (28sp, Black, Italic) ở dưới
- **Lý do**: Commit 42b1678 chuẩn hóa headers — tên 2 từ split màu (ví dụ "MONTHLY TIMELINE" một từ primary) gây khó nhất quán khi đổi nội dung. Pattern label+title cho phép thêm context mà không thay đổi layout

### QĐ-14: `Exercise.category` là string, không phải enum
- **Quyết định**: `category: String` thay vì `enum class ExerciseCategory`
- **Lý do**: Pool exercises của `WorkoutPlanGenerator` có 4 category cố định (Cardio/Strength/Endurance/Maintenance) nhưng `LibraryScreen` có thể muốn category tùy ý trong tương lai. String đơn giản hơn, không cần migrate enum nếu thêm category mới

### QĐ-15: Downgrade build toolchain về phiên bản ổn định
- **Quyết định**: compileSdk 36→35, AGP 8.9.1→8.7.3, Kotlin 2.2.10→2.0.21
- **Lý do**: Phiên bản mới (AGP 8.9.1 + compileSdk 36) gây build instability trên môi trường dev. Downgrade về phiên bản GA ổn định. Đây là thay đổi kỹ thuật không ảnh hưởng đến runtime behavior

### QĐ-16: Steps tracking dùng chiến lược `sensor-first, manual-fallback`
- **Quyết định**: Ưu tiên lấy steps từ sensor (`TYPE_STEP_COUNTER` / `TYPE_STEP_DETECTOR`), khi thiếu permission hoặc không có sensor thì fallback manual increment
- **Lý do**: Cân bằng giữa độ chính xác dữ liệu và khả năng hoạt động trên nhiều thiết bị, không chặn UX khi sensor unavailable

### QĐ-17: Health metrics lưu theo ngày trong SharedPreferences (MVP)
- **Quyết định**: Lưu `healthMetricsHistory` dạng serialized map theo `epochDay`, kèm trim policy 90 ngày
- **Lý do**: Triển khai nhanh cho MVP, đủ để phục vụ dashboard/profile weekly charts; sẽ cân nhắc migrate Room ở phase hardening

### QĐ-18: Tracking lifecycle theo foreground Activity
- **Quyết định**: `onResume` refresh + start tracking (khi có permission), `onStop` stop tracking
- **Lý do**: Tránh chạy sensor listener không cần thiết ở background, giảm rủi ro pin và complexity trước release MVP

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

### 2026-06-04 — Remote Sync Batch #49 -> #61 (commits 461ea64 -> 7fda846)

#### 1) Plan provisioning + asset-driven plan generation (`f137ec9`, `6b50baa`, `6f67de4`, `8f57dbb`, `d78f6fe`)
- Thêm `PlanRepository` đọc plan JSON từ `app/src/main/assets/plans/*.json` và map theo `FitnessGoal`
- Bổ sung bộ plan assets mới: `weight-loss`, `muscle-gain`, `endurance`, `maintenance` (+ các file Jefit legacy)
- `WorkoutPlanGenerator` ưu tiên lấy plan từ assets qua `PlanRepository`, fallback sang registry/pool nếu thiếu
- `UserViewModel` thêm flow provisioning kế hoạch với state tiến trình 2 phase (`PlanProvisioningState`) và điều phối retry/consent
- `MainActivity` cập nhật wiring: `WorkoutSetupScreen` -> `LoadingScreen` -> `dashboard` sau khi provisioning hoàn tất

#### 2) Loading/provisioning UX + network gate (`3011487`)
- Thêm `NetworkStateHelper` để phân biệt trạng thái no-network và mobile-data consent trước khi prefetch media
- `LoadingScreen` mở rộng hành vi CTA theo trạng thái (`CONTINUE WITH MOBILE DATA` / `RETRY`)
- `UserPreferences` thêm cờ chữ ký cache hybrid GIF (`KEY_HYBRID_GIF_CACHE_READY`, `KEY_HYBRID_GIF_CACHE_SIGNATURE`)

#### 3) Workout session voice/music & phase hardening (`d521b4a`, `8ec0988`, `bfdc39e`, `4797589`)
- Thêm `TextToSpeechHelper` và tích hợp voice guide trong `WorkoutSessionScreen`
- Fix bug lặp câu "go" khi bắt đầu exercise phase
- Thêm background music runtime (assets `music/track_01..03.mp3`) và control từ `WorkoutSettingsViewModel`/`WorkoutSettingsScreen`
- Chỉnh logic countdown ban đầu để tránh skip nhầm phase PREPARING

#### 4) UI/flow polish trên dashboard & planner (`461ea64`, `7d7be32`, `f674201`, `5620ef7`, `7087d2d`, `e08fe3e`)
- Tích hợp entry mở `workout_settings` từ nhiều màn hình chính (dashboard/planner/profile/day detail/session)
- `EditPlanScreen` thêm drag-and-drop reorder
- `ExerciseInstructionOverlayScreen` và chi tiết workout được redesign
- Bottom nav bỏ nút add/workout setup trung tâm, giữ 4 tab `Home | Plan | Library | Me`
- Dashboard cập nhật vị trí `Weekly Goal` và thêm coach quote theo ngày

#### 5) Streak logic + màn hình tổng kết ngày tập (`8f1e80f`, `e1a2148`)
- Refactor logic streak (tăng streak theo ngày liên tiếp, reset khi ngắt quãng > 1 ngày)
- Persist + expose `currentStreak` qua `UserPreferences`/`UserViewModel` và hiển thị trên dashboard
- Thêm file màn hình mới `DayWorkoutSummaryScreen.kt` (đã có UI file, chưa thấy route trong `NavHost` ở nhánh `main` hiện tại)

#### 6) Merge timeline
- Các nhánh/PR đã được merge vào `main`: #49, #50, #51, #52, #53, #54, #55, #57, #58, #59, #60, #61

### 2026-06-04 — Local Hardening Sessions A -> F (current workspace)

#### 1) i18n + summary flow + repo hygiene
- Migrate text người dùng ở `DashboardScreen`, `PlannerScreen`, `WorkoutDayDetailScreen` sang `strings.xml`
- Wire `DayWorkoutSummaryScreen` vào `MainActivity` với route `day_workout_summary/{dayNumber}` và finish-flow từ `WorkoutSessionScreen`
- Xóa 2 file `.kotlin/errors/*.log` khỏi repo và thêm ignore rule `.kotlin/errors/`

#### 2) Health tracking lifecycle hardening
- `UserViewModel` thêm state `stepTrackingActive` để tách lifecycle-tracking khỏi sensor availability
- `StepCounterManager.start()` trả về kết quả đăng ký listener để xác định availability ngay từ lúc start
- `DashboardScreen` chỉnh badge/message để không còn mâu thuẫn giữa `LIVE SENSOR`, `SENSOR READY`, paused và manual mode

#### 3) Provisioning/loading + settings/edit-plan regression fixes
- `LoadingScreen` luôn hiển thị status copy và cho retry cả ở case no-network lẫn generic provisioning error
- `WorkoutSettingsViewModel` đổi nhánh tắt background music sang `stopMusic()` để release player/state triệt để
- `WorkoutPlannerViewModel` + `EditPlanScreen` consume `savedExercises` như one-shot event để tránh replay save khi reopen màn hình

#### 4) Day detail theme consistency
- `WorkoutDayDetailScreen` bỏ các token màu cũ còn sót (`OrangePrimary`, `OrangeGlow`, hardcoded white/gray) để dùng `MaterialTheme.colorScheme` nhất quán

### 2026-05-24 — Planner & Settings Expansion (commits c61b557 → 542cd77)

#### 1) `feat(planner): add edit plan functionality with reorderable list` (c61b557)
- Thêm route mới `edit_plan/{dayNumber}` và tích hợp từ `WorkoutDayDetailScreen` qua callback `onEditPlan`
- Tạo màn hình mới `EditPlanScreen` với các hành vi: reorder bài tập, tăng/giảm reps hoặc duration, replace bài từ DB, save về ngày hiện tại
- Tạo `WorkoutPlannerViewModel` để quản lý `editablePlan`, `isEditMode`, `savedExercises`
- `UserPreferences` bổ sung lưu/đọc custom day plan (`saveCustomDayPlan`, `getCustomDayPlan`) bằng JSON
- `UserViewModel` thêm `updateDayPlan(...)` và merge custom day plan vào plan khi `loadUserProfile()`
- `DayPlan` mở rộng metadata (`title`, `difficulty`, `muscleGroup`), `WorkoutExercise` thêm `description`
- Thêm màn hình/route `workout_settings` + `WorkoutSettingsScreen` và `WorkoutSettingsViewModel`
- `UserPreferences` mở rộng persistence cho settings: background music, voice guide, coach, sound effect, auto counting, rest timer, countdown
- `WorkoutPlanGenerator` cập nhật hướng tạo plan: `WEIGHT_LOSS` dùng `buildJefitMonth1Plan()` từ file mới `JefitFatToFitPlan.kt`; các goal khác theo pool hiện có

#### 2) `Merge pull request #48 from wind5293/feature/workout-setup` (542cd77)
- Merge toàn bộ tính năng Edit Plan + Workout Settings vào `main`
- Cập nhật navigation để ẩn bottom nav ở `edit_plan/{dayNumber}` và `workout_settings`

### 2026-05-24 — Remote Sync Updates (commits 209af48 → 18191b3)

#### 1) `feat(ui): add img for WorkoutSessionScreen from github release` (209af48)
- `FitFlowApplication` thêm `imageLoader` dùng chung toàn app (Coil + GIF decoder + disk/memory cache)
- Chuẩn hóa schema GIF ở tầng workout: `WorkoutExercise` dùng `gifFileName` thay cho `localGifs`
- `WorkoutPlanGenerator` tra `gifFileName` từ repository theo tên bài để gắn ảnh động cho bài tập runtime
- `WorkoutSessionScreen`/`WorkoutSessionViewModel` nối lại luồng load GIF theo schema mới

#### 2) `fix(ui): change gifUrl value data and image for ExerciseExpandableItem` (d0a3c1e)
- `ExerciseInstructionOverlayScreen` đổi sang lấy URL qua `GifUrlHelper` + dùng global `imageLoader`
- `WorkoutDayDetailScreen` thêm preview ảnh/GIF thật trong `ExerciseExpandableItem` (thay placeholder `IMG`)
- Sửa `HeaderAndSummarySection` để dùng đúng `dayPlan.dayNumber` thay vì hardcode `1`

#### 3) `Merge pull request #47 from wind5293/fix/ui` (18191b3)
- Tích hợp toàn bộ fix GIF URL + image preview vào `main`
- Resolve lỗi compile do lệch schema giữa `localGifs` và `gifFileName` sau khi đồng bộ remote

### 2026-05-23 — MVP Health Tracking Integration (local implementation)

#### 1) `feat(data): add daily health model + sensor manager`
- Tạo `DailyHealthMetrics` + `StepSource` để chuẩn hóa dữ liệu ngày (steps, water intake/goal, source)
- Tạo `StepCounterManager` bọc `SensorManager` cho step counter/detector với callback listener

#### 2) `feat(data): extend UserPreferences for health history`
- Thêm persistence cho `healthMetricsHistory`, `step baseline`, `stepSensorEnabled`
- Bổ sung APIs: `getTodayHealthMetrics`, `getHealthMetricsHistory`, `addWater`, `setWaterGoal`, `setTodaySteps`, `incrementTodaySteps`
- Áp dụng trim policy 90 ngày cho health history

#### 3) `feat(viewmodel): wire full steps/water state machine`
- `UserViewModel` thêm state: `todayHealthMetrics`, `healthMetricsHistory`, `activityRecognitionGranted`, `stepSensorEnabled`
- Thêm actions: start/stop tracking, add manual steps, add water, set water goal, refresh metrics
- Đồng bộ trạng thái sensor-enabled khi permission deny hoặc sensor unavailable

#### 4) `feat(ui): dashboard health cards now functional`
- `DashboardScreen` nhận data thật từ ViewModel, hiển thị steps/water realtime
- Water actions hoạt động: `+250ml`, `+500ml`, `SET GOAL`
- Step card hiển thị `LIVE` khi source là sensor, fallback manual khi cần
- Thêm status message cho 3 trạng thái: permission off / sensor unavailable / live tracking

#### 5) `feat(ui): profile adds weekly health charts`
- `ProfileScreen` nhận `healthMetricsHistory` và render chart tuần cho `STEPS` + `WATER`
- Kết hợp với weight record/history flow đã hoàn thiện để tạo tracking hub cơ bản cho MVP

#### 6) `feat(app): activity recognition wiring + lifecycle handling`
- `AndroidManifest.xml` thêm `ACTIVITY_RECOGNITION`
- `MainActivity` request permission, truyền state/actions health metrics xuống Dashboard/Profile
- Thêm lifecycle hooks `onResume`/`onStop` để refresh/start/stop tracking hợp lý

### 2026-05-23 — Recent GitHub Updates (commits 47e675b → 5418841)

#### 1) `feat(ui): redesign workout session with 3-phase flow and dark theme` (47e675b)
- `WorkoutSessionScreen.kt` refactor lớn sang state machine 3 phase: `PREPARING` → `EXERCISING` → `RESTING`
- Thêm hằng số cấu hình: `PREPARE_SECONDS = 5`, `REST_SECONDS = 25`
- Luồng tập hỗ trợ cả bài theo reps và bài theo thời gian (`durationSec`)
- Thêm UI riêng cho màn hình nghỉ (`RestScreen`) và vùng media (`ExerciseMediaArea`)
- Tích hợp hiển thị GIF từ `assets/gifs/*` qua Coil (`AsyncImage`, `GifDecoder`/`ImageDecoderDecoder`)
- Nâng cấp controls: previous, pause/play/check, next, skip, +20s khi nghỉ

#### 2) `refactor(domain): restructure WorkoutPlanGenerator for scalability` (57a06fd)
- Tách cấu trúc plan thành `ExercisePool(exercises, restDays)`
- Tạo helper `ex(...)` để rút gọn khai báo `WorkoutExercise`
- Đưa mapping goal → pool vào `planRegistry` (dễ mở rộng thêm goal/pool mới)
- Tách logic chọn bài thành `pickExercises(...)`
- Thêm `TEST override` cho Day 1 dùng dữ liệu có `localGifs` (`band_cross-over_1.gif`, `barbell_shoulder_press_1.gif`)
- Bổ sung sample asset: `app/src/main/assets/gifs/band_cross-over_1.gif`

#### 3) `style(ui): replace Material icons with Lucide icons in BottomNavbar` (5bd4081)
- `BottomNavbar.kt` đổi icon `Home/Plan/Add/Library/Me` từ Material Icons sang Lucide (`House`, `Calendar`, `Plus`, `List`, `User`)
- Giữ nguyên route điều hướng và layout navbar

#### 4) `feat(data): add localGifs support for exercises and import sample assets` (1855f0b)
- `WorkoutExercise.kt` thêm field mới: `localGifs: List<String> = emptyList()`
- Mục tiêu: cho phép mỗi bài tập có danh sách asset GIF local phục vụ media preview trong session

#### 5) `chore: update navigation routes and IDE configurations` (5418841)
- `MainActivity.kt` mở lại route `workout_session/{dayNumber}` (không còn bị comment)
- Route mới truyền trực tiếp `dayPlan.workoutExercises` vào `WorkoutSessionScreen`
- `onFinish` vẫn giữ logic mark complete + popBackStack

### 2026-05-09 — Sprint 3: Data Model & UI Polish (commits 183b059 → 941263f)

#### Exercise.kt (`data/model/`)
- ✅ Thêm field `category: String` (first param) — phân loại Cardio/Strength/Endurance/Maintenance
- ✅ Đổi `durationSec: Int?` → `durationSec: Int` (non-nullable, bắt buộc khai báo)

#### WorkoutPlanGenerator.kt
- ✅ Cập nhật tất cả 4 pools (36 exercises) — thêm `category` đầu mỗi constructor call

#### WorkoutSessionScreen.kt
- ✅ Xóa hoàn toàn `data class TimedExercise` — dùng `data.model.Exercise` trực tiếp
- ✅ Cập nhật `sampleExercises()` dùng `Exercise` với đủ fields (category, name, sets, reps, kcal, durationSec)
- ✅ `remaining` type khai báo tường minh: `var remaining: Int by remember {...}`

#### MainActivity.kt
- ✅ Xóa `import TimedExercise`, thêm `import Exercise`
- ✅ `workout_session` route: tạo `Exercise(category=ex.category, ..., durationSec=ex.reps*3)` thay vì `TimedExercise`
- ✅ Fix indentation `hideNav` condition (align `||` về cùng mức)
- ✅ `dashboard` route: thêm `userProfile` param → `DashboardScreen(completedDays, workoutPlan, userProfile, onStartWorkout)`
- ✅ `onboarding` route: `onComplete { h, w, tw }` — nhận thêm `targetWeight`

#### DashboardScreen.kt
- ✅ **Chuyển sang `LazyColumn`**: xóa `verticalScroll(rememberScrollState())`, wrap mỗi section trong `item {}`
- ✅ Thêm `userProfile: UserProfile? = null` param
- ✅ Thêm `TodaysWeightSection(userProfile)` — hiển thị current weight, "X kg to goal", emoji motivational (🔥/💪/🎉)
- ✅ Redesign `HealthMetricsSection` → `MetricHorizontalCard` layout: 2 card ngang dùng Lucide icons (`Footprints`, `GlassWater`), button "SET A GOAL" / "UNLOCK"
- ⚠️ Steps/water value hiện hardcoded `"0"` — không nối với state nữa
- ✅ `HeaderSection`: "DASHBOARD" (1 từ, onBackground) thay vì split "FITFLOW" + "DAILY"

#### OnboardingScreen.kt
- ✅ Thêm `targetWeight: Float` state (default 60f, range 30..150)
- ✅ Thêm slider TARGET WEIGHT (secondary color) với label và value display
- ✅ Callback đổi thành `onComplete(height, weight, targetWeight)`
- ✅ Layout header: label "CRAFT YOUR MONTHLY JOURNEY" trên, logo "FITFLOW" 28sp dưới (thứ tự đảo so với trước)

#### UserProfile.kt
- ✅ Thêm field `targetWeight: Float`

#### UserPreferences.kt
- ✅ Thêm `KEY_TARGET_WEIGHT = "target_weight"`
- ✅ `saveUserProfile(height, weight, targetWeight)` — persist targetWeight
- ✅ `getUserProfile()` load targetWeight từ prefs

#### UserViewModel.kt
- ✅ `saveProfile(height, weight, targetWeight)` — forward targetWeight xuống UserPreferences

#### Headers chuẩn hóa (commit 42b1678, tất cả screens)
- ✅ Pattern chung: label phụ nhỏ (10sp, alpha 0.4, letterSpacing 3sp) + tên màn hình 1 từ (28sp, Black, Italic)
- ✅ DashboardScreen: "FITFLOW" header → "DASHBOARD"
- ✅ PlannerScreen: "MONTHLY TIMELINE" → "PLANNER"
- ✅ LibraryScreen: "DATABASE" → "LIBRARY" (label: "KNOWLEDGE")
- ✅ ProfileScreen: "SUBJECT ZERO" → "PROFILE" (label: "IDENTITY")
- ✅ WorkoutDayDetailScreen: "WORKOUT SESSION" 22sp → "WORKOUT/SESSION" 2 màu 28sp (label: "DAY X")
- ✅ WorkoutSetupScreen: font size 32sp → 28sp
- ✅ LoadingScreen: "EXERCISES FOR YOU" split 2 màu

#### Build toolchain (commit 773f247)
- ✅ compileSdk: 36 → 35
- ✅ AGP: 8.9.1 → 8.7.3 | Kotlin: 2.2.10 → 2.0.21
- ✅ Thêm dependency `com.composables:icons-lucide:1.1.0`

---

### 2026-05-09 — Feature Sprint (commit d227222)

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
