<a href="https://margelo.com">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="./docs/static/img/banner-dark.png" />
    <source media="(prefers-color-scheme: light)" srcset="./docs/static/img/banner-light.png" />
    <img alt="VisionCamera" src="./docs/static/img/banner-light.png" />
  </picture>
</a>

<br />

<div>
  <img align="right" width="35%" src="docs/static/img/example.png">
</div>

### Features

VisionCamera is a powerful, high-performance Camera library for React Native. It features:

* 📸 Photo and Video capture
* 👁️ QR/Barcode scanner
* 📱 Customizable devices and multi-cameras ("fish-eye" zoom)
* 🎞️ Customizable resolutions and aspect-ratios (4k/8k images)
* ⏱️ Customizable FPS (30..240 FPS)
* 🧩 [Frame Processors](https://react-native-vision-camera.com/docs/guides/frame-processors) (JS worklets to run facial recognition, AI object detection, realtime video chats, ...)
* 🎨 Drawing shapes, text, filters or shaders onto the Camera
* 🔍 Smooth zooming (Reanimated)
* ⏯️ Fast pause and resume
* 🌓 HDR & Night modes
* ⚡ Custom C++/GPU accelerated video pipeline (OpenGL)

Install VisionCamera from npm:

```sh
npm i react-native-vision-camera
cd ios && pod install
```

..and get started by [setting up permissions](https://react-native-vision-camera.com/docs/guides)!

### Documentation

* [Guides](https://react-native-vision-camera.com/docs/guides)
* [API](https://react-native-vision-camera.com/docs/api)
* [Example](./example/)
* [Frame Processor Plugins](https://react-native-vision-camera.com/docs/guides/frame-processor-plugins-community)

### ShadowLens

To see VisionCamera in action, check out [ShadowLens](https://mrousavy.com/projects/shadowlens)!

<div>
  <a href="https://apps.apple.com/app/shadowlens/id6471849004">
    <img height="40" src="docs/static/img/appstore.svg" />
  </a>
  <a href="https://play.google.com/store/apps/details?id=com.mrousavy.shadowlens">
    <img height="40" src="docs/static/img/googleplay.svg" />
  </a>
</div>

### Example

```tsx
function App() {
  const device = useCameraDevice('back')

  if (device == null) return <NoCameraErrorView />
  return (
    <Camera
      style={StyleSheet.absoluteFill}
      device={device}
      isActive={true}
    />
  )
}
```

> See the [example](./example/) app

### Adopting at scale

<a href="https://github.com/sponsors/mrousavy">
  <img align="right" width="160" alt="This library helped you? Consider sponsoring!" src=".github/funding-octocat.svg">
</a>

VisionCamera is provided _as is_, I work on it in my free time.

If you're integrating VisionCamera in a production app, consider [funding this project](https://github.com/sponsors/mrousavy) and <a href="mailto:me@mrousavy.com?subject=Adopting VisionCamera at scale">contact me</a> to receive premium enterprise support, help with issues, prioritize bugfixes, request features, help at integrating VisionCamera and/or Frame Processors, and more.

### Socials

* 🐦 [**Follow me on Twitter**](https://twitter.com/mrousavy) for updates
* 📝 [**Check out my blog**](https://mrousavy.com/blog) for examples and experiments
* 💬 [**Join the Margelo Community Discord**](https://margelo.com/discord) for chatting about VisionCamera
* 💖 [**Sponsor me on GitHub**](https://github.com/sponsors/mrousavy) to support my work
* 🍪 [**Buy me a Ko-Fi**](https://ko-fi.com/mrousavy) to support my work
--------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------

THIS IS A GUIDE OF HOW YOU CAN HAVE THE LIBRAY IN YOUR LOCAL MACHINE. SO THE AIM IS TO HAVE THE LIBRARY IN YOUR LAPTOP AND THEN NAVIGATE TO THE PROJECT S FOLDER NAME streamy6 AND THEN OPEN THAT IN VS CODE THEN RUN THE PROJECT.... GOOD LUCK BUDDY!

---------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------

1. you need to make sure that you have access to the project
2. open you powershell run as admin
3. this assumes you have git installed in your computer
4. run this git clone https://github.com/monahengphakoana692/react-native-vision-camera.git
5. cd react-native-vision-camera
6. npm install
7. cd streamy6
8. code . , to open vs code, then open terminal in vs code 
9. npm install
10. npx react-native start
11. leave the terminal and open another one 
12. npx react-native run-android
13. 