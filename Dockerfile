FROM postgres:16.2
#latest

# Sao chép file insert.sql vào thư mục init script của PostgreSQL
COPY insert.sql /docker-entrypoint-initdb.d/

# Thiết lập quyền truy cập cho file
RUN apt-get update && apt-get install -y postgresql-16-pgvector
RUN chmod 755 /docker-entrypoint-initdb.d/insert.sql

## Sao chép file function.sql vào thư mục init script của PostgreSQL
#COPY function.sql /docker-entrypoint-initdb.d/
#
#RUN chmod 755 /docker-entrypoint-initdb.d/function.sql