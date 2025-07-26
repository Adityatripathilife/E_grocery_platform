package com.example.e_grocery_platform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.e_grocery_platform.ui.theme.E_grocery_platformTheme
import kotlinx.coroutines.launch
// --- Data Provider ---
object DataProvider {
    fun getItemList(): Map<String, List<Item>> {
        return mapOf(
            "Fresh Fruits" to listOf(
                Item(1, "Apple", "A sweet, crisp red fruit, perfect for a healthy snack.", R.drawable.apple),
                Item(2, "Banana", "A long, yellow fruit rich in potassium and energy.", R.drawable.bananas),
                Item(3, "Cherry", "A small, sweet red fruit, great for desserts.", R.drawable.cherry),
                Item(4, "Mango", "The delicious and juicy king of fruits.", R.drawable.mango),
                Item(5, "Watermelon", "A big and hydrating summer fruit, perfect for hot days.", R.drawable.watermelon),
                Item(6, "Grapes", "Sweet and tasty bunches of fruit, easy to eat.", R.drawable.grapes)
            ),
            "Farm Vegetables" to listOf(
                Item(7, "Carrot", "A long, orange root vegetable, great for eyesight.", R.drawable.carrot),
                Item(8, "Lettuce", "A leafy green vegetable, the base for many salads.", R.drawable.lettuce),
                Item(9, "Broccoli", "A beautiful and healthy green vegetable packed with vitamins.", R.drawable.brocoli),
                Item(10, "Onion", "An essential vegetable for adding flavor to any dish.", R.drawable.onion),
                Item(11, "Potato", "A versatile and starchy yellow vegetable for countless recipes.", R.drawable.potato),
                Item(12, "Tomato", "A red, juicy fruit often used as a vegetable in cooking.", R.drawable.tomato),
                Item(13, "Pea", "Sweet little green vegetables, great in a variety of dishes.", R.drawable.pea)
            )
        )
    }
}

// --- Custom Colors for a more vibrant UI ---
object AppColors {
    val fruitHeaderStart = Color(0xFFF8BBD0) // Light Pink
    val fruitHeaderEnd = Color(0xFFF48FB1)   // Pink
    val vegHeaderStart = Color(0xFFC8E6C9)   // Light Green
    val vegHeaderEnd = Color(0xFFA5D6A7)     // Green
    val cardCollapsedBackground = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFF1F8E9))
    )
    val cardExpandedBackground = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFE8F5E9))
    )
    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF4CAF50), Color(0xFF81C784))
    )
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            E_grocery_platformTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GroceryApp()
                }
            }
        }
    }
}

// --- Main App Composable with Navigation Logic ---
@Composable
fun GroceryApp() {
    val allItems = remember { DataProvider.getItemList() }
    val cartItems = remember { mutableStateListOf<Item>() }
    var currentScreen by remember { mutableStateOf("list") } // "list" or "cart"

    when (currentScreen) {
        "list" -> GroceryListScreen(
            allItems = allItems,
            cartItemCount = cartItems.size,
            onAddToCart = { item -> cartItems.add(item) },
            onNavigateToCart = { currentScreen = "cart" }
        )
        "cart" -> CartScreen(
            cartItems = cartItems,
            onNavigateBack = { currentScreen = "list" }
        )
    }
}

// --- UI Composables ---

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GroceryListScreen(
    allItems: Map<String, List<Item>>,
    cartItemCount: Int,
    onAddToCart: (Item) -> Unit,
    onNavigateToCart: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(searchQuery, allItems) {
        if (searchQuery.isBlank()) {
            allItems
        } else {
            allItems.mapValues { (_, itemList) ->
                itemList.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }.filter { it.value.isNotEmpty() }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("QuickCart", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    actions = {
                        IconButton(onClick = onNavigateToCart) {
                            BadgedBox(
                                badge = {
                                    if (cartItemCount > 0) {
                                        Badge { Text(cartItemCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Shopping Cart"
                                )
                            }
                        }
                    }
                )
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search for groceries...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            filteredItems.forEach { (category, itemList) ->
                stickyHeader {
                    val headerGradient = if (category.contains("Fruits")) {
                        Brush.horizontalGradient(colors = listOf(AppColors.fruitHeaderStart, AppColors.fruitHeaderEnd))
                    } else {
                        Brush.horizontalGradient(colors = listOf(AppColors.vegHeaderStart, AppColors.vegHeaderEnd))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(headerGradient)
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
                items(items = itemList, key = { it.id }) { item ->
                    GroceryItemCard(
                        item = item,
                        modifier = Modifier.animateItemPlacement(),
                        onAddToCart = {
                            onAddToCart(item)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("${item.name} added to cart!")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GroceryItemCard(
    item: Item,
    modifier: Modifier = Modifier,
    onAddToCart: (Item) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "Icon Rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .background(if (isExpanded) AppColors.cardExpandedBackground else AppColors.cardCollapsedBackground)
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = item.imageResId),
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand or collapse",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.rotate(rotationAngle)
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onAddToCart(item) },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(AppColors.buttonGradient)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Add to Cart Icon",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add to Cart", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(cartItems: List<Item>, onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cart", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Empty Cart",
                        modifier = Modifier.size(100.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Your cart is empty!", style = MaterialTheme.typography.titleMedium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(items = cartItems, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = item.imageResId),
                            contentDescription = item.name,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(item.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroceryListScreenPreview() {
    E_grocery_platformTheme {
        GroceryApp()
    }
}
