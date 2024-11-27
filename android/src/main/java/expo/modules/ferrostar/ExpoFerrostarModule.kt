package expo.modules.ferrostar

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.net.URL

class ExpoFerrostarModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("ExpoFerrostar")

    View(ExpoFerrostarView::class) {
      AsyncFunction("getRoutes") Coroutine { view: ExpoFerrostarView, initialLocation: UserLocation, waypoints: List<Waypoint> ->
        return@Coroutine view.getRoutes(initialLocation, waypoints)
      }

      AsyncFunction("startNavigation") { view: ExpoFerrostarView, route: Route, options: NavigationControllerConfig? ->
        view.startNavigation(route, options)
      }

      AsyncFunction("stopNavigation") { view: ExpoFerrostarView, stopLocationUpdates: Boolean? ->
        view.stopNavigation(stopLocationUpdates)
      }

      AsyncFunction("replaceRoute") { view: ExpoFerrostarView, route: Route, options: NavigationControllerConfig? ->
        view.replaceRoute(route, options)
      }

      AsyncFunction("advanceToNextStep") { view: ExpoFerrostarView -> view.advanceToNextStep() }

      Prop("navigationOptions") { view: ExpoFerrostarView, options: FerrostarNavigationOptions ->
        view.setNavigationOptions(options)
      }

      Prop("coreOptions") { view: ExpoFerrostarView, options: FerrostarCoreOptions ->
        view.setCoreOptions(options)
      }
    }
  }
}
