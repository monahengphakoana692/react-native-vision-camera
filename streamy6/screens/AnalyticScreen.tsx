import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  SafeAreaView,
  ScrollView,
  Button,
  ActivityIndicator,
} from 'react-native';

import {
  getLatestStream,
  getAllStreams,
  insertStream,
  clearStreams,
} from '../Database/streams.repo';

const AnalyticScreen = () => {
  const [latest, setLatest] = useState<any>(null);
  const [streams, setStreams] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    const all = await getAllStreams();
    const last = await getLatestStream();
    setStreams(all);
    setLatest(last);
    setLoading(false);
  };

  useEffect(() => {
    load();
  }, []);

  const addSample = async () => {
    const duration = `0${Math.floor(Math.random() * 2)}:${String(
      Math.floor(Math.random() * 60)
    ).padStart(2, '0')}:${String(
      Math.floor(Math.random() * 60)
    ).padStart(2, '0')}`;

    await insertStream(
      duration,
      Math.floor(Math.random() * 200) + 50,
      Math.floor(Math.random() * 300) + 200
    );

    load();
  };

  if (loading) {
    return (
      <SafeAreaView style={styles.centered}>
        <ActivityIndicator size="large" color="#22C55E" />

      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container}>

      <ScrollView>
        <Text style={styles.title}>Stream Analytics</Text>

        {latest && (
          <>
            <Text style={styles.stat}>Duration: {latest.duration}</Text>
            <Text style={styles.stat}>Current: {latest.current_viewers}</Text>
            <Text style={styles.stat}>Peak: {latest.peak_viewers}</Text>
            <Text style={styles.stat}>
              Average (DB Trigger): {latest.average_viewers}
            </Text>
          </>
        )}

        <Button title="Add Sample Stream" onPress={addSample} />
        <Button title="Clear Data" color="red" onPress={clearStreams} />

        <Text style={styles.subtitle}>All Records</Text>

        {streams.map((s) => (
          <Text key={s.id} style={styles.row}>
            {s.duration} | Avg: {s.average_viewers}
          </Text>
        ))}


      </ScrollView>
    </SafeAreaView>
  );
};

export default AnalyticScreen;

const styles = StyleSheet.create({

  container: { flex: 1, padding: 16, backgroundColor: '#0E0E13' },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  title: { fontSize: 26, color: '#FFF', marginBottom: 16 },
  subtitle: { marginTop: 24, color: '#AAA' },
  stat: { color: '#22C55E', marginBottom: 4 },
  row: { color: '#DDD', marginVertical: 2 },
});

