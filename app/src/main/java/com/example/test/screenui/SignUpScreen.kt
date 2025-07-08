package com.example.test.screenui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.example.test.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

@Composable
fun SignUpScreen(
    onSignupSuccess: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("회원가입", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = userId,
            onValueChange = { userId = it },
            label = { Text("아이디") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("나이 (숫자만)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        val context = LocalContext.current

        Button(
            onClick = {
                val ageNum = age.toIntOrNull()
                if (userId.isNotEmpty() && password.isNotEmpty() && ageNum != null) {
                    val user = User(userId, password, ageNum)
                    saveUser(context, user)
                    onSignupSuccess(userId)
                } else {
                    errorMessage = "모든 항목을 올바르게 입력하세요"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("회원가입 완료")
        }

        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onBackClick) {
            Text("돌아가기")
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

fun saveUser(context: Context, user: User) {
    val file = File(context.filesDir, "users.json")
    val gson = Gson()
    val users = loadUsers(context).toMutableList()
    users.add(user)
    file.writeText(gson.toJson(users))
}

fun loadUsers(context: Context): List<User> {
    val file = File(context.filesDir, "users.json")
    if (!file.exists()) return emptyList()
    val json = file.readText()
    return Gson().fromJson(json, object : TypeToken<List<User>>() {}.type)
}

