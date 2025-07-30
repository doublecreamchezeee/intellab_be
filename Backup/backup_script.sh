# Cấp quyền cho script để có thể thực thi
# chmod +x /path/to/backup_postgres.sh

#!/bin/bash

# Thông tin cấu hình
USER="postgres"
HOST="localhost"
PORT="5433"
DB_NAME="intellab-db"
BACKUP_DIR="/backup"
DATE=$(date +"%Y%m%d")
BACKUP_FILE="$BACKUP_DIR/${DB_NAME}_$DATE.backup"

# Tạo thư mục backup nếu chưa có
mkdir -p "$BACKUP_DIR"

# Thực hiện backup
pg_dump -U "$USER" -h "$HOST" -p "$PORT" -F c -b -v -f "$BACKUP_FILE" "$DB_NAME"

# Kiểm tra kết quả
if [ $? -eq 0 ]; then
    echo "Backup thành công: $BACKUP_FILE"
else
    echo "Backup thất bại!"
fi


# Lập lịch chạy script hàng ngày
# Mở crontab
# crontab -e
# Thêm dòng sau vào crontab để chạy script hàng ngày lúc 2 giờ sáng
# 0 2 * * 2,5 /path/to/backup_postgres.sh >> /var/log/pg_backup.log 2>&1
# Ghi log kết quả vào file
# Lưu ý: Đảm bảo rằng người dùng chạy script có quyền truy cập vào cơ sở dữ liệu và thư mục backup
# Kiểm tra quyền truy cập


# Xoá backup cũ hơn 14 ngày
find "$BACKUP_DIR" -name "${DB_NAME}_*.backup" -type f -mtime +14 -exec rm {} \;

