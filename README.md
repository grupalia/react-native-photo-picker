# react-native-photo-picker

Provides access to [Android Photo Picker](https://developer.android.com/training/data-storage/shared/photopicker)

## Installation

```sh
# with npm
npm install @grupalia/react-native-photo-picker

# with yarn
yarn add @grupalia/react-native-photo-picker
```

## Usage


```js
import RNPhotoPicker from '@grupalia/react-native-photo-picker';

// ...
RNPhotoPicker.launchPicker().then((result) => {
  if (result) {
    setImage(result);
  }
});
```


## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob) with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
