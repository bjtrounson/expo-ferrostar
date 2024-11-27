import { requireNativeViewManager } from "expo-modules-core";
import React from "react";
import {
  ExpoFerrostarModule,
  NativeViewProps,
  NavigationControllerConfig,
  FerrostarViewProps,
  Route,
  UserLocation,
  Waypoint,
} from "./ExpoFerrostar.types";
import { StyleProp, ViewStyle } from "react-native";

const NativeView: React.ComponentType<
  NativeViewProps & {
    ref: React.RefObject<ExpoFerrostarModule>;
    style: StyleProp<ViewStyle>;
  }
> = requireNativeViewManager("ExpoFerrostar");

export class FerrostarNativeComponent extends React.Component<
  FerrostarViewProps & { style: StyleProp<ViewStyle> },
  {
    isNavigating: boolean;
  }
> {
  ref = React.createRef<ExpoFerrostarModule>();

  constructor(props: FerrostarViewProps & { style: StyleProp<ViewStyle> }) {
    super(props);
    this.state = {
      isNavigating: false,
    };
  }

  startNavigation(route: Route, options?: NavigationControllerConfig) {
    this.ref.current?.startNavigation(route, options);
    this.setState({
      isNavigating: true,
    });
  }

  stopNavigation(stopLocationUpdates?: boolean) {
    this.ref.current?.stopNavigation(stopLocationUpdates);
    this.setState({
      isNavigating: false,
    });
  }

  replaceRoute(route: Route, options?: NavigationControllerConfig) {
    this.ref.current?.replaceRoute(route, options);
  }

  advanceToNextStep() {
    this.ref.current?.advanceToNextStep();
  }

  async getRoutes(initialLocation: UserLocation, waypoints: Waypoint[]) {
    return await this.ref.current?.getRoutes(initialLocation, waypoints);
  }

  render(): React.ReactNode {
    return (
      <NativeView
        ref={this.ref}
        style={this.props.style}
        navigationOptions={{
          ...this.props,
        }}
        coreOptions={{
          ...this.props,
        }}
      />
    );
  }
}
