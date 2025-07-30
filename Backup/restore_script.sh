#!/bin/bash

# ======== CONFIG ============
USER="postgres"
HOST="localhost"
PORT="5433"
TARGET_DB="intellab_db_restored"
BACKUP_FILE="$1"
# ============================

if [ -z "$BACKUP_FILE" ]; then
    echo "Bạn phải cung cấp đường dẫn tới file backup."
    echo "Cách dùng: ./restore_postgres.sh /backup/mydb_20250730.backup"
    exit 1
fi

# Tạo database mới
echo "Tạo database mới: $TARGET_DB ..."
createdb -U "$USER" -h "$HOST" -p "$PORT" "$TARGET_DB"
if [ $? -ne 0 ]; then
    echo "Có thể database đã tồn tại. Xóa hoặc đổi tên."
    exit 1
fi

EXTENSIONS=("uuid-ossp" "pg_trgm" "vector" "pgcrypto")


for ext in "${EXTENSIONS[@]}"; do
    psql -U "$USER" -h "$HOST" -p "$PORT" -d "$TARGET_DB" -c "CREATE EXTENSION IF NOT EXISTS \"$ext\";"
done

# Thực hiện restore
echo "Đang khôi phục từ file backup: $BACKUP_FILE ..."
pg_restore -U "$USER" -h "$HOST" -p "$PORT" -d "$TARGET_DB" -v "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    echo "Restore thành công vào database: $TARGET_DB"
else
    echo "Restore thất bại!"
fi
