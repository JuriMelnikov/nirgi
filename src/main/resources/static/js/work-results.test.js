// Явно импортируем тестовые функции из vitest
import { describe, test, expect } from 'vitest'
// Импортируем функции напрямую из экспортированного модуля
import { formatEuros, getWeekNumber, formatTime } from './work-results.js';

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