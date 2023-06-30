package com.apps.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.apps.todo.ui.theme.ToDoTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyNotes : ComponentActivity() {
    private val taskViewModel: TaskViewModel by lazy {
        ViewModelProvider(this)[TaskViewModel::class.java]
    }


    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ToDoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    TaskListScreen(tasks = taskViewModel.tasks)
                   // Greeting4("Android")
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun TaskListScreen(tasks: LiveData<List<Task2>>) {
    val taskList by tasks.observeAsState(emptyList())
    if (taskList.isEmpty()) {
        Text(
            text = "No notes saved",
            style = MaterialTheme.typography.body1,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
    else{
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(taskList.size) { index ->
            val task = taskList[index]
            TaskItem(task)
        }
    }

    }
}

@Composable
fun TaskItem(task: Task2) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp
    ) {
        Text(
            text = task.description,
            modifier = Modifier.padding(16.dp)
        )
    }
}

data class Task2(val description: String)

class TaskViewModel : ViewModel() {
    private val tasksLiveData: MutableLiveData<List<Task2>> = MutableLiveData()


    init {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Tasks")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val taskList = mutableListOf<Task2>()
                for (childSnapshot in dataSnapshot.children) {
                    val description = childSnapshot.child("description").value as? String ?: ""
                    val task = Task2(description)
                    taskList.add(task)
                }
                tasksLiveData.value = taskList
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error condition if needed
            }
        })
    }

    val tasks: LiveData<List<Task2>> = tasksLiveData
}



@Composable
fun Greeting4(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview5() {
    ToDoTheme {
        Greeting4("Android")
    }
}