// @vitest-environment jsdom
import { describe, test, expect, beforeEach, afterEach, vi } from 'vitest'
import fs from 'fs'
import path from 'path'

// Helper to get the innerHTML of models.html
function getModelsHTMLInnerHTML() {
  const filePath = path.resolve(process.cwd(), 'src/main/resources/templates/models.html')
  const html = fs.readFileSync(filePath, 'utf8')
  // Extract content between <body> and </body>
  const bodyMatch = html.match(/<body[^>]*>([\s\S]*?)<\/body>/i)
  return bodyMatch ? bodyMatch[1] : ''
}

let domContentLoadedHandlers = []

// Test-specific response globals
let testModelListResponse = []
let testSectionListResponse = []
let testTechmapsResponse = []
let testPostModelResponse = { id: undefined }
let testPostSectionResponse = { id: undefined }
let testGetModelByIdResponse = { id: undefined }
let testPutModelResponse = { id: undefined }

async function setupModule() {
  // Clean up any leftover DOMContentLoaded listeners from previous runs
  domContentLoadedHandlers.forEach(h => document.removeEventListener('DOMContentLoaded', h))
  domContentLoadedHandlers = []

  // Clear localStorage and DOM
  localStorage.clear()
  document.body.innerHTML = ''

  // Set up global mocks
  global.requireAuth = vi.fn(() => true)

  // Use mockImplementation so responses are chosen by URL pattern, not call count
  global.authFetch = vi.fn(async (url, options) => {
    const u = String(url)
    const method = (options && options.method) || 'GET'

    if (u === '/api/model-list' && method === 'GET') {
      return Promise.resolve({ ok: true, json: async () => testModelListResponse })
    }
    if (u === '/api/section-lists' && method === 'GET') {
      return Promise.resolve({ ok: true, json: async () => testSectionListResponse })
    }
    if (u === '/api/techmaps' && method === 'GET') {
      return Promise.resolve({ ok: true, json: async () => testTechmapsResponse })
    }
    if (u === '/api/model-list' && method === 'POST') {
      return Promise.resolve({ ok: true, json: async () => testPostModelResponse })
    }
    if (u === '/api/section-lists' && method === 'POST') {
      return Promise.resolve({ ok: true, json: async () => testPostSectionResponse })
    }
    if (u.startsWith('/api/model-list/') && method === 'GET') {
      return Promise.resolve({ ok: true, json: async () => testGetModelByIdResponse })
    }
    if (u.startsWith('/api/model-list/') && method === 'PUT') {
      return Promise.resolve({ ok: true, json: async () => testPutModelResponse })
    }

    // Default fallback for any other call
    return Promise.resolve({ ok: true, json: async () => [] })
  })

  // Track DOMContentLoaded listeners added by the module
  const origAddEventListener = document.addEventListener.bind(document)
  document.addEventListener = ((type, listener, options) => {
    if (type === 'DOMContentLoaded') {
      domContentLoadedHandlers.push(listener)
    }
    return origAddEventListener(type, listener, options)
  })

  // Set DOM to the models.html body content
  document.body.innerHTML = getModelsHTMLInnerHTML()

  // Import the module under test (this will set up event listeners)
  await import('./models.js')

  // Restore original addEventListener
  document.addEventListener = origAddEventListener

  // Simulate DOMContentLoaded so that the initialization code runs
  document.dispatchEvent(new Event('DOMContentLoaded'))

  // Wait for async initialization (loadModelLists, loadSectionLists, loadTechmaps) to complete
  await new Promise(resolve => setTimeout(resolve, 50))
}

beforeEach(async () => {
  // Reset test-specific response globals to defaults
  testModelListResponse = []
  testSectionListResponse = []
  testTechmapsResponse = []
  testPostModelResponse = { id: undefined }
  testPostSectionResponse = { id: undefined }
  testGetModelByIdResponse = { id: undefined }
  testPutModelResponse = { id: undefined }

  vi.restoreAllMocks()
  vi.resetModules()
})

afterEach(() => {
  vi.restoreAllMocks()
  // Clean up tracked listeners
  domContentLoadedHandlers.forEach(h => document.removeEventListener('DOMContentLoaded', h))
  domContentLoadedHandlers = []
})

describe('models.js - model list form submission', () => {
  test('sets selectedTechmapModelId in localStorage after successful model save', async () => {
    // Configure responses for this test BEFORE module initialization
    testModelListResponse = [{ id: 1, name: 'Model 1' }]
    testSectionListResponse = [{ id: 1, name: 'Section 1' }]
    testTechmapsResponse = []
    testPostModelResponse = { id: 123, name: 'Test Model' }

    await setupModule()

    // Set form field value before submitting
    document.getElementById('modelListName').value = 'Test Model'

    // Get the form and submit it
    const form = document.getElementById('addModelListForm')
    const submitEvent = new Event('submit', { cancelable: true })
    form.dispatchEvent(submitEvent)

    // Wait for all async processing (including post-submit loadModelLists/loadTechmaps)
    await new Promise(resolve => setTimeout(resolve, 50))

    // Verify authFetch was called with correct arguments for POST (adding new model)
    expect(authFetch).toHaveBeenCalledWith(
      '/api/model-list',
      expect.objectContaining({
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: 'Test Model' })
      })
    )

    // Verify localStorage was set
    expect(localStorage.getItem('selectedTechmapModelId')).toBe('123')
  })

  test('sets selectedTechmapModelId when editing an existing model', async () => {
    // Configure responses for this test BEFORE module initialization
    testModelListResponse = [{ id: 456, name: 'Existing Model' }, { id: 789, name: 'Another Model' }]
    testSectionListResponse = [{ id: 1, name: 'Section 1' }]
    testTechmapsResponse = []
    testGetModelByIdResponse = { id: 456, name: 'Existing Model' }
    testPutModelResponse = { id: 456, name: 'Updated Model' }

    await setupModule()

    // Simulate user selecting the model from dropdown
    const modelSelect = document.getElementById('modelListSelect')
    // Find the option with value "456" and select it by index
    let optionIndex = -1
    for (let i = 0; i < modelSelect.options.length; i++) {
      if (modelSelect.options[i].value === '456') {
        optionIndex = i
        break
      }
    }
    expect(optionIndex).toBeGreaterThanOrEqual(0, 'option with value 456 should exist in modelListSelect')
    if (optionIndex >= 0) {
      modelSelect.selectedIndex = optionIndex
    }
    modelSelect.dispatchEvent(new Event('change'))

    // Wait for the GET request to finish and populate the form
    await new Promise(resolve => setTimeout(resolve, 50))

    // Change the name to updated value before submitting
    document.getElementById('modelListName').value = 'Updated Model'

    // Submit the form
    const form = document.getElementById('addModelListForm')
    const submitEvent = new Event('submit', { cancelable: true })
    form.dispatchEvent(submitEvent)

    await new Promise(resolve => setTimeout(resolve, 50))

    // Verify the PUT call was made with the updated data
    expect(authFetch).toHaveBeenCalledWith(
      '/api/model-list/456',
      expect.objectContaining({
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: 'Updated Model' })
      })
    )

    // Verify localStorage was set to the model id
    expect(localStorage.getItem('selectedTechmapModelId')).toBe('456')
  })
})

describe('models.js - section list form submission', () => {
  test('sets selectedTechmapSectionId in localStorage after successful section save', async () => {
    // Configure responses for this test BEFORE module initialization
    testModelListResponse = [{ id: 1, name: 'Model 1' }]
    testSectionListResponse = [{ id: 1, name: 'Section 1' }]
    testTechmapsResponse = []
    testPostSectionResponse = { id: 789, name: 'Test Section' }

    await setupModule()

    // Set form field value before submitting
    document.getElementById('sectionListName').value = 'Test Section'

    // Get the form and submit it
    const form = document.getElementById('addSectionListForm')
    const submitEvent = new Event('submit', { cancelable: true })
    form.dispatchEvent(submitEvent)

    // Wait for all async processing (including post-submit loadSectionLists/loadTechmaps)
    await new Promise(resolve => setTimeout(resolve, 50))

    expect(authFetch).toHaveBeenCalledWith(
      '/api/section-lists',
      expect.objectContaining({
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: 'Test Section' })
      })
    )

    expect(localStorage.getItem('selectedTechmapSectionId')).toBe('789')
  })
})
