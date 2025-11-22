package mp.apk.presentation.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import mp.apk.R
import mp.apk.viewmodel.LibraryViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource



@Composable
fun MapScreen(
    context: Context,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    Configuration.getInstance().load(
        context,
        context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE)
    )

    val mapItems by viewModel.mapScans.collectAsState()
    val latinLabel = stringResource(id = R.string.latin_name)
    val scanDateLabel = stringResource(id = R.string.scan_date)

    var isMapCentered by remember { mutableStateOf(false) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                controller.setZoom(10.0)
            }
        },
        update = { mapView ->
            Log.d("MapScreen", "Aktualizacja mapy - mapItems size: ${mapItems.size}")

            mapView.overlays.clear()

            if (mapItems.isEmpty()) {
                Log.w("MapScreen", "Brak danych mapItems - nie dodano znaczników.")
            } else {

                mapItems.forEachIndexed { index, item ->
                    if (item.locationLat != null && item.locationLon != null) {
                        val point = GeoPoint(item.locationLat, item.locationLon)
                        val marker = Marker(mapView).apply {
                            position = point
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = item.plantName
                            subDescription = buildString {
                                appendLine("$latinLabel: ${item.latinName}")
                                appendLine("$scanDateLabel: ${item.scanDate}")
                            }
                            icon = context.getDrawable(R.drawable.ic_plant_marker)
                        }
                        mapView.overlays.add(marker)
                    }
                }

                if (!isMapCentered) {

                    val lastItem = mapItems.lastOrNull()

                    if (lastItem?.locationLat != null && lastItem.locationLon != null) {
                        val lastPoint = GeoPoint(lastItem.locationLat, lastItem.locationLon)

                        mapView.controller.animateTo(lastPoint)
                        mapView.controller.setZoom(15.0)
                        isMapCentered = true
                    }
                }
            }

            mapView.invalidate()
        }
    )
}