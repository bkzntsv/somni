/**
 * MongoDB index initialization for Somni backend.
 * Run: mongosh "<uri>" --file init-indexes.js
 * URI must include database name (e.g. .../somni).
 */

const TTL_TWO_YEARS_SECONDS = 2 * 365 * 24 * 60 * 60;

// users
db.users.createIndex({ userId: 1 }, { unique: true });
db.users.createIndex({ email: 1 }, { unique: true });
db.users.createIndex({ lastSyncTimestamp: 1 });

// sleep_logs
db.sleep_logs.createIndex({ userId: 1, startTime: -1 });
db.sleep_logs.createIndex({ babyId: 1, startTime: -1 });
db.sleep_logs.createIndex({ sessionId: 1 }, { unique: true });
db.sleep_logs.createIndex({ createdAt: 1 }, { expireAfterSeconds: TTL_TWO_YEARS_SECONDS });

// baby_profiles
db.baby_profiles.createIndex({ babyId: 1 }, { unique: true });
db.baby_profiles.createIndex({ userId: 1 });

// ai_recommendations
db.ai_recommendations.createIndex({ recommendationId: 1 }, { unique: true });
db.ai_recommendations.createIndex({ userId: 1, generatedAt: -1 });
db.ai_recommendations.createIndex({ expiresAt: 1 }, { expireAfterSeconds: 0 });

print("Somni MongoDB indexes created successfully.");
print("Database: " + db.getName());
print("Collections: " + db.getCollectionNames().join(", "));
