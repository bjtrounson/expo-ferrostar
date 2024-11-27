// Reexport the native module. On web, it will be resolved to ExpoFerrostarModule.web.ts
// and on native platforms to ExpoFerrostarModule.ts
export { FerrostarNativeComponent as Ferrostar } from "./FerrostarNativeComponent";
export * from "./ExpoFerrostar.types";
