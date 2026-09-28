package com.example.studentrecord

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.studentrecord.data.AppDatabase
import com.example.studentrecord.data.Student
import com.example.studentrecord.ui.theme.StudentRecordManagementTheme
import com.example.studentrecord.viewmodel.StudentViewModel
import com.example.studentrecord.viewmodel.StudentViewModelFactory
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: StudentViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        StudentViewModelFactory(database.studentDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudentRecordManagementTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StudentRecordApp(viewModel = viewModel)
                }
            }
        }
    }
}

// Simple state to switch between Screen 1 (List) and Screen 2 (Add/Edit)
enum class AppScreen { LIST, ADD_EDIT }

@Composable
fun StudentRecordApp(viewModel: StudentViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.LIST) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }

    when (currentScreen) {
        AppScreen.LIST -> {
            StudentListScreen(
                viewModel = viewModel,
                onAddStudentClick = {
                    selectedStudent = null
                    currentScreen = AppScreen.ADD_EDIT
                },
                onEditStudentClick = { student ->
                    selectedStudent = student
                    currentScreen = AppScreen.ADD_EDIT
                }
            )
        }
        AppScreen.ADD_EDIT -> {
            AddEditStudentScreen(
                studentToEdit = selectedStudent,
                onSaveStudent = { student ->
                    if (student.id == 0) {
                        viewModel.addStudent(student)
                    } else {
                        viewModel.updateStudent(student)
                    }
                    currentScreen = AppScreen.LIST
                },
                onBackClick = { currentScreen = AppScreen.LIST }
            )
        }
    }
}

// ==========================================
// SCREEN 1: STUDENT LIST & DASHBOARD
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(
    viewModel: StudentViewModel,
    onAddStudentClick: () -> Unit,
    onEditStudentClick: (Student) -> Unit
) {
    val students by viewModel.studentsList.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val totalStudents = students.size
    val averageScore = if (students.isNotEmpty()) {
        students.asSequence().map { it.averageMarks }.average().toFloat()
    } else 0f

    val filteredStudents = students.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.rollNumber.contains(searchQuery, ignoreCase = true) ||
                it.course.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Records", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddStudentClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+ Add", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Dashboard Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Students", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = totalStudents.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Class Average", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f%%", averageScore),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by Name, Roll No, or Course...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Student List
            if (filteredStudents.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "No students added yet.\nTap + Add to create one!" else "No student found matching '$searchQuery'",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = student.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Roll No: ${student.rollNumber}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Course: ${student.course}", style = MaterialTheme.typography.bodyMedium)

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Marks: Java (${student.javaMarks}) | DBMS (${student.dbmsMarks}) | OS (${student.osMarks})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Text(
                                    text = "Total: ${student.totalMarks}/300 (${String.format(Locale.getDefault(), "%.1f%%", student.averageMarks)})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = { onEditStudentClick(student) },
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text("Edit")
                                    }
                                    Button(
                                        onClick = { viewModel.deleteStudent(student) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 2: ADD / EDIT STUDENT FORM
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStudentScreen(
    studentToEdit: Student?,
    onSaveStudent: (Student) -> Unit,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf(studentToEdit?.name ?: "") }
    var rollNumber by remember { mutableStateOf(studentToEdit?.rollNumber ?: "") }
    var course by remember { mutableStateOf(studentToEdit?.course ?: "") }
    var javaMarks by remember { mutableStateOf(studentToEdit?.javaMarks?.toString() ?: "") }
    var dbmsMarks by remember { mutableStateOf(studentToEdit?.dbmsMarks?.toString() ?: "") }
    var osMarks by remember { mutableStateOf(studentToEdit?.osMarks?.toString() ?: "") }

    var errorMessage by remember { mutableStateOf("") }
    val isEdit = studentToEdit != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Student" else "Add New Student") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Student Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = rollNumber,
                onValueChange = { rollNumber = it },
                label = { Text("Roll Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = course,
                onValueChange = { course = it },
                label = { Text("Course (e.g. TY BSc IT)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = javaMarks,
                onValueChange = { javaMarks = it },
                label = { Text("Java Marks (0-100)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = dbmsMarks,
                onValueChange = { dbmsMarks = it },
                label = { Text("DBMS Marks (0-100)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = osMarks,
                onValueChange = { osMarks = it },
                label = { Text("OS Marks (0-100)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val jMarks = javaMarks.toIntOrNull()
                    val dMarks = dbmsMarks.toIntOrNull()
                    val oMarks = osMarks.toIntOrNull()

                    if (name.isBlank() || rollNumber.isBlank() || course.isBlank()) {
                        errorMessage = "Please fill in all details."
                    } else if (jMarks == null || jMarks !in 0..100 ||
                        dMarks == null || dMarks !in 0..100 ||
                        oMarks == null || oMarks !in 0..100
                    ) {
                        errorMessage = "Marks must be numbers between 0 and 100."
                    } else {
                        val student = Student(
                            id = studentToEdit?.id ?: 0,
                            name = name.trim(),
                            rollNumber = rollNumber.trim(),
                            course = course.trim(),
                            javaMarks = jMarks,
                            dbmsMarks = dMarks,
                            osMarks = oMarks
                        )
                        onSaveStudent(student)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(if (isEdit) "UPDATE STUDENT" else "SAVE STUDENT", fontWeight = FontWeight.Bold)
            }
        }
    }
}
