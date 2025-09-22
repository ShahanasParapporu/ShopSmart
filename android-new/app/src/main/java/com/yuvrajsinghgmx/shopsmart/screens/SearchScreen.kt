package com.yuvrajsinghgmx.shopsmart.screens

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.yuvrajsinghgmx.shopsmart.data.modelClasses.Product
import com.yuvrajsinghgmx.shopsmart.data.modelClasses.Shop
import com.yuvrajsinghgmx.shopsmart.screens.home.HomeEvent
import com.yuvrajsinghgmx.shopsmart.screens.home.HomeViewModel
import com.yuvrajsinghgmx.shopsmart.screens.home.components.MiniProductCard
import com.yuvrajsinghgmx.shopsmart.sharedComponents.NoResultsFound
import com.yuvrajsinghgmx.shopsmart.sharedComponents.SearchBarWithQuickActions
import com.yuvrajsinghgmx.shopsmart.sharedComponents.rememberVoiceSearchLauncher
import java.util.Locale

@Composable
fun SearchScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onProductClick: (Product) -> Unit = {},
    onShopClick: (Shop) -> Unit = {}
) {
    val state = viewModel.state.value
    var isSearchActive by remember { mutableStateOf(false) }

    val voiceSearchLauncher = rememberVoiceSearchLauncher { spokenText ->
        viewModel.onEvent(HomeEvent.Search(spokenText))
        isSearchActive = true
    }

    // Group results by shop
    val groupedResults: Map<Shop, List<Product>> = state.searchResults
        .filter { it.product != null && it.shop != null }
        .groupBy({ it.shop!! }, { it.product!! })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
/*        SearchBarComposable(
            query = state.searchQuery.orEmpty(),
            onQueryChange = { viewModel.onEvent(HomeEvent.Search(it)) },
            onSearch = {},
            modifier = Modifier.padding(16.dp)
        )*/

        SearchBarWithQuickActions(
            searchQuery = state.searchQuery.orEmpty(),
            onSearchChange = {
                viewModel.onEvent(HomeEvent.Search(it))
                isSearchActive = it.isNotBlank()
            },
            onSearchSubmit = {
                isSearchActive = true
            },
            onVoiceSearchClick = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now…")
                }
                voiceSearchLauncher.launch(intent)
            },
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (!state.isLoading && state.error.isNullOrBlank()) {
            Text(
                text = "${state.searchResults.size} results found nearby",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            !state.error.isNullOrBlank() -> {
//                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    Text(state.error, color = MaterialTheme.colorScheme.error)
//                }
                NoResultsFound()
            }
            else -> {
                    // Normal results
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        groupedResults.forEach { (shop, products) ->
                            item {
                                ShopProductRow(
                                    shop = shop,
                                    products = products,
                                    onProductClick = onProductClick,
                                    onShopClick = onShopClick
                                )
                            }
                        }
                    }

            }
        }
    }
}

@Composable
fun ShopProductRow(
    shop: Shop,
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onShopClick: (Shop) -> Unit
) {
    Surface(
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(2.5f),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products) { product ->
                    MiniProductCard(product = product, onClick = { onProductClick(product) })
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onShopClick(shop) }
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ShopCircleImage(imageUrl = shop.imageUrl.firstOrNull())
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = shop.shopName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = shop.distance,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ShopCircleImage(imageUrl: String?) {
    AsyncImage(
        model = imageUrl ?: "",
        contentDescription = "Shop Logo",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}
