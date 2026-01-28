// screens/StreamScreen/components/StreamStatus.tsx
import React from 'react';
import { View, Text } from 'react-native';
import Icon from 'react-native-vector-icons/MaterialIcons';
import { styles } from '../Components/styles/StreamScreenStyles';

interface StreamStatusProps {
  isStreaming: boolean;
}

const StreamStatus: React.FC<StreamStatusProps> = ({ isStreaming }) => {
  if (!isStreaming) return null;

  return (
    <View style={styles.streamStatusOverlay}>
      <View style={styles.statusRow}>
        <View style={styles.statusItem}>
          <Icon name="schedule" size={16} color="#FFFFFF" />
          <Text style={styles.statusText}>02:45:18</Text>
        </View>
        <View style={styles.statusItem}>
          <Icon name="network-wifi" size={16} color="#34C759" />
          <Text style={styles.statusText}>Excellent</Text>
        </View>
        <View style={styles.statusItem}>
          <Icon name="battery-full" size={16} color="#FFFFFF" />
          <Text style={styles.statusText}>78%</Text>
        </View>
      </View>
    </View>
  );
};

export default StreamStatus;