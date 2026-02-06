// database/database.tsx
import SQLite from 'react-native-sqlite-storage';

SQLite.DEBUG(false);
SQLite.enablePromise(true);

let dbInstance: SQLite.SQLiteDatabase | null = null;

export const connectToDatabase = async (): Promise<SQLite.SQLiteDatabase> => {
  if (dbInstance) {
    return dbInstance;
  }

  try {
    dbInstance = await SQLite.openDatabase({
      name: 'analytics2.db',
      location: 'default',
    });

    console.log('Database connected');
    return dbInstance;
  } catch (error) {
    console.error('Database connection failed', error);
    throw error;
  }
};

export const getDatabase = (): SQLite.SQLiteDatabase => {
  if (!dbInstance) {
    throw new Error('Database not initialized. Call connectToDatabase first.');
  }
  return dbInstance;
};

export const closeDatabase = async (): Promise<void> => {
  if (dbInstance) {
    await dbInstance.close();
    dbInstance = null;
    console.log('Database closed');
  }
};

// Add these database operation functions:

export const insertStream = async (
  duration: string,
  current_viewers: number,
  peak_viewers: number,
  average_viewers: number
): Promise<void> => {
  try {
    const db = getDatabase();
    await db.executeSql(
      `INSERT INTO streams (duration, current_viewers, peak_viewers, average_viewers) 
       VALUES (?, ?, ?, ?)`,
      [duration, current_viewers, peak_viewers, average_viewers]
    );
    console.log('Stream data inserted successfully');
  } catch (error) {
    console.error('Error inserting stream data', error);
    throw error;
  }
};

export const getLatestStream = async (): Promise<any | null> => {
  try {
    const db = getDatabase();
    const results = await db.executeSql(
      `SELECT * FROM streams 
       ORDER BY created_at DESC 
       LIMIT 1`
    );
    
    if (results[0] && results[0].rows.length > 0) {
      return results[0].rows.item(0);
    }
    
    return null;
  } catch (error) {
    console.error('Error getting latest stream', error);
    throw error;
  }
};

export const getAllStreams = async (): Promise<any[]> => {
  try {
    const db = getDatabase();
    const results = await db.executeSql(
      `SELECT * FROM streams 
       ORDER BY created_at DESC`
    );
    
    const streams: any[] = [];
    
    if (results[0]) {
      for (let i = 0; i < results[0].rows.length; i++) {
        streams.push(results[0].rows.item(i));
      }
    }
    
    return streams;
  } catch (error) {
    console.error('Error getting all streams', error);
    return []; // Return empty array instead of throwing
  }
};

export const getStreamById = async (id: number): Promise<any | null> => {
  try {
    const db = getDatabase();
    const results = await db.executeSql(
      'SELECT * FROM streams WHERE id = ?',
      [id]
    );
    
    if (results[0] && results[0].rows.length > 0) {
      return results[0].rows.item(0);
    }
    
    return null;
  } catch (error) {
    console.error('Error getting stream by ID', error);
    throw error;
  }
};

export const clearAllStreams = async (): Promise<void> => {
  try {
    const db = getDatabase();
    await db.executeSql('DELETE FROM streams');
    console.log('All streams cleared');
  } catch (error) {
    console.error('Error clearing streams', error);
    throw error;
  }
};

export const getStreamCount = async (): Promise<number> => {
  try {
    const db = getDatabase();
    const results = await db.executeSql('SELECT COUNT(*) as count FROM streams');
    
    if (results[0] && results[0].rows.length > 0) {
      return results[0].rows.item(0).count;
    }
    
    return 0;
  } catch (error) {
    console.error('Error getting stream count', error);
    return 0;
  }
};