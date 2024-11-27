# Expo Ferrostar

This is a library for using [Ferrostar](https://github.com/stadiamaps/ferrostar) navigation library for [Expo](https://expo.dev/).

## Installation

```sh
expo install expo-ferrostar
```

## Simple Usage

```tsx
import { Ferrostar } from "expo-ferrostar";

export default function App() {
  return (
    <SafeAreaView style={styles.container}>
      <Ferrostar style={{ flex: 1, width: "100%" }} />
    </SafeAreaView>
  );
}
```
