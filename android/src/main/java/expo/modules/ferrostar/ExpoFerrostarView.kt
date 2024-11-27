package expo.modules.ferrostar

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.ComposeView
import com.stadiamaps.ferrostar.core.AndroidSystemLocationProvider
import com.stadiamaps.ferrostar.core.DefaultNavigationViewModel
import com.stadiamaps.ferrostar.core.FerrostarCore
import com.stadiamaps.ferrostar.core.LocationProvider
import com.stadiamaps.ferrostar.core.SimulatedLocationProvider
import com.stadiamaps.ferrostar.googleplayservices.FusedLocationProvider
import expo.modules.ferrostar.records.BoundingBox
import expo.modules.ferrostar.records.FerrostarCoreOptions
import expo.modules.ferrostar.records.FerrostarNavigationOptions
import expo.modules.ferrostar.records.GeographicCoordinate
import expo.modules.ferrostar.records.LaneInfo
import expo.modules.ferrostar.records.LocationMode
import expo.modules.ferrostar.records.ManeuverModifier
import expo.modules.ferrostar.records.ManeuverType
import expo.modules.ferrostar.records.NavigationControllerConfig
import expo.modules.ferrostar.records.Route
import expo.modules.ferrostar.records.RouteStep
import expo.modules.ferrostar.records.SpokenInstruction
import expo.modules.ferrostar.records.UserLocation
import expo.modules.ferrostar.records.VisualInstruction
import expo.modules.ferrostar.records.VisualInstructionContent
import expo.modules.ferrostar.records.Waypoint
import expo.modules.ferrostar.records.WaypointKind
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.views.ExpoView
import okhttp3.OkHttpClient
import java.time.Duration

@SuppressLint("ViewConstructor")
class ExpoFerrostarView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
  private val httpClient = OkHttpClient
    .Builder()
    .callTimeout(Duration.ofSeconds(15))
    .build()
  private lateinit var core: FerrostarCore
  private lateinit var viewModel: DefaultNavigationViewModel

  private var coreOptions: FerrostarCoreOptions = FerrostarCoreOptions()
  private var navigationOptions: FerrostarNavigationOptions = FerrostarNavigationOptions()

  private val composeView = ComposeView(context).also {
    it.layoutParams = LayoutParams(
      LayoutParams.MATCH_PARENT,
      LayoutParams.MATCH_PARENT
    )

    addView(it)
  }

  init {
    updateCore()
  }

  fun setNavigationOptions(options: FerrostarNavigationOptions) {
    navigationOptions = options
    updateView()
  }

  fun setCoreOptions(options: FerrostarCoreOptions) {
    coreOptions = options
    updateCore()
  }

  fun startNavigation(route: Route, options: NavigationControllerConfig?) {
    val config = options?.toConfig()

    try {
      core.startNavigation(route.toRoute(), config)
    } catch (e: Exception) {
      Log.e("ExpoFerrostarNavigationController", "Error starting navigation", e)
    }
  }

  fun stopNavigation(stopLocationUpdates: Boolean?) {
    if (stopLocationUpdates == null) {
      core.stopNavigation()
      return
    }

    core.stopNavigation(stopLocationUpdates)
  }

  fun replaceRoute(route: Route, options: NavigationControllerConfig? = null) {
    core.replaceRoute(route.toRoute(), options?.toConfig())
  }

  fun advanceToNextStep() {
    core.advanceToNextStep()
  }

  suspend fun getRoutes(
    initialLocation: UserLocation,
    waypoints: List<Waypoint>
  ): List<Route> {
    val location = initialLocation.toUserLocation()
    val points = waypoints.map { waypoint: Waypoint -> waypoint.toWaypoint() }
    var routes = emptyList<uniffi.ferrostar.Route>()
    try {
      routes = core.getRoutes(location, points)
      Log.d("ExpoFerrostarNavigationController", "Got routes ${routes.size}")
    }
    catch (e: Exception) {
      Log.e("ExpoFerrostarNavigationController", "Error getting routes", e)
    }
    // Make routes to local record routes
    val localRoutes =
      routes.map { currentRoute ->
        val route = Route()
        val bbox = BoundingBox()
        val northEast = GeographicCoordinate()
        northEast.lat = currentRoute.bbox.ne.lat
        northEast.lng = currentRoute.bbox.ne.lng
        val southWest = GeographicCoordinate()
        southWest.lat = currentRoute.bbox.sw.lat
        southWest.lng = currentRoute.bbox.sw.lng
        bbox.ne = northEast
        bbox.sw = southWest

        route.bbox = bbox

        route.waypoints =
          currentRoute.waypoints.map { point ->
            val waypoint = Waypoint()
            val coordinate = GeographicCoordinate()
            coordinate.lat = point.coordinate.lat
            coordinate.lng = point.coordinate.lng
            waypoint.coordinate = coordinate
            waypoint.kind = WaypointKind.valueOf(point.kind.name)

            waypoint
          }

        route.steps =
          currentRoute.steps.map { step ->
            val routeStep = RouteStep()

            routeStep.distance = step.distance
            routeStep.duration = step.duration
            routeStep.roadName = step.roadName
            routeStep.annotations = step.annotations
            routeStep.instruction = step.instruction

            routeStep.geometry =
              step.geometry.map { point ->
                val coordinate = GeographicCoordinate()
                coordinate.lat = point.lat
                coordinate.lng = point.lng

                coordinate
              }

            routeStep.visualInstructions =
              step.visualInstructions.map { instruction ->
                val visualInstruction = VisualInstruction()
                val primaryContent = VisualInstructionContent()

                primaryContent.text = instruction.primaryContent.text
                primaryContent.maneuverType = instruction.primaryContent.maneuverType?.let {
                  ManeuverType.valueOf(it.name)
                }
                primaryContent.maneuverModifier = instruction.primaryContent.maneuverModifier?.let {
                  ManeuverModifier.valueOf(it.name)
                }
                primaryContent.roundaboutExitDegrees = instruction.primaryContent.roundaboutExitDegrees?.toInt()
                primaryContent.laneInfo = instruction.primaryContent.laneInfo?.map { currentLaneInfo ->
                  val laneInfo = LaneInfo()

                  laneInfo.active = currentLaneInfo.active
                  laneInfo.directions = currentLaneInfo.directions
                  laneInfo.activeDirection = currentLaneInfo.activeDirection

                  laneInfo
                }

                visualInstruction.primaryContent = primaryContent

                if (instruction.secondaryContent != null) {
                  val secondaryContent = VisualInstructionContent()
                  secondaryContent.text = instruction.secondaryContent!!.text
                  secondaryContent.maneuverType = instruction.secondaryContent!!.maneuverType?.let {
                    ManeuverType.valueOf(it.name)
                  }
                  secondaryContent.maneuverModifier = instruction.secondaryContent!!.maneuverModifier?.let {
                    ManeuverModifier.valueOf(it.name)
                  }
                  secondaryContent.roundaboutExitDegrees = instruction.secondaryContent!!.roundaboutExitDegrees?.toInt()
                  secondaryContent.laneInfo =
                    instruction.secondaryContent!!.laneInfo?.map { currentLaneInfo ->
                      val laneInfo = LaneInfo()

                      laneInfo.active = currentLaneInfo.active
                      laneInfo.directions = currentLaneInfo.directions
                      laneInfo.activeDirection = currentLaneInfo.activeDirection

                      laneInfo
                    }

                  visualInstruction.secondaryContent = secondaryContent
                }

                if (instruction.subContent != null) {
                  val subContent = VisualInstructionContent()
                  subContent.text = instruction.subContent!!.text
                  subContent.maneuverType = instruction.subContent!!.maneuverType?.let {
                    ManeuverType.valueOf(it.name)
                  }
                  subContent.maneuverModifier = instruction.subContent!!.maneuverModifier?.let {
                    ManeuverModifier.valueOf(it.name)
                  }
                  subContent.roundaboutExitDegrees = instruction.subContent!!.roundaboutExitDegrees?.toInt()
                  subContent.laneInfo =
                    instruction.subContent!!.laneInfo?.map { currentLaneInfo ->
                      val laneInfo = LaneInfo()

                      laneInfo.active = currentLaneInfo.active
                      laneInfo.directions = currentLaneInfo.directions
                      laneInfo.activeDirection = currentLaneInfo.activeDirection

                      laneInfo
                    }

                  visualInstruction.subContent = subContent
                }

                visualInstruction.triggerDistanceBeforeManeuver = instruction.triggerDistanceBeforeManeuver

                visualInstruction
              }

            routeStep.spokenInstructions =
              step.spokenInstructions.map { instruction ->
                val spokenInstruction = SpokenInstruction()
                spokenInstruction.text = instruction.text
                spokenInstruction.ssml = instruction.ssml
                spokenInstruction.utteranceId = instruction.utteranceId.toString()
                spokenInstruction.triggerDistanceBeforeManeuver =
                  instruction.triggerDistanceBeforeManeuver

                spokenInstruction
              }

            routeStep
          }

        route.distance = currentRoute.distance
        route.geometry =
          currentRoute.geometry.map { point ->
            val coordinate = GeographicCoordinate()
            coordinate.lat = point.lat
            coordinate.lng = point.lng

            coordinate
          }

        route
      }

    return localRoutes
  }

  private fun updateCore() {
    Log.d("ExpoFerrostarNavigationView", "Updating core")
    var locationProvider: LocationProvider = AndroidSystemLocationProvider(context)

    if (coreOptions.locationMode == LocationMode.FUSED) {
      locationProvider = FusedLocationProvider(context)
    }

    if (coreOptions.locationMode == LocationMode.SIMULATED) {
      locationProvider = SimulatedLocationProvider()
    }

    core = FerrostarCore(
      httpClient = httpClient,
      valhallaEndpointURL = this.coreOptions.valhallaEndpointURL,
      profile = this.coreOptions.profile,
      locationProvider = locationProvider,
      options = coreOptions.options,
      navigationControllerConfig = coreOptions.navigationControllerConfig.toConfig()
    )

    updateViewModel()
  }

  private fun updateViewModel() {
    Log.d("ExpoFerrostarNavigationView", "Updating view model")
    viewModel = DefaultNavigationViewModel(core)
    updateView()
  }

  private fun updateView() {
    Log.d("ExpoFerrostarNavigationView", "Calculating view")

    composeView.setContent {
      NavigationScene(viewModel = viewModel, options = navigationOptions)
    }

    Log.d("ExpoFerrostarNavigationView", "BAZINGA!")
  }
}
