package com.example.bakeryappworking

import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseManager private constructor() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient
    
    companion object {
        @Volatile
        private var instance: FirebaseManager? = null
        
        fun getInstance(): FirebaseManager {
            return instance ?: synchronized(this) {
                instance ?: FirebaseManager().also { instance = it }
            }
        }
    }

    fun initializeGoogleSignIn(activity: android.app.Activity) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("225860284566-oa19rfo7vogb1qlhp15k66a38lg5l3eh.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    fun getGoogleSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    suspend fun handleGoogleSignInResult(data: Intent?): Result<String> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Result.failure(e)
        }
    }

    private suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()
            Result.success(result.user?.uid ?: throw Exception("User ID is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Authentication methods
    suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: throw Exception("User ID is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: throw Exception("User ID is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // Database methods for bakery items
    fun saveBakeryItem(item: FirebaseBakeryItem, onComplete: (Boolean) -> Unit) {
        val itemRef = database.getReference("bakery_items").child(item.id.toString())
        itemRef.setValue(item)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getBakeryItems(onItemsLoaded: (List<Product>) -> Unit) {
        val itemsRef = database.getReference("products")
        itemsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<Product>()
                for (itemSnapshot in snapshot.children) {
                    itemSnapshot.getValue(Product::class.java)?.let { 
                        items.add(it)
                    }
                }
                onItemsLoaded(items)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseManager", "Error loading products: ${error.message}")
            }
        })
    }

    // Database methods for cart
    fun saveCartItems(userId: String, items: List<CartItem>, onComplete: (Boolean) -> Unit) {
        val cartRef = database.getReference("carts").child(userId)
        cartRef.setValue(items)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getCartItems(userId: String, onItemsLoaded: (List<CartItem>) -> Unit) {
        val cartRef = database.getReference("carts").child(userId)
        cartRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<CartItem>()
                for (itemSnapshot in snapshot.children) {
                    itemSnapshot.getValue(CartItem::class.java)?.let { items.add(it) }
                }
                onItemsLoaded(items)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    // Initialize database with sample data
    fun initializeDatabase(onComplete: (Boolean) -> Unit) {
        val sampleItems = listOf(
            FirebaseBakeryItem(1, "Sweet Delights", 4.5, "2.5 km", 15.99, "ic_launcher_background"),
            FirebaseBakeryItem(2, "Bread & Butter", 4.2, "3.1 km", 12.99, "ic_launcher_background"),
            FirebaseBakeryItem(3, "Cake Paradise", 4.8, "1.8 km", 18.99, "ic_launcher_background"),
            FirebaseBakeryItem(4, "Pastry Heaven", 4.6, "1.2 km", 14.99, "ic_launcher_background"),
            FirebaseBakeryItem(5, "Cookie Corner", 4.3, "3.5 km", 9.99, "ic_launcher_background")
        )

        val itemsRef = database.getReference("bakery_items")
        itemsRef.setValue(sampleItems)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // Order methods
    fun saveOrder(order: Order, onComplete: (Boolean) -> Unit) {
        Log.d("FirebaseManager", "Attempting to save order: ${order.id}")
        val orderRef = database.getReference("orders").child(order.id)
        orderRef.setValue(order)
            .addOnSuccessListener { 
                Log.d("FirebaseManager", "Order ${order.id} saved successfully")
                onComplete(true) 
            }
            .addOnFailureListener { e -> 
                Log.e("FirebaseManager", "Failed to save order ${order.id}", e)
                onComplete(false) 
            }
    }

    fun getOrders(userId: String, onOrdersLoaded: (List<Order>) -> Unit) {
        Log.d("FirebaseManager", "Fetching orders for user: $userId")
        val ordersRef = database.getReference("orders")
            .orderByChild("userId")
            .equalTo(userId)

        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    Log.d("FirebaseManager", "Orders data received")
                    val orders = mutableListOf<Order>()
                    for (orderSnapshot in snapshot.children) {
                        try {
                            val order = orderSnapshot.getValue(Order::class.java)
                            if (order != null) {
                                Log.d("FirebaseManager", "Order loaded: ${order.id}")
                                orders.add(order)
                            } else {
                                Log.e("FirebaseManager", "Failed to parse order: ${orderSnapshot.key}")
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseManager", "Error parsing order: ${orderSnapshot.key}", e)
                        }
                    }
                    // Sort orders by date in descending order (newest first)
                    orders.sortByDescending { it.orderDate }
                    Log.d("FirebaseManager", "Total orders loaded: ${orders.size}")
                    onOrdersLoaded(orders)
                } catch (e: Exception) {
                    Log.e("FirebaseManager", "Error processing orders data", e)
                    onOrdersLoaded(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseManager", "Error loading orders: ${error.message}")
                onOrdersLoaded(emptyList())
            }
        })
    }

    fun createOrder(items: List<CartItem>, totalAmount: Double): Order {
        val userId = getCurrentUserId() ?: throw Exception("User not logged in")
        Log.d("FirebaseManager", "Creating order for user: $userId")
        Log.d("FirebaseManager", "Order items count: ${items.size}")
        Log.d("FirebaseManager", "Order total amount: $totalAmount")
        
        val order = Order(
            id = UUID.randomUUID().toString(),
            userId = userId,
            items = items,
            totalAmount = totalAmount
        )
        Log.d("FirebaseManager", "Created order with ID: ${order.id}")
        return order
    }
} 