// 페이지 로드 시 인증 확인
checkAuth();

// 로그아웃
document.getElementById('logoutBtn').addEventListener('click', (e) => {
    e.preventDefault();
    if (showConfirm('로그아웃 하시겠습니까?')) {
        localStorage.removeItem('authToken');
        localStorage.removeItem('username');
        window.location.href = '/index.html';
    }
});

// 전체 신고 목록
let allReports = [];
let currentFilter = 'ALL';

// 필터 버튼 이벤트
document.querySelectorAll('[data-filter]').forEach(btn => {
    btn.addEventListener('click', function() {
        // 모든 버튼 스타일 초기화
        document.querySelectorAll('[data-filter]').forEach(b => {
            b.classList.remove('btn-primary');
            b.classList.add('btn-secondary');
        });

        // 클릭된 버튼 활성화
        this.classList.remove('btn-secondary');
        this.classList.add('btn-primary');

        // 필터 적용
        currentFilter = this.dataset.filter;
        displayReports();
    });
});

// 신고 내역 로드
async function loadReports() {
    const reportsList = document.getElementById('reportsList');
    reportsList.innerHTML = '<div style="text-align: center; padding: 40px;"><div class="loading"></div></div>';

    try {
        allReports = await apiCall('/emergency-reports/my');
        displayReports();
    } catch (error) {
        console.error('신고 내역 로드 실패:', error);
        reportsList.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">⚠️</div>
                <h3>신고 내역을 불러올 수 없습니다</h3>
                <p>잠시 후 다시 시도해주세요</p>
            </div>
        `;
    }
}

// 신고 내역 표시
function displayReports() {
    const reportsList = document.getElementById('reportsList');

    // 필터링
    let filteredReports = allReports;
    if (currentFilter !== 'ALL') {
        filteredReports = allReports.filter(report => report.status === currentFilter);
    }

    if (filteredReports.length === 0) {
        reportsList.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">📭</div>
                <h3>해당하는 신고 내역이 없습니다</h3>
                <p>${currentFilter === 'ALL' ? '긴급 버튼을 사용한 기록이 없습니다' : getStatusText(currentFilter) + ' 상태의 신고가 없습니다'}</p>
            </div>
        `;
        return;
    }

    // 최신순 정렬
    filteredReports.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    reportsList.innerHTML = filteredReports.map(report => `
        <div class="report-item" onclick="location.href='/detail.html?id=${report.id}'">
            <div class="report-header">
                <div class="report-time">🕐 ${formatDate(report.createdAt)}</div>
                <div class="report-status ${getStatusClass(report.status)}">${getStatusText(report.status)}</div>
            </div>
            <div class="report-info">
                <div class="report-location">📍 위도: ${report.latitude.toFixed(6)}, 경도: ${report.longitude.toFixed(6)}</div>
                ${report.description ? `<div class="report-description">${report.description}</div>` : ''}
            </div>
        </div>
    `).join('');
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', () => {
    loadReports();
});