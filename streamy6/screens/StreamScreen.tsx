import React, { useEffect, useState } from 'react';
import {
  PermissionsAndroid,
  Platform,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
  Pressable,
} from 'react-native';

import Streamy6View from '../specs/Streamy6NativeComponent';

export default function StreamScreen() {
  const isDarkMode = useColorScheme() === 'dark';
  const [hasPermission, setHasPermission] = useState(false);
  const [checked, setChecked] = useState(false);
  const [startStream, setStartStream] = useState(false);

  useEffect(() => {
    const requestPermissions = async () => {
      if (Platform.OS === 'android') {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.CAMERA
        );
        setHasPermission(granted === PermissionsAndroid.RESULTS.GRANTED);
      } else {
        setHasPermission(true);
      }
      setChecked(true);
    };

    requestPermissions();
  }, []);

  if (!checked) {
    return (
      <View style={styles.center}>
        <Text style={styles.text}>Checking permissions…</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />

      {hasPermission && (
        <Streamy6View
          style={StyleSheet.absoluteFill}
          enabled={true}
          showDetection={true}
          startStreaming={startStream}
        />
      )}

      <View style={styles.controls}>
        <Pressable
          style={styles.button}
          onPress={() => setStartStream(true)}
        >
          <Text style={styles.buttonText}>START STREAM</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'black',
  },
  center: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'black',
  },
  text: {
    color: 'white',
  },
  controls: {
    position: 'absolute',
    bottom: 40,
    alignSelf: 'center',
  },
  button: {
    backgroundColor: '#e11d48',
    paddingHorizontal: 24,
    paddingVertical: 14,
    borderRadius: 8,
  },
  buttonText: {
    color: 'white',
    fontWeight: 'bold',
    letterSpacing: 1,
  },
});