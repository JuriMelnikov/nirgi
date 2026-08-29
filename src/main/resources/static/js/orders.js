// Orders management functionality

// Global variables
let currentYear = new Date().getFullYear();
let currentMonth = new Date().getMonth(); // 0-11
let selectedModels = [];
let allModelLists = [];
let editSelectedModels = [];

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    initializeYearSelects();
    initializeWeekSelects();
    initializeEditYearSelects();
    initializeEditWeekSelects();
    initializeDublYearSelects();
    initializeDublWeekSelects();
    initializeExecutionHistoryYearSelects();
    initializeExecutionHistoryWeekSelects();
    initializeCalendar();
    loadModelLists();
    setFilterDefaults();
    loadOrders();
    setupEventListeners();
    setupEditEventListeners();
    setupDublEventListeners();
});

// Initialize year selects (both for order creation and filtering)
function initializeYearSelects() {
    const currentYear = new Date().getFullYear();
    const orderYearSelect = document.getElementById('orderYear');
    const filterYearSelect = document.getElementById('filterYear');
    
    for (let year = currentYear - 2; year <= currentYear + 2; year++) {
        const option1 = document.createElement('option');
        option1.value = year;
        option1.textContent = year;
        orderYearSelect.appendChild(option1);
        
        const option2 = document.createElement('option');
        option2.value = year;
        option2.textContent = year;
        filterYearSelect.appendChild(option2);
    }
    
    // Set current year as default
    orderYearSelect.value = currentYear;
}

// Initialize week selects (1-53)
function initializeWeekSelects() {
    const orderWeekSelect = document.getElementById('orderWeek');
    const filterWeekSelect = document.getElementById('filterWeek');
    
    for (let week = 1; week <= 53; week++) {
        const option1 = document.createElement('option');
        option1.value = week;
        option1.textContent = week;
        orderWeekSelect.appendChild(option1);
        
        const option2 = document.createElement('option');
        option2.value = week;
        option2.textContent = week;
        filterWeekSelect.appendChild(option2);
    }
}

// Initialize edit year select
function initializeEditYearSelects() {
    const currentYear = new Date().getFullYear();
    const editYearSelect = document.getElementById('editOrderYear');
    
    for (let year = currentYear - 2; year <= currentYear + 2; year++) {
        const option = document.createElement('option');
        option.value = year;
        option.textContent = year;
        editYearSelect.appendChild(option);
    }
}

// Initialize edit week select
function initializeEditWeekSelects() {
    const editWeekSelect = document.getElementById('editOrderWeek');
    
    for (let week = 1; week <= 53; week++) {
        const option = document.createElement('option');
        option.value = week;
        option.textContent = week;
        editWeekSelect.appendChild(option);
    }
}

// Initialize dubl year select
function initializeDublYearSelects() {
    const currentYear = new Date().getFullYear();
    const dublYearSelect = document.getElementById('dublTargetYear');
    
    for (let year = currentYear - 2; year <= currentYear + 2; year++) {
        const option = document.createElement('option');
        option.value = year;
        option.textContent = year;
        dublYearSelect.appendChild(option);
    }
    
    dublYearSelect.value = currentYear;
}

// Initialize dubl week select
function initializeDublWeekSelects() {
    const dublWeekSelect = document.getElementById('dublTargetWeek');
    
    for (let week = 1; week <= 53; week++) {
        const option = document.createElement('option');
        option.value = week;
        option.textContent = week;
        dublWeekSelect.appendChild(option);
    }
}

// Initialize execution history year select
function initializeExecutionHistoryYearSelects() {
    const currentYear = new Date().getFullYear();
    const yearSelect = document.getElementById('executionHistoryYear');
    
    for (let year = currentYear - 2; year <= currentYear + 2; year++) {
        const option = document.createElement('option');
        option.value = year;
        option.textContent = year;
        yearSelect.appendChild(option);
    }
}

// Initialize execution history week select
function initializeExecutionHistoryWeekSelects() {
    const weekSelect = document.getElementById('executionHistoryWeek');
    
    for (let week = 1; week <= 53; week++) {
        const option = document.createElement('option');
        option.value = week;
        option.textContent = week;
        weekSelect.appendChild(option);
    }
}

// Set filter defaults to current date
function setFilterDefaults() {
    console.log('setFilterDefaults() called - resetting filters to current date');
    const now = new Date();
    const currentYear = now.getFullYear();
    const currentMonth = now.getMonth() + 1; // 1-12
    const currentWeek = getWeekNumber(now);
    
    console.log('Setting filters to:', { currentYear, currentMonth, currentWeek });
    document.getElementById('filterYear').value = currentYear;
    document.getElementById('filterMonth').value = currentMonth;
    document.getElementById('filterWeek').value = currentWeek;
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
    const startingDay = firstDay.getDay(); // 0 = Sunday, 1 = Monday, etc.
    const totalDays = lastDay.getDate();
    
    // Adjust for Monday-first calendar (0 = Monday, 6 = Sunday)
    let adjustedStartingDay = startingDay === 0 ? 6 : startingDay - 1;
    
    const selectedWeek = parseInt(document.getElementById('orderWeek').value);
    
    // Calculate total cells needed
    const totalCells = adjustedStartingDay + totalDays;
    const totalRows = Math.ceil(totalCells / 7);
    
    // Build calendar row by row
    for (let row = 0; row < totalRows; row++) {
        // Get week number for this row's Monday
        const mondayDay = row * 7 - adjustedStartingDay + 1;
        let weekNumber;
        
        if (mondayDay <= 0) {
            // Use first day of month for week number
            weekNumber = getWeekNumber(firstDay);
        } else if (mondayDay > totalDays) {
            // Use last day of month for week number
            weekNumber = getWeekNumber(lastDay);
        } else {
            weekNumber = getWeekNumber(new Date(year, month, mondayDay));
        }
        
        // Add week number cell
        const weekCell = document.createElement('div');
        weekCell.className = 'py-0.5 text-center font-bold text-[10px] leading-tight bg-base-300 rounded';
        weekCell.textContent = weekNumber;
        
        if (selectedWeek && weekNumber === selectedWeek) {
            weekCell.classList.add('bg-primary', 'text-primary-content');
        }
        
        weekCell.addEventListener('click', function() {
            document.getElementById('orderWeek').value = weekNumber;
            document.getElementById('orderMonth').value = month + 1;
            renderCalendar(year, month);
        });
        
        calendarDays.appendChild(weekCell);
        
        // Add 7 day cells for this row
        for (let col = 0; col < 7; col++) {
            const dayNum = row * 7 + col - adjustedStartingDay + 1;
            
            if (dayNum > 0 && dayNum <= totalDays) {
                const currentDate = new Date(year, month, dayNum);
                const dayCell = document.createElement('div');
                dayCell.className = 'py-0.5 text-center text-[10px] leading-tight cursor-pointer hover:bg-base-300 rounded';
                dayCell.textContent = dayNum;
                
                // Highlight Saturday (5) and Sunday (6) with red tint
                if (col === 5 || col === 6) {
                    dayCell.classList.add('bg-red-100', 'text-red-800');
                }
                
                // Highlight if this week is selected
                if (selectedWeek && weekNumber === selectedWeek) {
                    dayCell.classList.remove('bg-red-100', 'text-red-800');
                    dayCell.classList.add('bg-primary', 'text-primary-content');
                }
                
                dayCell.addEventListener('click', function() {
                    document.getElementById('orderWeek').value = weekNumber;
                    document.getElementById('orderMonth').value = month + 1;
                    renderCalendar(year, month);
                });
                
                calendarDays.appendChild(dayCell);
            } else {
                // Empty cell
                const emptyCell = document.createElement('div');
                emptyCell.className = 'py-0.5';
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

// Load model lists for dropdown
async function loadModelLists() {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/model-list', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            allModelLists = await response.json();
            
            // Populate main model select
            const modelSelect = document.getElementById('modelSelect');
            modelSelect.innerHTML = '<option value="">-- Выберите модель --</option>';
            
            allModelLists.forEach(modelList => {
                const option = document.createElement('option');
                option.value = modelList.id;
                option.textContent = modelList.name;
                modelSelect.appendChild(option);
            });
            
            // Populate edit model select
            const editModelSelect = document.getElementById('editModelSelect');
            editModelSelect.innerHTML = '<option value="">-- Выберите модель --</option>';
            
            allModelLists.forEach(modelList => {
                const option = document.createElement('option');
                option.value = modelList.id;
                option.textContent = modelList.name;
                editModelSelect.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Error loading model lists:', error);
    }
}

// Setup event listeners
function setupEventListeners() {
    // Add model button
    document.getElementById('addModelBtn').addEventListener('click', addModel);
    
    // Form submission
    document.getElementById('addOrderForm').addEventListener('submit', handleOrderSubmit);
    
    // Filter changes
    document.getElementById('filterYear').addEventListener('change', function() {
        console.log('filterYear changed to:', this.value);
        loadOrders();
    });
    document.getElementById('filterMonth').addEventListener('change', function() {
        console.log('filterMonth changed to:', this.value);
        loadOrders();
    });
    document.getElementById('filterWeek').addEventListener('change', function() {
        console.log('filterWeek changed to:', this.value);
        loadOrders();
    });
    
    // Update calendar when year/month/week changes
    document.getElementById('orderYear').addEventListener('change', function() {
        currentYear = parseInt(this.value);
        renderCalendar(currentYear, currentMonth);
    });
    
    document.getElementById('orderMonth').addEventListener('change', function() {
        currentMonth = parseInt(this.value) - 1;
        renderCalendar(currentYear, currentMonth);
    });
    
    document.getElementById('orderWeek').addEventListener('change', function() {
        renderCalendar(currentYear, currentMonth);
    });
    
    // Execution history filter changes
    document.getElementById('executionHistoryYear').addEventListener('change', function() {
        loadExecutionHistoryForWeek();
    });
    
    document.getElementById('executionHistoryMonth').addEventListener('change', function() {
        loadExecutionHistoryForWeek();
    });
    
    document.getElementById('executionHistoryWeek').addEventListener('change', function() {
        loadExecutionHistoryForWeek();
    });
    
    document.getElementById('executionHistoryModelSelect').addEventListener('change', function() {
        populateExecutionHistorySectionSelect(this.value);
        filterExecutionHistoryTable();
    });
    
    document.getElementById('executionHistorySectionSelect').addEventListener('change', function() {
        filterExecutionHistoryTable();
    });
}

// Add model to selected models list
function addModel() {
    const modelSelect = document.getElementById('modelSelect');
    const modelCount = document.getElementById('modelCount');
    
    const modelId = parseInt(modelSelect.value);
    const count = parseInt(modelCount.value);
    
    if (!modelId || count < 1) {
        alert('Пожалуйста, выберите модель и укажите количество');
        return;
    }
    
    // Check if model already selected
    const existingIndex = selectedModels.findIndex(m => m.modelListId === modelId);
    if (existingIndex !== -1) {
        selectedModels[existingIndex].count += count;
    } else {
        const modelList = allModelLists.find(m => m.id === modelId);
        selectedModels.push({
            modelListId: modelId,
            modelListName: modelList.name,
            count: count
        });
    }
    
    // Reset inputs
    modelSelect.value = '';
    modelCount.value = 1;
    
    // Update display
    renderSelectedModels();
}

// Get correct form of "экземпляр" based on number
function getInstanceForm(count) {
    const lastTwo = count % 100;
    const lastOne = count % 10;
    
    if (lastTwo >= 11 && lastTwo <= 19) {
        return 'экземпляров';
    }
    
    if (lastOne === 1) {
        return 'экземпляр';
    }
    
    if (lastOne >= 2 && lastOne <= 4) {
        return 'экземпляра';
    }
    
    return 'экземпляров';
}

// Render selected models
function renderSelectedModels() {
    const container = document.getElementById('selectedModels');
    container.innerHTML = '';
    
    selectedModels.forEach((model, index) => {
        const div = document.createElement('div');
        div.className = 'flex items-center justify-between bg-base-200 p-2 rounded';
        div.innerHTML = `
            <span>${model.modelListName} - ${model.count} ${getInstanceForm(model.count)}</span>
            <button type="button" class="btn btn-sm btn-error" style="height: 25px; min-height: 25px;" onclick="removeModel(${index})">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
            </button>
        `;
        container.appendChild(div);
    });
}

// Remove model from selected models
function removeModel(index) {
    selectedModels.splice(index, 1);
    renderSelectedModels();
}

// Update model count
function updateModelCount(index, newCount) {
    const count = parseInt(newCount);
    if (count >= 1) {
        selectedModels[index].count = count;
    }
}

// Make removeModel and updateModelCount globally accessible
window.removeModel = removeModel;
window.updateModelCount = updateModelCount;

// Handle order form submission
async function handleOrderSubmit(event) {
    event.preventDefault();
    
    const name = document.getElementById('orderName').value;
    const year = parseInt(document.getElementById('orderYear').value);
    const month = parseInt(document.getElementById('orderMonth').value);
    const week = parseInt(document.getElementById('orderWeek').value);
    
    if (!name || !year || !month || !week) {
        alert('Пожалуйста, заполните все поля');
        return;
    }
    
    if (selectedModels.length === 0) {
        alert('Пожалуйста, добавьте хотя бы одну модель');
        return;
    }
    
    const orderData = {
        name: name,
        year: year,
        month: month,
        week: week,
        models: selectedModels.map(m => ({
            modelList: { id: m.modelListId },
            count: m.count
        }))
    };
    
    try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/orders', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(orderData)
        });
        
        if (response.ok) {
            // Reset form
            document.getElementById('addOrderForm').reset();
            selectedModels = [];
            renderSelectedModels();
            
            // Reset year/month to current
            const currentYear = new Date().getFullYear();
            document.getElementById('orderYear').value = currentYear;
            currentMonth = new Date().getMonth();
            
            // Reload orders
            loadOrders();
            
            alert('Заказ успешно создан');
        } else {
            const error = await response.json();
            alert('Ошибка при создании заказа: ' + (error.message || 'Неизвестная ошибка'));
        }
    } catch (error) {
        console.error('Error creating order:', error);
        alert('Ошибка при создании заказа');
    }
}

// Load orders with filters
async function loadOrders() {
    try {
        const token = localStorage.getItem('token');
        const year = document.getElementById('filterYear').value;
        const month = document.getElementById('filterMonth').value;
        const week = document.getElementById('filterWeek').value;
        
        console.log('Loading orders with filters:', { year, month, week });
        
        let url = '/api/orders/search-with-transferred?';
        if (year) url += `year=${year}&`;
        if (month) url += `month=${month}&`;
        if (week) url += `week=${week}&`;
        
        console.log('Fetching orders from:', url);
        
        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const orders = await response.json();
            console.log('Orders loaded:', orders);
            renderOrdersTable(orders);
        } else {
            console.error('Failed to load orders:', response.status, response.statusText);
        }
    } catch (error) {
        console.error('Error loading orders:', error);
    }
}

// Render orders table
function renderOrdersTable(orders) {
    console.log('renderOrdersTable called with', orders.length, 'orders');
    console.log('Current filter values in renderOrdersTable:', {
        year: document.getElementById('filterYear').value,
        month: document.getElementById('filterMonth').value,
        week: document.getElementById('filterWeek').value
    });
    
    const tbody = document.getElementById('ordersTableBody');
    tbody.innerHTML = '';
    
    if (orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center">Нет заказов</td></tr>';
        return;
    }
    
    orders.forEach(order => {
        const tr = document.createElement('tr');
        
        const isTransferred = order.isTransferred;
        const orderIdForActions = isTransferred ? order.originalOrderId : order.id;
        
        // Highlight transferred orders
        const rowClass = isTransferred ? 'bg-yellow-50' : '';
        tr.className = rowClass;
        
        tr.innerHTML = `
            <td>${order.name}</td>
            <td>
                <div class="flex gap-1">
                    ${!isTransferred ? `
                    <button class="btn btn-xs btn-info h-6 min-h-6 px-2" onclick="editOrder(${order.id})">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                        </svg>
                    </button>
                    <button class="btn btn-xs btn-error h-6 min-h-6 px-2" onclick="deleteOrder(${order.id})">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                    </button>
                    ` : `
                    <button class="btn btn-xs btn-error h-6 min-h-6 px-2" onclick="deleteDublOrder(${order.id})" title="Удалить перенос">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                    </button>
                    `}
                </div>
            </td>
            <td>
                <button class="btn btn-xs btn-success h-6 min-h-6 px-2" onclick="openDublModal(${orderIdForActions})">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
                    </svg>
                </button>
            </td>
            <td>
                <button class="btn btn-xs btn-accent h-6 min-h-6 px-2" onclick="openExecutionHistoryModal(${orderIdForActions})">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                    </svg>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Delete order
async function deleteOrder(orderId) {
    if (!confirm('Вы уверены, что хотите удалить этот заказ?')) {
        return;
    }
    
    console.log('Starting delete order for ID:', orderId);
    console.log('Current filter values before delete:', {
        year: document.getElementById('filterYear').value,
        month: document.getElementById('filterMonth').value,
        week: document.getElementById('filterWeek').value
    });
    
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/orders/${orderId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            console.log('Delete successful, calling loadOrders()');
            loadOrders();
            alert('Заказ успешно удален');
        } else {
            const errorText = await response.text();
            console.error('Delete error:', errorText);
            alert('Ошибка при удалении заказа: ' + errorText);
        }
    } catch (error) {
        console.error('Error deleting order:', error);
        alert('Ошибка при удалении заказа: ' + error.message);
    }
}

// Delete dubl order
async function deleteDublOrder(dublOrderId) {
    if (!confirm('Вы уверены, что хотите удалить этот перенос заказа?')) {
        return;
    }
    
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/dubl-orders/${dublOrderId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            loadOrders();
            alert('Перенос заказа успешно удален');
        } else {
            const errorText = await response.text();
            console.error('Delete dubl order error:', errorText);
            alert('Ошибка при удалении переноса заказа: ' + errorText);
        }
    } catch (error) {
        console.error('Error deleting dubl order:', error);
        alert('Ошибка при удалении переноса заказа: ' + error.message);
    }
}

// Make deleteOrder and deleteDublOrder globally accessible
window.deleteOrder = deleteOrder;
window.deleteDublOrder = deleteDublOrder;

// Edit order functions
let editingOrderId = null;

function setupEditEventListeners() {
    document.getElementById('editAddModelBtn').addEventListener('click', addEditModel);
    document.getElementById('editOrderForm').addEventListener('submit', handleEditOrderSubmit);
}

async function editOrder(orderId) {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/orders/${orderId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const order = await response.json();
            editingOrderId = orderId;
            
            // Populate form fields
            document.getElementById('editOrderId').value = order.id;
            document.getElementById('editOrderName').value = order.name;
            document.getElementById('editOrderYear').value = order.year;
            document.getElementById('editOrderMonth').value = order.month;
            document.getElementById('editOrderWeek').value = order.week;
            
            // Populate models
            editSelectedModels = [];
            if (order.models && order.models.length > 0) {
                order.models.forEach(model => {
                    editSelectedModels.push({
                        modelListId: model.modelList.id,
                        modelListName: model.modelList.name,
                        count: model.count
                    });
                });
            }
            renderEditSelectedModels();
            
            // Show modal
            document.getElementById('editOrderModal').showModal();
        } else {
            alert('Ошибка при загрузке заказа');
        }
    } catch (error) {
        console.error('Error loading order:', error);
        alert('Ошибка при загрузке заказа');
    }
}

function closeEditModal() {
    document.getElementById('editOrderModal').close();
    editingOrderId = null;
    editSelectedModels = [];
}

function addEditModel() {
    const modelSelect = document.getElementById('editModelSelect');
    const modelCount = document.getElementById('editModelCount');
    
    const modelId = parseInt(modelSelect.value);
    const count = parseInt(modelCount.value);
    
    if (!modelId || count < 1) {
        alert('Пожалуйста, выберите модель и укажите количество');
        return;
    }
    
    const existingIndex = editSelectedModels.findIndex(m => m.modelListId === modelId);
    if (existingIndex !== -1) {
        editSelectedModels[existingIndex].count += count;
    } else {
        const modelList = allModelLists.find(m => m.id === modelId);
        editSelectedModels.push({
            modelListId: modelId,
            modelListName: modelList.name,
            count: count
        });
    }
    
    modelSelect.value = '';
    modelCount.value = 1;
    renderEditSelectedModels();
}

function renderEditSelectedModels() {
    const container = document.getElementById('editSelectedModels');
    container.innerHTML = '';
    
    editSelectedModels.forEach((model, index) => {
        const div = document.createElement('div');
        div.className = 'flex items-center justify-between bg-base-200 p-2 rounded';
        div.innerHTML = `
            <span>${model.modelListName} - ${model.count} ${getInstanceForm(model.count)}</span>
            <button type="button" class="btn btn-sm btn-error" style="height: 25px; min-height: 25px;" onclick="removeEditModel(${index})">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
            </button>
        `;
        container.appendChild(div);
    });
}

function removeEditModel(index) {
    editSelectedModels.splice(index, 1);
    renderEditSelectedModels();
}

function updateEditModelCount(index, newCount) {
    const count = parseInt(newCount);
    if (count >= 1) {
        editSelectedModels[index].count = count;
    }
}

// Make edit model functions globally accessible
window.removeEditModel = removeEditModel;
window.updateEditModelCount = updateEditModelCount;

async function handleEditOrderSubmit(event) {
    event.preventDefault();
    
    const name = document.getElementById('editOrderName').value;
    const year = parseInt(document.getElementById('editOrderYear').value);
    const month = parseInt(document.getElementById('editOrderMonth').value);
    const week = parseInt(document.getElementById('editOrderWeek').value);
    
    if (!name || !year || !month || !week) {
        alert('Пожалуйста, заполните все поля');
        return;
    }
    
    const orderData = {
        name: name,
        year: year,
        month: month,
        week: week,
        models: editSelectedModels.map(m => ({
            modelList: { id: m.modelListId },
            count: m.count
        }))
    };
    
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/orders/${editingOrderId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(orderData)
        });
        
        if (response.ok) {
            closeEditModal();
            loadOrders();
            alert('Заказ успешно обновлен');
        } else {
            const error = await response.json();
            alert('Ошибка при обновлении заказа: ' + (error.message || 'Неизвестная ошибка'));
        }
    } catch (error) {
        console.error('Error updating order:', error);
        alert('Ошибка при обновлении заказа');
    }
}

// Make edit functions globally accessible
window.editOrder = editOrder;
window.closeEditModal = closeEditModal;
window.removeEditModel = removeEditModel;

// DublOrder functions
let currentDublOrderId = null;
let dublModelsData = [];

function setupDublEventListeners() {
    document.getElementById('createDublOrderForm').addEventListener('submit', handleDublOrderSubmit);
}

function openDublModal(orderId) {
    try {
        const token = localStorage.getItem('token');
        fetch(`/api/orders/${orderId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        .then(response => {
            if (!response.ok) throw new Error('Failed to load order');
            return response.json();
        })
        .then(order => {
            currentDublOrderId = orderId;
            dublModelsData = [];
            
            // Set default target week to next week from order's week
            document.getElementById('dublTargetYear').value = order.year;
            document.getElementById('dublTargetMonth').value = order.month;
            document.getElementById('dublTargetWeek').value = order.week + 1;
            
            // Populate models with completed work inputs
            const container = document.getElementById('dublModelsContainer');
            container.innerHTML = '';
            
            if (order.models && order.models.length > 0) {
                // Fetch completed work from work_results table
                fetch(`/api/work-results/completed-work-by-order-week?orderId=${orderId}&year=${order.year}&month=${order.month}&week=${order.week}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                })
                .then(response => {
                    if (!response.ok) throw new Error('Failed to load completed work');
                    return response.json();
                })
                .then(completedWorkMap => {
                    order.models.forEach(model => {
                        const completedWork = completedWorkMap[model.modelList.id] || 0;
                        const div = document.createElement('div');
                        div.className = 'flex items-center gap-2 bg-base-200 p-2 rounded';
                        div.innerHTML = `
                            <span class="flex-1">${model.modelList.name} (всего: ${model.count}, выполнено: ${completedWork})</span>
                            <input type="number" 
                                   class="input input-bordered w-24" 
                                   min="0" 
                                   max="${model.count}" 
                                   value="${completedWork}" 
                                   data-model-id="${model.modelList.id}" 
                                   data-total-count="${model.count}"
                                   placeholder="Выполнено">
                        `;
                        container.appendChild(div);
                        
                        dublModelsData.push({
                            modelListId: model.modelList.id,
                            totalCount: model.count,
                            completedInOriginalWeek: completedWork
                        });
                    });
                    
                    document.getElementById('createDublOrderModal').showModal();
                })
                .catch(error => {
                    console.error('Error loading completed work:', error);
                    // Fallback to manual input if API fails
                    order.models.forEach(model => {
                        const div = document.createElement('div');
                        div.className = 'flex items-center gap-2 bg-base-200 p-2 rounded';
                        div.innerHTML = `
                            <span class="flex-1">${model.modelList.name} (всего: ${model.count})</span>
                            <input type="number" 
                                   class="input input-bordered w-24" 
                                   min="0" 
                                   max="${model.count}" 
                                   value="0" 
                                   data-model-id="${model.modelList.id}" 
                                   data-total-count="${model.count}"
                                   placeholder="Выполнено">
                        `;
                        container.appendChild(div);
                        
                        dublModelsData.push({
                            modelListId: model.modelList.id,
                            totalCount: model.count,
                            completedInOriginalWeek: 0
                        });
                    });
                    
                    document.getElementById('createDublOrderModal').showModal();
                });
            } else {
                document.getElementById('createDublOrderModal').showModal();
            }
        })
        .catch(error => {
            console.error('Error loading order:', error);
            alert('Ошибка при загрузке заказа');
        });
    } catch (error) {
        console.error('Error opening dubl modal:', error);
        alert('Ошибка при открытии модального окна');
    }
}

function closeDublModal() {
    document.getElementById('createDublOrderModal').close();
    currentDublOrderId = null;
    dublModelsData = [];
}

async function handleDublOrderSubmit(event) {
    event.preventDefault();
    
    const targetYear = parseInt(document.getElementById('dublTargetYear').value);
    const targetMonth = parseInt(document.getElementById('dublTargetMonth').value);
    const targetWeek = parseInt(document.getElementById('dublTargetWeek').value);
    
    console.log('Creating dubl order with:', { targetYear, targetMonth, targetWeek });
    
    // Collect completed work from inputs
    const completedWork = {};
    const inputs = document.querySelectorAll('#dublModelsContainer input');
    inputs.forEach(input => {
        const modelId = parseInt(input.dataset.modelId);
        const totalCount = parseInt(input.dataset.totalCount);
        const completed = parseInt(input.value) || 0;
        
        if (completed < 0 || completed > totalCount) {
            alert(`Значение должно быть от 0 до ${totalCount}`);
            return;
        }
        
        completedWork[modelId] = completed;
    });
    
    const orderData = {
        orderId: currentDublOrderId,
        completedWork: completedWork,
        targetYear: targetYear,
        targetMonth: targetMonth,
        targetWeek: targetWeek
    };
    
    console.log('Order data:', orderData);
    
    try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/dubl-orders', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(orderData)
        });
        
        if (response.ok) {
            const result = await response.json();
            console.log('Dubl order created:', result);
            closeDublModal();
            alert('Заказ успешно перенесен на следующую неделю');
        } else if (response.status === 409) {
            const error = await response.json();
            console.error('Dubl order already exists:', error);
            
            // Load existing dubl orders for this order
            const existingResponse = await fetch(`/api/dubl-orders/order/${currentDublOrderId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (existingResponse.ok) {
                const existingOrders = await existingResponse.json();
                console.log('Existing dubl orders for this order:', existingOrders);
                alert(`Заказ уже был перенесен ранее. Существующие переносы:\n${existingOrders.map(o => 
                    `ID: ${o.id}, Целевая неделя: ${o.targetYear}/${o.targetMonth}/${o.targetWeek}`
                ).join('\n')}`);
            } else {
                alert('Ошибка при переносе заказа: ' + (error.message || 'Неизвестная ошибка'));
            }
        } else {
            const error = await response.json();
            console.error('Error creating dubl order:', error);
            alert('Ошибка при переносе заказа: ' + (error.message || 'Неизвестная ошибка'));
        }
    } catch (error) {
        console.error('Error creating dubl order:', error);
        alert('Ошибка при переносе заказа');
    }
}

// Make dubl functions globally accessible
window.openDublModal = openDublModal;
window.closeDublModal = closeDublModal;

// Execution History functions
let currentExecutionHistoryOrderId = null;
let currentExecutionHistoryWorkResults = [];
let currentExecutionHistoryOrder = null;

async function openExecutionHistoryModal(orderId) {
    try {
        const token = localStorage.getItem('token');
        
        // Load order details
        const orderResponse = await fetch(`/api/orders/${orderId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (!orderResponse.ok) {
            throw new Error('Failed to load order');
        }
        
        const order = await orderResponse.json();
        currentExecutionHistoryOrderId = orderId;
        currentExecutionHistoryOrder = order;
        
        // Set order info in modal
        document.getElementById('executionHistoryOrderName').textContent = order.name;
        document.getElementById('executionHistoryOrderDate').textContent = 
            `Год: ${order.year}, Месяц: ${order.month}, Неделя: ${order.week}`;
        
        // Set week selectors to order's week
        document.getElementById('executionHistoryYear').value = order.year;
        document.getElementById('executionHistoryMonth').value = order.month;
        document.getElementById('executionHistoryWeek').value = order.week;
        
        // Load work results for this order and week
        await loadExecutionHistoryForWeek();
        
        // Populate model select
        populateExecutionHistoryModelSelect(order.models);
        
        // Reset section select
        const sectionSelect = document.getElementById('executionHistorySectionSelect');
        sectionSelect.innerHTML = '<option value="">-- Все разделы --</option>';
        sectionSelect.disabled = true;
        
        // Show modal
        document.getElementById('executionHistoryModal').showModal();
    } catch (error) {
        console.error('Error loading execution history:', error);
        alert('Ошибка при загрузке истории выполнения');
    }
}

function populateExecutionHistoryModelSelect(models) {
    const modelSelect = document.getElementById('executionHistoryModelSelect');
    modelSelect.innerHTML = '<option value="">-- Все модели --</option>';
    
    if (models && models.length > 0) {
        models.forEach(model => {
            const option = document.createElement('option');
            option.value = model.modelList.id;
            option.textContent = model.modelList.name;
            modelSelect.appendChild(option);
        });
    }
}

function populateExecutionHistorySectionSelect(modelId) {
    const sectionSelect = document.getElementById('executionHistorySectionSelect');
    sectionSelect.innerHTML = '<option value="">-- Все разделы --</option>';
    
    if (!modelId) {
        sectionSelect.disabled = true;
        return;
    }
    
    // Get unique sections from work results for this model
    const sections = new Map();
    currentExecutionHistoryWorkResults.forEach(workResult => {
        if (workResult.modelList?.id === parseInt(modelId) && workResult.sectionList?.id) {
            if (!sections.has(workResult.sectionList.id)) {
                sections.set(workResult.sectionList.id, workResult.sectionList.name || 'Неизвестный раздел');
            }
        }
    });
    
    if (sections.size > 0) {
        sections.forEach((name, id) => {
            const option = document.createElement('option');
            option.value = id;
            option.textContent = name;
            sectionSelect.appendChild(option);
        });
        sectionSelect.disabled = false;
    } else {
        sectionSelect.disabled = true;
    }
}

function filterExecutionHistoryTable() {
    const modelId = document.getElementById('executionHistoryModelSelect').value;
    const sectionId = document.getElementById('executionHistorySectionSelect').value;
    
    let filteredResults = currentExecutionHistoryWorkResults;
    
    if (modelId) {
        filteredResults = filteredResults.filter(wr => wr.modelList?.id === parseInt(modelId));
    }
    
    if (sectionId) {
        filteredResults = filteredResults.filter(wr => wr.sectionList?.id === parseInt(sectionId));
    }
    
    // Get selected week parameters for rendering
    const selectedYear = document.getElementById('executionHistoryYear').value;
    const selectedMonth = document.getElementById('executionHistoryMonth').value;
    const selectedWeek = document.getElementById('executionHistoryWeek').value;
    
    // Create a temporary order object with selected week parameters
    const orderForRendering = {
        ...currentExecutionHistoryOrder,
        year: parseInt(selectedYear),
        month: parseInt(selectedMonth),
        week: parseInt(selectedWeek)
    };
    
    renderExecutionHistoryTable(filteredResults, orderForRendering);
}

function closeExecutionHistoryModal() {
    document.getElementById('executionHistoryModal').close();
    currentExecutionHistoryOrderId = null;
    currentExecutionHistoryWorkResults = [];
    currentExecutionHistoryOrder = null;
    
    // Reset filters
    document.getElementById('executionHistoryModelSelect').innerHTML = '<option value="">-- Все модели --</option>';
    document.getElementById('executionHistorySectionSelect').innerHTML = '<option value="">-- Все разделы --</option>';
    document.getElementById('executionHistorySectionSelect').disabled = true;
}

async function loadExecutionHistoryForWeek() {
    const year = document.getElementById('executionHistoryYear').value;
    const month = document.getElementById('executionHistoryMonth').value;
    const week = document.getElementById('executionHistoryWeek').value;
    
    if (!year || !month || !week) {
        return;
    }
    
    try {
        const token = localStorage.getItem('token');
        const workResultsResponse = await fetch(
            `/api/work-results/order/${currentExecutionHistoryOrderId}?year=${year}&month=${month}&week=${week}`,
            {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }
        );
        
        if (!workResultsResponse.ok) {
            throw new Error('Failed to load work results');
        }
        
        const workResults = await workResultsResponse.json();
        console.log('Work results loaded for week:', { year, month, week }, workResults);
        currentExecutionHistoryWorkResults = workResults;
        
        // Render table with loaded data
        renderExecutionHistoryTable(workResults, currentExecutionHistoryOrder);
    } catch (error) {
        console.error('Error loading execution history for week:', error);
    }
}


function renderExecutionHistoryTable(workResults, order) {
    const tbody = document.getElementById('executionHistoryTableBody');
    tbody.innerHTML = '';
    
    if (workResults.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center">Нет данных о выполнении</td></tr>';
        return;
    }
    
    // Get selected week parameters
    const selectedYear = document.getElementById('executionHistoryYear').value;
    const selectedMonth = document.getElementById('executionHistoryMonth').value;
    const selectedWeek = document.getElementById('executionHistoryWeek').value;
    
    // Group work results by employee, model, section, and operation
    const groupedResults = new Map();
    
    workResults.forEach(workResult => {
        const employeeName = workResult.employee?.name && workResult.employee?.surname
            ? `${workResult.employee.name} ${workResult.employee.surname}`
            : 'Неизвестный работник';
        
        const key = `${employeeName}_${workResult.modelList?.id}_${workResult.sectionList?.id}_${workResult.techmap?.id}`;
        
        if (!groupedResults.has(key)) {
            groupedResults.set(key, {
                employeeName: employeeName,
                operationSerial: workResult.techmap?.serial || '-',
                modelListId: workResult.modelList?.id,
                sectionListId: workResult.sectionList?.id,
                techmapId: workResult.techmap?.id,
                completedQuantity: 0,
                createdAt: workResult.createdAt
            });
        }
        
        const group = groupedResults.get(key);
        group.completedQuantity += workResult.quantity;
    });
    
    // Render grouped results
    groupedResults.forEach((group, key) => {
        const tr = document.createElement('tr');
        
        // Get total quantity from order models
        const model = order.models?.find(m => m.modelList.id === group.modelListId);
        const totalQuantity = model ? model.count : 0;
        const remainingQuantity = Math.max(0, totalQuantity - group.completedQuantity);
        
        // Format date and time - use selected week parameters
        let dateTimeStr = `${selectedYear}-${selectedMonth}-${selectedWeek}`;
        if (group.createdAt) {
            const date = new Date(group.createdAt);
            const formattedDate = date.toLocaleDateString('ru-RU');
            const formattedTime = date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
            dateTimeStr = `${formattedDate} ${formattedTime}`;
        }
        
        tr.innerHTML = `
            <td>${group.employeeName}</td>
            <td>${group.operationSerial}</td>
            <td>${group.completedQuantity} / ${remainingQuantity}</td>
            <td>${dateTimeStr}</td>
        `;
        tbody.appendChild(tr);
    });
}

// Make execution history functions globally accessible
window.openExecutionHistoryModal = openExecutionHistoryModal;
window.closeExecutionHistoryModal = closeExecutionHistoryModal;
