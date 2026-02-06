import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  SafeAreaView,
  ScrollView,
  Button,
  RefreshControl,
  ActivityIndicator,
} from 'react-native';

import { 
  getLatestStream, 
  insertStream, 
  getAllStreams,
  clearAllStreams 
} from '../Database/database';

const AnalyticScreen = () => {
  const [streamDuration, setStreamDuration] = useState('00:00:00');
  const [currentViewers, setCurrentViewers] = useState(0);
  const [peakViewers, setPeakViewers] = useState(0);
  const [averageViewers, setAverageViewers] = useState(0);
  const [allStreams, setAllStreams] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const loadAnalytics = async () => {
    try {
      setLoading(true);
      
      // First, check if we have any data
      const allData = await getAllStreams();
      setAllStreams(allData);
      
      // If no data exists, seed with sample data
      if (allData.length === 0) {
        console.log('No data found, inserting sample data...');
        await insertStream('01:24:36', 128, 342, 210);
        await insertStream('02:15:45', 156, 398, 245);
        await insertStream('00:45:22', 89, 210, 156);
        
        // Reload data after seeding
        const updatedData = await getAllStreams();
        setAllStreams(updatedData);
      }
      
      // Get the latest stream for display
      const latestStream = await getLatestStream();

      if (latestStream) {
        setStreamDuration(latestStream.duration);
        setCurrentViewers(latestStream.current_viewers);
        setPeakViewers(latestStream.peak_viewers);
        setAverageViewers(latestStream.average_viewers);
      } else {
        console.log('No stream data found');
      }
    } catch (error) {
      console.error('Error loading analytics:', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadAnalytics();
  };

  const handleAddSampleStream = async () => {
    try {
      // Add a new sample stream
      const randomDuration = `${Math.floor(Math.random() * 2) + 1}:${String(Math.floor(Math.random() * 60)).padStart(2, '0')}:${String(Math.floor(Math.random() * 60)).padStart(2, '0')}`;
      const randomCurrent = Math.floor(Math.random() * 200) + 50;
      const randomPeak = Math.floor(Math.random() * 300) + 200;
      const randomAverage = Math.floor((randomCurrent + randomPeak) / 2);
      
      await insertStream(randomDuration, randomCurrent, randomPeak, randomAverage);
      await loadAnalytics();
    } catch (error) {
      console.error('Error adding sample stream:', error);
    }
  };

  const handleClearData = async () => {
    try {
      await clearAllStreams();
      await loadAnalytics();
    } catch (error) {
      console.error('Error clearing data:', error);
    }
  };

  useEffect(() => {
    loadAnalytics();
  }, []);

  if (loading && !refreshing) {
    return (
      <SafeAreaView style={[styles.container, styles.centered]}>
        <ActivityIndicator size="large" color="#22C55E" />
        <Text style={styles.loadingText}>Loading analytics...</Text>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView 
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            tintColor="#22C55E"
            colors={['#22C55E']}
          />
        }
      >
        <Text style={styles.title}>Stream Analytics</Text>
        
        <View style={styles.dataInfo}>
          <Text style={styles.infoText}>
            Displaying latest stream data from database
          </Text>
          <Text style={styles.infoText}>
            Total streams recorded: {allStreams.length}
          </Text>
        </View>

        <View style={styles.mainCard}>
          <Text style={styles.label}>Stream Duration</Text>
          <Text style={styles.duration}>{streamDuration}</Text>
        </View>

        <View style={styles.row}>
          <View style={styles.statCard}>
            <Text style={styles.label}>Current Viewers</Text>
            <Text style={styles.value}>{currentViewers}</Text>
          </View>

          <View style={styles.statCard}>
            <Text style={styles.label}>Peak Viewers</Text>
            <Text style={styles.value}>{peakViewers}</Text>
          </View>
        </View>

        <View style={styles.statWide}>
          <Text style={styles.label}>Average Viewers</Text>
          <Text style={styles.value}>{averageViewers}</Text>
        </View>

        <View style={styles.controlsContainer}>
          <Button
            title="Add Sample Stream"
            onPress={handleAddSampleStream}
            color="#22C55E"
          />
          {allStreams.length > 0 && (
            <Button
              title="Clear All Data"
              onPress={handleClearData}
              color="#EF4444"
            />
          )}
        </View>

        <View style={styles.chartPlaceholder}>
          <Text style={styles.chartText}>
            Database Records: {allStreams.length} stream(s)
          </Text>
          {allStreams.length > 0 && (
            <ScrollView style={styles.dataList}>
              {allStreams.map((stream, index) => (
                <View key={stream.id} style={styles.dataRow}>
                  <Text style={styles.dataText}>
                    #{index + 1}: {stream.duration} - Current: {stream.current_viewers}, Peak: {stream.peak_viewers}, Avg: {stream.average_viewers}
                  </Text>
                </View>
              ))}
            </ScrollView>
          )}
        </View>

      </ScrollView>
    </SafeAreaView>
  );
};

export default AnalyticScreen;

const styles = StyleSheet.create({
  container: { 
    flex: 1, 
    backgroundColor: '#0E0E13', 
    paddingHorizontal: 16,
  },
  centered: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  title: { 
    fontSize: 28, 
    fontWeight: '700', 
    color: '#FFFFFF', 
    marginVertical: 20,
  },
  dataInfo: {
    backgroundColor: '#1F1F2B',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
  },
  infoText: {
    fontSize: 14,
    color: '#9CA3AF',
    marginBottom: 4,
  },
  mainCard: { 
    backgroundColor: '#1F1F2B', 
    borderRadius: 16, 
    padding: 20, 
    marginBottom: 20,
  },
  duration: { 
    fontSize: 32, 
    fontWeight: '700', 
    color: '#22C55E', 
    marginTop: 8,
  },
  row: { 
    flexDirection: 'row', 
    gap: 12, 
    marginBottom: 16,
  },
  statCard: { 
    flex: 1, 
    backgroundColor: '#1C1C26', 
    borderRadius: 14, 
    padding: 16,
  },
  statWide: { 
    backgroundColor: '#1C1C26', 
    borderRadius: 14, 
    padding: 16, 
    marginBottom: 20,
  },
  label: { 
    fontSize: 13, 
    color: '#9CA3AF', 
    marginBottom: 6,
  },
  value: { 
    fontSize: 24, 
    fontWeight: '600', 
    color: '#FFFFFF',
  },
  chartPlaceholder: { 
    height: 300,
    borderRadius: 14, 
    borderWidth: 1, 
    borderColor: '#2E2E3E', 
    padding: 16,
    marginBottom: 20,
  },
  chartText: { 
    color: '#6B7280', 
    fontSize: 14,
    marginBottom: 12,
  },
  dataList: {
    flex: 1,
  },
  dataRow: {
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#2E2E3E',
  },
  dataText: {
    color: '#D1D5DB',
    fontSize: 12,
  },
  controlsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginBottom: 20,
    gap: 12,
  },
  loadingText: {
    color: '#FFFFFF',
    marginTop: 16,
    fontSize: 16,
  },
});