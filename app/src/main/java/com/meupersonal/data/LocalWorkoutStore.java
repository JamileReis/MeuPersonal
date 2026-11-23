package com.meupersonal.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public final class LocalWorkoutStore extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "meu_personal_local.db";
    private static final int DATABASE_VERSION = 5;

    public LocalWorkoutStore(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE plans (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "objective TEXT NOT NULL," +
                "created_at INTEGER NOT NULL," +
                "plan_json TEXT NOT NULL," +
                "remote_client_id TEXT," +
                "remote_synced INTEGER NOT NULL DEFAULT 0)");

        db.execSQL("CREATE TABLE plan_images (" +
                "plan_id INTEGER NOT NULL," +
                "day_index INTEGER NOT NULL," +
                "image BLOB NOT NULL," +
                "PRIMARY KEY(plan_id, day_index)," +
                "FOREIGN KEY(plan_id) REFERENCES plans(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE workout_sessions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "plan_id INTEGER NOT NULL," +
                "workout_index INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "started_at INTEGER NOT NULL," +
                "finished_at INTEGER," +
                "completed INTEGER NOT NULL DEFAULT 0," +
                "FOREIGN KEY(plan_id) REFERENCES plans(id) ON DELETE CASCADE)");

        db.execSQL("CREATE INDEX idx_sessions_started " +
                "ON workout_sessions(started_at DESC)");

        createExerciseImagesTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            createExerciseImagesTable(db);
        }
        ensurePlanSyncColumns(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Também repara bancos parcialmente migrados cuja versão já foi incrementada.
        ensurePlanSyncColumns(db);
    }

    private void ensurePlanSyncColumns(SQLiteDatabase db) {
        addColumnIfMissing(db, "remote_client_id", "TEXT");
        addColumnIfMissing(db, "remote_synced", "INTEGER NOT NULL DEFAULT 0");
    }

    private void addColumnIfMissing(
            SQLiteDatabase db,
            String columnName,
            String columnDefinition
    ) {
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(plans)", null)) {
            int nameColumn = cursor.getColumnIndexOrThrow("name");
            while (cursor.moveToNext()) {
                if (columnName.equals(cursor.getString(nameColumn))) {
                    return;
                }
            }
        }
        db.execSQL("ALTER TABLE plans ADD COLUMN " + columnName + " " + columnDefinition);
    }

    private void createExerciseImagesTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS exercise_images (" +
                "plan_id INTEGER NOT NULL," +
                "day_index INTEGER NOT NULL," +
                "exercise_index INTEGER NOT NULL," +
                "image BLOB NOT NULL," +
                "PRIMARY KEY(plan_id, day_index, exercise_index)," +
                "FOREIGN KEY(plan_id) REFERENCES plans(id) ON DELETE CASCADE)");
    }

    public void saveExerciseImage(
            long planId,
            int dayIndex,
            int exerciseIndex,
            byte[] image
    ) {
        ContentValues values = new ContentValues();
        values.put("plan_id", planId);
        values.put("day_index", dayIndex);
        values.put("exercise_index", exerciseIndex);
        values.put("image", image);

        getWritableDatabase().insertWithOnConflict(
                "exercise_images",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    public byte[] getExerciseImage(
            long planId,
            int dayIndex,
            int exerciseIndex
    ) {
        try (Cursor c = getReadableDatabase().query(
                "exercise_images",
                new String[]{"image"},
                "plan_id=? AND day_index=? AND exercise_index=?",
                new String[]{
                        String.valueOf(planId),
                        String.valueOf(dayIndex),
                        String.valueOf(exerciseIndex)
                },
                null,
                null,
                null,
                "1"
        )) {
            return c.moveToFirst() ? c.getBlob(0) : null;
        }
    }

    public long savePlan(
            String title,
            String objective,
            String json,
            long createdAt
    ) {
        return savePlan(title, objective, json, createdAt, null);
    }

    public long savePlan(
            String title,
            String objective,
            String json,
            long createdAt,
            String remoteClientId
    ) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("objective", objective == null ? "" : objective);
        values.put("created_at", createdAt);
        values.put("plan_json", json);
        if (remoteClientId == null || remoteClientId.trim().isEmpty()) {
            values.putNull("remote_client_id");
        } else {
            values.put("remote_client_id", remoteClientId);
        }
        return getWritableDatabase().insertOrThrow("plans", null, values);
    }

    public PlanRecord getPlan(long planId) {
        try (Cursor c = getReadableDatabase().query(
                "plans",
                new String[]{"id", "title", "objective", "created_at", "plan_json", "remote_client_id", "remote_synced"},
                "id=?",
                new String[]{String.valueOf(planId)},
                null,
                null,
                null,
                "1"
        )) {
            if (!c.moveToFirst()) {
                return null;
            }

            return new PlanRecord(
                    c.getLong(0),
                    c.getString(1),
                    c.getString(2),
                    c.getLong(3),
                    c.getString(4),
                    c.isNull(5) ? null : c.getString(5),
                    c.getInt(6) == 1
            );
        }
    }

    public PlanRecord getLatestPlan() {
        try (Cursor c = getReadableDatabase().query(
                "plans",
                new String[]{"id", "title", "objective", "created_at", "plan_json", "remote_client_id", "remote_synced"},
                null,
                null,
                null,
                null,
                "created_at DESC",
                "1"
        )) {
            if (!c.moveToFirst()) {
                return null;
            }

            return new PlanRecord(
                    c.getLong(0),
                    c.getString(1),
                    c.getString(2),
                    c.getLong(3),
                    c.getString(4),
                    c.isNull(5) ? null : c.getString(5),
                    c.getInt(6) == 1
            );
        }
    }

    public List<PlanRecord> listPlans() {
        List<PlanRecord> result = new ArrayList<>();

        try (Cursor c = getReadableDatabase().query(
                "plans",
                new String[]{"id", "title", "objective", "created_at", "plan_json", "remote_client_id", "remote_synced"},
                null,
                null,
                null,
                null,
                "created_at DESC"
        )) {
            while (c.moveToNext()) {
                result.add(new PlanRecord(
                        c.getLong(0),
                        c.getString(1),
                        c.getString(2),
                        c.getLong(3),
                        c.getString(4),
                        c.isNull(5) ? null : c.getString(5),
                        c.getInt(6) == 1
                ));
            }
        }

        return result;
    }

    public int countCompletedSessionsLastDays(int days) {
        if (days <= 0) {
            return 0;
        }

        long start = System.currentTimeMillis()
                - days * 24L * 60L * 60L * 1000L;

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM workout_sessions " +
                        "WHERE completed = 1 AND finished_at >= ?",
                new String[]{String.valueOf(start)}
        )) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    public void markPlanSynced(long planId) {
        ContentValues values = new ContentValues();
        values.put("remote_synced", 1);
        getWritableDatabase().update(
                "plans",
                values,
                "id=?",
                new String[]{String.valueOf(planId)}
        );
    }

    public void deletePlan(long planId) {
        getWritableDatabase().delete(
                "plans",
                "id=?",
                new String[]{String.valueOf(planId)}
        );
    }

    public void saveImage(long planId, int dayIndex, byte[] image) {
        ContentValues values = new ContentValues();
        values.put("plan_id", planId);
        values.put("day_index", dayIndex);
        values.put("image", image);

        getWritableDatabase().insertWithOnConflict(
                "plan_images",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    public byte[] getImage(long planId, int dayIndex) {
        try (Cursor c = getReadableDatabase().query(
                "plan_images",
                new String[]{"image"},
                "plan_id=? AND day_index=?",
                new String[]{
                        String.valueOf(planId),
                        String.valueOf(dayIndex)
                },
                null,
                null,
                null,
                "1"
        )) {
            return c.moveToFirst() ? c.getBlob(0) : null;
        }
    }

    public void deleteImage(long planId, int dayIndex) {
        getWritableDatabase().delete(
                "plan_images",
                "plan_id=? AND day_index=?",
                new String[]{
                        String.valueOf(planId),
                        String.valueOf(dayIndex)
                }
        );
    }

    public long startSession(
            long planId,
            int workoutIndex,
            String title,
            long now
    ) {
        ContentValues values = new ContentValues();
        values.put("plan_id", planId);
        values.put("workout_index", workoutIndex);
        values.put("title", title);
        values.put("started_at", now);
        values.put("completed", 0);

        return getWritableDatabase().insertOrThrow(
                "workout_sessions",
                null,
                values
        );
    }

    public long getOpenSession(long planId, int workoutIndex) {
        try (Cursor c = getReadableDatabase().query(
                "workout_sessions",
                new String[]{"id"},
                "plan_id=? AND workout_index=? AND completed=0",
                new String[]{
                        String.valueOf(planId),
                        String.valueOf(workoutIndex)
                },
                null,
                null,
                "started_at DESC",
                "1"
        )) {
            return c.moveToFirst() ? c.getLong(0) : -1L;
        }
    }

    public void finishSession(long sessionId, long now) {
        ContentValues values = new ContentValues();
        values.put("finished_at", now);
        values.put("completed", 1);

        getWritableDatabase().update(
                "workout_sessions",
                values,
                "id=?",
                new String[]{String.valueOf(sessionId)}
        );
    }

    public List<SessionRecord> listSessions() {
        List<SessionRecord> result = new ArrayList<>();

        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, title, started_at, finished_at, completed " +
                        "FROM workout_sessions " +
                        "ORDER BY started_at DESC LIMIT 100",
                null
        )) {
            while (c.moveToNext()) {
                Long finished = c.isNull(3) ? null : c.getLong(3);

                result.add(new SessionRecord(
                        c.getLong(0),
                        c.getString(1),
                        c.getLong(2),
                        finished,
                        c.getInt(4) == 1
                ));
            }
        }

        return result;
    }

    public List<SessionRecord> listFinishedSessions() {
        List<SessionRecord> result = new ArrayList<>();

        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, title, started_at, finished_at, completed " +
                        "FROM workout_sessions " +
                        "WHERE completed = 1 AND finished_at IS NOT NULL " +
                        "ORDER BY finished_at DESC LIMIT 100",
                null
        )) {
            while (c.moveToNext()) {
                result.add(new SessionRecord(
                        c.getLong(0),
                        c.getString(1),
                        c.getLong(2),
                        c.getLong(3),
                        true
                ));
            }
        }

        return result;
    }

    public static final class PlanRecord {
        public final long id;
        public final String title;
        public final String objective;
        public final long createdAt;
        public final String json;
        public final String remoteClientId;
        public final boolean remoteSynced;

        public PlanRecord(
                long id,
                String title,
                String objective,
                long createdAt,
                String json,
                String remoteClientId,
                boolean remoteSynced
        ) {
            this.id = id;
            this.title = title;
            this.objective = objective;
            this.createdAt = createdAt;
            this.json = json;
            this.remoteClientId = remoteClientId;
            this.remoteSynced = remoteSynced;
        }
    }

    public static final class SessionRecord {
        public final long id;
        public final String title;
        public final long startedAt;
        public final Long finishedAt;
        public final boolean completed;

        public SessionRecord(
                long id,
                String title,
                long startedAt,
                Long finishedAt,
                boolean completed
        ) {
            this.id = id;
            this.title = title;
            this.startedAt = startedAt;
            this.finishedAt = finishedAt;
            this.completed = completed;
        }
    }
}
