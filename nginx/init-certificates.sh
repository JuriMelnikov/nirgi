#!/bin/sh

# Скрипт для первоначального получения SSL сертификатов Let's Encrypt

# Проверяем, существуют ли сертификаты
if [ -f /etc/letsencrypt/live/jvm.ee/fullchain.pem ]; then
    echo "Сертификаты уже существуют. Пропускаем получение."
    exit 0
fi

# Получаем сертификаты
certbot certonly --webroot \
    --webroot-path /var/www/certbot \
    --email ${LETSENCRYPT_EMAIL} \
    --agree-tos \
    --no-eff-email \
    -d jvm.ee
