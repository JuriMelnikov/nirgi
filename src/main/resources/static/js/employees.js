document.addEventListener('DOMContentLoaded', function() {
    if (!requireAuth()) {
        return;
    }

    const form = document.getElementById('addEmployeeForm');
    const stopDayCard = document.getElementById('stopDayCard');
    const stopDayInput = document.getElementById('stopDayInput');
    const updateStopDayBtn = document.getElementById('updateStopDayBtn');
    const stopDaySuccess = document.getElementById('stopDaySuccess');
    const stopDayError = document.getElementById('stopDayError');
    const stopDayTitle = document.getElementById('stopDayTitle');
    const stopDayContent = document.getElementById('stopDayContent');
    const stopDayToggleIcon = document.getElementById('stopDayToggleIcon');

    // Load stop day settings and show card for managers
    loadStopDaySettings();

    // Toggle stop day form visibility on title click
    stopDayTitle.addEventListener('click', function() {
        stopDayContent.classList.toggle('hidden');
        stopDayToggleIcon.classList.toggle('rotate-180');
    });

    async function loadStopDaySettings() {
        try {
            const response = await authFetch('/api/settings');
            if (response.ok) {
                const settings = await response.json();
                stopDayInput.value = settings.stopDay;
                
                // Show card only for managers
                const userRoles = await getUserRoles();
                if (userRoles.includes('MANAGER') || userRoles.includes('ADMINISTRATOR')) {
                    stopDayCard.classList.remove('hidden');
                }
            }
        } catch (error) {
            console.error('Error loading stop day settings:', error);
        }
    }

    async function getUserRoles() {
        try {
            const response = await authFetch('/api/auth/me');
            if (response.ok) {
                const user = await response.json();
                return user.roles || [];
            }
        } catch (error) {
            console.error('Error getting user roles:', error);
        }
        return [];
    }

    updateStopDayBtn.addEventListener('click', async function() {
        const stopDay = parseInt(stopDayInput.value);
        
        if (isNaN(stopDay) || stopDay < 1 || stopDay > 31) {
            stopDayError.textContent = 'День должен быть от 1 до 31';
            stopDayError.classList.remove('hidden');
            stopDaySuccess.classList.add('hidden');
            return;
        }

        try {
            const response = await authFetch('/api/settings/stop-day', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ stopDay })
            });

            if (response.ok) {
                stopDaySuccess.classList.remove('hidden');
                stopDayError.classList.add('hidden');
                setTimeout(() => {
                    stopDaySuccess.classList.add('hidden');
                    stopDayContent.classList.add('hidden');
                    stopDayToggleIcon.classList.remove('rotate-180');
                }, 3000);
            } else {
                const errorText = await response.text();
                stopDayError.textContent = errorText || 'Ошибка при сохранении настройки';
                stopDayError.classList.remove('hidden');
                stopDaySuccess.classList.add('hidden');
            }
        } catch (error) {
            stopDayError.textContent = 'Ошибка соединения с сервером';
            stopDayError.classList.remove('hidden');
            stopDaySuccess.classList.add('hidden');
        }
    });

    const employeeSelect = document.getElementById('employeeSelect');
    const employeeDetails = document.getElementById('employeeDetails');
    const submitButton = document.getElementById('submitButton');
    const submitButtonText = document.getElementById('submitButtonText');
    const cancelButton = document.getElementById('cancelButton');
    const deleteButton = document.getElementById('deleteButton');
    const formTitle = document.getElementById('formTitle');
    const formIcon = document.getElementById('formIcon');
    const nameInput = document.getElementById('name');
    const surnameInput = document.getElementById('surname');
    const loginInput = document.getElementById('login');
    const passwordInput = document.getElementById('password');
    const togglePasswordBtn = document.getElementById('togglePassword');
    const eyeIcon = document.getElementById('eyeIcon');
    const eyeOffIcon = document.getElementById('eyeOffIcon');
    let editingEmployeeId = null;

    // Toggle password visibility
    togglePasswordBtn.addEventListener('click', function() {
        if (passwordInput.type === 'password') {
            passwordInput.type = 'text';
            eyeIcon.classList.add('hidden');
            eyeOffIcon.classList.remove('hidden');
        } else {
            passwordInput.type = 'password';
            eyeIcon.classList.remove('hidden');
            eyeOffIcon.classList.add('hidden');
        }
    });

    // Load existing employees on page load
    loadEmployees();

    // Auto-generate login when name or surname changes (only when creating new employee and login is empty)
    function autoGenerateLogin() {
        // Don't auto-generate when editing existing employee
        if (editingEmployeeId) {
            return;
        }
        // Don't auto-generate if login already has a value
        if (loginInput.value.trim() !== '') {
            return;
        }
        const name = nameInput.value.trim();
        const surname = surnameInput.value.trim();
        if (name && surname) {
            loginInput.value = name.toLowerCase() + '.' + surname.toLowerCase();
        }
    }

    nameInput.addEventListener('input', autoGenerateLogin);
    surnameInput.addEventListener('input', autoGenerateLogin);

    // Active only checkbox change handler
    const activeOnlyCheckbox = document.getElementById('activeOnly');
    activeOnlyCheckbox.addEventListener('change', function() {
        loadEmployees();
    });

    // Cancel button click handler
    cancelButton.addEventListener('click', function() {
        resetEditMode();
        form.reset();
    });

    // Delete button click handler
    deleteButton.addEventListener('click', async function() {
        if (editingEmployeeId) {
            if (confirm('Вы уверены, что хотите деактивировать этого работника?')) {
                try {
                    const response = await authFetch(`/api/employees/${editingEmployeeId}`, {
                        method: 'DELETE'
                    });
                    if (response.ok) {
                        alert('Работник успешно деактивирован');
                        resetEditMode();
                        form.reset();
                        loadEmployees();
                    } else {
                        alert('Ошибка при деактивации работника');
                    }
                } catch (error) {
                    // console.error('Error:', error);
                    alert('Ошибка соединения с сервером');
                }
            }
        }
    });

    form.addEventListener('submit', async function(e) {
        e.preventDefault();

        const employeeData = {
            name: document.getElementById('name').value,
            surname: document.getElementById('surname').value,
            login: document.getElementById('login').value,
            day: parseInt(document.getElementById('dey').value),
            month: parseInt(document.getElementById('month').value),
            year: parseInt(document.getElementById('year').value),
            phone: document.getElementById('phone').value,
            active: document.getElementById('active').checked,
            city: document.getElementById('city').value,
            street: document.getElementById('street').value,
            house: document.getElementById('house').value,
            room: document.getElementById('room').value || '',
            roles: getSelectedRoles()
        };

        // Only include password if it's not empty
        const passwordValue = document.getElementById('password').value;
        if (passwordValue && passwordValue.trim() !== '') {
            employeeData.password = passwordValue;
        }

        // Password is required when creating a new employee
        if (!editingEmployeeId && (!passwordValue || passwordValue.trim() === '')) {
            alert('Пароль обязателен при создании работника');
            return;
        }

        // Check if phone already exists (only when creating new employee)
        if (!editingEmployeeId) {
            const phone = employeeData.phone;
            const employees = await authFetch('/api/employees').then(r => r.json());
            const phoneExists = employees.some(employee => employee.phone === phone);
            if (phoneExists) {
                alert('Работник с таким номером телефона уже существует');
                return;
            }
        }

        // console.log('Sending employee data:', employeeData);

        try {
            let response;
            if (editingEmployeeId) {
                // Update existing employee
                response = await authFetch(`/api/employees/${editingEmployeeId}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(employeeData)
                });
            } else {
                // Add new employee
                response = await authFetch('/api/employees', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(employeeData)
                });
            }

            // console.log('Response status:', response.status);

            if (response.ok) {
                const result = await response.json();
                if (editingEmployeeId) {
                    alert('Работник успешно обновлен! ID: ' + result.id);
                } else {
                    alert('Работник успешно добавлен! ID: ' + result.id);
                }
                form.reset();
                resetEditMode();
                loadEmployees(); // Reload employees list after adding/updating employee
            } else {
                const errorText = await response.text();
                // console.error('Error response:', errorText);
                if (response.status === 400 && !errorText) {
                    alert('Ошибка: Пользователь с таким логином уже существует');
                } else {
                    try {
                        const errorJson = JSON.parse(errorText);
                        alert('Ошибка: ' + (errorJson.message || errorJson.error || errorText));
                    } catch {
                        alert('Ошибка: ' + errorText);
                    }
                }
            }
        } catch (error) {
            // console.error('Error:', error);
            alert('Ошибка соединения с сервером');
        }
    });

    // Show employee details when selected and populate form
    employeeSelect.addEventListener('change', async function() {
        const employeeId = this.value;
        if (!employeeId) {
            employeeDetails.innerHTML = '';
            resetEditMode();
            return;
        }

        try {
            const response = await authFetch(`/api/employees/${employeeId}`);
            if (response.ok) {
                const employee = await response.json();
                populateForm(employee);
                displayEmployeeDetails(employee);
                setEditMode(employeeId);
            } else {
                employeeDetails.innerHTML = '<p style="color: red;">Ошибка загрузки данных работника</p>';
            }
        } catch (error) {
            // console.error('Error:', error);
            employeeDetails.innerHTML = '<p style="color: red;">Ошибка соединения с сервером</p>';
        }
    });

    function populateForm(employee) {
        document.getElementById('name').value = employee.name || '';
        document.getElementById('surname').value = employee.surname || '';
        document.getElementById('login').value = employee.login || '';
        document.getElementById('dey').value = employee.day || '';
        document.getElementById('month').value = employee.month || '';
        document.getElementById('year').value = employee.year || '';
        document.getElementById('phone').value = employee.phone || '';
        document.getElementById('password').value = ''; // Don't populate password for security
        document.getElementById('active').checked = employee.active !== false; // Default to true if undefined
        document.getElementById('city').value = employee.city || '';
        document.getElementById('street').value = employee.street || '';
        document.getElementById('house').value = employee.house || '';
        document.getElementById('room').value = employee.room || '';
        
        // Reset all role radios
        document.querySelectorAll('input[name="roles"]').forEach(radio => {
            radio.checked = false;
        });
        
        // Set role radio if employee has roles
        if (employee.roles && Array.isArray(employee.roles) && employee.roles.length > 0) {
            // Determine primary role based on hierarchy
            const roles = employee.roles;
            let primaryRole = 'EMPLOYEE';
            
            if (roles.includes('ADMINISTRATOR')) {
                primaryRole = 'ADMINISTRATOR';
            } else if (roles.includes('MANAGER')) {
                primaryRole = 'MANAGER';
            } else if (roles.includes('TECHNOLOGIST')) {
                primaryRole = 'TECHNOLOGIST';
            } else if (roles.includes('MASTER')) {
                primaryRole = 'MASTER';
            } else if (roles.includes('ACCOUNTANT')) {
                primaryRole = 'ACCOUNTANT';
            } else if (roles.includes('EMPLOYEE')) {
                primaryRole = 'EMPLOYEE';
            }
            
            const radio = document.querySelector(`input[name="roles"][value="${primaryRole}"]`);
            if (radio) {
                radio.checked = true;
            }
        }
    }

    function setEditMode(employeeId) {
        editingEmployeeId = employeeId;
        submitButtonText.textContent = 'Изменить';
        submitButton.classList.remove('btn-primary');
        submitButton.classList.add('btn-warning');
        cancelButton.classList.remove('hidden');
        deleteButton.classList.remove('hidden');
        formTitle.textContent = 'Изменение данных выбранного работника';
        formIcon.innerHTML = '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />';
        
        // Login field is now editable when editing
        loginInput.readOnly = false;
    }

    function resetEditMode() {
        editingEmployeeId = null;
        submitButtonText.textContent = 'Добавить работника';
        submitButton.classList.remove('btn-warning');
        submitButton.classList.add('btn-primary');
        cancelButton.classList.add('hidden');
        deleteButton.classList.add('hidden');
        employeeSelect.value = '';
        employeeDetails.innerHTML = '';
        formTitle.textContent = 'Добавление работника';
        formIcon.innerHTML = '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />';
        
        // Reset all role radios
        document.querySelectorAll('input[name="roles"]').forEach(radio => {
            radio.checked = false;
        });
        
        // Reset login field
        loginInput.readOnly = false;
        loginInput.value = '';
        autoGenerateLogin();
    }

    function getSelectedRoles() {
        const selectedRadio = document.querySelector('input[name="roles"]:checked');
        return selectedRadio ? [selectedRadio.value] : ['EMPLOYEE'];
    }

    function calculateAge(day, month, year) {
        const today = new Date();
        const birthDate = new Date(year, month - 1, day);
        let age = today.getFullYear() - birthDate.getFullYear();
        const monthDiff = today.getMonth() - birthDate.getMonth();
        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
            age--;
        }
        return age;
    }

    function getAgeWord(age) {
        const lastDigit = age % 10;
        const lastTwoDigits = age % 100;
        
        if (lastTwoDigits >= 11 && lastTwoDigits <= 14) {
            return 'лет';
        }
        
        if (lastDigit === 1) {
            return 'год';
        } else if (lastDigit >= 2 && lastDigit <= 4) {
            return 'года';
        } else {
            return 'лет';
        }
    }

    async function loadEmployees() {
        console.log('Loading employees...');
        const activeOnlyCheckbox = document.getElementById('activeOnly');
        const activeOnly = activeOnlyCheckbox ? activeOnlyCheckbox.checked : false;
        console.log('Active only:', activeOnly);
        try {
            const url = activeOnly ? '/api/employees?activeOnly=true' : '/api/employees';
            console.log('Fetching URL:', url);
            const response = await authFetch(url);
            console.log('Response status:', response.status);
            console.log('Response ok:', response.ok);
            if (response.ok) {
                const employees = await response.json();
                console.log('Employees loaded:', employees);
                populateEmployeeSelect(employees);
            } else {
                console.error('Failed to load employees:', response.status);
                const errorText = await response.text();
                console.error('Error text:', errorText);
                employeeSelect.innerHTML = '<option value="">-- Ошибка загрузки --</option>';
            }
        } catch (error) {
            console.error('Error loading employees:', error);
            employeeSelect.innerHTML = '<option value="">-- Ошибка соединения --</option>';
        }
    }

    function populateEmployeeSelect(employees) {
        employeeSelect.innerHTML = '<option value="">-- Выберите работника --</option>';
        employees.forEach(employee => {
            const option = document.createElement('option');
            option.value = employee.id;
            const statusText = employee.active === false ? ' (неактивен)' : '';
            option.textContent = `${employee.surname} ${employee.name} (${employee.phone})${statusText}`;
            if (employee.active === false) {
                option.style.color = 'red';
            }
            employeeSelect.appendChild(option);
        });
    }

    function displayEmployeeDetails(employee) {
        // Determine primary role based on hierarchy
        let primaryRole = 'EMPLOYEE';
        if (employee.roles && Array.isArray(employee.roles) && employee.roles.length > 0) {
            const roles = employee.roles;
            
            if (roles.includes('ADMINISTRATOR')) {
                primaryRole = 'ADMINISTRATOR';
            } else if (roles.includes('MANAGER')) {
                primaryRole = 'MANAGER';
            } else if (roles.includes('TECHNOLOGIST')) {
                primaryRole = 'TECHNOLOGIST';
            } else if (roles.includes('MASTER')) {
                primaryRole = 'MASTER';
            } else if (roles.includes('ACCOUNTANT')) {
                primaryRole = 'ACCOUNTANT';
            } else if (roles.includes('EMPLOYEE')) {
                primaryRole = 'EMPLOYEE';
            }
        }
        
        employeeDetails.innerHTML = `
            <div style="margin-top: 20px; padding: 15px; border: 1px solid #ccc; border-radius: 5px;">
                <h3>Детали работника</h3>
                <p><strong>ID:</strong> ${employee.id}</p>
                <p><strong>Имя:</strong> ${employee.name}</p>
                <p><strong>Фамилия:</strong> ${employee.surname}</p>
                <p><strong>Дата рождения:</strong> ${employee.day}.${employee.month}.${employee.year} (${calculateAge(employee.day, employee.month, employee.year)} ${getAgeWord(calculateAge(employee.day, employee.month, employee.year))})</p>
                <p><strong>Телефон:</strong> ${employee.phone}</p>
                <p><strong>Роль:</strong> ${primaryRole}</p>
                <p><strong>Адрес:</strong> ${employee.city}, ${employee.street}, д. ${employee.house}${employee.room ? ', кв. ' + employee.room : ''}</p>
            </div>
        `;
    }
});
