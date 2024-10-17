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

export interface ExifData {
  // Image Data
  DateTimeOriginal?: string;
  Make?: string;
  Model?: string;
  Orientation?: string;
  ImageWidth?: string;
  ImageLength?: string;

  // Camera Settings
  ExposureTime?: string;
  FNumber?: string;
  ISOSpeedRatings?: string;
  FocalLength?: string;

  // GPS Data
  GPSLatitude?: string;
  GPSLatitudeRef?: string;
  GPSLongitude?: string;
  GPSLongitudeRef?: string;
  GPSAltitude?: string;
  GPSAltitudeRef?: string;

  // Flash Data
  Flash?: string;
}

interface PhotoPickerResult {
  uri: string;
  width: number;
  height: number;
  fileSize: number;
  exif: ExifData;
}

interface PhotoPickerOptions {
  maxSize?: number; // max size in pixels
}

interface PhotoPickerInterface {
  launchPicker(options?: PhotoPickerOptions): Promise<PhotoPickerResult | null>;
}

const photoPicker: PhotoPickerInterface = {
  launchPicker: async (options?: PhotoPickerOptions) => {
    const opts = options || {};
    return PhotoPickerModule.launchPicker(opts);
  },
};

export type { PhotoPickerResult, PhotoPickerInterface, PhotoPickerOptions };
export default photoPicker;
