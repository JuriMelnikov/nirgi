// Work results management functionality

// Global variables
let currentYear = new Date().getFullYear();
let currentMonth = new Date().getMonth(); // 0-11
let allEmployees = [];
let allOrders = [];
let allModelLists = [];
let allSections = [];
let allTechmaps = [];
let selectedWorkResults = [];
let selectedOrder = null;

// Money is displayed in euros with three decimals
function formatEuros(amount) {
    const euros = Number(amount);
    return isNaN(euros) ? '0.000' : euros.toFixed(3);
}

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    initializeYearSelect();
    initializeWeekSelect();
    initializeCalendar();
    initializeEmployeeSelector();
    setDefaults();
    setupEventListeners();
    loadOrdersForWeek();
    loadWorkResults();
});

// Initialize year select
function initializeYearSelect() {
    const yearSelect = document.getElementById('yearSelect');
    
    for (let year = currentYear - 2; year <= currentYear + 2; year++) {
        const option = document.createElement('option');
        option.value = year;
        option.textContent = year;
        yearSelect.appendChild(option);
    }
    
    yearSelect.value = currentYear;
}

// Initialize week select
function initializeWeekSelect() {
    const weekSelect = document.getElementById('weekSelect');
    
    for (let week = 1; week <= 53; week++) {
        const option = document.createElement('option');
        option.value = week;
        option.textContent = week;
        weekSelect.appendChild(option);
    }
}

// Set defaults to current date
function setDefaults() {
    const now = new Date();
    const currentYear = now.getFullYear();
    const currentMonth = now.getMonth() + 1; // 1-12
    const currentWeek = getWeekNumber(now);
    
    document.getElementById('yearSelect').value = currentYear;
    document.getElementById('monthSelect').value = currentMonth;
    document.getElementById('weekSelect').value = currentWeek;
}

// Initialize calendar
function initializeCalendar() {
    renderCalendar(currentYear, currentMonth);
    
    document.getElementById('prevMonth').addEventListener('click', function() {
        currentMonth--;
        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        }
        renderCalendar(currentYear, currentMonth);
    });
    
    document.getElementById('nextMonth').addEventListener('click', function() {
        currentMonth++;
        if (currentMonth > 11) {
            currentMonth = 0;
            currentYear++;
        }
        renderCalendar(currentYear, currentMonth);
    });
}

// Render calendar with week numbers
function renderCalendar(year, month) {
    const calendarTitle = document.getElementById('calendarTitle');
    const calendarDays = document.getElementById('calendarDays');
    
    const monthNames = ['Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь', 
                        'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'];
    calendarTitle.textContent = `${monthNames[month]} ${year}`;
    
    calendarDays.innerHTML = '';
    
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const startingDay = firstDay.getDay();
    const totalDays = lastDay.getDate();
    
    let adjustedStartingDay = startingDay === 0 ? 6 : startingDay - 1;
    
    const selectedWeek = parseInt(document.getElementById('weekSelect').value);
    
    // Get current date for highlighting
    const now = new Date();
    const currentDay = now.getDate();
    const currentMonth = now.getMonth();
    const currentYear = now.getFullYear();
    
    const totalCells = adjustedStartingDay + totalDays;
    const totalRows = Math.ceil(totalCells / 7);
    
    for (let row = 0; row < totalRows; row++) {
        const mondayDay = row * 7 - adjustedStartingDay + 1;
        let weekNumber;
        
        if (mondayDay <= 0) {
            weekNumber = getWeekNumber(firstDay);
        } else if (mondayDay > totalDays) {
            weekNumber = getWeekNumber(lastDay);
        } else {
            weekNumber = getWeekNumber(new Date(year, month, mondayDay));
        }
        
        const weekCell = document.createElement('div');
        weekCell.className = 'p-1 text-center font-bold text-xs bg-base-300 rounded';
        weekCell.textContent = weekNumber;
        
        if (selectedWeek && weekNumber === selectedWeek) {
            weekCell.classList.add('bg-primary', 'text-primary-content');
        }
        
        weekCell.addEventListener('click', function() {
            document.getElementById('weekSelect').value = weekNumber;
            document.getElementById('monthSelect').value = month + 1;
            renderCalendar(year, month);
            loadOrdersForWeek();
        });
        
        calendarDays.appendChild(weekCell);
        
        for (let col = 0; col < 7; col++) {
            const dayNum = row * 7 + col - adjustedStartingDay + 1;
            
            if (dayNum > 0 && dayNum <= totalDays) {
                const dayCell = document.createElement('div');
                dayCell.className = 'p-1 text-center cursor-pointer hover:bg-base-300 rounded text-xs';
                dayCell.textContent = dayNum;
                
                // Highlight current date
                if (dayNum === currentDay && month === currentMonth && year === currentYear) {
                    dayCell.classList.add('ring-2', 'ring-accent', 'ring-offset-1', 'font-bold');
                }
                
                if (col === 5 || col === 6) {
                    dayCell.classList.add('bg-red-100', 'text-red-800');
                }
                
                if (selectedWeek && weekNumber === selectedWeek) {
                    dayCell.classList.remove('bg-red-100', 'text-red-800');
                    dayCell.classList.add('bg-primary', 'text-primary-content');
                }
                
                dayCell.addEventListener('click', function() {
                    document.getElementById('weekSelect').value = weekNumber;
                    document.getElementById('monthSelect').value = month + 1;
                    renderCalendar(year, month);
                    loadOrdersForWeek();
                });
                
                calendarDays.appendChild(dayCell);
            } else {
                const emptyCell = document.createElement('div');
                emptyCell.className = 'p-1';
                calendarDays.appendChild(emptyCell);
            }
        }
    }
}

// Get ISO week number
function getWeekNumber(date) {
    const d = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
    const dayNum = d.getUTCDay() || 7;
    d.setUTCDate(d.getUTCDate() + 4 - dayNum);
    const yearStart = new Date(Date.UTC(d.getUTCFullYear(), 0, 1));
    const weekNo = Math.ceil((((d - yearStart) / 86400000) + 1) / 7);
    return weekNo;
}

// Initialize employee selector based on user roles
async function initializeEmployeeSelector() {
    const userRoles = JSON.parse(localStorage.getItem('userRoles') || '[]');
    
    // Check if user has only EMPLOYEE role
    const isEmployeeOnly = userRoles.length === 1 && userRoles.includes('EMPLOYEE');
    
    if (isEmployeeOnly) {
        // Show label with current employee's name
        try {
            const token = localStorage.getItem('token');
            
            // Get current employee info from /api/employees (EMPLOYEE now has access)
            const employeesResponse = await fetch('/api/employees', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (employeesResponse.ok) {
                const employees = await employeesResponse.json();
                
                // For EMPLOYEE, the endpoint returns only the current employee
                if (employees && employees.length > 0) {
                    const employee = employees[0];
                    const employeeLabel = document.getElementById('employeeLabel');
                    const employeeSelect = document.getElementById('employeeSelect');
                    const employeeIdInput = document.getElementById('employeeIdInput');
                    
                    employeeLabel.textContent = `${employee.name} ${employee.surname}`;
                    employeeLabel.classList.remove('hidden');
                    employeeIdInput.value = employee.id;
                    employeeSelect.classList.add('hidden');
                    
                    // Load work results for the current employee
                    loadWorkResults();
                }
            }
        } catch (error) {
            console.error('Error loading current employee:', error);
        }
    } else {
        // Show select with all employees (for ADMIN, MANAGER, MASTER, TECHNOLOGIST, etc.)
        console.log('Loading employees for user with roles:', userRoles);
        loadEmployees();
    }
}

// Get current employee ID from either select or hidden input
function getCurrentEmployeeId() {
    const employeeSelect = document.getElementById('employeeSelect');
    const employeeIdInput = document.getElementById('employeeIdInput');
    
    if (!employeeSelect.classList.contains('hidden') && employeeSelect.value) {
        return parseInt(employeeSelect.value);
    } else if (employeeIdInput.value) {
        return parseInt(employeeIdInput.value);
    }
    return null;
}

// Load employees
async function loadEmployees() {
    try {
        const token = localStorage.getItem('token');
        console.log('Loading employees...');
        const response = await fetch('/api/employees', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        console.log('Employees response status:', response.status);
        
        if (response.ok) {
            allEmployees = await response.json();
            console.log('Employees loaded:', allEmployees);
            
            const employeeSelect = document.getElementById('employeeSelect');
            employeeSelect.innerHTML = '<option value="">-- Выберите работника --</option>';
            employeeSelect.classList.remove('hidden');
            
            allEmployees.forEach(employee => {
                const option = document.createElement('option');
                option.value = employee.id;
                option.textContent = `${employee.name} ${employee.surname}`;
                employeeSelect.appendChild(option);
            });
            
            console.log('Employee select options count:', employeeSelect.options.length);
        } else {
            console.error('Failed to load employees:', response.status, response.statusText);
        }
    } catch (error) {
        console.error('Error loading employees:', error);
    }
}

// Load orders for selected week
async function loadOrdersForWeek() {
    const year = parseInt(document.getElementById('yearSelect').value);
    const month = parseInt(document.getElementById('monthSelect').value);
    const week = parseInt(document.getElementById('weekSelect').value);

    if (!year || !month || !week) {
        return;
    }

    console.log('Loading orders for week:', { year, month, week });

    try {
        const token = localStorage.getItem('token');
        let url = `/api/orders/search-with-transferred?year=${year}&month=${month}&week=${week}`;

        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            allOrders = await response.json();
            console.log('Orders loaded:', allOrders);

            const orderSelect = document.getElementById('orderSelect');
            orderSelect.innerHTML = '<option value="">-- Выберите заказ --</option>';
            orderSelect.disabled = false;

            allOrders.forEach(order => {
                const option = document.createElement('option');
                option.value = order.id;
                option.textContent = order.name;
                option.dataset.isTransferred = order.isTransferred;
                option.dataset.originalOrderId = order.originalOrderId || '';
                orderSelect.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Error loading orders:', error);
    }
}

// Load models for selected order
async function loadModelsForOrder() {
    const orderId = parseInt(document.getElementById('orderSelect').value);
    const orderSelect = document.getElementById('orderSelect');
    const selectedOption = orderSelect.selectedOptions[0];

    if (!orderId || !selectedOption) {
        return;
    }

    const isTransferred = selectedOption.dataset.isTransferred === 'true';
    const originalOrderId = selectedOption.dataset.originalOrderId ? parseInt(selectedOption.dataset.originalOrderId) : null;

    selectedOrder = allOrders.find(o => o.id === orderId);

    try {
        const modelSelect = document.getElementById('modelSelect');
        modelSelect.innerHTML = '<option value="">-- Выберите модель --</option>';
        modelSelect.disabled = false;

        if (selectedOrder.models && selectedOrder.models.length > 0) {
            selectedOrder.models.forEach(model => {
                const option = document.createElement('option');
                option.value = model.modelListId;
                option.textContent = model.modelListName;
                option.dataset.remainingCount = model.remainingCount || model.count;
                modelSelect.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Error loading models:', error);
    }
}

// Load sections for selected model
async function loadSectionsForModel() {
    const modelListId = parseInt(document.getElementById('modelSelect').value);
    
    if (!modelListId) {
        return;
    }
    
    try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/section-lists', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            allSections = await response.json();
            
            const sectionSelect = document.getElementById('sectionSelect');
            sectionSelect.innerHTML = '<option value="">-- Выберите раздел --</option>';
            sectionSelect.disabled = false;
            
            allSections.forEach(section => {
                const option = document.createElement('option');
                option.value = section.id;
                option.textContent = section.name;
                sectionSelect.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Error loading sections:', error);
    }
}

// Load operations for selected model and section
async function loadOperationsForModelAndSection() {
    const modelListId = parseInt(document.getElementById('modelSelect').value);
    const sectionListId = parseInt(document.getElementById('sectionSelect').value);
    
    if (!modelListId || !sectionListId) {
        return;
    }
    
    try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/techmaps', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            allTechmaps = await response.json();
            
            const operationSelect = document.getElementById('operationSelect');
            operationSelect.innerHTML = '<option value="">-- Выберите операцию --</option>';
            operationSelect.disabled = false;
            
            const filteredTechmaps = allTechmaps.filter(t => 
                t.modelList.id === modelListId && t.sectionList.id === sectionListId
            );
            
            filteredTechmaps.forEach(techmap => {
                const option = document.createElement('option');
                option.value = techmap.id;
                option.textContent = techmap.serial;
                option.dataset.descriptor = techmap.descriptor;
                option.dataset.time = techmap.time;
                option.dataset.price = techmap.price;
                operationSelect.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Error loading operations:', error);
    }
}

// Show techmap info when operation is selected
function showTechmapInfo() {
    const operationSelect = document.getElementById('operationSelect');
    const selectedOption = operationSelect.selectedOptions[0];
    
    if (selectedOption && selectedOption.value) {
        const techmapInfo = document.getElementById('techmapInfo');
        const tbody = document.getElementById('techmapTableBody');
        
        tbody.innerHTML = `
            <tr>
                <td>${selectedOption.textContent}</td>
                <td>${selectedOption.dataset.descriptor}</td>
                <td>${selectedOption.dataset.time}</td>
                <td>${formatEuros(selectedOption.dataset.price / 100)}</td>
            </tr>
        `;
        
        techmapInfo.classList.remove('hidden');
        
        // Enable quantity input and add button
        document.getElementById('quantityInput').disabled = false;
        document.getElementById('addButton').disabled = false;
        
        // Calculate available quantity
        calculateAvailableQuantity();
    } else {
        document.getElementById('techmapInfo').classList.add('hidden');
        document.getElementById('quantityInput').disabled = true;
        document.getElementById('addButton').disabled = true;
    }
}

// Calculate available quantity (total - completed)
async function calculateAvailableQuantity() {
    const employeeId = getCurrentEmployeeId();
    const orderId = parseInt(document.getElementById('orderSelect').value);
    const modelListId = parseInt(document.getElementById('modelSelect').value);
    const sectionListId = parseInt(document.getElementById('sectionSelect').value);
    const techmapId = parseInt(document.getElementById('operationSelect').value);
    const year = parseInt(document.getElementById('yearSelect').value);
    const month = parseInt(document.getElementById('monthSelect').value);
    const week = parseInt(document.getElementById('weekSelect').value);

    if (!employeeId || !orderId || !modelListId || !sectionListId || !techmapId) {
        return;
    }

    try {
        const token = localStorage.getItem('token');
        const orderSelect = document.getElementById('orderSelect');
        const selectedOption = orderSelect.selectedOptions[0];
        const isTransferred = selectedOption.dataset.isTransferred === 'true';
        const originalOrderId = selectedOption.dataset.originalOrderId ? parseInt(selectedOption.dataset.originalOrderId) : orderId;
        const modelSelect = document.getElementById('modelSelect');
        const selectedModelOption = modelSelect.selectedOptions[0];
        const remainingCount = selectedModelOption ? parseInt(selectedModelOption.dataset.remainingCount) : null;

        let totalQuantity;
        if (isTransferred && remainingCount !== null) {
            totalQuantity = remainingCount;
        } else {
            const orderResponse = await fetch(`/api/orders/${originalOrderId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (orderResponse.ok) {
                const order = await orderResponse.json();
                const model = order.models.find(m => m.modelList.id === modelListId);
                totalQuantity = model ? model.count : 0;
            } else {
                return;
            }
        }

        // Get completed quantity in current week only (not total across all periods)
        const completedResponse = await fetch(
            `/api/work-results/completed-quantity?employeeId=${employeeId}&orderId=${originalOrderId}&modelListId=${modelListId}&sectionListId=${sectionListId}&techmapId=${techmapId}&year=${year}&month=${month}&week=${week}`,
            {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }
        );

        // Get transferred quantity for current week
        const transferredResponse = await fetch(
            `/api/work-results/transferred-quantity?orderId=${originalOrderId}&modelListId=${modelListId}&year=${year}&month=${month}&week=${week}`,
            {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }
        );

        if (completedResponse.ok && transferredResponse.ok) {
            const completedQuantity = await completedResponse.json();
            const transferredQuantity = await transferredResponse.json();
            
            // Available quantity = total - transferred - completed in current week
            const availableQuantity = totalQuantity - transferredQuantity - completedQuantity;

            const quantityInput = document.getElementById('quantityInput');
            quantityInput.max = availableQuantity;
            quantityInput.value = Math.min(1, availableQuantity);

            if (availableQuantity <= 0) {
                quantityInput.disabled = true;
                document.getElementById('addButton').disabled = true;
            }
        }
    } catch (error) {
        console.error('Error calculating available quantity:', error);
    }
}

// Setup event listeners
function setupEventListeners() {
    // Period radio buttons
    document.querySelectorAll('input[name="period"]').forEach(radio => {
        radio.addEventListener('change', function() {
            const weekSelect = document.getElementById('weekSelect');
            if (this.value === 'month') {
                weekSelect.disabled = true;
            } else {
                weekSelect.disabled = false;
            }
        });
    });
    
    // Date selectors
    document.getElementById('yearSelect').addEventListener('change', function() {
        currentYear = parseInt(this.value);
        renderCalendar(currentYear, currentMonth);
    });
    
    document.getElementById('monthSelect').addEventListener('change', function() {
        currentMonth = parseInt(this.value) - 1;
        renderCalendar(currentYear, currentMonth);
    });
    
    document.getElementById('weekSelect').addEventListener('change', function() {
        renderCalendar(currentYear, currentMonth);
        loadOrdersForWeek();
    });
    
    // Employee selection
    document.getElementById('employeeSelect').addEventListener('change', function() {
        loadWorkResults();
    });
    
    // Order selection
    document.getElementById('orderSelect').addEventListener('change', function() {
        loadModelsForOrder();
    });
    
    // Model selection
    document.getElementById('modelSelect').addEventListener('change', function() {
        loadSectionsForModel();
        document.getElementById('sectionSelect').value = '';
        document.getElementById('operationSelect').innerHTML = '<option value="">-- Сначала выберите модель и раздел --</option>';
        document.getElementById('operationSelect').disabled = true;
        document.getElementById('techmapInfo').classList.add('hidden');
        document.getElementById('quantityInput').disabled = true;
        document.getElementById('addButton').disabled = true;
    });
    
    // Section selection
    document.getElementById('sectionSelect').addEventListener('change', function() {
        loadOperationsForModelAndSection();
        document.getElementById('operationSelect').value = '';
        document.getElementById('techmapInfo').classList.add('hidden');
        document.getElementById('quantityInput').disabled = true;
        document.getElementById('addButton').disabled = true;
    });
    
    // Operation selection
    document.getElementById('operationSelect').addEventListener('change', function() {
        showTechmapInfo();
    });
    
    // Add button
    document.getElementById('addButton').addEventListener('click', addWorkResult);
}

// Add work result
async function addWorkResult() {
    const employeeId = getCurrentEmployeeId();
    const orderId = parseInt(document.getElementById('orderSelect').value);
    const modelListId = parseInt(document.getElementById('modelSelect').value);
    const sectionListId = parseInt(document.getElementById('sectionSelect').value);
    const techmapId = parseInt(document.getElementById('operationSelect').value);
    const quantity = parseInt(document.getElementById('quantityInput').value);
    const year = parseInt(document.getElementById('yearSelect').value);
    const month = parseInt(document.getElementById('monthSelect').value);
    const week = parseInt(document.getElementById('weekSelect').value);

    if (!employeeId || !orderId || !modelListId || !sectionListId || !techmapId || !quantity || !year || !month || !week) {
        alert('Пожалуйста, заполните все поля');
        return;
    }

    const orderSelect = document.getElementById('orderSelect');
    const selectedOption = orderSelect.selectedOptions[0];
    const isTransferred = selectedOption.dataset.isTransferred === 'true';
    const originalOrderId = selectedOption.dataset.originalOrderId ? parseInt(selectedOption.dataset.originalOrderId) : orderId;

    try {
        const token = localStorage.getItem('token');
        const modelSelect = document.getElementById('modelSelect');
        const selectedModelOption = modelSelect.selectedOptions[0];
        const remainingCount = selectedModelOption ? parseInt(selectedModelOption.dataset.remainingCount) : null;

        let totalQuantity;
        if (isTransferred && remainingCount !== null) {
            totalQuantity = remainingCount;
        } else {
            const orderResponse = await fetch(`/api/orders/${originalOrderId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (orderResponse.ok) {
                const order = await orderResponse.json();
                const model = order.models.find(m => m.modelList.id === modelListId);
                totalQuantity = model ? model.count : 0;
            } else {
                return;
            }
        }

        // Get completed quantity in current week only
        const completedResponse = await fetch(
            `/api/work-results/completed-quantity?employeeId=${employeeId}&orderId=${originalOrderId}&modelListId=${modelListId}&sectionListId=${sectionListId}&techmapId=${techmapId}&year=${year}&month=${month}&week=${week}`,
            {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }
        );

        // Get transferred quantity for current week
        const transferredResponse = await fetch(
            `/api/work-results/transferred-quantity?orderId=${originalOrderId}&modelListId=${modelListId}&year=${year}&month=${month}&week=${week}`,
            {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }
        );

        if (completedResponse.ok && transferredResponse.ok) {
            const completedQuantity = await completedResponse.json();
            const transferredQuantity = await transferredResponse.json();
            
            // Available quantity = total - transferred - completed in current week
            const availableQuantity = totalQuantity - transferredQuantity - completedQuantity;

            if (quantity > availableQuantity) {
                alert(`Невозможно добавить ${quantity} изделий. Доступное количество: ${availableQuantity} (Всего: ${totalQuantity}, Перенесено: ${transferredQuantity}, Уже выполнено: ${completedQuantity})`);
                return;
            }
        }
    } catch (error) {
        console.error('Error validating quantity:', error);
    }

    const workResultData = {
        employeeId: employeeId,
        orderId: originalOrderId,
        modelListId: modelListId,
        sectionListId: sectionListId,
        techmapId: techmapId,
        quantity: quantity,
        year: year,
        month: month,
        week: week
    };

    try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/work-results', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(workResultData)
        });

        if (response.ok) {
            alert('Результат работы успешно добавлен');
            loadWorkResults();
            calculateAvailableQuantity();

            document.getElementById('modelSelect').value = '';
            document.getElementById('modelSelect').disabled = true;
            document.getElementById('sectionSelect').value = '';
            document.getElementById('sectionSelect').disabled = true;
            document.getElementById('sectionSelect').innerHTML = '<option value="">-- Сначала выберите модель --</option>';
            document.getElementById('operationSelect').value = '';
            document.getElementById('operationSelect').disabled = true;
            document.getElementById('operationSelect').innerHTML = '<option value="">-- Сначала выберите модель и раздел --</option>';
            document.getElementById('techmapInfo').classList.add('hidden');
            document.getElementById('quantityInput').value = '1';
            document.getElementById('quantityInput').disabled = true;
            document.getElementById('addButton').disabled = true;
        } else {
            const error = await response.json();
            alert('Ошибка при добавлении результата работы: ' + (error.message || 'Неизвестная ошибка'));
        }
    } catch (error) {
        console.error('Error adding work result:', error);
        alert('Ошибка при добавлении результата работы');
    }
}

// Load work results
async function loadWorkResults() {
    try {
        const token = localStorage.getItem('token');
        const employeeId = getCurrentEmployeeId();
        const year = document.getElementById('yearSelect').value;
        const month = document.getElementById('monthSelect').value;
        const week = document.getElementById('weekSelect').value;
        const period = document.querySelector('input[name="period"]:checked').value;
        
        console.log('Loading work results:', { employeeId, year, month, week, period });
        
        if (!employeeId) {
            console.error('No employee ID selected');
            return;
        }
        
        let url = `/api/work-results/employee/${employeeId}?year=${year}&month=${month}`;
        if (period === 'week') {
            url += `&week=${week}`;
        }
        
        console.log('Fetching work results from:', url);
        
        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const workResults = await response.json();
            console.log('Work results loaded:', workResults);
            
            // Load orders to get total quantities
            const ordersResponse = await fetch('/api/orders', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            let orders = [];
            if (ordersResponse.ok) {
                orders = await ordersResponse.json();
            }
            
            renderWorkResultsTable(workResults, orders);
        } else {
            console.error('Failed to load work results:', response.status, response.statusText);
        }
    } catch (error) {
        console.error('Error loading work results:', error);
    }
}

// Render work results table
function renderWorkResultsTable(workResults, orders) {
    const tbody = document.getElementById('workResultsTableBody');
    const summarySection = document.getElementById('summarySection');
    const totalTimeElement = document.getElementById('totalTime');
    const totalEarningsElement = document.getElementById('totalEarnings');
    
    tbody.innerHTML = '';
    
    if (workResults.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">Нет результатов работы</td></tr>';
        summarySection.classList.add('hidden');
        return;
    }
    
    summarySection.classList.remove('hidden');
    
    let totalSeconds = 0;
    let totalEarnings = 0;
    
    workResults.forEach(workResult => {
        const tr = document.createElement('tr');
        
        const price = workResult.techmap ? workResult.techmap.price / 100 : 0;
        const time = workResult.techmap ? workResult.techmap.time : 0;
        const totalCost = workResult.quantity * price;
        const totalTimeForItem = workResult.quantity * time;
        
        totalSeconds += totalTimeForItem;
        totalEarnings += totalCost;
        
        // Find order to get total quantity
        const order = orders.find(o => o.id === workResult.orderId);
        const model = order?.models?.find(m => m.modelList.id === workResult.modelListId);
        const totalQuantity = model ? model.count : 0;
        
        // Get total completed quantity for this employee, order, model, section, and techmap
        const completedQuantity = workResults
            .filter(wr => 
                wr.orderId === workResult.orderId &&
                wr.modelListId === workResult.modelListId &&
                wr.sectionListId === workResult.sectionListId &&
                wr.techmapId === workResult.techmapId
            )
            .reduce((sum, wr) => sum + wr.quantity, 0);
        
        const remainingQuantity = Math.max(0, totalQuantity - completedQuantity);
        
        tr.innerHTML = `
            <td>${workResult.order.name}</td>
            <td>${workResult.modelList.name}</td>
            <td>${workResult.sectionList ? workResult.sectionList.name : '-'}</td>
            <td>${workResult.techmap ? workResult.techmap.serial : '-'}</td>
            <td>${completedQuantity} / ${remainingQuantity}</td>
            <td>${formatEuros(totalCost)} €</td>
            <td>
                <button class="btn btn-sm btn-error" onclick="deleteWorkResult(${workResult.id})">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
    
    totalTimeElement.textContent = formatTime(totalSeconds);
    totalEarningsElement.textContent = `€${formatEuros(totalEarnings)}`;
}

// Format time from seconds to HH:MM:SS
function formatTime(totalSeconds) {
    if (totalSeconds === null || totalSeconds === undefined || totalSeconds === 0) {
        return '00:00:00';
    }
    
    const seconds = Math.floor(totalSeconds);
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const remainingSeconds = seconds % 60;
    
    const pad = (num) => num.toString().padStart(2, '0');
    return `${pad(hours)}:${pad(minutes)}:${pad(remainingSeconds)}`;
}

// Delete work result
async function deleteWorkResult(workResultId) {
    if (!confirm('Вы уверены, что хотите удалить этот результат работы?')) {
        return;
    }
    
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/work-results/${workResultId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            loadWorkResults();
            alert('Результат работы успешно удален');
        } else {
            alert('Ошибка при удалении результата работы');
        }
    } catch (error) {
        console.error('Error deleting work result:', error);
        alert('Ошибка при удалении результата работы');
    }
}

// Make deleteWorkResult globally accessible
window.deleteWorkResult = deleteWorkResult;
