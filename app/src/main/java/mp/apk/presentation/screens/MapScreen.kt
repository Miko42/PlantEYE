package mp.apk.presentation.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
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

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->

            MapView(ctx).apply {

                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                val mapController = controller
                mapController.setZoom(10.0)
                val startPoint = GeoPoint(53.0, 16.0)
                mapController.setCenter(startPoint)
            }
        },
        update = { mapView ->

            Log.d("MapScreen", "Aktualizacja mapy - mapItems size: ${mapItems.size}")


            mapView.overlays.clear()

            if (mapItems.isEmpty()) {
                Log.w("MapScreen", "Brak danych mapItems - nie dodano znaczników.")
            }

            mapItems.forEachIndexed { index, item ->
                if (item.locationLat == null || item.locationLon == null) {
                    Log.e("MapScreen", "Pusta lokalizacja w item[$index]: $item")
                    return@forEachIndexed
                }

                val point = GeoPoint(item.locationLat, item.locationLon)
                Log.d("MapScreen", "Dodawanie znacznika dla: ${item.plantName} w punkcie $point")

                val marker = Marker(mapView).apply {
                    position = point
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = item.plantName
                    subDescription = buildString {
                        appendLine("Nazwa łacińska: ${item.latinName}")
                        appendLine("Data skanu: ${item.scanDate}")
                    }
                    icon = context.getDrawable(R.drawable.ic_plant_marker)
                }

                mapView.overlays.add(marker)
            }

            mapView.invalidate()
        }
    )
}
