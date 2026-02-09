import { openDatabase } from './index';

export const insertStream = async (
  duration: string,
  current: number,
  peak: number
) => {
  const db = await openDatabase();

  await db.executeSql(
    `INSERT INTO streams (
      duration,
      current_viewers,
      peak_viewers,
      created_at
    ) VALUES (?, ?, ?, strftime('%s','now'));`,
    [duration, current, peak]
  );
};


export const getAllStreams = async () => {
  const db = await openDatabase();
  const [res] = await db.executeSql(`
    SELECT
      s.id,
      s.duration,
      s.current_viewers,
      s.peak_viewers,
      IFNULL(st.average_viewers, 0) AS average_viewers
    FROM streams s
    LEFT JOIN stream_stats st ON s.id = st.stream_id
    ORDER BY s.created_at DESC;
  `);

  return res.rows.raw();
};

export const getLatestStream = async () => {
  const db = await openDatabase();
  const [res] = await db.executeSql(`
    SELECT
      s.duration,
      s.current_viewers,
      s.peak_viewers,
      IFNULL(st.average_viewers, 0) AS average_viewers
    FROM streams s
    LEFT JOIN stream_stats st ON s.id = st.stream_id
    ORDER BY s.created_at DESC
    LIMIT 1;
  `);

  return res.rows.length ? res.rows.item(0) : null;
};

export const clearStreams = async () => {
  const db = await openDatabase();
  await db.executeSql(`DELETE FROM stream_stats;`);
  await db.executeSql(`DELETE FROM streams;`);
};
