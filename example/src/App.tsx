import { useEffect, useRef, useState } from 'react';
import { View, Text, Pressable, Image } from 'react-native';
import RNPhotoPicker, {
  type PhotoPickerResult,
} from '@grupalia/react-native-photo-picker';
import RNFS from 'react-native-fs'; // Install this package if you need to read file size from URI

export default function App() {
  const [activity, setActivity] = useState<string | null>(null);
  const [image, setImage] = useState<PhotoPickerResult | undefined>();
  const [imageSize, setImageSize] = useState<number | null>(null); // To store image size in bytes
  const timeoutRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    timeoutRef.current = setTimeout(() => {
      setActivity('active');
    }, 3000);

    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, []);

  function handlePress() {
    RNPhotoPicker.launchPicker().then((result) => {
      if (result) {
        setImage(result);
        getFileSize(result.uri); // Get the file size after selecting image
      }
    });
  }

  function handleResizePress() {
    RNPhotoPicker.launchPicker({ maxSize: 512 }).then((result) => {
      if (result) {
        setImage(result);
        getFileSize(result.uri); // Get the file size after selecting image
      }
    });
  }

  // Function to get the file size in bytes
  async function getFileSize(uri: string) {
    try {
      const fileInfo = await RNFS.stat(uri);
      setImageSize(fileInfo.size); // Size in bytes
    } catch (error) {
      console.error('Error getting file size:', error);
      setImageSize(null);
    }
  }

  return (
    <View
      style={{
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        marginVertical: 48,
      }}
    >
      <Pressable
        style={{ borderRadius: 12, backgroundColor: 'blue', padding: 12 }}
        onPress={handlePress}
      >
        <Text style={{ fontSize: 24, color: 'white' }}>Open PhotoPicker</Text>
      </Pressable>
      <Pressable
        style={{
          borderRadius: 12,
          backgroundColor: 'blue',
          padding: 12,
          marginTop: 24,
        }}
        onPress={handleResizePress}
      >
        <Text style={{ fontSize: 24, color: 'white' }}>
          Open Resized PhotoPicker
        </Text>
      </Pressable>
      {/* to check if the activity was closed when opening the picker */}
      <Text>{activity}</Text>
      {image && (
        <>
          <Text style={{ marginTop: 48 }}>Selected image:</Text>
          <Image
            source={{ uri: image.uri }}
            style={{ width: 300, height: 300, marginTop: 64 }}
            resizeMode="cover"
          />
          <Text style={{ marginTop: 16 }}>
            Image size in bytes: {imageSize} bytes
          </Text>
          <Text style={{ marginTop: 16 }}>
            Exif: {JSON.stringify(image.exif)}
          </Text>
        </>
      )}
    </View>
  );
}
