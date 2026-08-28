#!/bin/bash

# Переменные (ТОКЕН ТЕПЕРЬ НЕ ХРАНИТСЯ В КОДЕ)
CORRECT_ORIGIN="https://github.com/JuriMelnikov/nirgi.git"

# 1. Запрос токена, если он не был передан в окружении
if [ -z "$NIRGI_TOKEN" ]; then
    echo -n "Введите ваш GitHub personal access token: "
    read -s NIRGI_TOKEN
    echo ""
fi

if [ -z "$NIRGI_TOKEN" ]; then
    echo "Ошибка: Токен не может быть пустым!"
    exit 1
fi

REMOTE_WITH_TOKEN="https://JuriMelnikov:${NIRGI_TOKEN}@github.com/JuriMelnikov/nirgi.git"

# 2. Запрос комментария для коммита
echo -n "Введите комментарий к коммиту: "
read commit_message

if [ -z "$commit_message" ]; then
    echo "Ошибка: Комментарий не может быть пустым!"
    exit 1
fi

# 3. Добавление файлов и коммит
git add .
git commit -m "$commit_message"

# Временно подменяем URL для origin
git remote set-url origin "$REMOTE_WITH_TOKEN"

# Выполняем push
git push origin main
PUSH_STATUS=$?

# Сразу возвращаем ПРАВИЛЬНЫЙ чистый URL назад
git remote set-url origin "$CORRECT_ORIGIN"

if [ $PUSH_STATUS -ne 0 ]; then
    echo "Ошибка при отправке кода на GitHub. Скрипт остановлен."
    exit 1
fi

# 4. Подключение по SSH и выполнение git pull origin main на сервере
echo "Подключение к серверу jvm.ee..."
osascript -e '
tell application "Terminal"
    do script "ssh -o StrictHostKeyChecking=no root@jvm.ee \"cd /srv/nirgi-java && git remote set-url origin '"$REMOTE_WITH_TOKEN"' && git pull origin main && git remote set-url origin '"$CORRECT_ORIGIN"' && exit\""
    delay 2
    do script "'"$JVM_EE_PASS"'"  in front window
end tell'

echo "Скрипт завершил работу локально, обновление на сервере запущено!"

