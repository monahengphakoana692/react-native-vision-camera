import SQLite from 'react-native-sqlite-storage';

SQLite.enablePromise(true);

let dbInstance: SQLite.SQLiteDatabase | null = null;

export const openDatabase = async () => {
  if (dbInstance) return dbInstance;

  dbInstance = await SQLite.openDatabase({
    name: 'streamy6DB.db',
    location: 'default',
  });

  return dbInstance;
};
