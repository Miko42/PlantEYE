import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import mp.apk.viewmodel.ScanViewModel
import androidx.compose.runtime.LaunchedEffect
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import mp.apk.presentation.components.ImagePreviewDialog

@Composable
fun ResultScreen(
    viewModel: ScanViewModel,
    scanId: Int? = null,
) {
    val images by viewModel.photoUris.collectAsState()
    val similarImages by viewModel.similarImagesList.collectAsState()
    val scanItem by viewModel.scanItem.collectAsState()
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }
    var selectedImageSource by remember { mutableStateOf<String?>(null) }

    if (selectedImageIndex != null && selectedImageSource != null) {
        val selectedList = when (selectedImageSource) {
            "main" -> images
            "similar" -> similarImages
            else -> emptyList()
        }

        ImagePreviewDialog(
            images = selectedList,
            currentIndex = selectedImageIndex!!,
            onClose = {
                selectedImageIndex = null
                selectedImageSource = null
            }
        )
    }


    LaunchedEffect(scanId) {
        if (scanId != null) {
            viewModel.loadScanById(scanId)
        }
    }
    LaunchedEffect(scanId, images, similarImages, scanItem) {
        if (scanId != null) {
            viewModel.loadScanById(scanId)
        }

        Log.d("ResultScreen", "Liczba zdjęć (main): ${images.size}")
        images.forEach { uri ->
            Log.d("ResultScreen", "Main URI: $uri")
        }

        Log.d("ResultScreen", "Liczba zdjęć podobnych: ${similarImages.size}")
        similarImages.forEach { uri ->
            Log.d("ResultScreen", "Similar URI: $uri")
        }

        Log.d("ResultScreen", "Dane skanu:")
        Log.d("ResultScreen", "Nazwa rośliny: ${scanItem?.plantName}")
        Log.d("ResultScreen", "Nazwa łacińska: ${scanItem?.latinName}")
        Log.d("ResultScreen", "Opis: ${scanItem?.description}")
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.saveParsedScanItem()
                },
                shape = RoundedCornerShape(16),
                elevation = FloatingActionButtonDefaults.elevation(0.dp),
                modifier = Modifier
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    tint = Color.Black,
                    contentDescription = "Save note"
                )
            }

        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyRow (
                modifier = Modifier

                    .wrapContentHeight()
                    .padding(horizontal = 0.dp)
            ){
                itemsIndexed(images) { index, imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(200.dp)
                            .padding(4.dp)
                            .clickable {
                                selectedImageIndex = index
                                selectedImageSource = "main"
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = scanItem?.plantName ?: "Nieznana roślina",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${scanItem?.latinName}",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = scanItem?.description ?: "Brak opisu",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 0.dp)
            ){
                itemsIndexed(similarImages) {index, imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(200.dp)
                            .padding(4.dp)
                            .clickable{
                                selectedImageIndex = index
                                selectedImageSource = "similar"
                            }
                    )
                }
            }
        }
    }
}


