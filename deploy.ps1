# 1. Запрос комментария для коммита у пользователя
$commitMessage = Read-Host "Введите комментарий для коммита"

# Проверка: если комментарий пустой, останавливаем скрипт
if ([string]::IsNullOrWhiteSpace($commitMessage)) {
    Write-Error "Комментарий не может быть пустым. Скрипт остановлен."
    exit
}

# 2. Локальные операции Git
Write-Host "Добавление файлов..." -ForegroundColor Cyan
git add .

Write-Host "Создание коммита..." -ForegroundColor Cyan
git commit -m "$commitMessage"

Write-Host "Отправка изменений на удаленный репозиторий..." -ForegroundColor Cyan
git push origin main

# 3. Подключение по SSH и выполнение команд на сервере
#Write-Host "Подключение к серверу и обновление кода..." -ForegroundColor Cyan

# Используем переменную окружения GIT_MERGE_AUTO_EDIT для автоматического сохранения merge-коммита без открытия nano
#ssh root@debian-server "cd /srv/nirgi-java && export GIT_MERGE_AUTO_EDIT=no && git pull origin main"

#Write-Host "Деплой успешно завершен!" -ForegroundColor Green
