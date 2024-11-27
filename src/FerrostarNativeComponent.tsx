import { requireNativeViewManager } from "expo-modules-core";
import React, { forwardRef, useImperativeHandle, useRef } from "react";
import {
  ExpoFerrostarModule,
  NativeViewProps,
  NavigationControllerConfig,
  FerrostarViewProps,
  Route,
  UserLocation,
  Waypoint,
  NavigationStateChangeEvent,
} from "./ExpoFerrostar.types";
import { StyleProp, ViewStyle } from "react-native";

const NativeView: React.ComponentType<
  NativeViewProps & {
    ref: React.RefObject<ExpoFerrostarModule>;
    style: StyleProp<ViewStyle>;
    onNavigationStateChange?: (event: NavigationStateChangeEvent) => void;
  }
> = requireNativeViewManager("ExpoFerrostar");

export const FerrostarNativeComponent = forwardRef<
  ExpoFerrostarModule,
  FerrostarViewProps & {
    style: StyleProp<ViewStyle>;
    onNavigationStateChange?: (event: NavigationStateChangeEvent) => void;
  }
>((props, ref) => {
  const innerRef = useRef<ExpoFerrostarModule>(null);

  useImperativeHandle(ref, () => ({
    startNavigation: (route: Route, options?: NavigationControllerConfig) => {
      innerRef.current?.startNavigation(route, options);
    },
    stopNavigation: (stopLocationUpdates?: boolean) => {
      innerRef.current?.stopNavigation(stopLocationUpdates);
    },
    replaceRoute: (route: Route, options?: NavigationControllerConfig) => {
      innerRef.current?.replaceRoute(route, options);
    },
    advanceToNextStep: () => {
      innerRef.current?.advanceToNextStep();
    },
    getRoutes: async (initialLocation: UserLocation, waypoints: Waypoint[]) => {
      if (!innerRef.current) return [];
      return await innerRef.current.getRoutes(initialLocation, waypoints);
    },
  }));

  return (
    <NativeView
      ref={innerRef}
      style={props.style}
      onNavigationStateChange={(e) => {
        props.onNavigationStateChange?.(e);
      }}
      navigationOptions={{
        ...props,
      }}
      coreOptions={{
        ...props,
      }}
    />
  );
});
