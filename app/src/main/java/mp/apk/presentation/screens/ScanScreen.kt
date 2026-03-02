package mp.apk.presentation.screens

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import mp.apk.takePhoto
import mp.apk.presentation.components.CameraPreview
import mp.apk.presentation.components.RequestCameraPermission
import mp.apk.presentation.components.RequestLocationPermission
import mp.apk.viewmodel.ScanViewModel
import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.flow.collectLatest
import mp.apk.R
import mp.apk.utils.MockUtils.getMockImageUri

@Composable
fun ScanScreen(viewModel: ScanViewModel, navController: NavController) {
    RequestCameraPermission()
    RequestLocationPermission()
    val context = LocalContext.current
    val activity = LocalActivity.current as? ComponentActivity ?: error("Not in an Activity")
    val images by viewModel.photoUris.collectAsState()
    val shouldNavigate by viewModel.shouldNavigate.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.clearPhotos()
        if (mp.apk.BuildConfig.FLAVOR == "mock") {
            val mockUri1 = mp.apk.utils.MockUtils.getMockImageUri(
                context,
                R.drawable.sample_plant,
                "mock_image_1.jpg"
            )
            viewModel.onTakePhoto(mockUri1, context)
            val mockUri2 = mp.apk.utils.MockUtils.getMockImageUri(
                context,
                R.drawable.sample_plant_2,
                "mock_image_2.jpg"
            )
            viewModel.onTakePhoto(mockUri2, context)
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is ScanViewModel.UiEvent.ShowPhotoLimitReached -> {
                    val message = context.getString(mp.apk.R.string.photo_limit_reached)
                    snackbarHostState.showSnackbar(
                        message = message,
                        withDismissAction = true
                    )
                }
                is ScanViewModel.UiEvent.ShowNoPhotoSelected -> {
                    val message = context.getString(mp.apk.R.string.no_photo)
                    snackbarHostState.showSnackbar(
                        message = message,
                        withDismissAction = true
                    )
                }
            }
        }
    }

    LaunchedEffect(shouldNavigate) {
        if (shouldNavigate) {
            navController.navigate("result")
            viewModel.resetNavigationFlag()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.take(3).forEach { uri ->
            viewModel.onTakePhoto(uri, context)
        }
    }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
    Column(modifier = Modifier.fillMaxSize()) {

        Box(modifier = Modifier.weight(1f)) {
            CameraPreview(
                controller = controller,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {


            if (images.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(images) { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                ControlItem(
                    text = stringResource(R.string.gallery),
                    icon = Icons.Default.PhotoLibrary,
                    onClick = { galleryLauncher.launch("image/*") }
                )


                ControlItem(
                    text = stringResource(R.string.take_photo),
                    icon = Icons.Default.CameraAlt,
                    onClick = { takePhoto(controller, { uri -> viewModel.onTakePhoto(uri, context) }, activity) }
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = { viewModel.identifyPlant(context) },
                        modifier = Modifier.size(70.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Wyślij",
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.send),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    }
}


@Composable
fun ControlItem(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF92A1AD))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,

            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
