const filterYear = document.querySelector('#filterYear');
const filterMonth = document.querySelector('#filterMonth');
const loadSalaryBtn = document.querySelector('#loadSalaryBtn');
const salaryTableBody = document.querySelector('#salaryTableBody');
const emptyState = document.querySelector('#emptyState');
const salaryTable = document.querySelector('#salaryTable');
const salaryTableFoot = document.querySelector('#salaryTableFoot');
const totalEarningsFooter = document.querySelector('#totalEarningsFooter');

const currentYear = new Date().getFullYear();

function populateYearSelect() {
    const startYear = currentYear - 5;
    const endYear = currentYear + 1;

    for (let year = startYear; year <= endYear; year++) {
        const option = document.createElement('option');
        option.value = year;
        option.textContent = year;
        if (year === currentYear) {
            option.selected = true;
        }
        filterYear.appendChild(option);
    }
}

function formatNumber(num) {
    if (num === null || num === undefined) {
        return '0.00';
    }
    return Number(num).toFixed(2);
}

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

function renderSalaryTable(records) {
    salaryTableBody.innerHTML = '';

    if (records.length === 0) {
        emptyState.classList.remove('hidden');
        salaryTable.classList.add('hidden');
        salaryTableFoot.classList.add('hidden');
        return;
    }

    emptyState.classList.add('hidden');
    salaryTable.classList.remove('hidden');
    salaryTableFoot.classList.remove('hidden');

    const totalEarnings = records.reduce((sum, record) => sum + (record.totalEarnings || 0), 0);
    totalEarningsFooter.textContent = `€${formatNumber(totalEarnings)}`;

    records.forEach(record => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${record.employeeName || '-'}</td>
            <td>${record.employeeSurname || '-'}</td>
            <td>${formatTime(record.totalTime)}</td>
            <td>€${formatNumber(record.totalEarnings)}</td>
        `;
        salaryTableBody.appendChild(row);
    });
}

async function loadSalaryData() {
    const year = parseInt(filterYear.value);
    const month = parseInt(filterMonth.value);

    if (!year || !month) {
        alert('Пожалуйста, выберите год и месяц');
        return;
    }

    try {
        loadSalaryBtn.disabled = true;
        loadSalaryBtn.innerHTML = `
            <span class="loading loading-spinner"></span>
            Загрузка...
        `;

        const response = await authFetch(`/api/salary?year=${year}&month=${month}`);

        if (!response.ok) {
            throw new Error('Ошибка при загрузке данных');
        }

        const records = await response.json();
        renderSalaryTable(records);
    } catch (error) {
        console.error('Error loading salary data:', error);
        alert('Ошибка при загрузке данных о заработной плате');
    } finally {
        loadSalaryBtn.disabled = false;
        loadSalaryBtn.innerHTML = `
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
            </svg>
            Загрузить данные
        `;
    }
}

populateYearSelect();
loadSalaryBtn.addEventListener('click', loadSalaryData);
