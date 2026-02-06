// Connection.tsx (updated import path)
import React, { useEffect } from 'react';
import { View, Text, Alert, ActivityIndicator } from 'react-native';
import { connectToDatabase, insertStream } from './database'; // Add insertStream to import
import { useNavigation } from '@react-navigation/native';

const Connection = () => {
  const navigation = useNavigation<any>();

  useEffect(() => {
    const initDB = async () => {
      try {
        const db = await connectToDatabase();
        
        // Ensure the stream table exists with the correct schema
        await db.executeSql(`
          CREATE TABLE IF NOT EXISTS streams (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            duration TEXT,
            current_viewers INTEGER,
            peak_viewers INTEGER,
            average_viewers INTEGER,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
          )
        `);
        
        console.log('Table verified/created successfully');
        
        // Optional: Add some initial sample data if table is empty
        const checkEmpty = await db.executeSql('SELECT COUNT(*) as count FROM streams');
        const count = checkEmpty[0]?.rows?.item(0)?.count || 0;
        
        if (count === 0) {
          await insertStream('01:24:36', 128, 342, 210);
          await insertStream('02:15:45', 156, 398, 245);
          await insertStream('00:45:22', 89, 210, 156);
          console.log('Sample data added');
        }

        Alert.alert(
          'Analytics',
          'Database connected successfully ✅',
          [
            {
              text: 'OK',
              onPress: () => navigation.replace('AnalyticScreen'),
            },
          ],
          { cancelable: false }
        );
      } catch (error) {
        console.error('Connection error:', error);
        Alert.alert(
          'Analytics',
          'Failed to connect to database ❌\nPlease restart the app.',
          [
            {
              text: 'Retry',
              onPress: () => initDB(),
            }
          ]
        );
      }
    };

    initDB();
  }, [navigation]);

  return (
    <View style={{ 
      flex: 1, 
      justifyContent: 'center', 
      alignItems: 'center',
      backgroundColor: '#0E0E13'
    }}>
      <ActivityIndicator size="large" color="#22C55E" />
      <Text style={{ 
        marginTop: 12, 
        color: '#FFFFFF',
        fontSize: 16 
      }}>
        Connecting to database…
      </Text>
      <Text style={{ 
        marginTop: 8, 
        color: '#9CA3AF',
        fontSize: 12 
      }}>
        analytics2.db
      </Text>
    </View>
  );
};

export default Connection;