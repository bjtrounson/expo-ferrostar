package expo.modules.ferrostar.records

import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record
import java.net.URL

class CoreOptions : Record {
    @Field val valhallaEndpointURL: URL = URL("https://valhalla1.openstreeetmap.de/route")

    @Field val profile: String = "auto"

    @Field val options: Map<String, Any> = emptyMap()

    @Field val locationMode: LocationMode = LocationMode.DEFAULT

    @Field val navigationControllerConfig: NavigationControllerConfig = NavigationControllerConfig()
}


