import React, { useEffect, useState } from 'react';
import {
  PermissionsAndroid,
  Platform,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
} from 'react-native';

import Streamy6View from '../specs/Streamy6NativeComponent';

export default function StreamScreen() {
  const isDarkMode = useColorScheme() === 'dark';
  const [hasPermission, setHasPermission] = useState(false);
  const [checked, setChecked] = useState(false);

  useEffect(() => {
    const requestPermissions = async () => {
      if (Platform.OS === 'android') {
        try {
          const granted = await PermissionsAndroid.request(
            PermissionsAndroid.PERMISSIONS.CAMERA
          );
          setHasPermission(granted === PermissionsAndroid.RESULTS.GRANTED);
        } catch (e) {
          setHasPermission(false);
        }
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

      {hasPermission ? (
        <Streamy6View
          style={StyleSheet.absoluteFill}
          enabled={true}          // 🔥 THIS STARTS CAMERA + FRAME PROCESSING
          showDetection={true}    // 🔥 DRAWS FACE BOXES
        />
      ) : (
        <Text style={styles.text}>Camera permission not granted</Text>
      )}
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
});
