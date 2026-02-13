import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Alert } from 'react-native';
import { LoginButton } from 'react-native-fbsdk-next';
import { connectFacebook } from '../services/facebookAuth';

const LinkedAppsScreen = () => {
  const [connected, setConnected] = useState(false);

  const handleConnect = async () => {
    const result = await connectFacebook();

    if (!result.success) {
      Alert.alert('Facebook Login Failed', result.message || '');
      return;
    }

    console.log('Facebook Access Token:', result.token);
    setConnected(true);
    Alert.alert('Success', 'Facebook connected!');
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Linked Apps</Text>

      <View style={styles.card}>
        <View>
          <Text style={styles.appName}>Facebook</Text>
          <Text style={styles.status}>
            {connected ? 'Connected' : 'Not connected'}
          </Text>
        </View>

        <TouchableOpacity
          style={[
            styles.button,
            connected && { backgroundColor: '#22C55E' },
          ]}
          onPress={handleConnect}
          disabled={connected}
        >
          <Text style={styles.buttonText}>
            {connected ? 'Connected' : 'Connect'}
          </Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

export default LinkedAppsScreen;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#000',
    padding: 20,
  },
  title: {
    color: '#fff',
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
  },
  card: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    backgroundColor: '#111',
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  appName: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  status: {
    color: '#aaa',
    fontSize: 12,
    marginTop: 4,
  },
  button: {
    backgroundColor: '#1877F2',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 8,
  },
  buttonText: {
    color: '#fff',
    fontWeight: '600',
  },
});
