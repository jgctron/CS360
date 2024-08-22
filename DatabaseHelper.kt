package com.example.cs340final

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.cs340final.model.InventoryItem

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Constants for database name, version, and table names
        private const val DATABASE_NAME = "inventoryDatabase"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "inventory"
        private const val USER_TABLE_NAME = "users"
    }

    // Function called when the database is created for the first time
    override fun onCreate(db: SQLiteDatabase) {
        // SQL statement to create the inventory table
        val createInventoryTableStatement = "CREATE TABLE $TABLE_NAME (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, quantity INTEGER)"
        db.execSQL(createInventoryTableStatement)

        // SQL statement to create the user table
        val createUserTableStatement = "CREATE TABLE $USER_TABLE_NAME (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT)"
        db.execSQL(createUserTableStatement)
    }

    // Function called when the database needs to be upgraded
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $USER_TABLE_NAME")
        onCreate(db)
    }

    // Function to add a new item to the inventory table
    fun addItem(name: String, quantity: Int) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("name", name)
        values.put("quantity", quantity)
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    // Function to retrieve all items from the inventory table
    fun getAllItems(): List<InventoryItem> {
        val items = mutableListOf<InventoryItem>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        // Iterate through the cursor to populate the list of items
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex("id"))
                val name = cursor.getString(cursor.getColumnIndex("name"))
                val quantity = cursor.getInt(cursor.getColumnIndex("quantity"))
                items.add(InventoryItem(id, name, quantity))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return items
    }

    // Function to delete an item from the inventory table based on the item's ID
    fun deleteItem(itemId: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "id = ?", arrayOf(itemId.toString()))
        db.close()
    }

    // Function to update the name and quantity of an existing inventory item
    fun updateItem(itemId: Int, newName: String, newQuantity: Int) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("name", newName)
        values.put("quantity", newQuantity)
        db.update(TABLE_NAME, values, "id = ?", arrayOf(itemId.toString()))
        db.close()
    }

    // Function to retrieve items from the inventory table with quantity less than or equal to a threshold
    fun getLowInventoryItems(threshold: Int = 1): List<InventoryItem> {
        val lowInventoryItems = mutableListOf<InventoryItem>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE quantity <= ?", arrayOf(threshold.toString()))

        // Iterate through the cursor to populate the list of low inventory items
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex("id"))
                val name = cursor.getString(cursor.getColumnIndex("name"))
                val quantity = cursor.getInt(cursor.getColumnIndex("quantity"))
                lowInventoryItems.add(InventoryItem(id, name, quantity))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return lowInventoryItems
    }

    // Function to add a new user to the users table
    fun addUser(username: String, password: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("username", username)
        values.put("password", password)
        db.insert(USER_TABLE_NAME, null, values)
        db.close()
    }

    // Function to check if a user exists in the users table with the given username and password
    fun getUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $USER_TABLE_NAME WHERE username = ? AND password = ?", arrayOf(username, password))

        val userExists = cursor.count > 0 // Check if the cursor returned any results
        cursor.close()
        db.close()

        return userExists
    }
}
