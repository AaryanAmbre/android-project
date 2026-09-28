# 🎓 Simple Student Record Management System (TY BSc IT Project)
> **A Lightweight, 2-Screen Android App built with Kotlin, Jetpack Compose, Room (SQLite), and ViewModel.**

---

## 📌 File Overview (Only 5 Files to Learn!)

```text
com.example.studentrecord/
│
├── data/
│   ├── Student.kt         # File 1: Database Entity (Table schema)
│   ├── StudentDao.kt      # File 2: Database DAO (SQL Queries)
│   └── AppDatabase.kt     # File 3: Room Database Helper (Singleton)
│
├── viewmodel/
│   └── StudentViewModel.kt# File 4: ViewModel (Manages state and DB operations)
│
└── MainActivity.kt        # File 5: UI with 2 Simple Screens (List & Form)
```

---

## 📱 The 2 Screens Explained

### **Screen 1: Student List & Dashboard (`StudentListScreen`)**
1. **Summary Card at Top:** Calculates and displays Total Students count and overall Class Average Marks %.
2. **Search Bar:** Live search filtering by Name, Roll Number, or Course.
3. **Student Cards List:** Displays each student's details, marks in Java/DBMS/OS, Total Marks (/300), and Average Percentage.
4. **Action Buttons:** `Edit` button (opens Screen 2 with prefilled data) and `Delete` button (removes record immediately from SQLite database).
5. **Floating Action Button (`+ Add`):** Switches state to Screen 2.

### **Screen 2: Add / Edit Student Form (`AddEditStudentScreen`)**
1. **Input Fields:** Name, Roll Number, Course, Java Marks, DBMS Marks, OS Marks.
2. **Form Validation:** Checks for blank fields and enforces 0–100 mark bounds.
3. **Save Button:**
   * If adding new student (`id == 0`), inserts a new row into SQLite.
   * If editing existing student (`id > 0`), updates the existing row in SQLite.
4. **Back Button (`←`):** Returns back to Screen 1.

---

## ⚙️ How Data Flows (Explained simply for Viva)

```text
User Types/Clicks on UI (MainActivity.kt)
             │
             ▼
      StudentViewModel
             │
             ▼
        StudentDao
             │
             ▼
   Room (SQLite Database)
```

1. **User interacts with UI** in `MainActivity.kt`.
2. **UI triggers ViewModel methods** (`addStudent`, `updateStudent`, `deleteStudent`).
3. **ViewModel calls `StudentDao`** on a background thread using Coroutines (`viewModelScope.launch`).
4. **Room updates SQLite Database**, and because `getAllStudents()` returns a `Flow`, the UI automatically refreshes itself!

---
*Created for TY BSc IT Student Record Management System.*
