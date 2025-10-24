// 기본 API URL
const API_BASE_URL = '/api';

// 로컬 스토리지에서 토큰 가져오기
function getToken() {
    return localStorage.getItem('authToken');
}

// 사용자 이름 가져오기
function getUsername() {
    return localStorage.getItem('username');
}

// API 호출 공통 함수
async function apiCall(endpoint, method = 'GET', body = null, isFormData = false) {
    const headers = {};

    if (!isFormData) {
        headers['Content-Type'] = 'application/json';
    }

    const token = getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const options = {
        method,
        headers
    };

    if (body) {
        options.body = isFormData ? body : JSON.stringify(body);
    }

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);

        // 401 에러 처리
        if (response.status === 401) {
            localStorage.removeItem('authToken');
            localStorage.removeItem('username');
            alert('로그인이 필요합니다.');
            window.location.href = '/index.html';
            return;
        }

        // 에러 응답 처리
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }

        // 204 No Content 처리
        if (response.status === 204) {
            return null;
        }

        return await response.json();
    } catch (error) {
        console.error('API 호출 실패:', error);
        throw error;
    }
}

// 로그인 확인
function checkAuth() {
    if (!getToken()) {
        window.location.href = '/index.html';
        return false;
    }
    return true;
}

// 날짜 포맷팅
function formatDate(dateString) {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${year}.${month}.${day} ${hours}:${minutes}`;
}

// 상태 텍스트 변환
function getStatusText(status) {
    const statusMap = {
        'PENDING': '대기중',
        'IN_PROGRESS': '처리중',
        'RESOLVED': '완료',
        'CANCELLED': '취소됨'
    };
    return statusMap[status] || status;
}

// 상태 클래스 반환
function getStatusClass(status) {
    const statusClassMap = {
        'PENDING': 'status-pending',
        'IN_PROGRESS': 'status-in_progress',
        'RESOLVED': 'status-resolved',
        'CANCELLED': 'status-cancelled'
    };
    return statusClassMap[status] || '';
}

// 로딩 표시
function showLoading(element) {
    if (element) {
        element.innerHTML = '<div class="loading"></div>';
        element.disabled = true;
    }
}

// 로딩 숨기기
function hideLoading(element, originalText) {
    if (element) {
        element.innerHTML = originalText;
        element.disabled = false;
    }
}

// 에러 메시지 표시
function showError(message) {
    alert('❌ ' + message);
}

// 성공 메시지 표시
function showSuccess(message) {
    alert('✅ ' + message);
}

// 확인 다이얼로그
function showConfirm(message) {
    return confirm(message);
}