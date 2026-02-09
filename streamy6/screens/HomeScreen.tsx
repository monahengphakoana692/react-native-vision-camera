// src/screens/HomeScreen.tsx
import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
} from 'react-native';
import SimpleIcon from '../Components/SimpleIcon';

// Define the navigation prop type
type HomeScreenProps = {
  navigation: {
    navigate: (screen: string) => void;
  };
};

const HomeScreen: React.FC<HomeScreenProps> = ({ navigation }) => {
  const menuItems = [
    {
      id: 1,
      title: 'Go Live',
      icon: 'videocam',
      color: '#007AFF',
      gradient: ['#FF3B30', '#FF5E3A'],
      // FIX: Use the screen name, not file path
      onPress: () => navigation.navigate('StreamScreen'),
    },
    {
      id: 2,
      title: 'Linked Apps',
      icon: 'link',
      color: '#007AFF',
      gradient: ['#007AFF', '#5856D6'],
      onPress: () => console.log('Linked Apps pressed'),
    },
    {
      id: 3,
      title: 'Settings',
      icon: 'settings',
      color: '#34C759',
      gradient: ['#34C759', '#4CD964'],
      onPress: () => console.log('Settings pressed'),
    },
    {
      id: 4,
      title: 'Analytics',
      icon: 'analytics',
      color: '#AF52DE',
      gradient: ['#AF52DE', '#FF2D55'],
      onPress: () => navigation.navigate('AnalyticScreen'),
    },
  ];

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#0A0A0A" />
      
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.appTitle}>LVContentFilter</Text>
        <Text style={styles.appSubtitle}>Live Video Content Filtering System</Text>
      </View>

      {/* Main Content */}
      <View style={styles.content}>
        {/* Grid of Menu Items */}
        <View style={styles.grid}>
          {menuItems.map((item) => (
            <TouchableOpacity
              key={item.id}
              style={[styles.card, { 
                backgroundColor: item.color + '20',
                shadowColor: item.color,
              }]}
              onPress={item.onPress}
              activeOpacity={0.8}>
              <View style={styles.iconContainer}>
                <SimpleIcon name={item.icon} color={item.color} size={32} />
              </View>
              <Text style={styles.cardTitle}>{item.title}</Text>
            </TouchableOpacity>
          ))}
        </View>
      </View>

      {/* Footer */}
      <View style={styles.footer}>
        <Text style={styles.footerText}>
          Live Video Content Filtering System v1.0
        </Text>
        <Text style={styles.footerSubtext}>
          For Journalists • Privacy First • Real-time AI
        </Text>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0A0A0A',
  },
  header: {
    paddingHorizontal: 24,
    paddingTop: 20,
    paddingBottom: 10,
    alignItems: 'center',
  },
  appTitle: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#FFFFFF',
    letterSpacing: 1,
    marginBottom: 4,
  },
  appSubtitle: {
    fontSize: 14,
    color: '#8E8E93',
    textAlign: 'center',
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 20,
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    marginBottom: 40,
  },
  card: {
    width: '48%',
    marginBottom: 20,
    borderRadius: 20,
    padding: 24,
    aspectRatio: 1,
    justifyContent: 'center',
    alignItems: 'center',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.3,
    shadowRadius: 20,
    elevation: 10,
  },
  iconContainer: {
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 16,
  },
  cardTitle: {
    fontSize: 20,
    fontWeight: '600',
    color: '#FFFFFF',
    marginTop: 8,
  },
  footer: {
    paddingVertical: 20,
    paddingHorizontal: 24,
    alignItems: 'center',
    borderTopWidth: 1,
    borderTopColor: 'rgba(255, 255, 255, 0.1)',
  },
  footerText: {
    fontSize: 14,
    color: '#8E8E93',
    marginBottom: 4,
  },
  footerSubtext: {
    fontSize: 12,
    color: '#636366',
  },
});

export default HomeScreen;