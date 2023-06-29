package com.apps.todo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.apps.todo.ui.theme.ToDoTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ToDoTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    RegistrationScreen()
                }



            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview3() {
    ToDoTheme {
        RegistrationScreen()
    }
}
@Composable
fun RegistrationScreen() {
    val context = LocalContext.current
    val name = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val age = remember { mutableStateOf("") }
    val dateOfBirth = remember { mutableStateOf("") }
    // val successMessage = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Register",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = name.value,
            onValueChange = { name.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            label = { Text("Name") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = email.value,
            onValueChange = { email.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = password.value,
            onValueChange = { password.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = age.value,
            onValueChange = { age.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = dateOfBirth.value,
            onValueChange = { dateOfBirth.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Date of Birth") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (name.value.isNotEmpty() && email.value.isNotEmpty() && password.value.isNotEmpty() && dateOfBirth.value.isNotEmpty()) {
                    registerUser(context, name.value, email.value, password.value,age.value, dateOfBirth.value)
                } else {
                    errorMessage.value = "Please fill in all fields"
                }
            }
        ) {

            Text("Register")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)

            }
        ) {
            Text("Login")
        }


        if (errorMessage.value.isNotEmpty()) {
            Text(
                errorMessage.value,
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colors.error
            )
        }
    }
}

private fun registerUser(context: Context, name: String, email: String, password: String, age:String, dateOfBirth: String) {
    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = task.result?.user
                if (user != null) {
                    saveUserDetails(user.uid,
                        name,
                        email,
                        age,
                        dateOfBirth)
                    val successMessage = "Registration Success"
                    showErrorToast(context, successMessage)
                }
            } else {
                val errorMessage = task.exception?.message ?: "Registration failed"
                showErrorToast(context, errorMessage)
            }
        }
}

private fun saveUserDetails(userId: String,
                            name: String,
                            email: String,
                            age: String,
                            dateOfBirth: String) {
    val database = FirebaseDatabase.getInstance().reference
    val userRef = database.child("Users").child(userId)

    userRef.child("name").setValue(name)
    userRef.child("email").setValue(email)
    userRef.child("age").setValue(age)
    userRef.child("dateOfBirth").setValue(dateOfBirth)
}

private fun showErrorToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

