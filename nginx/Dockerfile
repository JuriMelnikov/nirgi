FROM nginx:alpine

# Устанавливаем certbot и необходимые зависимости
RUN apk add --no-cache certbot certbot-nginx py3-pip openssl

# Создаем директории для сертификатов
RUN mkdir -p /etc/letsencrypt/live /etc/letsencrypt/archive /var/www/certbot /etc/nginx

# Копируем сертификаты
COPY cert.crt /etc/nginx/cert.crt
COPY cert.key /etc/nginx/cert.key

# Копируем конфигурацию nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Копируем скрипт для обновления сертификатов
COPY renew-certificates.sh /etc/periodic/daily/renew-certificates.sh
RUN chmod +x /etc/periodic/daily/renew-certificates.sh

# Открываем порты
EXPOSE 80 443

# Запускаем nginx в foreground
CMD ["nginx", "-g", "daemon off;"]
