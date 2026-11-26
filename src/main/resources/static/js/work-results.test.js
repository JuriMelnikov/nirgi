// Явно импортируем тестовые функции из vitest
import { describe, test, expect, beforeEach, afterEach, it, vi } from 'vitest'
// Импортируем функции напрямую из экспортированного модуля
import { formatEuros, getWeekNumber, formatTime, calculateAvailableQuantity, addWorkResult } from './work-results.js';

describe('formatEuros', () => {
  test('форматирует положительные числа правильно', () => {
    expect(formatEuros(10)).toBe('10.000');
    expect(formatEuros(10.5)).toBe('10.500');
    expect(formatEuros(10.555)).toBe('10.555');
    expect(formatEuros(10.5555)).toBe('10.556'); // округление
  });

  test('возвращает 0.000 для неверных входов', () => {
    expect(formatEuros(NaN)).toBe('0.000');
    expect(formatEuros(undefined)).toBe('0.000');
    expect(formatEuros(null)).toBe('0.000');
  });

  test('работает с отрицательными числами', () => {
    expect(formatEuros(-5)).toBe('-5.000');
  });
});

describe('getWeekNumber', () => {
  test('возвращает правильный номер недели для известных дат', () => {
    // 3 января 2023 года был вторник и это была неделя 1 2023 года (ISO 8601)
    expect(getWeekNumber(new Date(2023, 0, 3))).toBe(1);
    // 1 января 2024 года был понедельник, неделя 1
    expect(getWeekNumber(new Date(2024, 0, 1))).toBe(1);
    // 31 декабря 2023 года был воскресенье, но это была неделя 52 2023 года
    expect(getWeekNumber(new Date(2023, 11, 31))).toBe(52);
    // 1 июля 2023 года - примерно середина года
    expect(getWeekNumber(new Date(2023, 6, 1))).toBe(26);
  });
});

describe('formatTime', () => {
  test('форматирует ноль секунд как 00:00:00', () => {
    expect(formatTime(0)).toBe('00:00:00');
    expect(formatTime(null)).toBe('00:00:00');
    expect(formatTime(undefined)).toBe('00:00:00');
  });

  test('форматирует секунды в часы:минуты:секунды', () => {
    expect(formatTime(3661)).toBe('01:01:01'); // 1 час, 1 минута, 1 секунда
    expect(formatTime(3600)).toBe('01:00:00');
    expect(formatTime(60)).toBe('00:01:00');
    expect(formatTime(1)).toBe('00:00:01');
  });

  test('добавляет ведущие нули', () => {
    expect(formatTime(3601)).toBe('01:00:01');
    expect(formatTime(61)).toBe('00:01:01');
    expect(formatTime(1)).toBe('00:00:01');
  });
});

describe('calculateAvailableQuantity', () => {
  let fetchMock;
  let completedQty = 0;

  beforeEach(() => {
    completedQty = 0;
    fetchMock = vi.spyOn(global, 'fetch').mockImplementation(async (url) => {
      const u = String(url);
      if (u.startsWith('/api/orders/')) {
        return { ok: true, json: async () => ({ models: [{ modelList: { id: 1 }, count: 10 }] }) };
      }
      if (u.includes('/total-completed-quantity')) {
        return { ok: true, json: async () => completedQty };
      }
      return { ok: false, status: 404, json: async () => ({}) };
    });
  });

  afterEach(() => {
    fetchMock.mockRestore();
    document.body.innerHTML = '';
  });

  function setupDom({ isTransferred = false, year = 2026, month = 3, week = 11 } = {}) {
    document.body.innerHTML = `
      <select id="employeeSelect" class="hidden"></select>
      <input id="employeeIdInput" value="1">
      <select id="orderSelect">
        <option value="100" data-original-order-id="${isTransferred ? '50' : ''}">Order</option>
      </select>
      <select id="modelSelect"><option value="1">Model</option></select>
      <select id="sectionSelect"><option value="2">Section</option></select>
      <select id="operationSelect"><option value="3">Op</option></select>
      <select id="yearSelect"><option value="${year}" selected>${year}</option></select>
      <select id="monthSelect"><option value="${month}" selected>${month}</option></select>
      <select id="weekSelect"><option value="${week}" selected>${week}</option></select>
      <select id="quantitySelect"></select>
      <div id="techmapInfo"></div>
      <button id="addButton"></button>
    `;
    for (const id of ['orderSelect', 'modelSelect', 'sectionSelect', 'operationSelect', 'yearSelect', 'monthSelect', 'weekSelect']) {
      document.getElementById(id).selectedIndex = 0;
    }
  }

  it('обычный заказ: выполнено считается по всем неделям, доступно = всего − выполнено', async () => {
    setupDom({ isTransferred: false });
    completedQty = 2; // 1 экземпляр в неделе 11 + 1 в неделе 33

    await calculateAvailableQuantity();

    // Запрос по всем неделям, а не неделя-фильтрованный
    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringContaining('/total-completed-quantity'),
      expect.anything()
    );
    expect(fetchMock).not.toHaveBeenCalledWith(
      expect.stringContaining('/completed-quantity'),
      expect.anything()
    );
    // transferred-quantity не запрашивается
    expect(fetchMock).not.toHaveBeenCalledWith(
      expect.stringContaining('/transferred-quantity'),
      expect.anything()
    );

    // available = 10 всего - 2 выполнено (все недели) = 8
    const quantitySelect = document.getElementById('quantitySelect');
    expect(quantitySelect.options.length).toBe(9); // "0" + 1..8
  });

  it('перенесённый заказ: выполнено по всем неделям, доступно = всего − выполнено', async () => {
    setupDom({ isTransferred: true });
    completedQty = 4;

    await calculateAvailableQuantity();

    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringContaining('/total-completed-quantity'),
      expect.anything()
    );
    expect(fetchMock).not.toHaveBeenCalledWith(
      expect.stringContaining('/completed-quantity'),
      expect.anything()
    );
    expect(fetchMock).not.toHaveBeenCalledWith(
      expect.stringContaining('/transferred-quantity'),
      expect.anything()
    );

    // available = 10 всего - 4 выполнено = 6
    const quantitySelect = document.getElementById('quantitySelect');
    expect(quantitySelect.options.length).toBe(7); // "0" + 1..6
  });

  it('когда выполнено больше или равно всего, доступное количество равно 0', async () => {
    setupDom({ isTransferred: false });
    completedQty = 10;

    await calculateAvailableQuantity();

    const quantitySelect = document.getElementById('quantitySelect');
    expect(quantitySelect.options.length).toBe(1); // только "0"
    expect(quantitySelect.disabled).toBe(true);
  });
});

describe('addWorkResult', () => {
  let fetchMock;
  let completedQty = 0;
  let postFails = false;

  beforeEach(() => {
    completedQty = 0;
    postFails = false;
    fetchMock = vi.spyOn(global, 'fetch').mockImplementation(async (url, options) => {
      const u = String(url);
      const method = (options && options.method) || 'GET';
      if (u.startsWith('/api/orders/')) {
        return { ok: true, json: async () => ({ models: [{ modelList: { id: 1 }, count: 10 }] }) };
      }
      if (u.includes('/total-completed-quantity')) {
        return { ok: true, json: async () => completedQty };
      }
      if (u === '/api/work-results' && method === 'POST') {
        if (postFails) {
          return { ok: false, json: async () => ({ message: 'Серверная ошибка' }) };
        }
        return { ok: true, json: async () => ({ id: 999 }) };
      }
      return { ok: false, status: 404, json: async () => ({}) };
    });
    vi.stubGlobal('localStorage', {
      getItem: () => 'test-token',
    });
    vi.stubGlobal('alert', vi.fn());
  });

  afterEach(() => {
    fetchMock.mockRestore();
    vi.restoreAllMocks();
    document.body.innerHTML = '';
  });

  function setupDom({ isTransferred = false, quantity = 3, year = 2026, month = 3, week = 11 } = {}) {
    document.body.innerHTML = `
      <label><input type="radio" name="period" value="month" checked> Месяц</label>
      <select id="employeeSelect" class="hidden"></select>
      <input id="employeeIdInput" value="1">
      <select id="orderSelect">
        <option value="100" data-original-order-id="${isTransferred ? '50' : ''}">Order</option>
      </select>
      <select id="modelSelect"><option value="1">Model</option></select>
      <select id="sectionSelect"><option value="2">Section</option></select>
      <select id="operationSelect"><option value="3">Op</option></select>
      <select id="yearSelect"><option value="${year}" selected>${year}</option></select>
      <select id="monthSelect"><option value="${month}" selected>${month}</option></select>
      <select id="weekSelect"><option value="${week}" selected>${week}</option></select>
      <select id="quantitySelect"><option value="${quantity}" selected>${quantity}</option></select>
      <div id="techmapInfo"></div>
      <button id="addButton"></button>
    `;
    for (const id of ['orderSelect', 'modelSelect', 'sectionSelect', 'operationSelect', 'yearSelect', 'monthSelect', 'weekSelect', 'quantitySelect']) {
      document.getElementById(id).selectedIndex = 0;
    }
  }

  it('успешное добавление: POST с правильным телом, алерт не вызывается', async () => {
    setupDom({ quantity: 3 });
    completedQty = 2; // доступно = 10 всего − 2 выполнено = 8, 3 <= 8

    await addWorkResult();

    const postCalls = fetchMock.mock.calls.filter(([url]) => String(url) === '/api/work-results');
    expect(postCalls.length).toBe(1);
    const [, options] = postCalls[0];
    expect(options.method).toBe('POST');
    expect(options.headers.Authorization).toBe('Bearer test-token');
    expect(JSON.parse(options.body)).toEqual({
      employeeId: 1,
      orderId: 100,
      modelListId: 1,
      sectionListId: 2,
      techmapId: 3,
      quantity: 3,
      year: 2026,
      month: 3,
      week: 11,
    });
    expect(alert).not.toHaveBeenCalled();
  });

  it('перенесённый заказ: в запросе к заказу и в теле POST используется оригинальный orderId', async () => {
    setupDom({ isTransferred: true, quantity: 3 });
    completedQty = 4; // доступно = 10 − 4 = 6, 3 <= 6

    await addWorkResult();

    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringContaining('/api/orders/50'),
      expect.anything()
    );
    const postCalls = fetchMock.mock.calls.filter(([url]) => String(url) === '/api/work-results');
    expect(postCalls.length).toBe(1);
    expect(JSON.parse(postCalls[0][1].body).orderId).toBe(50);
    expect(alert).not.toHaveBeenCalled();
  });

  it('количество больше доступного: алерт, POST не выполняется', async () => {
    setupDom({ quantity: 9 });
    completedQty = 2; // доступно = 10 − 2 = 8, 9 > 8

    await addWorkResult();

    const postCalls = fetchMock.mock.calls.filter(([url]) => String(url) === '/api/work-results');
    expect(postCalls.length).toBe(0);
    expect(alert).toHaveBeenCalledWith(
      expect.stringContaining('Невозможно добавить 9 изделий')
    );
  });

  it('не заполнены все поля: алерт, запросов нет', async () => {
    document.body.innerHTML = `
      <select id="employeeSelect" class="hidden"></select>
      <input id="employeeIdInput" value="1">
      <select id="orderSelect"><option value="100">Order</option></select>
      <select id="modelSelect"><option value="1">Model</option></select>
      <select id="sectionSelect"><option value="2">Section</option></select>
      <select id="operationSelect"><option value="3">Op</option></select>
      <select id="yearSelect"><option value="2026" selected>2026</option></select>
      <select id="monthSelect"><option value="3" selected>3</option></select>
      <select id="weekSelect"><option value="11" selected>11</option></select>
      <select id="quantitySelect"></select>
    `;

    await addWorkResult();

    expect(fetchMock).not.toHaveBeenCalled();
    expect(alert).toHaveBeenCalledWith('Пожалуйста, заполните все поля');
  });

  it('ошибка сервера при добавлении: алерт об ошибке', async () => {
    setupDom({ quantity: 3 });
    completedQty = 2;
    postFails = true;

    await addWorkResult();

    expect(alert).toHaveBeenCalledWith(
      expect.stringContaining('Ошибка при добавлении результата работы')
    );
  });
});