# color-tools Demo (ClojureScript)

This is a ClojureScript demo application for the `color-tools` library. It demonstrates the library's features using a reactive UI built with Reagent.

## Prerequisites

- Node.js and npm
- Java

## Setup

1. Install dependencies:
   ```bash
   npm install
   ```

## Development

To start the development server with hot reloading:

```bash
npx shadow-cljs watch app
```

This will compile the application and serve it at `http://localhost:8080`.

## Building for Production

To build a minified release version:

```bash
npx shadow-cljs release app
```

## Project Structure

- `src/demo/core.cljs`: Main application logic using Reagent and `jon.color-tools`
- `public/index.html`: Entry point HTML
- `public/css/styles.css`: Application styles
- `deps.edn`: Clojure dependencies (includes local `color-tools` reference)
- `shadow-cljs.edn`: Build configuration

## Features Demonstrated

- **Color Conversions**: Real-time HEX/RGB/HSL/HSV conversion
- **Blending Modes**: Multiply, Screen, Overlay
- **Tints/Shades/Tones**: Generation of color variations
- **Gradients**: Interpolation in different color spaces
- **Alpha Blending**: Porter-Duff compositing
- **Delta E**: Perceptual color difference
- **Kelvin**: Color temperature visualization
- **Accessibility**: Contrast ratio checking
- **Harmony**: Color harmony generation

All logic is performed using the `jon.color-tools` library directly in the browser.
