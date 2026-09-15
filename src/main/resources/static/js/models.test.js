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

beforeEach(async () => {
  // Clear localStorage
  localStorage.clear()

  // Set up global mocks for auth-helper functions
  global.requireAuth = vi.fn(() => true)
  global.authFetch = vi.fn()

  // Set DOM to the models.html body content
  document.body.innerHTML = getModelsHTMLInnerHTML()

  // Import the module under test (this will set up event listeners)
  await import('./models.js')

  // Simulate DOMContentLoaded so that the initialization code runs
  document.dispatchEvent(new Event('DOMContentLoaded'))
})

afterEach(() => {
  vi.restoreAllMocks()
  // Reset the module registry to avoid state leakage
  vi.resetModules()
})

describe('models.js - model list form submission', () => {
  test('sets selectedTechmapModelId in localStorage after successful model save', async () => {
    // Mock authFetch for the initialization calls and the submit call
    // Order of calls during initialization:
    // 1. GET /api/model-list (loadModelLists)
    // 2. GET /api/section-lists (loadSectionLists)
    // 3. GET /api/techmaps (loadTechmaps)
    // Then, when we submit the form (adding a new model):
    // 4. POST /api/model-list

    const mockModelListResponse = { id: 1, name: 'Model 1' } // dummy, not really used
    const mockSectionListResponse = { id: 1, name: 'Section 1' }
    const mockTechmapsResponse = []

    const mockNewModelResponse = { id: 123, name: 'Test Model' }

    authFetch
      .mockResolvedValueOnce({ ok: true, json: async () => mockModelListResponse }) // loadModelLists
      .mockResolvedValueOnce({ ok: true, json: async () => mockSectionListResponse }) // loadSectionLists
      .mockResolvedValueOnce({ ok: true, json: async () => mockTechmapsResponse }) // loadTechmaps
      .mockResolvedValueOnce({ ok: true, json: async () => mockNewModelResponse }) // submit form

    // Get the form and submit it
    const form = document.getElementById('addModelListForm')
    const submitEvent = new Event('submit', { cancelable: true })
    form.dispatchEvent(submitEvent)

    // Wait for next tick to allow async processing
    await Promise.resolve()

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
    // Mock authFetch for initialization and the editing flow
    // Initialization calls:
    // 1. GET /api/model-list
    // 2. GET /api/section-lists
    // 3. GET /api/techmaps
    // Then, when we select a model from the dropdown (to edit):
    // 4. GET /api/model-list/456 (to load model data)
    // Then, when we submit the form (updating the model):
    // 5. PUT /api/model-list/456

    const mockModelListResponse = [{ id: 456, name: 'Existing Model' }, { id: 789, name: 'Another Model' }]
    const mockSectionListResponse = [{ id: 1, name: 'Section 1' }]
    const mockTechmapsResponse = []
    const mockModelData = { id: 456, name: 'Existing Model' }
    const updatedModelData = { id: 456, name: 'Updated Model' }

    authFetch
      .mockResolvedValueOnce({ ok: true, json: async () => mockModelListResponse }) // loadModelLists
      .mockResolvedValueOnce({ ok: true, json: async () => mockSectionListResponse }) // loadSectionLists
      .mockResolvedValueOnce({ ok: true, json: async () => mockTechmapsResponse }) // loadTechmaps
      .mockResolvedValueOnce({ ok: true, json: async () => mockModelData }) // GET for dropdown change
      .mockResolvedValueOnce({ ok: true, json: async () => updatedModelData }) // PUT for form submit

    // Simulate user selecting the model from dropdown
    const modelSelect = document.getElementById('modelListSelect')
    modelSelect.value = '456'
    modelSelect.dispatchEvent(new Event('change'))

    // Wait for the GET request to finish and populate the form
    await Promise.resolve()

    // Now the form should be populated with the model name and editingModelListId set
    // Submit the form
    const form = document.getElementById('addModelListForm')
    const submitEvent = new Event('submit', { cancelable: true })
    form.dispatchEvent(submitEvent)

    await Promise.resolve()

    // Verify the PUT call was made with the updated data
    expect(authFetch).toHaveBeenNthCalledWith(
      5,
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
    // Mock authFetch for initialization and the submit call
    // Initialization calls:
    // 1. GET /api/model-list
    // 2. GET /api/section-lists
    // 3. GET /api/techmaps
    // Then, when we submit the form (adding a new section):
    // 4. POST /api/section-lists

    const mockModelListResponse = [{ id: 1, name: 'Model 1' }]
    const mockSectionListResponse = [{ id: 1, name: 'Section 1' }]
    const mockTechmapsResponse = []
    const mockNewSectionResponse = { id: 789, name: 'Test Section' }

    authFetch
      .mockResolvedValueOnce({ ok: true, json: async () => mockModelListResponse }) // loadModelLists
      .mockResolvedValueOnce({ ok: true, json: async () => mockSectionListResponse }) // loadSectionLists
      .mockResolvedValueOnce({ ok: true, json: async () => mockTechmapsResponse }) // loadTechmaps
      .mockResolvedValueOnce({ ok: true, json: async () => mockNewSectionResponse }) // submit form

    // Get the form and submit it
    const form = document.getElementById('addSectionListForm')
    const submitEvent = new Event('submit', { cancelable: true })
    form.dispatchEvent(submitEvent)

    await Promise.resolve()

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