document.addEventListener('DOMContentLoaded', function() {
    // Check authentication
    if (!requireAuth()) {
        return;
    }

    // Model List Form
    const modelListForm = document.getElementById('addModelListForm');
    const modelListSelect = document.getElementById('modelListSelect');
    let editingModelListId = null;

    // Section List Form
    const sectionListForm = document.getElementById('addSectionListForm');
    const sectionListSelect = document.getElementById('sectionListSelect');
    let editingSectionListId = null;

    // Techmap Form
    const techmapForm = document.getElementById('addTechmapForm');
    let editingTechmapId = null;
    let allTechmaps = [];

    // Price is stored in tenths of cents (for 3 decimal places), but edited in euros
    function eurosToTenthsOfCents(value) {
        const euros = parseFloat(String(value).replace(',', '.'));
        return isNaN(euros) ? '' : String(Math.round(euros * 1000));
    }

    function tenthsOfCentsToEuros(value) {
        const tenthsOfCents = parseFloat(value);
        return isNaN(tenthsOfCents) ? '' : (tenthsOfCents / 1000).toFixed(3);
    }

    function formatPrice(value) {
        const euros = tenthsOfCentsToEuros(value);
        return euros === '' ? (value == null ? '' : value) : euros;
    }

    // Load existing data on page load
    loadModelLists();
    loadSectionLists();
    loadTechmaps();

    // Collapsible form functionality
    function toggleFormVisibility(formContentId, chevronId) {
        const formContent = document.getElementById(formContentId);
        const chevron = document.getElementById(chevronId);
        
        if (formContent.classList.contains('hidden')) {
            formContent.classList.remove('hidden');
            chevron.classList.add('rotate-180');
        } else {
            formContent.classList.add('hidden');
            chevron.classList.remove('rotate-180');
        }
    }

    // Model List Form Header click
    document.getElementById('modelListFormHeader').addEventListener('click', function() {
        toggleFormVisibility('modelListFormContent', 'modelListChevron');
    });

    // Section List Form Header click
    document.getElementById('sectionListFormHeader').addEventListener('click', function() {
        toggleFormVisibility('sectionListFormContent', 'sectionListChevron');
    });

    // Hide Model List Form when any button is clicked
    const modelListButtons = document.querySelectorAll('#addModelListForm button');
    modelListButtons.forEach(button => {
        button.addEventListener('click', function() {
            setTimeout(() => {
                const formContent = document.getElementById('modelListFormContent');
                const chevron = document.getElementById('modelListChevron');
                formContent.classList.add('hidden');
                chevron.classList.remove('rotate-180');
            }, 100);
        });
    });

    // Hide Section List Form when any button is clicked
    const sectionListButtons = document.querySelectorAll('#addSectionListForm button');
    sectionListButtons.forEach(button => {
        button.addEventListener('click', function() {
            setTimeout(() => {
                const formContent = document.getElementById('sectionListFormContent');
                const chevron = document.getElementById('sectionListChevron');
                formContent.classList.add('hidden');
                chevron.classList.remove('rotate-180');
            }, 100);
        });
    });

    // Model List Cancel Button
    document.getElementById('modelListCancelButton').addEventListener('click', function() {
        resetModelListEditMode();
        modelListForm.reset();
    });

    // Model List Delete Button
    document.getElementById('modelListDeleteButton').addEventListener('click', async function() {
        if (editingModelListId && confirm('Вы уверены, что хотите удалить эту модель?')) {
            try {
                const token = localStorage.getItem('token');
                const userRoles = localStorage.getItem('userRoles');
                console.log('Current user roles:', userRoles);
                const response = await fetch(`/api/model-list/${editingModelListId}`, {
                    method: 'DELETE',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                console.log('Delete response status:', response.status);
                if (response.ok) {
                    alert('Модель успешно удалена');
                    resetModelListEditMode();
                    modelListForm.reset();
                    loadModelLists();
                    loadTechmaps();
                } else if (response.status === 401 || response.status === 403) {
                    const errorText = await response.text();
                    console.log('Error text:', errorText);
                    alert('Ошибка: недостаточно прав для удаления модели. Текущие роли: ' + userRoles + '. Пожалуйста, перезалогиньтесь для обновления прав.');
                } else {
                    const errorText = await response.text();
                    alert('Ошибка при удалении модели: ' + errorText);
                }
            } catch (error) {
                console.error('Error:', error);
                alert('Ошибка соединения с сервером');
            }
        }
    });

    // Section List Cancel Button
    document.getElementById('sectionListCancelButton').addEventListener('click', function() {
        resetSectionListEditMode();
        sectionListForm.reset();
    });

    // Section List Delete Button
    document.getElementById('sectionListDeleteButton').addEventListener('click', async function() {
        if (editingSectionListId && confirm('Вы уверены, что хотите удалить этот раздел?')) {
            try {
                const token = localStorage.getItem('token');
                const userRoles = localStorage.getItem('userRoles');
                console.log('Current user roles:', userRoles);
                const response = await fetch(`/api/section-lists/${editingSectionListId}`, {
                    method: 'DELETE',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                console.log('Delete response status:', response.status);
                if (response.ok) {
                    alert('Раздел успешно удален');
                    resetSectionListEditMode();
                    sectionListForm.reset();
                    loadSectionLists();
                    loadTechmaps();
                } else if (response.status === 401 || response.status === 403) {
                    const errorText = await response.text();
                    console.log('Error text:', errorText);
                    alert('Ошибка: недостаточно прав для удаления раздела. Текущие роли: ' + userRoles + '. Пожалуйста, перезалогиньтесь для обновления прав.');
                } else {
                    const errorText = await response.text();
                    alert('Ошибка при удалении раздела: ' + errorText);
                }
            } catch (error) {
                console.error('Error:', error);
                alert('Ошибка соединения с сервером');
            }
        }
    });

    // Techmap Cancel Button
    document.getElementById('techmapCancelButton').addEventListener('click', function() {
        resetTechmapEditMode();
        techmapForm.reset();
    });

    // Filter techmaps table when model or section changes
    document.getElementById('techmapModelList').addEventListener('change', filterTechmapsTable);
    document.getElementById('techmapSectionList').addEventListener('change', filterTechmapsTable);

    // Model List Form Submit
    modelListForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const modelListData = {
            name: document.getElementById('modelListName').value
        };

        try {
            let response;
            if (editingModelListId) {
                // Update existing model list
                response = await authFetch(`/api/model-list/${editingModelListId}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(modelListData)
                });
            } else {
                // Add new model list
                response = await authFetch('/api/model-list', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(modelListData)
                });
            }

            if (response.ok) {
                const result = await response.json();
                modelListForm.reset();
                resetModelListEditMode();
                loadModelLists();
                loadTechmaps(); // Refresh techmaps to update model dropdown
            } else {
                const errorText = await response.text();
                alert('Ошибка: ' + errorText);
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Ошибка соединения с сервером');
        }
    });

    // Section List Form Submit
    sectionListForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const sectionListData = {
            name: document.getElementById('sectionListName').value
        };

        try {
            let response;
            if (editingSectionListId) {
                // Update existing section list
                response = await authFetch(`/api/section-lists/${editingSectionListId}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(sectionListData)
                });
            } else {
                // Add new section list
                response = await authFetch('/api/section-lists', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(sectionListData)
                });
            }

            if (response.ok) {
                const result = await response.json();
                sectionListForm.reset();
                resetSectionListEditMode();
                loadSectionLists();
                loadTechmaps(); // Refresh techmaps to update section dropdown
            } else {
                const errorText = await response.text();
                alert('Ошибка: ' + errorText);
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Ошибка соединения с сервером');
        }
    });

    // Techmap Form Submit
    techmapForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const techmapData = {
            serial: document.getElementById('techmapSerial').value,
            descriptor: document.getElementById('techmapDescriptor').value,
            time: document.getElementById('techmapTime').value,
            price: eurosToTenthsOfCents(document.getElementById('techmapPrice').value),
            modelList: {
                id: document.getElementById('techmapModelList').value
            },
            sectionList: {
                id: document.getElementById('techmapSectionList').value
            }
        };

        try {
            let response;
            if (editingTechmapId) {
                // Update existing techmap
                response = await authFetch(`/api/techmaps/${editingTechmapId}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(techmapData)
                });
            } else {
                // Add new techmap
                response = await authFetch('/api/techmaps', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(techmapData)
                });
            }

            if (response.ok) {
                const result = await response.json();
                if (editingTechmapId) {
                    // Keep model and section selected for filtering, reset other fields
                    const selectedModelId = document.getElementById('techmapModelList').value;
                    const selectedSectionId = document.getElementById('techmapSectionList').value;
                    techmapForm.reset();
                    document.getElementById('techmapModelList').value = selectedModelId;
                    document.getElementById('techmapSectionList').value = selectedSectionId;
                    resetTechmapEditMode();
                } else {
                    // Keep model and section selected for filtering, reset other fields
                    const selectedModelId = document.getElementById('techmapModelList').value;
                    const selectedSectionId = document.getElementById('techmapSectionList').value;
                    techmapForm.reset();
                    document.getElementById('techmapModelList').value = selectedModelId;
                    document.getElementById('techmapSectionList').value = selectedSectionId;
                }
                await loadTechmaps();
            } else {
                const errorText = await response.text();
                alert('Ошибка: ' + errorText);
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Ошибка соединения с сервером');
        }
    });

    // Model List Select Change
    modelListSelect.addEventListener('change', async function() {
        const modelListId = this.value;
        if (modelListId) {
            try {
                const response = await authFetch(`/api/model-list/${modelListId}`);
                if (response.ok) {
                    const modelList = await response.json();
                    document.getElementById('modelListName').value = modelList.name;
                    setModelListEditMode(modelListId);
                }
            } catch (error) {
                console.error('Error:', error);
            }
        } else {
            resetModelListEditMode();
        }
    });

    // Section List Select Change
    sectionListSelect.addEventListener('change', async function() {
        const sectionListId = this.value;
        if (sectionListId) {
            try {
                const response = await authFetch(`/api/section-lists/${sectionListId}`);
                if (response.ok) {
                    const sectionList = await response.json();
                    document.getElementById('sectionListName').value = sectionList.name;
                    setSectionListEditMode(sectionListId);
                }
            } catch (error) {
                console.error('Error:', error);
            }
        } else {
            resetSectionListEditMode();
        }
    });

    async function loadModelLists() {
        try {
            const response = await authFetch('/api/model-list');
            if (response.ok) {
                const modelLists = await response.json();
                populateModelListSelect(modelLists);
                populateTechmapModelSelect(modelLists);
            }
        } catch (error) {
            console.error('Error loading model lists:', error);
        }
    }

    async function loadSectionLists() {
        try {
            const response = await authFetch('/api/section-lists');
            if (response.ok) {
                const sectionLists = await response.json();
                populateSectionListSelect(sectionLists);
                populateTechmapSectionSelect(sectionLists);
            }
        } catch (error) {
            console.error('Error loading section lists:', error);
        }
    }

    async function loadTechmaps() {
        try {
            const response = await authFetch('/api/techmaps');
            if (response.ok) {
                allTechmaps = await response.json();
                filterTechmapsTable();
            }
        } catch (error) {
            console.error('Error loading techmaps:', error);
        }
    }

    function filterTechmapsTable() {
        const selectedModelId = document.getElementById('techmapModelList').value;
        const selectedSectionId = document.getElementById('techmapSectionList').value;
        
        // Hide all if either model or section is not selected
        if (!selectedModelId || !selectedSectionId) {
            populateTechmapsTable([]);
            return;
        }
        
        // Show only techmaps that match both selected model and section
        const filteredTechmaps = allTechmaps.filter(techmap => {
            const modelMatch = techmap.modelList && String(techmap.modelList.id) === selectedModelId;
            const sectionMatch = techmap.sectionList && String(techmap.sectionList.id) === selectedSectionId;
            return modelMatch && sectionMatch;
        });
        
        populateTechmapsTable(filteredTechmaps);
    }

    function populateModelListSelect(modelLists) {
        modelListSelect.innerHTML = '<option value="">-- Выберите модель --</option>';
        modelLists.forEach(modelList => {
            const option = document.createElement('option');
            option.value = modelList.id;
            option.textContent = modelList.name;
            modelListSelect.appendChild(option);
        });
    }

    function populateSectionListSelect(sectionLists) {
        sectionListSelect.innerHTML = '<option value="">-- Выберите раздел --</option>';
        sectionLists.forEach(sectionList => {
            const option = document.createElement('option');
            option.value = sectionList.id;
            option.textContent = sectionList.name;
            sectionListSelect.appendChild(option);
        });
    }

    function populateTechmapModelSelect(modelLists) {
        const techmapModelSelect = document.getElementById('techmapModelList');
        if (techmapModelSelect) {
            techmapModelSelect.innerHTML = '<option value="">-- Выберите модель --</option>';
            modelLists.forEach(modelList => {
                const option = document.createElement('option');
                option.value = modelList.id;
                option.textContent = modelList.name;
                techmapModelSelect.appendChild(option);
            });
        }
    }

    function populateTechmapSectionSelect(sectionLists) {
        const techmapSectionSelect = document.getElementById('techmapSectionList');
        if (techmapSectionSelect) {
            techmapSectionSelect.innerHTML = '<option value="">-- Выберите раздел --</option>';
            sectionLists.forEach(sectionList => {
                const option = document.createElement('option');
                option.value = sectionList.id;
                option.textContent = sectionList.name;
                techmapSectionSelect.appendChild(option);
            });
        }
    }

    function populateTechmapsTable(techmaps) {
        const tableBody = document.querySelector('#techmapsTable tbody');
        if (!tableBody) {
            // If table doesn't exist, reload the page to get updated data from server
            location.reload();
            return;
        }

        tableBody.innerHTML = '';
        techmaps.forEach(techmap => {
            const row = document.createElement('tr');
            row.style.height = '25px';
            row.style.cursor = 'pointer';
            row.classList.add('hover:bg-warning/20');
            row.onclick = () => editTechmap(techmap.id);
            row.innerHTML = `
                <td class="text-xs text-center" style="padding: 0px 4px; height: 25px; border: 1px solid #e5e7eb; width: 40px;">${techmap.serial}</td>
                <td class="text-xs text-left" style="padding: 0px 4px; height: 25px; border: 1px solid #e5e7eb;">${techmap.descriptor}</td>
                <td class="text-xs text-left" style="padding: 0px 4px; height: 25px; border: 1px solid #e5e7eb; width: 80px;">${techmap.time}</td>
                <td class="text-xs text-left" style="padding: 0px 4px; height: 25px; border: 1px solid #e5e7eb; width: 80px;">${formatPrice(techmap.price)}</td>
                <td class="text-xs text-left" style="padding: 0px 4px; height: 25px; border: 1px solid #e5e7eb; width: 80px;">
                    <button class="btn btn-xs btn-error h-4 min-h-4 px-1" onclick="event.stopPropagation(); deleteTechmap(${techmap.id})">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                    </button>
                </td>
            `;
            tableBody.appendChild(row);
        });
    }

    // Global function for delete button
    window.deleteTechmap = async function(id) {
        if (confirm('Вы уверены, что хотите удалить эту технологическую карту?')) {
            try {
                const token = localStorage.getItem('token');
                const userRoles = localStorage.getItem('userRoles');
                console.log('Current user roles:', userRoles);
                const response = await fetch(`/api/techmaps/${id}`, {
                    method: 'DELETE',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                console.log('Delete response status:', response.status);
                if (response.ok) {
                    alert('Технологическая карта успешно удалена');
                    await loadTechmaps();
                } else if (response.status === 401 || response.status === 403) {
                    const errorText = await response.text();
                    console.log('Error text:', errorText);
                    alert('Ошибка: недостаточно прав для удаления технологической карты. Текущие роли: ' + userRoles + '. Пожалуйста, перезалогиньтесь для обновления прав.');
                } else {
                    const errorText = await response.text();
                    alert('Ошибка при удалении технологической карты: ' + errorText);
                }
            } catch (error) {
                console.error('Error:', error);
                alert('Ошибка соединения с сервером');
            }
        }
    };

    // Global function for edit button
    window.editTechmap = async function(id) {
        try {
            // Reload model and section lists to ensure they contain all options
            await loadModelLists();
            await loadSectionLists();

            const response = await authFetch(`/api/techmaps/${id}`);
            if (response.ok) {
                const techmap = await response.json();
                populateTechmapForm(techmap);
                setTechmapEditMode(id);
            } else {
                alert('Ошибка загрузки данных технологической карты');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Ошибка соединения с сервером');
        }
    };

    function populateTechmapForm(techmap) {
        document.getElementById('techmapSerial').value = techmap.serial || '';
        document.getElementById('techmapDescriptor').value = techmap.descriptor || '';
        document.getElementById('techmapTime').value = techmap.time || '';
        document.getElementById('techmapPrice').value = tenthsOfCentsToEuros(techmap.price);
        document.getElementById('techmapModelList').value = techmap.modelList ? String(techmap.modelList.id) : '';
        document.getElementById('techmapSectionList').value = techmap.sectionList ? String(techmap.sectionList.id) : '';
    }

    function setTechmapEditMode(id) {
        editingTechmapId = id;
        document.getElementById('techmapSubmitButtonText').textContent = 'Изменить технологическую карту';
        document.getElementById('techmapSubmitButton').classList.remove('btn-primary');
        document.getElementById('techmapSubmitButton').classList.add('btn-warning');
        document.getElementById('techmapCancelButton').classList.remove('hidden');
        document.getElementById('techmapFormTitle').textContent = 'Изменение технологической карты';
        document.getElementById('techmapFormIcon').innerHTML = '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />';
    }

    function resetTechmapEditMode() {
        editingTechmapId = null;
        document.getElementById('techmapSubmitButtonText').textContent = 'Добавить технологическую карту';
        document.getElementById('techmapSubmitButton').classList.remove('btn-warning');
        document.getElementById('techmapSubmitButton').classList.add('btn-primary');
        document.getElementById('techmapCancelButton').classList.add('hidden');
        document.getElementById('techmapFormTitle').textContent = 'Технологическая карта';
        document.getElementById('techmapFormIcon').innerHTML = '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />';
    }

    function setModelListEditMode(id) {
        editingModelListId = id;
        document.getElementById('modelListSubmitButtonText').textContent = 'Изменить модель';
        document.getElementById('modelListSubmitButton').classList.remove('btn-primary');
        document.getElementById('modelListSubmitButton').classList.add('btn-warning');
        document.getElementById('modelListCancelButton').classList.remove('hidden');
        document.getElementById('modelListDeleteButton').classList.remove('hidden');
        document.getElementById('modelListFormTitle').textContent = 'Изменение модели';
        document.getElementById('modelListFormIcon').innerHTML = '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />';
    }

    function resetModelListEditMode() {
        editingModelListId = null;
        document.getElementById('modelListSubmitButtonText').textContent = 'Добавить модель';
        document.getElementById('modelListSubmitButton').classList.remove('btn-warning');
        document.getElementById('modelListSubmitButton').classList.add('btn-primary');
        document.getElementById('modelListCancelButton').classList.add('hidden');
        document.getElementById('modelListDeleteButton').classList.add('hidden');
        document.getElementById('modelListFormTitle').textContent = 'Список моделей';
        document.getElementById('modelListFormIcon').innerHTML = '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 13h6m-3-3v6m-9 1V7a2 2 0 012-2h6l2 2h6a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2z" />';
    }

    function setSectionListEditMode(id) {
        editingSectionListId = id;
        document.getElementById('sectionListSubmitButtonText').textContent = 'Изменить раздел';
        document.getElementById('sectionListSubmitButton').classList.remove('btn-primary');
        document.getElementById('sectionListSubmitButton').classList.add('btn-warning');
        document.getElementById('sectionListCancelButton').classList.remove('hidden');
        document.getElementById('sectionListDeleteButton').classList.remove('hidden');
        document.getElementById('sectionListFormTitle').textContent = 'Изменение раздела';
        document.getElementById('sectionListFormIcon').innerHTML = '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />';
    }

    function resetSectionListEditMode() {
        editingSectionListId = null;
        document.getElementById('sectionListSubmitButtonText').textContent = 'Добавить раздел';
        document.getElementById('sectionListSubmitButton').classList.remove('btn-warning');
        document.getElementById('sectionListSubmitButton').classList.add('btn-primary');
        document.getElementById('sectionListCancelButton').classList.add('hidden');
        document.getElementById('sectionListDeleteButton').classList.add('hidden');
        document.getElementById('sectionListFormTitle').textContent = 'Раздел';
        document.getElementById('sectionListFormIcon').innerHTML = '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />';
    }
});
