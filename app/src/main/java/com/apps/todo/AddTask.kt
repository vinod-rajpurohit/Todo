package com.apps.todo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.apps.todo.ui.theme.ToDoTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddTask : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ToDoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    TaskScreen()
                 //   Greeting3("Android")
                }
            }
        }
    }
}
/*
@Composable
fun Greeting3(name: String) {
    Text(text = "Hello $name!")
}

 */

@Preview(showBackground = true)
@Composable
fun DefaultPreview4() {
    ToDoTheme {
        TaskScreen()
     //   Greeting3("Android")
    }
}
@Preview
@Composable
fun TaskScreen() {
    val context  = LocalContext.current
    val taskText = rememberSaveable { mutableStateOf("") }

    Surface(color = MaterialTheme.colors.background) {
        Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
             TextField(
                value = taskText.value,
                onValueChange = { taskText.value = it },
                label = { Text(text = "Enter Task") }
            )
            Button(
                onClick = { saveTask(taskText.value,context) },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = "Save")
            }
        }
    }
}

@SuppressLint("SuspiciousIndentation")
private fun saveTask(task: String, context: Context) {

    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid


        if (userId != null) {
            val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Tasks")
            val taskData = Task(task,userId)
            databaseReference.push().setValue(taskData)
            Toast.makeText(context, "Task Added SuccessFul", Toast.LENGTH_LONG).show()




        } else {
            // Handle the case when the user is not authenticated or the user ID is null

        }

    val intent = Intent(context, MainActivity::class.java)
    context.startActivity(intent)


}


    data class Task(val description: String, val userId: String){
}

