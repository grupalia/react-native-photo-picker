# react-native-photo-picker

Provides access to Android Photo Picker

## Installation

```sh
npm install react-native-photo-picker
```

## Usage


```js
import RNPhotoPicker from 'react-native-photo-picker';

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

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
