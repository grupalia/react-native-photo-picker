import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-photo-picker' doesn't seem to be linked. Make sure:\n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", android: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const PhotoPickerModule = NativeModules.PhotoPicker
  ? NativeModules.PhotoPicker
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

interface PhotoPickerInterface {
  launchPicker(): Promise<string | null>;
}

const PhotoPicker: PhotoPickerInterface =
  PhotoPickerModule as PhotoPickerInterface;

export default PhotoPicker;
