import { useEffect, useRef, useState } from 'react';
import { StyleSheet, View, Text, Pressable, Image } from 'react-native';
import RNPhotoPicker from 'react-native-photo-picker';

export default function App() {
  const [fixed, setFixed] = useState<string | null>(null);
  const [image, setImage] = useState<string | undefined>();
  const timeoutRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    timeoutRef.current = setTimeout(() => {
      setFixed('fixed');
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
      }
    });
  }

  return (
    <View style={styles.container}>
      <Pressable
        style={{ borderRadius: 12, backgroundColor: 'blue', padding: 12 }}
        onPress={() => handlePress()}
      >
        <Text style={{ fontSize: 24, color: 'white' }}>Open PhotoPicker</Text>
      </Pressable>
      <Text>{fixed}</Text>
      {image && (
        <>
          <Text style={{ marginTop: 48 }}>Selected image:</Text>
          <Image source={{ uri: image }} style={styles.box} />
        </>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
