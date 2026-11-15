// Явно импортируем тестовые функции из vitest
import { describe, test, expect, beforeEach, afterEach, it, vi } from 'vitest'
// Импортируем функции напрямую из экспортированного модуля
import { formatEuros, getWeekNumber, formatTime, calculateAvailableQuantity } from './work-results.js';

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
  let transferredQty = 0;

  beforeEach(() => {
    completedQty = 0;
    transferredQty = 0;
    fetchMock = vi.spyOn(global, 'fetch').mockImplementation(async (url) => {
      const u = String(url);
      if (u.startsWith('/api/orders/')) {
        return { ok: true, json: async () => ({ models: [{ modelList: { id: 1 }, count: 10 }] }) };
      }
      if (u.includes('/total-completed-quantity')) {
        return { ok: true, json: async () => completedQty };
      }
      if (u.includes('/transferred-quantity')) {
        return { ok: true, json: async () => transferredQty };
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

  it('обычный заказ: выполнено считается по всем неделям, перенесённое вычитается', async () => {
    setupDom({ isTransferred: false });
    completedQty = 2; // 1 экземпляр в неделе 11 + 1 в неделе 33
    transferredQty = 1;

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
    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringContaining('/transferred-quantity'),
      expect.anything()
    );

    // available = 10 всего - 2 выполнено (все недели) - 1 перенесено = 7
    const quantitySelect = document.getElementById('quantitySelect');
    expect(quantitySelect.options.length).toBe(8); // "0" + 1..7
  });

  it('перенесённый заказ: выполнено по всем неделям, перенесённое не запрашивается', async () => {
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
    transferredQty = 0;

    await calculateAvailableQuantity();

    const quantitySelect = document.getElementById('quantitySelect');
    expect(quantitySelect.options.length).toBe(1); // только "0"
    expect(quantitySelect.disabled).toBe(true);
  });
});