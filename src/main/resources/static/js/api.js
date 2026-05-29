// ========================================
// API CLIENT & AUTHENTICATION
// ========================================

const API_BASE = `${window.location.protocol}//${window.location.hostname}:8082/api`;

// Get auth token from localStorage
const getToken = () => localStorage.getItem('token');

// Get current user from localStorage
const getCurrentUser = () => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
};

// Check if user is authenticated
const isAuthenticated = () => {
    return !!getToken();
};

// Logout function
const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/index.html';
};

// API call wrapper with authentication
async function apiCall(endpoint, options = {}) {
    const token = getToken();

    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, {
            ...options,
            headers
        });

        if (response.status === 401) {
            logout();
            throw new Error('Session expired. Please login again.');
        }

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || 'Something went wrong');
        }

        return data;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

// ========================================
// AUTH API
// ========================================

async function signup(fullName, email, password, role, departmentId, classId) {
    const data = await apiCall('/auth/signup', {
        method: 'POST',
        body: JSON.stringify({ fullName, email, password, role, departmentId, classId })
    });

    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify({
        id: data.id,
        email: data.email,
        fullName: data.fullName,
        role: data.role
    }));

    return data;
}

// ========================================
// PUBLIC ACADEMIC API
// ========================================

async function getPublicDepartments() {
    return await apiCall('/auth/departments', { method: 'GET' });
}

async function getPublicClasses(departmentId) {
    return await apiCall(`/auth/departments/${departmentId}/classes`, { method: 'GET' });
}

async function login(email, password) {
    const data = await apiCall('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
    });

    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify({
        id: data.id,
        email: data.email,
        fullName: data.fullName,
        role: data.role
    }));

    return data;
}

// Admin creates a user without overwriting the current admin session
// Uses dedicated /api/admin/users endpoint which always creates ACTIVE users
async function adminCreateUser(fullName, email, password, role) {
    return await apiCall('/admin/users', {
        method: 'POST',
        body: JSON.stringify({ fullName, email, password, role })
    });
    // NOTE: intentionally NOT storing token/user – keeps the admin's session intact
}

// ========================================
// AI API
// ========================================

async function sendAiMessage(message, topic = 'General', mode = 'EXPLAIN', courseId = null) {
    return await apiCall('/ai/chat', {
        method: 'POST',
        body: JSON.stringify({ message, topic, mode, courseId })
    });
}

// ========================================
// DASHBOARD API
// ========================================

async function getStudentProgress(userId) {
    return await apiCall(`/dashboard/student/${userId}`);
}

async function updateProgress(userId, courseId, completion, quizScore) {
    return await apiCall(`/dashboard/progress?userId=${userId}&courseId=${courseId}&completion=${completion || ''}&quizScore=${quizScore || ''}`, {
        method: 'POST'
    });
}

async function incrementQuestion(userId, courseId) {
    return await apiCall(`/dashboard/progress/question?userId=${userId}&courseId=${courseId}`, {
        method: 'POST'
    });
}

async function incrementQuiz(userId, courseId, score) {
    return await apiCall(`/dashboard/progress/quiz?userId=${userId}&courseId=${courseId}&score=${score}`, {
        method: 'POST'
    });
}

async function incrementTopic(userId, courseId) {
    return await apiCall(`/dashboard/progress/topic?userId=${userId}&courseId=${courseId}`, {
        method: 'POST'
    });
}

// ========================================
// COURSE API
// ========================================

async function getAllCourses() {
    return await apiCall('/courses');
}

async function getCourse(id) {
    return await apiCall(`/courses/${id}`);
}

async function createCourse(name, description, aiMode) {
    const user = getCurrentUser();
    console.log('Creating course with user:', user);
    const payload = {
        name,
        description,
        aiMode,
        active: true,
        teacher: { id: user.id }
    };
    console.log('Sending payload:', JSON.stringify(payload));
    return await apiCall('/courses', {
        method: 'POST',
        body: JSON.stringify(payload)
    });
}

async function updateCourse(id, name, description, aiMode) {
    return await apiCall(`/courses/${id}`, {
        method: 'PUT',
        body: JSON.stringify({ name, description, aiMode })
    });
}

// ========================================
// ENROLLMENT API
// ========================================

async function enrollInCourse(courseId) {
    return await apiCall('/enrollments', {
        method: 'POST',
        body: JSON.stringify({ courseId })
    });
}

async function getMyEnrollments(studentId) {
    return await apiCall(`/enrollments/student/${studentId}`);
}

async function unenrollFromCourse(enrollmentId) {
    return await apiCall(`/enrollments/${enrollmentId}`, { method: 'DELETE' });
}

// ========================================
// SESSION API
// ========================================

async function createSession(courseId, sessionName) {
    const user = getCurrentUser();
    return await apiCall('/sessions', {
        method: 'POST',
        body: JSON.stringify({
            courseId,
            teacherId: user.id,
            sessionName: sessionName || 'Live Session'
        })
    });
}

async function startSession(sessionId) {
    return await apiCall(`/sessions/${sessionId}/start`, { method: 'PUT' });
}

async function endSession(sessionId) {
    return await apiCall(`/sessions/${sessionId}/end`, { method: 'PUT' });
}

async function getSession(sessionId) {
    return await apiCall(`/sessions/${sessionId}`);
}

// ========================================
// UI UTILITIES
// ========================================

const TOAST_ICONS = {
    success: '<i data-lucide="check-circle"></i>',
    error: '<i data-lucide="x-circle"></i>',
    warning: '<i data-lucide="alert-triangle"></i>',
    info: '<i data-lucide="info"></i>'
};

function showToast(message, type = 'info', duration = 4000) {
    // 1. Ensure .toast-container exists
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    // 2. Enforce max-3 stack — remove oldest if already at limit
    const existingToasts = container.querySelectorAll('.toast');
    if (existingToasts.length >= 3) {
        existingToasts[0].remove();
    }

    // 3. Build the toast element
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <span class="toast-icon">${TOAST_ICONS[type] || TOAST_ICONS.info}</span>
        <div class="toast-body">
            <p class="toast-message">${message}</p>
            <div class="toast-progress" style="animation-duration: ${duration}ms;"></div>
        </div>
        <button class="toast-close" aria-label="Close">&times;</button>
    `;

    // 4. Wire up close button
    toast.querySelector('.toast-close').addEventListener('click', () => toast.remove());

    container.appendChild(toast);

    // 5. Re-initialize Lucide icons after appending
    if (typeof lucide !== 'undefined') lucide.createIcons();

    // 6. Auto-dismiss
    setTimeout(() => toast.remove(), duration);
}

function showError(message) {
    showToast(message, 'error');
}

function showSuccess(message) {
    showToast(message, 'success');
}

function showWarning(message) {
    showToast(message, 'warning');
}

function showInfo(message) {
    showToast(message, 'info');
}

function getInitials(name) {
    return name
        .split(' ')
        .map(n => n[0])
        .join('')
        .toUpperCase()
        .substring(0, 2);
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric'
    });
}

// Protect routes - redirect if not authenticated
function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = '/index.html';
    }
}

// Check role-based access
function requireRole(allowedRoles) {
    const user = getCurrentUser();
    if (!user || !allowedRoles.includes(user.role)) {
        showError('Access denied');
        setTimeout(() => logout(), 1500);
    }
}
