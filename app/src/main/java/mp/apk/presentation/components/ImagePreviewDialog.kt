package mp.apk.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter


@Composable
fun ImagePreviewDialog(
    images: List<String>,
    currentIndex: Int,
    onClose: () -> Unit
) {
    if (images.isEmpty()) return

    val pagerState = rememberPagerState { images.size }

    LaunchedEffect(Unit) {
        pagerState.scrollToPage(currentIndex)
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false,)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {


            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onClose() })
                    }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.8f)
                    //.fillMaxHeight(0.9f)
            ) {
                HorizontalPager(
                    state = pagerState,
                    pageSize = PageSize.Fill,
                    key = { images[it] }
                ) { index ->
                        Image(
                            painter = rememberAsyncImagePainter(images[index]),
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                }
            }
        }
    }
}