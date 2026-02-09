import { openDatabase } from './index';

export const createTablesAndTriggers = async () => {
  const db = await openDatabase();

  // ---------- TABLES ----------
  await db.executeSql(`
    CREATE TABLE IF NOT EXISTS streams (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      duration TEXT NOT NULL,
      current_viewers INTEGER NOT NULL,
      peak_viewers INTEGER NOT NULL,
      created_at INTEGER NOT NULL
    );
  `);
  

  await db.executeSql(`
    CREATE TABLE IF NOT EXISTS stream_stats (
      stream_id INTEGER PRIMARY KEY,
      average_viewers INTEGER NOT NULL,
      FOREIGN KEY (stream_id) REFERENCES streams(id)
    );
  `);

  // ---------- TRIGGERS ----------
  //await db.executeSql(`DROP TRIGGER IF EXISTS trg_after_stream_insert;`);

  /*await db.executeSql(`
    CREATE TRIGGER trg_after_stream_insert
    AFTER INSERT ON streams
    BEGIN
      INSERT INTO stream_stats (stream_id, average_viewers)
      VALUES (
        NEW.id,
        (NEW.current_viewers + NEW.peak_viewers) / 2
      );
    END;
  `);*/
};
