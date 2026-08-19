document.addEventListener('DOMContentLoaded', function() {
    const authButton = document.getElementById('authButton');
    const userInfo = document.getElementById('userInfo');

    async function updateAuthMenu() {
        console.log('auth-menu.js: updateAuthMenu called');
        const token = localStorage.getItem('token');
        const userLogin = localStorage.getItem('userLogin');
        const userRoles = localStorage.getItem('userRoles');
        console.log('auth-menu.js: token:', token ? 'PRESENT' : 'MISSING');
        console.log('auth-menu.js: userLogin:', userLogin);
        console.log('auth-menu.js: userRoles:', userRoles);

        // Проверка валидности токена перед отображением меню
        if (token && userLogin) {
            try {
                console.log('auth-menu.js: checking token validity via /api/auth/me');
                const response = await fetch('/api/auth/me', {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                console.log('auth-menu.js: /api/auth/me response status:', response.status);
                
                if (response.status === 401 || response.status === 403) {
                    console.log('auth-menu.js: token invalid (401/403), clearing localStorage and redirecting');
                    // Токен недействителен, очищаем localStorage
                    localStorage.removeItem('token');
                    localStorage.removeItem('userLogin');
                    localStorage.removeItem('userRoles');
                    // Перенаправляем на login вместо рекурсивного вызова
                    window.location.href = '/login';
                    return;
                }
            } catch (error) {
                console.error('auth-menu.js: error checking token validity:', error);
                // При ошибке сети не очищаем данные, но логируем
            }
        }

        const currentToken = localStorage.getItem('token');
        const currentUserLogin = localStorage.getItem('userLogin');

        if (currentToken && currentUserLogin) {
            // Пользователь авторизован
            if (authButton) {
                authButton.innerHTML = `
                    <button id="logoutButton" class="btn btn-ghost">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                        </svg>
                        Выйти
                    </button>
                `;
                
                document.getElementById('logoutButton').addEventListener('click', handleLogout);
            }
            
            if (userInfo) {
                userInfo.textContent = currentUserLogin;
                userInfo.classList.remove('hidden');
            }

            // Скрывать недоступные ссылки в навигации
            hideInaccessibleNavLinks();
        } else {
            // Пользователь не авторизован
            if (authButton) {
                authButton.innerHTML = `
                    <a href="/login" class="btn btn-ghost">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
                        </svg>
                        Войти
                    </a>
                `;
            }
            
            if (userInfo) {
                userInfo.classList.add('hidden');
            }
        }
    }

    function hideInaccessibleNavLinks() {
        const userRoles = localStorage.getItem('userRoles');
        const roles = userRoles ? JSON.parse(userRoles) : [];

        // Define allowed pages for each role (must match SecurityConfig)
        const rolePageAccess = {
            'EMPLOYEE': ['/work-results'],
            'MASTER': ['/work-results', '/orders'],
            'TECHNOLOGIST': ['/work-results', '/orders', '/models'],
            'MANAGER': ['/work-results', '/orders', '/models', '/employees', '/salary'],
            'ACCOUNTANT': ['/salary'],
            'ADMINISTRATOR': ['/work-results', '/orders', '/models', '/employees', '/salary']
        };

        // Collect all accessible pages for user's roles
        const accessiblePages = new Set();
        for (const role of roles) {
            if (rolePageAccess[role]) {
                rolePageAccess[role].forEach(page => accessiblePages.add(page));
            }
        }

        // Show nav links for accessible pages, hide others
        const navLinks = document.querySelectorAll('a[data-path]');
        navLinks.forEach(link => {
            const linkPath = link.getAttribute('data-path');
            if (linkPath && accessiblePages.has(linkPath)) {
                link.parentElement.style.display = 'block';
            } else {
                link.parentElement.style.display = 'none';
            }
        });

        // Show the menu containers after processing
        const menuContainers = document.querySelectorAll('.menu');
        menuContainers.forEach(menu => {
            menu.style.display = '';
        });
    }

    async function handleLogout() {
        try {
            await fetch('/api/auth/logout', {
                method: 'POST'
            });
        } catch (error) {
            console.error('Error during logout:', error);
        }
        
        // Очищаем localStorage независимо от результата запроса
        localStorage.removeItem('token');
        localStorage.removeItem('userLogin');
        localStorage.removeItem('userRoles');
        
        // Перенаправляем на страницу входа
        window.location.href = '/login';
    }

    // Инициализация меню при загрузке
    updateAuthMenu();

    // Подсветка активной страницы в navbar
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('a[data-path]');
    navLinks.forEach(link => {
        const linkPath = link.getAttribute('data-path');
        if (currentPath === linkPath) {
            link.parentElement.classList.add('active');
            link.classList.remove('font-semibold');
            link.classList.add('font-bold', 'text-primary');
        }
    });

    // Обновление меню при изменении localStorage (для синхронизации между вкладками)
    window.addEventListener('storage', function(e) {
        if (e.key === 'token' || e.key === 'userLogin') {
            updateAuthMenu();
        }
    });
});
