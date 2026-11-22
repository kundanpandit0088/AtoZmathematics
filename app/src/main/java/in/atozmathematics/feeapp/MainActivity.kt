@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.atozmathematics.feeapp

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.atozmathematics.feeapp.data.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { AtoZApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtoZApp() {
    val context = LocalContext.current
    val dao = remember { AppDatabase.get(context).dao() }
    val scope = rememberCoroutineScope()
    val students by dao.observeStudents().collectAsState(initial = emptyList())
    val payments by dao.observePayments().collectAsState(initial = emptyList())
    var page by remember { mutableStateOf("Home") }
    var hindi by remember { mutableStateOf(false) }
    var showAdd by remember { mutableStateOf(false) }
    var showPayment by remember { mutableStateOf(false) }

    val collected = payments.sumOf { it.amount }
    val labels = if (hindi) listOf("होम","विद्यार्थी","फीस","रिपोर्ट") else listOf("Home","Students","Fees","Reports")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column {
                    Text("AtoZ Mathematics")
                    Text(if (hindi) "ट्यूशन फीस मैनेजमेंट" else "Tuition Fee Management", style = MaterialTheme.typography.labelSmall)
                } },
                navigationIcon = {
                    if (page == "Portal" || page == "Reminders") {
                        IconButton(onClick = { page = "Home" }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                    }
                },
                actions = {
                    TextButton(onClick = { hindi = !hindi }) { Text(if (hindi) "EN" else "हिं") }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                labels.forEachIndexed { i, label ->
                    val keys = listOf("Home","Students","Fees","Reports")
                    NavigationBarItem(
                        selected = page == keys[i],
                        onClick = { page = keys[i] },
                        icon = { Icon(listOf(Icons.Default.Home, Icons.Default.People, Icons.Default.CurrencyRupee, Icons.Default.Assessment)[i], null) },
                        label = { Text(label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (page == "Students") FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add student")
            }
            if (page == "Fees") FloatingActionButton(onClick = { showPayment = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add payment")
            }
        }
    ) { padding ->
        when (page) {
            "Home" -> Dashboard(
                Modifier.padding(padding), students.size, collected,
                students.count { student ->
                    val month = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(Date())
                    outstanding(student, payments, "Monthly Fee", month) > 0 ||
                        outstanding(student, payments, "Course Fee", "Course") > 0
                },
                onStudents = { page = "Students" }, onFees = { page = "Fees" },
                onPortal = { page = "Portal" }, onReminders = { page = "Reminders" }, hindi = hindi
            )
            "Students" -> StudentList(Modifier.padding(padding), students, hindi)
            "Fees" -> FeeList(Modifier.padding(padding), payments, students)
            "Reports" -> ReportPage(Modifier.padding(padding), students, payments, hindi)
            "Portal" -> StudentPortalScreen(Modifier.padding(padding), students, payments, hindi) { student, monthlyDue, courseDue ->
                openWhatsAppReminder(context, student, monthlyDue, courseDue, hindi)
            }
            "Reminders" -> WhatsAppReminderScreen(Modifier.padding(padding), students, payments, hindi) { student, monthlyDue, courseDue ->
                openWhatsAppReminder(context, student, monthlyDue, courseDue, hindi)
            }
        }
    }

    if (showAdd) AddStudentDialog(hindi, onDismiss = { showAdd = false }) { name, phone, cls, monthly, course ->
        scope.launch { dao.addStudent(Student(name=name, phone=phone, className=cls, monthlyFee=monthly, courseFee=course)) }
        showAdd = false
    }
    if (showPayment) PaymentDialog(students, payments, hindi, onDismiss = { showPayment = false }) { student, amount, type, period ->
        scope.launch {
            dao.addPayment(Payment(studentId=student.id, amount=amount, feeType=type, period=period,
                receiptNumber="AZM-${System.currentTimeMillis().toString().takeLast(8)}"))
        }
        showPayment = false
    }
}

@Composable
fun Dashboard(modifier: Modifier, count: Int, collected: Double, dueStudents: Int, onStudents:()->Unit, onFees:()->Unit, onPortal:()->Unit, onReminders:()->Unit, hindi: Boolean) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Welcome, Admin", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Students", count.toString(), Modifier.weight(1f))
            StatCard("Collected", "₹${"%.0f".format(collected)}", Modifier.weight(1f))
        }
        StatCard("Students with fee due (basic check)", dueStudents.toString(), Modifier.fillMaxWidth())
        Button(onClick = onStudents, modifier = Modifier.fillMaxWidth()) { Text(if (hindi) "विद्यार्थी प्रबंधित करें" else "Manage Students") }
        OutlinedButton(onClick = onFees, modifier = Modifier.fillMaxWidth()) { Text(if (hindi) "फीस जमा करें" else "Collect Fee") }
        OutlinedButton(onClick = onPortal, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.AccountCircle, null); Spacer(Modifier.width(8.dp)); Text(if (hindi) "Student Fee Portal" else "Student Fee Portal") }
        OutlinedButton(onClick = onReminders, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Chat, null); Spacer(Modifier.width(8.dp)); Text(if (hindi) "WhatsApp Fee Reminders" else "WhatsApp Fee Reminders") }
        Text(if (hindi) "डेटा अभी इस डिवाइस में ऑफलाइन सेव होता है।" else "Offline-first database is active on this device.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
fun StudentList(modifier: Modifier, students: List<Student>, hindi: Boolean) {
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text(if (hindi) "विद्यार्थी सूची" else "Student Directory", style = MaterialTheme.typography.headlineSmall) }
        if (students.isEmpty()) item { Text(if (hindi) "अभी कोई विद्यार्थी नहीं जोड़ा गया। + दबाएँ।" else "No students yet. Tap + to add one.") }
        items(students) { s ->
            ElevatedCard {
                ListItem(
                    headlineContent = { Text(s.name) },
                    supportingContent = { Text("Student ID: ${s.id}  •  ${s.className}  •  ${s.phone}") },
                    trailingContent = { Text("₹${"%.0f".format(s.monthlyFee)} / mo") },
                    leadingContent = { Icon(Icons.Default.AccountCircle, null) }
                )
            }
        }
    }
}

@Composable
fun FeeList(modifier: Modifier, payments: List<Payment>, students: List<Student>) {
    val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(Date())
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Fee Due Overview", style = MaterialTheme.typography.headlineSmall) }
        item {
            students.forEach { s ->
                val monthlyDue = outstanding(s, payments, "Monthly Fee", currentMonth)
                val courseDue = outstanding(s, payments, "Course Fee", "Course")
                ElevatedCard(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(s.name, style = MaterialTheme.typography.titleMedium)
                        Text("Monthly due ($currentMonth): ₹${"%.2f".format(monthlyDue)}")
                        Text("Course balance: ₹${"%.2f".format(courseDue)}")
                        Text("Total due: ₹${"%.2f".format(monthlyDue + courseDue)}",
                            color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        item { Text("Payment History", style = MaterialTheme.typography.headlineSmall) }
        if (payments.isEmpty()) item { Text("No payments recorded yet. Tap + to collect a fee.") }
        items(payments) { p ->
            val name = students.firstOrNull { it.id == p.studentId }?.name ?: "Student"
            ElevatedCard {
                ListItem(
                    headlineContent = { Text(name) },
                    supportingContent = { Text("${p.feeType} • ${p.period} • ${p.receiptNumber}") },
                    trailingContent = { Text("₹${"%.0f".format(p.amount)}") }
                )
            }
        }
    }
}

@Composable
fun ReportPage(modifier: Modifier, students: List<Student>, payments: List<Payment>, hindi: Boolean) {
    val total = payments.sumOf { it.amount }
    val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(Date())
    val monthlyDue = students.sumOf { outstanding(it, payments, "Monthly Fee", currentMonth) }
    val courseDue = students.sumOf { outstanding(it, payments, "Course Fee", "Course") }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (hindi) "रिपोर्ट" else "Reports", style = MaterialTheme.typography.headlineSmall)
        StatCard(if (hindi) "कुल विद्यार्थी" else "Total Students", students.size.toString(), Modifier.fillMaxWidth())
        StatCard(if (hindi) "कुल जमा फीस" else "Total Fee Collected", "₹${"%.2f".format(total)}", Modifier.fillMaxWidth())
        StatCard(if (hindi) "कुल भुगतान एंट्री" else "Payment Entries", payments.size.toString(), Modifier.fillMaxWidth())
        StatCard(if (hindi) "मासिक बकाया ($currentMonth)" else "Monthly Due ($currentMonth)", "₹${"%.2f".format(monthlyDue)}", Modifier.fillMaxWidth())
        StatCard(if (hindi) "कोर्स फीस बकाया" else "Course Fee Due", "₹${"%.2f".format(courseDue)}", Modifier.fillMaxWidth())
        StatCard(if (hindi) "कुल बकाया" else "Total Outstanding", "₹${"%.2f".format(monthlyDue + courseDue)}", Modifier.fillMaxWidth())
        Text("Monthly due is calculated for the current month. Course due is course fee minus all course-fee instalments.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun AddStudentDialog(hindi: Boolean, onDismiss:()->Unit, onSave:(String,String,String,Double,Double)->Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var cls by remember { mutableStateOf("") }
    var monthly by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (hindi) "नया विद्यार्थी" else "Add Student") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name=it }, label={Text(if(hindi) "नाम" else "Name")}, singleLine=true)
                OutlinedTextField(phone, { phone=it }, label={Text(if(hindi) "मोबाइल नंबर" else "Phone")}, singleLine=true, keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Phone))
                OutlinedTextField(cls, { cls=it }, label={Text(if(hindi) "कक्षा / कोर्स" else "Class / Course")}, singleLine=true)
                OutlinedTextField(monthly, { monthly=it }, label={Text(if(hindi) "मासिक फीस" else "Monthly fee")}, singleLine=true, keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))
                OutlinedTextField(course, { course=it }, label={Text(if(hindi) "कोर्स फीस" else "Course fee")}, singleLine=true, keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))
            }
        },
        confirmButton = { TextButton(onClick = {
            if (name.isNotBlank()) onSave(name.trim(), phone.trim(), cls.trim(), monthly.toDoubleOrNull() ?: 0.0, course.toDoubleOrNull() ?: 0.0)
        }) { Text(if(hindi) "सेव" else "Save") } },
        dismissButton = { TextButton(onClick=onDismiss) { Text(if(hindi) "रद्द" else "Cancel") } }
    )
}

@Composable
fun PaymentDialog(students: List<Student>, payments: List<Payment>, hindi: Boolean, onDismiss:()->Unit, onSave:(Student,Double,String,String)->Unit) {
    var selected by remember { mutableStateOf(students.firstOrNull()?.id ?: 0L) }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Monthly Fee") }
    var period by remember { mutableStateOf(SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(Date())) }
    val chosen = students.firstOrNull { it.id == selected }
    val due = chosen?.let { outstanding(it, payments, type, if (type == "Course Fee") "Course" else period) } ?: 0.0
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if(hindi) "फीस जमा करें" else "Collect Fee") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (students.isEmpty()) Text(if(hindi) "पहले विद्यार्थी जोड़ें।" else "Add a student first.")
                else {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded=expanded, onExpandedChange={expanded=!expanded}) {
                        OutlinedTextField(
                            value=students.firstOrNull { it.id==selected }?.name ?: "",
                            onValueChange={}, readOnly=true, label={Text("Student")},
                            trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(expanded)},
                            modifier=Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded=expanded, onDismissRequest={expanded=false}) {
                            students.forEach { s -> DropdownMenuItem(text={Text(s.name)}, onClick={selected=s.id; expanded=false}) }
                        }
                    }
                    OutlinedTextField(amount,{amount=it},label={Text(if(hindi) "राशि (₹)" else "Amount (₹)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true)
                    var feeExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded=feeExpanded, onExpandedChange={feeExpanded=!feeExpanded}) {
                        OutlinedTextField(
                            value=type, onValueChange={}, readOnly=true,
                            label={Text(if(hindi) "फीस का प्रकार" else "Fee Type")},
                            trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(feeExpanded)},
                            modifier=Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded=feeExpanded, onDismissRequest={feeExpanded=false}) {
                            listOf("Monthly Fee","Course Fee").forEach { option ->
                                DropdownMenuItem(text={Text(option)}, onClick={type=option; feeExpanded=false})
                            }
                        }
                    }
                    if (type == "Monthly Fee") {
                        OutlinedTextField(period,{period=it},label={Text(if(hindi) "महीना (जैसे November 2026)" else "Billing month (e.g. November 2026)")},singleLine=true)
                    } else {
                        Text(if(hindi) "कोर्स की कुल फीस में से किस्त जमा करें।" else "Record an instalment against the total course fee.")
                    }
                    Text("Outstanding: ₹${"%.2f".format(due)}", style=MaterialTheme.typography.titleMedium, color=MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = { TextButton(enabled=students.isNotEmpty() && (amount.toDoubleOrNull() ?: 0.0)>0 &&
            (amount.toDoubleOrNull() ?: 0.0) <= due && due > 0, onClick={
            students.firstOrNull { it.id==selected }?.let { onSave(it, amount.toDouble(), type, if (type == "Course Fee") "Course" else period) }
        }) { Text(if(hindi) "जमा करें" else "Save Payment") } },
        dismissButton = { TextButton(onClick=onDismiss) { Text(if(hindi) "रद्द" else "Cancel") } }
    )
}


@Composable
fun StudentPortalScreen(
    modifier: Modifier,
    students: List<Student>,
    payments: List<Payment>,
    hindi: Boolean,
    onWhatsApp: (Student, Double, Double) -> Unit
) {
    var studentIdText by remember { mutableStateOf("") }
    var phoneText by remember { mutableStateOf("") }
    var matchedStudentId by remember { mutableStateOf<Long?>(null) }
    var error by remember { mutableStateOf("") }
    val student = students.firstOrNull { it.id == matchedStudentId }
    val month = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(Date())

    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(if (hindi) "विद्यार्थी फीस पोर्टल" else "Student Fee Portal", style = MaterialTheme.typography.headlineSmall)
            Text(if (hindi) "अपना Student ID और रजिस्टर्ड मोबाइल नंबर डालें।" else "Enter your Student ID and registered mobile number.", style = MaterialTheme.typography.bodyMedium)
        }
        if (student == null) {
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(studentIdText, { studentIdText = it; error = "" }, label = { Text("Student ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(phoneText, { phoneText = it; error = "" }, label = { Text(if (hindi) "रजिस्टर्ड मोबाइल नंबर" else "Registered mobile number") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
                        Button(onClick = {
                            val id = studentIdText.toLongOrNull()
                            val enteredPhone = phoneText.filter { it.isDigit() }.takeLast(10)
                            val found = students.firstOrNull { it.id == id && it.phone.filter { ch -> ch.isDigit() }.takeLast(10) == enteredPhone && enteredPhone.length == 10 }
                            if (found != null) matchedStudentId = found.id
                            else error = if (hindi) "Student ID या मोबाइल नंबर सही नहीं है।" else "Student ID or mobile number is incorrect."
                        }, modifier = Modifier.fillMaxWidth()) { Text(if (hindi) "लॉगिन करें" else "View Fee Status") }
                    }
                }
            }
            item { Text(if (hindi) "Student ID एडमिन की Student List में दिखाई देगा।" else "The admin can find the Student ID in the student list.", style = MaterialTheme.typography.bodySmall) }
        } else {
            val s = student!!
            val monthlyDue = outstanding(s, payments, "Monthly Fee", month)
            val courseDue = outstanding(s, payments, "Course Fee", "Course")
            val monthlyPaid = (s.monthlyFee - monthlyDue).coerceAtLeast(0.0)
            val coursePaid = (s.courseFee - courseDue).coerceAtLeast(0.0)
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(s.name, style = MaterialTheme.typography.titleLarge)
                        Text("Student ID: ${s.id}  •  ${s.className}")
                        Text(s.phone, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (hindi) "मासिक फीस – $month" else "Monthly Fee – $month", style = MaterialTheme.typography.titleMedium)
                        FeeStatusLine(if (hindi) "कुल फीस" else "Total fee", s.monthlyFee)
                        FeeStatusLine(if (hindi) "जमा" else "Paid", monthlyPaid)
                        FeeStatusLine(if (hindi) "बाकी" else "Remaining", monthlyDue, true)
                        HorizontalDivider()
                        Text(if (hindi) "कोर्स फीस" else "Course Fee", style = MaterialTheme.typography.titleMedium)
                        FeeStatusLine(if (hindi) "कुल फीस" else "Total fee", s.courseFee)
                        FeeStatusLine(if (hindi) "जमा" else "Paid", coursePaid)
                        FeeStatusLine(if (hindi) "बाकी" else "Remaining", courseDue, true)
                        HorizontalDivider()
                        FeeStatusLine(if (hindi) "कुल बकाया" else "Total outstanding", monthlyDue + courseDue, true)
                        Button(onClick = { onWhatsApp(s, monthlyDue, courseDue) }, modifier = Modifier.fillMaxWidth(), enabled = s.phone.isNotBlank()) {
                            Icon(Icons.Default.Chat, null); Spacer(Modifier.width(8.dp)); Text(if (hindi) "WhatsApp पर फीस विवरण भेजें" else "Share fee status on WhatsApp")
                        }
                        TextButton(onClick = { matchedStudentId = null; studentIdText = ""; phoneText = "" }) { Text(if (hindi) "लॉगआउट" else "Log out") }
                    }
                }
            }
            item { Text(if (hindi) "भुगतान इतिहास" else "Payment History", style = MaterialTheme.typography.titleLarge) }
            val history = payments.filter { it.studentId == s.id }.sortedByDescending { it.paidAt }
            if (history.isEmpty()) item { Text(if (hindi) "अभी कोई भुगतान दर्ज नहीं है।" else "No payments recorded yet.") }
            items(history) { payment ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("₹${"%.2f".format(payment.amount)}") },
                        supportingContent = { Text("${payment.feeType} • ${payment.period} • ${payment.receiptNumber}") },
                        trailingContent = { Text(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(payment.paidAt))) }
                    )
                }
            }
        }
    }
}

@Composable
fun FeeStatusLine(label: String, amount: Double, isDue: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text("₹${"%.2f".format(amount)}", color = if (isDue && amount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun WhatsAppReminderScreen(
    modifier: Modifier,
    students: List<Student>,
    payments: List<Payment>,
    hindi: Boolean,
    onSend: (Student, Double, Double) -> Unit
) {
    val month = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(Date())
    val dueStudents = students.map { s -> Triple(s, outstanding(s, payments, "Monthly Fee", month), outstanding(s, payments, "Course Fee", "Course")) }
        .filter { it.second + it.third > 0.0 }
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text(if (hindi) "WhatsApp फीस रिमाइंडर" else "WhatsApp Fee Reminders", style = MaterialTheme.typography.headlineSmall) }
        item { Text(if (hindi) "जिन विद्यार्थियों की फीस बाकी है: ${dueStudents.size}" else "Students with outstanding fees: ${dueStudents.size}") }
        if (dueStudents.isEmpty()) item { Text(if (hindi) "अभी किसी विद्यार्थी की फीस बाकी नहीं है।" else "No outstanding fee found.") }
        items(dueStudents) { (student, monthlyDue, courseDue) ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(student.name, style = MaterialTheme.typography.titleMedium)
                    Text("${student.className} • ${student.phone}", style = MaterialTheme.typography.bodySmall)
                    FeeStatusLine(if (hindi) "मासिक बकाया" else "Monthly due", monthlyDue, true)
                    FeeStatusLine(if (hindi) "कोर्स बकाया" else "Course due", courseDue, true)
                    FeeStatusLine(if (hindi) "कुल बकाया" else "Total due", monthlyDue + courseDue, true)
                    OutlinedButton(onClick = { onSend(student, monthlyDue, courseDue) }, enabled = student.phone.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Chat, null); Spacer(Modifier.width(8.dp)); Text(if (hindi) "WhatsApp रिमाइंडर भेजें" else "Send WhatsApp Reminder")
                    }
                }
            }
        }
    }
}

fun openWhatsAppReminder(context: android.content.Context, student: Student, monthlyDue: Double, courseDue: Double, hindi: Boolean) {
    val rawPhone = student.phone.filter { it.isDigit() }
    val normalized = if (rawPhone.length == 11 && rawPhone.startsWith("0")) rawPhone.drop(1) else rawPhone
    val phone = when {
        normalized.length == 10 -> "91$normalized"
        normalized.length == 12 && normalized.startsWith("91") -> normalized
        else -> normalized
    }
    if (phone.length < 10) {
        Toast.makeText(context, "Valid mobile number not available", Toast.LENGTH_SHORT).show()
        return
    }
    val total = monthlyDue + courseDue
    val message = if (hindi) {
        "नमस्ते ${student.name}, AtoZ Mathematics की ओर से फीस रिमाइंडर।\nमासिक फीस बाकी: ₹${"%.2f".format(monthlyDue)}\nकोर्स फीस बाकी: ₹${"%.2f".format(courseDue)}\nकुल बकाया: ₹${"%.2f".format(total)}\nकृपया फीस जमा करें। धन्यवाद।"
    } else {
        "Hello ${student.name}, this is a fee reminder from AtoZ Mathematics.\nMonthly fee due: ₹${"%.2f".format(monthlyDue)}\nCourse fee due: ₹${"%.2f".format(courseDue)}\nTotal outstanding: ₹${"%.2f".format(total)}\nPlease pay the pending fee. Thank you."
    }
    val encoded = URLEncoder.encode(message, "UTF-8")
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phone?text=$encoded"))
    try { context.startActivity(intent) }
    catch (_: Exception) { Toast.makeText(context, "Unable to open WhatsApp", Toast.LENGTH_SHORT).show() }
}

fun outstanding(student: Student, payments: List<Payment>, feeType: String, period: String): Double {
    val paid = payments.filter { it.studentId == student.id && it.feeType == feeType && it.period == period }.sumOf { it.amount }
    val total = if (feeType == "Monthly Fee") student.monthlyFee else student.courseFee
    return (total - paid).coerceAtLeast(0.0)
}
