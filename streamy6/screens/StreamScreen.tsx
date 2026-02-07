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

      {/* Always show camera preview when permission granted */}
      {hasPermission && (
        <>
          <Streamy6View
            style={StyleSheet.absoluteFill}
            enabled={true}
            showDetection={!startStream} // Only show detection when not streaming (optional)
            startStreaming={startStream}
          />
          
          {/* Recording indicator overlay */}
          {startStream && (
            <View style={styles.recordingIndicator}>
              <View style={styles.recordingDot} />
              <Text style={styles.recordingText}>REC</Text>
              <Text style={styles.recordingTime}>00:00</Text>
            </View>
          )}
        </>
      )}

      {/* Control buttons */}
      <View style={styles.controls}>
        {!startStream ? (
          <Pressable
            style={styles.button}
            onPress={() => setStartStream(true)}
          >
            <Text style={styles.buttonText}>START STREAM</Text>
          </Pressable>
        ) : (
          <Pressable
            style={[styles.button, styles.stopButton]}
            onPress={() => setStartStream(false)}
          >
            <Text style={styles.buttonText}>STOP STREAM</Text>
          </Pressable>
        )}
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
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
  stopButton: {
    backgroundColor: '#dc2626',
  },
  buttonText: {
    color: 'white',
    fontWeight: 'bold',
    letterSpacing: 1,
    fontSize: 16,
  },
  recordingIndicator: {
    position: 'absolute',
    top: 50,
    left: 20,
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
    borderRadius: 20,
    paddingHorizontal: 12,
    paddingVertical: 8,
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 2,
    borderColor: '#dc2626',
  },
  recordingDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: '#dc2626',
    marginRight: 8,
  },
  recordingText: {
    color: 'white',
    fontWeight: 'bold',
    fontSize: 14,
    marginRight: 8,
  },
  recordingTime: {
    color: 'white',
    fontWeight: 'bold',
    fontSize: 14,
  },
});