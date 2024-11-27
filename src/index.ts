// Reexport the native module. On web, it will be resolved to ExpoFerrostarModule.web.ts
// and on native platforms to ExpoFerrostarModule.ts
export { default } from './ExpoFerrostarModule';
export { default as ExpoFerrostarView } from './ExpoFerrostarView';
export * from  './ExpoFerrostar.types';
