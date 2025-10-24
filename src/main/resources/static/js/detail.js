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

// URL에서 신고 ID 가져오기
const urlParams = new URLSearchParams(window.location.search);
const reportId = urlParams.get('id');

if (!reportId) {
    alert('잘못된 접근입니다.');
    location.href = '/history.html';
}

// 신고 상세 정보 로드
async function loadReportDetail() {
    try {
        const report = await apiCall(`/emergency-reports/${reportId}`);

        // 기본 정보 표시
        document.getElementById('reportId').textContent = report.id;
        document.getElementById('reportTime').textContent = formatDate(report.createdAt);
        document.getElementById('reportLocation').textContent =
            `위도: ${report.latitude.toFixed(6)}, 경도: ${report.longitude.toFixed(6)}`;

        const statusHtml = `<div class="report-status ${getStatusClass(report.status)}">${getStatusText(report.status)}</div>`;
        document.getElementById('reportStatus').innerHTML = statusHtml;

        // 상황 설명
        document.getElementById('reportDescription').textContent =
            report.description || '상황 설명이 없습니다.';

        // AI 음성 분석 결과
        const aiAnalysisDiv = document.getElementById('aiAnalysis');
        if (report.aiAnalysis) {
            aiAnalysisDiv.innerHTML = `
                <div style="padding: 15px; background: #e8f4f8; border-radius: 8px; border-left: 4px solid #3498db;">
                    <strong>🤖 AI 분석 결과:</strong><br>
                    ${report.aiAnalysis}
                </div>
            `;
        } else {
            aiAnalysisDiv.innerHTML = `
                <div style="padding: 15px; background: #fff3cd; border-radius: 8px; border-left: 4px solid #f39c12;">
                    <strong>⏳ AI 음성 분석 대기 중...</strong><br>
                    아직 음성 분석이 완료되지 않았습니다.
                </div>
            `;
        }

        // 음성 파일
        const audioPlayerDiv = document.getElementById('audioPlayer');
        if (report.audioUrl) {
            audioPlayerDiv.innerHTML = `
                <audio controls style="width: 100%;">
                    <source src="${report.audioUrl}" type="audio/mpeg">
                    브라우저가 오디오를 지원하지 않습니다.
                </audio>
            `;
        } else {
            audioPlayerDiv.innerHTML = `
                <p style="color: #7f8c8d;">📎 녹음된 음성 파일이 없습니다.</p>
            `;
        }

        // 지도 표시
        displayDetailMap(report.latitude, report.longitude);

    } catch (error) {
        console.error('신고 상세 정보 로드 실패:', error);
        alert('신고 정보를 불러올 수 없습니다.');
        location.href = '/history.html';
    }
}

// 지도 표시
function displayDetailMap(lat, lng) {
    const mapDiv = document.getElementById('detailMap');

    mapDiv.innerHTML = `
        <iframe 
            width="100%" 
            height="400" 
            frameborder="0" 
            style="border:0; border-radius: 12px;"
            src="https://www.google.com/maps?q=${lat},${lng}&output=embed"
            allowfullscreen>
        </iframe>
    `;
}

// 상태 변경 버튼 이벤트
document.querySelectorAll('[data-status]').forEach(btn => {
    btn.addEventListener('click', async function() {
        const newStatus = this.dataset.status;

        if (!showConfirm(`상태를 "${getStatusText(newStatus)}"로 변경하시겠습니까?`)) {
            return;
        }

        const originalText = this.innerHTML;
        showLoading(this);

        try {
            await apiCall(`/emergency-reports/${reportId}/status`, 'PUT', { status: newStatus });
            showSuccess('상태가 변경되었습니다!');

            // 페이지 새로고침
            setTimeout(() => {
                location.reload();
            }, 1000);
        } catch (error) {
            console.error('상태 변경 실패:', error);
            showError('상태 변경에 실패했습니다.');
            hideLoading(this, originalText);
        }
    });
});

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', () => {
    loadReportDetail();
});