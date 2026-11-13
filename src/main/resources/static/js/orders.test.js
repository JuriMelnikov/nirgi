// @vitest-environment jsdom
import { describe, test, expect, beforeEach, afterEach, vi } from 'vitest'
import fs from 'fs'
import path from 'path'

function getOrdersHTMLInnerHTML() {
  const filePath = path.resolve(process.cwd(), 'src/main/resources/templates/orders.html')
  const html = fs.readFileSync(filePath, 'utf8')
  const bodyMatch = html.match(/<body[^>]*>([\s\S]*?)<\/body>/i)
  return bodyMatch ? bodyMatch[1] : ''
}

async function flushPromises() {
  await new Promise(resolve => setTimeout(resolve, 0))
  await new Promise(resolve => setTimeout(resolve, 0))
}

// Импортируем модуль один раз, чтобы на document остался ровно один
// обработчик DOMContentLoaded. Повторный import в beforeEach würde добавить
// ещё один обработчик, и диспатч события вызывал бы инициализацию N раз.
await import('./orders.js')

beforeEach(async () => {
  localStorage.clear()
  vi.stubGlobal('fetch', vi.fn())
  vi.stubGlobal('alert', vi.fn())
  HTMLDialogElement.prototype.showModal = vi.fn()
  document.body.innerHTML = getOrdersHTMLInnerHTML()

  // Начальные вызовы при инициализации:
  // GET /api/model-list (loadModelLists), GET /api/orders/search-with-transferred (loadOrders)
  fetch
    .mockResolvedValueOnce({ ok: true, json: async () => [] }) // loadModelLists
    .mockResolvedValueOnce({ ok: true, json: async () => [] }) // loadOrders

  document.dispatchEvent(new Event('DOMContentLoaded'))
  await flushPromises()
})

afterEach(() => {
  vi.restoreAllMocks()
})

function orderResponse(order) {
  return { ok: true, json: async () => order }
}

const baseOrder = {
  id: 1,
  name: 'Тестовый заказ',
  year: 2026,
  month: 11,
  week: 5,
  models: []
}

describe('orders.js - модальное окно переноса заказа (createDublOrderModal)', () => {
  test('при первом открытии целевая неделя = неделя заказа + 1 (не сохранено ещё ничего)', async () => {
    fetch.mockResolvedValueOnce(orderResponse(baseOrder)) // GET /api/orders/1
    fetch.mockResolvedValueOnce({ ok: true, json: async () => {} }) // completed work

    window.openDublModal(1)
    await flushPromises()

    expect(document.getElementById('dublTargetYear').value).toBe('2026')
    expect(document.getElementById('dublTargetMonth').value).toBe('11')
    expect(document.getElementById('dublTargetWeek').value).toBe('6')
  })

  test('при неделе заказа 53 целевая неделя переходит на 1 следующего месяца/года', async () => {
    const orderYearEnd = { ...baseOrder, id: 2, year: 2026, month: 12, week: 53 }

    fetch.mockResolvedValueOnce(orderResponse(orderYearEnd))
    fetch.mockResolvedValueOnce({ ok: true, json: async () => {} })

    window.openDublModal(2)
    await flushPromises()

    expect(document.getElementById('dublTargetYear').value).toBe('2027')
    expect(document.getElementById('dublTargetMonth').value).toBe('1')
    expect(document.getElementById('dublTargetWeek').value).toBe('1')
  })

  test('запомненные неделя, месяц и год используются при повторном открытии, а не сбрасываются на неделю заказа', async () => {
    // Первое открытие заказа с неделей 5 — целевая неделя по умолчанию 6
    fetch.mockResolvedValueOnce(orderResponse({ ...baseOrder, id: 1, week: 5 }))
    fetch.mockResolvedValueOnce({ ok: true, json: async () => {} })
    window.openDublModal(1)
    await flushPromises()

    expect(document.getElementById('dublTargetWeek').value).toBe('6')

    // Пользователь выбирает в селекторах другие значения
    const yearSelect = document.getElementById('dublTargetYear')
    const monthSelect = document.getElementById('dublTargetMonth')
    const weekSelect = document.getElementById('dublTargetWeek')

    yearSelect.value = '2027'
    monthSelect.value = '3'
    weekSelect.value = '10'
    yearSelect.dispatchEvent(new Event('change'))
    monthSelect.dispatchEvent(new Event('change'))
    weekSelect.dispatchEvent(new Event('change'))

    // Значения запомнены в localStorage
    expect(JSON.parse(localStorage.getItem('dublTargetDateSelection'))).toEqual({
      year: '2027',
      month: '3',
      week: '10'
    })

    // Повторное открытие модального окна для другого заказа (неделя 3, месяц 11, год 2026)
    // должно использовать запомненные значения, а не сбрасываться на 4-ю неделю
    fetch.mockResolvedValueOnce(orderResponse({ ...baseOrder, id: 3, week: 3 }))
    fetch.mockResolvedValueOnce({ ok: true, json: async () => {} })
    window.openDublModal(3)
    await flushPromises()

    expect(document.getElementById('dublTargetYear').value).toBe('2027')
    expect(document.getElementById('dublTargetMonth').value).toBe('3')
    expect(document.getElementById('dublTargetWeek').value).toBe('10')
  })
})
