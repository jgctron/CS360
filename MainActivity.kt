package com.example.cs340final

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cs340final.model.InventoryItem
import com.example.cs340final.ui.theme.Cs340finalTheme

class MainActivity : ComponentActivity() {

    // Database helper instance to manage the database operations
    private lateinit var dbHelper: DatabaseHelper

    // State variables to manage the app's state
    private var isAuthenticated by mutableStateOf(false) // Tracks user authentication status
    private var wantsSms by mutableStateOf(false) // Tracks if the user wants SMS notifications
    private var phoneNumber by mutableStateOf("") // Stores the user's phone number for SMS
    private var showRegistration by mutableStateOf(false) // Controls whether to show the registration screen
    private var skipSmsPermission by mutableStateOf(false) // Controls whether to skip SMS permission prompt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(this) // Initialize the database helper
        checkForSmsPermission() // Check for SMS permission

        // Set the content of the activity
        setContent {
            Cs340finalTheme {
                when {
                    // Show inventory management screen if authenticated and SMS permission is skipped
                    isAuthenticated && skipSmsPermission -> InventoryManagementApp()

                    // Show SMS permission prompt if authenticated
                    isAuthenticated -> AskForSmsPermission()

                    // Show registration screen if registration flag is true
                    showRegistration -> RegistrationScreen()

                    // Show login screen if none of the above
                    else -> LoginScreen()
                }
            }
        }
    }

    // Function to check for SMS permission
    private fun checkForSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Request SMS permission if not already granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SMS_REQUEST_CODE)
        }
    }

    // Handle the result of the SMS permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If permission is granted, update the wantsSms flag
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show()
                wantsSms = true
            } else {
                // If permission is denied, update the wantsSms flag
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                wantsSms = false
            }
        }
    }

    // Composable function to ask the user for SMS permission
    @Composable
    fun AskForSmsPermission() {
        var showPhoneNumberInput by remember { mutableStateOf(false) } // State variable to control phone number input visibility

        if (!showPhoneNumberInput) {
            // Show the initial SMS permission prompt
            AlertDialog(
                onDismissRequest = { /* Do nothing on dismiss */ },
                title = { Text("SMS Notifications") },
                text = { Text("Would you like to receive SMS notifications for inventory updates?") },
                confirmButton = {
                    Button(onClick = { showPhoneNumberInput = true }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        skipSmsPermission = true // Skip SMS permission
                    }) {
                        Text("No")
                    }
                }
            )
        } else {
            // Show the phone number input dialog
            AlertDialog(
                onDismissRequest = { /* Do nothing on dismiss */ },
                title = { Text("Enter Phone Number") },
                text = {
                    TextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (phoneNumber.isNotEmpty()) {
                            wantsSms = true
                            Toast.makeText(this@MainActivity, "SMS notifications enabled.", Toast.LENGTH_SHORT).show()
                            skipSmsPermission = true  // Proceed to InventoryManagementApp after enabling SMS
                        } else {
                            Toast.makeText(this@MainActivity, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Submit")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        skipSmsPermission = true // Skip SMS permission
                    }) {
                        Text("No")
                    }
                }
            )
        }
    }

    // Composable function to show the login screen
    @Composable
    fun LoginScreen() {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var username by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }

            Text("Use username 'admin' and password 'admin' to log in, or create a new account.", style = MaterialTheme.typography.bodyLarge)
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Login button, checks credentials and updates authentication state
            Button(onClick = {
                if ((username == "admin" && password == "admin") || dbHelper.getUser(username, password)) {
                    isAuthenticated = true
                } else {
                    Toast.makeText(this@MainActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(text = "Login")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Button to navigate to the registration screen
            Button(onClick = {
                showRegistration = true
            }) {
                Text(text = "Create Account")
            }
        }
    }

    // Composable function to show the registration screen
    @Composable
    fun RegistrationScreen() {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var newUsername by remember { mutableStateOf("") }
            var newPassword by remember { mutableStateOf("") }

            OutlinedTextField(
                value = newUsername,
                onValueChange = { newUsername = it },
                label = { Text("New Username") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Register button, adds the new user to the database and logs them in
            Button(onClick = {
                if (newUsername.isNotEmpty() && newPassword.isNotEmpty()) {
                    dbHelper.addUser(newUsername, newPassword)
                    Toast.makeText(this@MainActivity, "Account created successfully", Toast.LENGTH_SHORT).show()
                    isAuthenticated = true // Set isAuthenticated to true after registration
                    showRegistration = false // Return to the login screen
                } else {
                    Toast.makeText(this@MainActivity, "Please enter valid credentials", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(text = "Register")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Button to return to the login screen
            Button(onClick = {
                showRegistration = false
            }) {
                Text(text = "Back to Login")
            }
        }
    }

    // Composable function to show the inventory management screen
    @Composable
    fun InventoryManagementApp() {
        var name by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("") }
        val inventoryItems = remember { mutableStateOf(dbHelper.getAllItems()) } // List of inventory items

        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Button to add a new item or update an existing one
            Button(
                onClick = {
                    if (quantity.isNotEmpty() && quantity.all { it.isDigit() }) {
                        val itemQuantity = quantity.toInt()

                        // Check if item exists in inventory
                        val existingItem = inventoryItems.value.find { it.name == name }
                        if (existingItem != null) {
                            existingItem.updateQuantity(itemQuantity)
                            dbHelper.updateItem(existingItem.id, existingItem.name, existingItem.quantity)
                        } else {
                            dbHelper.addItem(name, itemQuantity)
                        }

                        name = ""
                        quantity = ""
                        inventoryItems.value = dbHelper.getAllItems()

                        // Check for low inventory and send SMS if necessary
                        val lowInventoryItems = dbHelper.getLowInventoryItems()
                        if (lowInventoryItems.isNotEmpty()) {
                            for (item in lowInventoryItems) {
                                sendLowInventorySms(item)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Item")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Button to refresh the inventory list
            Button(
                onClick = {
                    inventoryItems.value = dbHelper.getAllItems()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh List")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Display the inventory items in a grid format
            InventoryGrid(dbHelper, inventoryItems)
        }
    }

    // Composable function to display inventory items in a grid view
    @Composable
    fun InventoryGrid(dbHelper: DatabaseHelper, inventoryItems: MutableState<List<InventoryItem>>) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(inventoryItems.value.size) { index ->
                val item = inventoryItems.value[index]
                InventoryItemCard(item, dbHelper, inventoryItems) // Display each item in a card view
            }
        }
    }

    // Composable function to display an individual inventory item in a card with options to edit or delete
    @Composable
    fun InventoryItemCard(
        item: InventoryItem,
        dbHelper: DatabaseHelper,
        inventoryItems: MutableState<List<InventoryItem>>
    ) {
        Card(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display the item name and quantity
                Text("Item: ${item.name}", style = MaterialTheme.typography.titleMedium)
                Text("Quantity: ${item.quantity}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    var showDialog by remember { mutableStateOf(false) } // Controls whether to show the edit dialog
                    Button(onClick = { showDialog = true }) {
                        Text("Edit")
                    }
                    if (showDialog) {
                        showEditItemDialog(item, dbHelper, inventoryItems) { showDialog = false }
                    }
                    // Button to delete the item
                    Button(onClick = {
                        dbHelper.deleteItem(item.id)
                        inventoryItems.value = dbHelper.getAllItems() // Refresh the inventory list
                    }) {
                        Text("Delete")
                    }
                }
            }
        }
    }

    // Composable function to show a dialog for editing an existing inventory item
    @Composable
    fun showEditItemDialog(
        item: InventoryItem,
        dbHelper: DatabaseHelper,
        inventoryItems: MutableState<List<InventoryItem>>,
        onDismiss: () -> Unit
    ) {
        var newName by remember { mutableStateOf(item.name) } // State variable to hold the new item name
        var newQuantity by remember { mutableStateOf(item.quantity.toString()) } // State variable to hold the new item quantity

        AlertDialog(
            onDismissRequest = { onDismiss() }, // Close the dialog on dismiss
            title = { Text("Edit Item") },
            text = {
                Column {
                    // Input field for the new item name
                    TextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Name") }
                    )
                    // Input field for the new item quantity
                    TextField(
                        value = newQuantity,
                        onValueChange = { newQuantity = it },
                        label = { Text("Quantity") }
                    )
                }
            },
            confirmButton = {
                // Save the changes to the item
                Button(onClick = {
                    if (newQuantity.all { it.isDigit() }) {
                        item.updateQuantity(newQuantity.toInt())
                        dbHelper.updateItem(item.id, newName, item.quantity)
                        inventoryItems.value = dbHelper.getAllItems() // Refresh the inventory list

                        // Check if the updated quantity is low
                        if (item.quantity <= 1) {
                            sendLowInventorySms(item)
                        }

                        onDismiss() // Close the dialog
                    }
                }) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                Button(onClick = { onDismiss() }) {
                    Text("Cancel") // Cancel the edit and close the dialog
                }
            }
        )
    }

    // Function to send an SMS notification for low inventory
    private fun sendLowInventorySms(item: InventoryItem) {
        val smsManager = SmsManager.getDefault()
        val message = "Alert: Inventory for ${item.name} is low. Only ${item.quantity} left in stock."

        if (wantsSms && phoneNumber.isNotEmpty()) {
            // Send the SMS if the user wants SMS notifications and provided a phone number
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(this, "Low inventory SMS sent for ${item.name}", Toast.LENGTH_SHORT).show()
        } else {
            // Show a toast if SMS was not sent
            Toast.makeText(this, "SMS not sent. No phone number provided or permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val SMS_REQUEST_CODE = 101 // Constant for SMS permission request code
    }
}
