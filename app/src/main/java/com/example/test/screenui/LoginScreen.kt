package com.example.test.screenui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.test.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File


@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onSignupClick: () -> Unit
) {
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(" FitStack ", style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 45.sp, fontWeight = FontWeight.W500))
        Text("   반가워요, 오늘도 함께 운동해요!",style = MaterialTheme.typography.headlineSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.Normal),)
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

        Spacer(modifier = Modifier.height(24.dp))

        val context = LocalContext.current

        Button(
            onClick = {
                if (userId.isNotEmpty() && password.isNotEmpty()) {
                    val users = loadUsers(context)
                    val matchedUser = users.find { it.id == userId && it.password == password }
                    if (matchedUser != null) {
                        onLoginSuccess(userId)
                    } else {
                        errorMessage = "아이디 또는 비밀번호가 틀렸어요"
                    }
                } else {
                    errorMessage = "아이디와 비밀번호를 입력하세요"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("로그인")
        }


        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onSignupClick) {
            Text("회원가입 하러가기")
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}


