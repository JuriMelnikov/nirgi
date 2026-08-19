document.addEventListener('DOMContentLoaded', init);

function init() {
    const btn4 = document.querySelector('#btn4');
    // Проверяем, существует ли кнопка на странице, чтобы избежать ошибок
    if (btn4) {
        btn4.addEventListener('click', btn4Click);
    }
}

async function btn4Click() {
    const data = {
        Week: '23',
        Year: '2012'
    };

    // Анимация через Web Animations API (вместо $.animate)
    const proba = document.querySelector('#proba');
    if (proba) {
        proba.animate(
            [
                { marginTop: proba.style.marginTop || '0px', marginLeft: proba.style.marginLeft || '0px' },
                { marginTop: '300px', marginLeft: '400px' }
            ], 
            {
                duration: 1000,
                fill: 'forwards', // Сохраняет конечную точку анимации
                easing: 'ease'
            }
        );
    }

    // Подготовка данных. Формат x-www-form-urlencoded, как было в оригинале ("value=...")
    const urlParams = new URLSearchParams();
    urlParams.append('value', JSON.stringify(data));

    try {
        // Отправка POST запроса через fetch с уникальным timestamp в URL против кэширования
        const response = await fetch(`index.php?stamp=${Date.now()}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: urlParams
        });

        // Проверяем статус ответа сервера (200-299)
        if (!response.ok) {
            throw new Error(`Ошибка HTTP: ${response.status}`);
        }

        // Автоматически парсим JSON ответ (evalJSON больше не нужен, он устарел)
        const resp = await response.json();
        
        alert(`code=success response=[object Object]`);
        alert(`Данные отправлены! Сервер вернул ответ: Week=${resp.Week}`);

    } catch (error) {
        alert('Error!');
        console.error('Детали ошибки:', error);
    }
}
