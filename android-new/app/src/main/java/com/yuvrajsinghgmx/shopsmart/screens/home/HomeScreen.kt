package com.yuvrajsinghgmx.shopsmart.screens.home

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.yuvrajsinghgmx.shopsmart.screens.home.components.ProductCard
import com.yuvrajsinghgmx.shopsmart.screens.home.components.ShopCard
import com.yuvrajsinghgmx.shopsmart.screens.shops.ShopViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    shopviewModel: ShopViewModel = hiltViewModel(),
    sharedViewModel: SharedShopViewModel,
   sharedProductViewModel: SharedProductViewModel,
    navController: NavController
) {
    val state = viewModel.state.value
    val categories = listOf("All", "Groceries", "Fashion", "Electronics")
    var selectedCategory by remember { mutableStateOf("All") }

    val shopState = shopviewModel.state.value

    LaunchedEffect(shopState.shops) {
        shopState.shops.forEach { shop ->
            Log.d(
                "ShopHomeScreen",
                "Shop: ${shop.name}, Owner: ${shop.owner_name}, Address: ${shop.address}"
            )
        }
    }

    when {
        shopState.isLoadingShops && shopState.shops.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        shopState.errorShops != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: ${shopState.errorShops}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hi, User",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Showing shops within 10 km",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        AsyncImage(
                            model = "https://i.pravatar.cc/150?img=3",
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable { },
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Search Bar
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { navController.navigate("searchScreen") }
                    ){
                        OutlinedTextField(
                            value = state.searchQuery ?: "",
                            //onValueChange = { viewModel.onEvent(HomeEvent.Search(it)) },
                            onValueChange = {
                            },
                            modifier = Modifier
                                .fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Filter"
                                )
                            },
                            placeholder = { Text("Search shops or products...") },
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.outline,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                }

                // Categories
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { category ->
                            CategoryChip(
                                label = category,
                                isSelected = selectedCategory == category,
                                onClick = { selectedCategory = category }
                            )
                        }
                    }
                }

                // Trending Products
                item {
                    Column {
                        Text(
                            text = "Trending Products",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(state.products) { product ->
                                ProductCard(
                                    product = product,
                                    onClick = {
                                        sharedProductViewModel.selectedProduct(product)
                                        navController.navigate("productScreen")
                                    })
                            }
                        }
                    }
                }

                // Nearby Shops
                item {
                    Text(
                        text = "Nearby Shops",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                items(state.nearbyShops) { shop ->
                    ShopCard(
                        shop = shop,
                        onClick = {
                            sharedViewModel.setSelectedShop(shop)
                            navController.navigate("shopDetails")
                        }
                    )
                }
                item {
                    Text(
                        text = "Explore More Products",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(50),
        tonalElevation = if (isSelected) 4.dp else 0.dp,
        modifier = Modifier
            .defaultMinSize(minHeight = 36.dp)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            )
        }
    }
}

