document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const loginButton = document.getElementById('loginButton');
    const errorMessage = document.getElementById('errorMessage');
    const errorText = document.getElementById('errorText');

    // Make setJustLoggedIn available globally (uses localStorage for sync)
    window.setJustLoggedIn = function() {
        const timestamp = Date.now();
        localStorage.setItem('justLoggedInTimestamp', timestamp.toString());
        console.log('setJustLoggedIn: flag set via localStorage, timestamp:', timestamp);
    };

    loginForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const login = document.getElementById('login').value;
        const password = document.getElementById('password').value;

        // Disable button and show loading state
        loginButton.disabled = true;
        loginButton.innerHTML = `
            <span class="loading loading-spinner"></span>
            Вход...
        `;

        try {
            console.log('login.js: Attempting login for user:', login);
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ login, password })
            });

            console.log('login.js: Response status:', response.status);
            const data = await response.json();
            console.log('login.js: Response data:', data);

            if (response.ok) {
                // Store token in localStorage
                localStorage.setItem('token', data.token);
                localStorage.setItem('userLogin', data.login);
                localStorage.setItem('userRoles', JSON.stringify(data.roles));

                console.log('login.js: Login successful. User:', data.login, 'Roles:', data.roles);
                console.log('login.js: Token stored:', data.token ? 'YES' : 'NO');
                console.log('login.js: localStorage token:', localStorage.getItem('token'));

                // Set flag to prevent immediate access checks
                window.setJustLoggedIn();

                // Redirect based on roles (following role hierarchy)
                const roles = data.roles;
                let redirectUrl = '/work-results'; // default

                if (roles.includes('ADMINISTRATOR')) {
                    redirectUrl = '/employees';
                } else if (roles.includes('ACCOUNTANT') && !roles.includes('MANAGER')) {
                    redirectUrl = '/salary';
                } else if (roles.includes('MANAGER')) {
                    redirectUrl = '/employees';
                } else if (roles.includes('MASTER') && !roles.includes('MANAGER') && !roles.includes('TECHNOLOGIST')) {
                    redirectUrl = '/orders';
                } else if (roles.includes('TECHNOLOGIST') && !roles.includes('MANAGER')) {
                    redirectUrl = '/models';
                } else if (roles.includes('EMPLOYEE')&& !roles.includes('MASTER') && !roles.includes('MANAGER') && !roles.includes('TECHNOLOGIST')) {
                    redirectUrl = '/work-results';
                }

                console.log('login.js: Redirecting to:', redirectUrl);
                // Add delay to ensure localStorage is saved
                setTimeout(() => {
                    console.log('login.js: Executing redirect to:', redirectUrl);
                    window.location.href = redirectUrl;
                }, 500);
            } else {
                // Show error message
                errorText.textContent = data.error || 'Ошибка входа';
                errorMessage.classList.remove('hidden');
                
                // Reset button
                loginButton.disabled = false;
                loginButton.innerHTML = `
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
                    </svg>
                    Войти
                `;
            }
        } catch (error) {
            console.error('Error:', error);
            errorText.textContent = 'Ошибка соединения с сервером';
            errorMessage.classList.remove('hidden');
            
            // Reset button
            loginButton.disabled = false;
            loginButton.innerHTML = `
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
                </svg>
                Войти
            `;
        }
    });

    // Hide error message when user starts typing
    document.getElementById('login').addEventListener('input', function() {
        errorMessage.classList.add('hidden');
    });
    
    document.getElementById('password').addEventListener('input', function() {
        errorMessage.classList.add('hidden');
    });
});
