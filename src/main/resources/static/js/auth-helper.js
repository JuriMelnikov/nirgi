// Helper functions for JWT authentication

// Make setJustLoggedIn available globally for login.js (uses sessionStorage for reliability)
window.setJustLoggedIn = function() {
    const timestamp = Date.now();
    sessionStorage.setItem('justLoggedInTimestamp', timestamp.toString());
    localStorage.setItem('justLoggedInTimestamp', timestamp.toString());
    console.log('auth-helper.js: setJustLoggedIn called, timestamp:', timestamp);
};

// Get token from localStorage
function getToken() {
    return localStorage.getItem('token');
}

// Check if user is authenticated
function isAuthenticated() {
    const token = getToken();
    const userLogin = localStorage.getItem('userLogin');
    return !!(token && userLogin);
}

// Get user roles from localStorage
function getUserRoles() {
    const roles = localStorage.getItem('userRoles');
    return roles ? JSON.parse(roles) : [];
}

// Check if user has specific role
function hasRole(role) {
    const roles = getUserRoles();
    return roles.includes(role);
}

// Check if user has any of the specified roles
function hasAnyRole(roles) {
    const userRoles = getUserRoles();
    return roles.some(role => userRoles.includes(role));
}

// Redirect to login if not authenticated
function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = '/login';
        return false;
    }
    return true;
}

// Check page access based on user roles and redirect if needed
let checkPageAccessInProgress = false;

async function checkPageAccess() {
    console.log('auth-helper.js: checkPageAccess called for:', window.location.pathname);
    // Prevent multiple simultaneous calls
    if (checkPageAccessInProgress) {
        console.log('auth-helper.js: checkPageAccess already in progress, skipping');
        return false;
    }
    
    // Skip checks if just logged in (check both sessionStorage and localStorage timestamp)
    const justLoggedInTimestamp = sessionStorage.getItem('justLoggedInTimestamp') || localStorage.getItem('justLoggedInTimestamp');
    console.log('auth-helper.js: justLoggedInTimestamp:', justLoggedInTimestamp);
    if (justLoggedInTimestamp) {
        const timestamp = parseInt(justLoggedInTimestamp);
        const now = Date.now();
        const elapsed = now - timestamp;
        console.log('auth-helper.js: elapsed time:', elapsed, 'ms');
        if (elapsed < 5000) {
            console.log('auth-helper.js: just logged in, skipping check');
            return true;
        } else {
            console.log('auth-helper.js: timestamp expired, clearing it');
            sessionStorage.removeItem('justLoggedInTimestamp');
            localStorage.removeItem('justLoggedInTimestamp');
        }
    }
    
    // Don't check access on login page
    if (window.location.pathname === '/login') {
        console.log('auth-helper.js: on login page, skipping check');
        return true;
    }
    
    checkPageAccessInProgress = true;
    console.log('auth-helper.js: starting access check');
    console.log('auth-helper.js: token exists:', !!getToken());
    console.log('auth-helper.js: userLogin:', localStorage.getItem('userLogin'));
    
    try {
        const response = await authFetch('/api/auth/me');
        console.log('auth-helper.js: authFetch response status:', response.status);
        
        if (response.status === 401 || response.status === 403) {
            console.log('auth-helper.js: unauthorized (401/403), redirecting to login');
            window.location.href = '/login';
            return false;
        }

        const data = await response.json();
        console.log('auth-helper.js: /api/auth/me response data:', data);
        
        if (data.error) {
            console.log('auth-helper.js: error in response, redirecting to login');
            window.location.href = '/login';
            return false;
        }

        const roles = data.roles || [];
        const currentPath = window.location.pathname;
        console.log('auth-helper.js: user roles:', roles, 'current path:', currentPath);

        // Define allowed pages for each role (must match SecurityConfig)
        const rolePageAccess = {
            'EMPLOYEE': ['/work-results'],
            'MASTER': ['/work-results', '/orders'],
            'TECHNOLOGIST': ['/work-results', '/orders', '/models'],
            'MANAGER': ['/work-results', '/orders', '/models', '/employees', '/salary'],
            'ACCOUNTANT': ['/salary'],
            'ADMINISTRATOR': ['/work-results', '/orders', '/models', '/employees', '/salary']
        };

        // Check if user has any role that allows access to current page
        let hasAccess = false;
        let defaultPage = '/work-results';

        for (const role of roles) {
            if (rolePageAccess[role] && rolePageAccess[role].includes(currentPath)) {
                hasAccess = true;
                console.log('auth-helper.js: access granted via role:', role);
                break;
            }
        }

        // Set default page based on highest priority role (following hierarchy)
        if (roles.includes('ADMINISTRATOR')) {
            defaultPage = '/employees';
        } else if (roles.includes('ACCOUNTANT') && !roles.includes('MANAGER')) {
            defaultPage = '/salary';
        } else if (roles.includes('MANAGER')) {
            defaultPage = '/employees';
        } else if (roles.includes('MASTER')) {
            defaultPage = '/orders';
        } else if (roles.includes('TECHNOLOGIST')) {
            defaultPage = '/models';
        } else if (roles.includes('EMPLOYEE')) {
            defaultPage = '/work-results';
        }

        if (!hasAccess) {
            console.log('auth-helper.js: no access, redirecting to default page:', defaultPage);
            window.location.href = defaultPage;
            return false;
        }

        console.log('auth-helper.js: access granted, no redirect needed');
        return true;
    } catch (error) {
        console.error('auth-helper.js: error during checkPageAccess:', error);
        // Don't redirect on network errors, just log them
        return true;
    } finally {
        checkPageAccessInProgress = false;
    }
}

// Add Authorization header to fetch options
function addAuthHeader(options = {}) {
    const token = getToken();
    if (token) {
        options.headers = options.headers || {};
        options.headers['Authorization'] = `Bearer ${token}`;
    }
    return options;
}

// Wrapper for fetch with automatic token inclusion
async function authFetch(url, options = {}) {
    const authOptions = addAuthHeader(options);
    console.log('auth-helper.js: authFetch URL:', url);
    console.log('auth-helper.js: authFetch has token:', !!getToken());
    console.log('auth-helper.js: authFetch Authorization header:', authOptions.headers?.Authorization ? 'PRESENT' : 'MISSING');
    const response = await fetch(url, authOptions);
    console.log('auth-helper.js: authFetch response status:', response.status);
    
    // If 401 or 403, redirect to login
    if (response.status === 401 || response.status === 403) {
        console.log('auth-helper.js: got 401/403, redirecting to login');
        localStorage.removeItem('token');
        localStorage.removeItem('userLogin');
        localStorage.removeItem('userRoles');
        window.location.href = '/login';
    }
    
    return response;
}
