@file:OptIn(ExperimentalFoundationApi::class)

package com.apps.todo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.apps.todo.ui.theme.ToDoTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.firebase.database.DataSnapshot
import androidx.compose.foundation.pager.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth




class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
/*
            ToDoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Notes App")
                }
            }
 */
            MaterialTheme {
                AppContent()

            }


        }
    }

}
/*
@Composable
fun Greeting(name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "Welcome to $name!",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(16.dp)
        )
    }

}

 */
@Composable
fun AppContent() {
  //  val navController = rememberNavController()
    UserScreen()
}

@Preview(showBackground = true)

@Composable
fun DefaultPreview() {
    ToDoTheme {

    }
}

data class User(val id: String, val name: String, val isSelf: Boolean, val isCompleted: Boolean)

class UserViewModel : ViewModel() {
    private val usersLiveData: MutableLiveData<List<User>> = MutableLiveData()

    init {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userList = mutableListOf<User>()
                for (childSnapshot in dataSnapshot.children) {
                    val id = childSnapshot.key ?: ""
                    val name = childSnapshot.child("name").value as? String ?: ""
                    val isSelf = childSnapshot.child("isSelf").value as? Boolean ?: false
                    val isCompleted = childSnapshot.child("isCompleted").value as? Boolean ?: false
                    val user = User(id, name, isSelf, isCompleted)
                    userList.add(user)
                }
                usersLiveData.value = userList
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error condition if needed
            }
        })
    }

    val users: LiveData<List<User>> = usersLiveData
}


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun UserScreen() {

    val viewModel: UserViewModel = viewModel()
    val users by viewModel.users.observeAsState(emptyList())

    val pendingUsers = users.filter { !it.isCompleted }
    val completedUsers = users.filter { it.isCompleted }

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Users") }) },
        floatingActionButton = { FabMenu() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp))
        {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val currentUserData = users.firstOrNull { it.id == currentUser?.uid }

            currentUserData?.let {
                SelfUserCard(user = it)
                Spacer(modifier = Modifier.height(8.dp))
            }

            TabLayout(pendingUsers = pendingUsers, completedUsers = completedUsers)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SelfUserCard(user: User) {
    val context  = LocalContext.current
    Card(
        modifier = Modifier.padding(8.dp),
        backgroundColor = Color.LightGray,
        onClick = {
            val intent = Intent(context, MyNotes::class.java)
            context.startActivity(intent)

        },

        ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = "My Profile", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = user.name, style = MaterialTheme.typography.body1)

        }
    }
}

@Composable
fun TabLayout(pendingUsers: List<User>, completedUsers: List<User>) {
    val tabTitles = listOf("Pending", "Completed")
    val pagerState = rememberPagerState(pageCount = {tabTitles.size})
    var selectedTabIndex by remember { mutableStateOf(pagerState.currentPage) }


    LaunchedEffect(selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        TabRow(selectedTabIndex = selectedTabIndex) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        // Update selectedTabIndex here
                    },
                    text = { Text(text = title) }
                )
            }
        }
        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> UserList(users = pendingUsers)
                1 -> UserList(users = completedUsers)
            }
        }
    }
}

@Composable
fun UserList(users: List<User>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        itemsIndexed(users) { index, user ->
            UserCard(user = user)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun UserCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = if (user.isSelf) Color.Blue else Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = user.name, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = if (user.isCompleted) "Completed" else "Pending", style = MaterialTheme.typography.body1)
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun FabMenu() {
    val context = LocalContext.current

    var isDarkMode by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    ExtendedFloatingActionButton(
        icon = { Icon(Icons.Default.Menu, contentDescription = "Menu") },
        onClick = {
            menuExpanded = true },
        text = { Text(text = "Add") },
        backgroundColor = MaterialTheme.colors.primary,

        modifier = Modifier
            .padding(16.dp)
        //         .align(Alignment.BottomEnd)
    )
    //Alignment()

    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false },
        //       modifier = Modifier.align(Alignment.BottomEnd)
    ) {
        DropdownMenuItem(onClick = {
            // Add new task for self
       //     navController.navigate("task_screen")
     //       menuExpanded = false

            val intent = Intent(context, AddTask::class.java)
                context.startActivity(intent)


           }) {
            Text(text = "Add New Task")
        }
        DropdownMenuItem(onClick = {
            // Switch between dark mode and light mode
            isDarkMode = !isDarkMode
            menuExpanded = false
        }) {
            val icon = if (isDarkMode) Icons.Default.Star else Icons.Default.Close
            Text(text = if (isDarkMode) "Light Mode" else "Dark Mode")
            Icon(
                icon,
                contentDescription = "Switch Mode")
        }
    }
}