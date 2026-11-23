# color-tools Demo (ClojureScript)

Try color conversions, palettes and contrast checks in a Reagent UI. The
demo calls `jon.color-tools` in the browser.

## Prerequisites

- Node.js and npm
- Java

## Setup

From the repository root:

```bash
cd demo
npm install
```

## Development

Start the development server with live reloading:

```bash
npx shadow-cljs watch app
```

Open `http://localhost:8080` after compilation.

## Building for Production

Build the minified application in `public/`:

```bash
npx shadow-cljs release app
```

## Project Structure

- `src/demo/core.cljs`: Main application logic using Reagent and `jon.color-tools`
- `public/index.html`: Entry point HTML
- `public/css/`: Application styles
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
