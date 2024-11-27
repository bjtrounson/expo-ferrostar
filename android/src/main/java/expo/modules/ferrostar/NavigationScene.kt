package expo.modules.ferrostar

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.mapbox.mapboxsdk.geometry.LatLng
import com.maplibre.compose.camera.MapViewCamera
import com.maplibre.compose.rememberSaveableMapViewCamera
import com.maplibre.compose.symbols.Circle
import com.stadiamaps.ferrostar.composeui.config.NavigationViewComponentBuilder
import com.stadiamaps.ferrostar.composeui.runtime.KeepScreenOnDisposableEffect
import com.stadiamaps.ferrostar.core.DefaultNavigationViewModel
import com.stadiamaps.ferrostar.maplibreui.views.DynamicallyOrientingNavigationView
import expo.modules.ferrostar.records.FerrostarNavigationOptions
import expo.modules.ferrostar.ui.FerrostarTheme
import kotlin.math.min

@Composable
fun NavigationScene(viewModel: DefaultNavigationViewModel, options: FerrostarNavigationOptions) {
    KeepScreenOnDisposableEffect()

    val scope = rememberCoroutineScope()

    val allPermissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        }

    val navigationUiState by viewModel.uiState.collectAsState(scope.coroutineContext)

    val permissionsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                permissions ->
            if (permissions.all { it.value }) {
                // All permissions granted, continue
                Log.d("NavigationScene", "All permissions granted")
            } else {
                // At least one permission denied, ask again
                Log.d("NavigationScene", "At least one permission denied")
            }
        }

    // FIXME: This is restarting navigation every time the screen is rotated.
    LaunchedEffect(scope) {
        // Request all permissions
        permissionsLauncher.launch(allPermissions)
    }

    val camera = rememberSaveableMapViewCamera(MapViewCamera.TrackingUserLocation())
    Log.d("NavigationScene", "Camera: $camera")
    Log.d("NavigationScene", "NavigationUiState: $navigationUiState")
    Log.d("NavigationScene", "Options: ${options.styleUrl}")
    FerrostarTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            DynamicallyOrientingNavigationView(
                modifier = Modifier.fillMaxSize(),
                styleUrl = options.styleUrl,
                viewModel = viewModel,
                camera = camera,
                snapUserLocationToRoute = options.snapUserLocationRoute,
                views = NavigationViewComponentBuilder.Default(),
                onTapExit = { viewModel.stopNavigation() }) { uiState ->
                // Trivial, if silly example of how to add your own overlay layers.
                // (Also incidentally highlights the lag inherent in MapLibre location tracking
                // as-is.)
                uiState.location?.let { location ->
                    Circle(
                        center = LatLng(location.coordinates.lat, location.coordinates.lng),
                        radius = 10f,
                        color = "Blue",
                        zIndex = 3,
                    )

                    if (location.horizontalAccuracy > 15) {
                        Circle(
                            center = LatLng(location.coordinates.lat, location.coordinates.lng),
                            radius = min(location.horizontalAccuracy.toFloat(), 150f),
                            color = "Blue",
                            opacity = 0.2f,
                            zIndex = 2,
                        )
                    }
                }
            }
        }
    }
}
