console.log('LinkedAppsScreen mounted');
import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';

console.log('LinkedAppsScreen mounted');

const LinkedAppsScreen = () => {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Linked Apps</Text>

      {/* Facebook Card */}
      <View style={styles.card}>
        <View style={styles.cardText}>
          <Text style={styles.appName}>Facebook Live</Text>
          <Text style={styles.status}>Not connected</Text>
        </View>

        <TouchableOpacity style={styles.button}>
          <Text style={styles.buttonText}>Connect</Text>
        </TouchableOpacity>
      </View>

      {/* Placeholder for future apps */}
      <Text style={styles.comingSoon}>More platforms coming soon…</Text>
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
    alignItems: 'center',
    backgroundColor: '#111',
    borderRadius: 12,
    padding: 16,
  },
  cardText: {
    flex: 1,
    marginLeft: 12,
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
  comingSoon: {
    marginTop: 30,
    color: '#666',
    textAlign: 'center',
  },
});
