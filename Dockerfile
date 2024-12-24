FROM postgres:latest

# Sao chép file insert.sql vào thư mục init script của PostgreSQL
COPY insert.sql /docker-entrypoint-initdb.d/

# Thiết lập quyền truy cập cho file
RUN chmod 755 /docker-entrypoint-initdb.d/insert.sql

# Sao chép file function.sql vào thư mục init script của PostgreSQL
COPY function.sql /docker-entrypoint-initdb.d/

RUN chmod 755 /docker-entrypoint-initdb.d/function.sql