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

// 사용자 정보 로드
async function loadUserInfo() {
    try {
        const user = await apiCall('/users/me');
        document.getElementById('userName').textContent = user.name;
    } catch (error) {
        console.error('사용자 정보 로드 실패:', error);
        document.getElementById('userName').textContent = getUsername() || '사용자';
    }
}

// 현재 위치 가져오기
function getCurrentLocation() {
    const locationDiv = document.getElementById('currentLocation');

    if (!navigator.geolocation) {
        locationDiv.textContent = '위치 서비스를 지원하지 않는 브라우저입니다.';
        return;
    }

    navigator.geolocation.getCurrentPosition(
        async (position) => {
            const latitude = position.coords.latitude;
            const longitude = position.coords.longitude;

            locationDiv.innerHTML = `
                <strong>위도:</strong> ${latitude.toFixed(6)}<br>
                <strong>경도:</strong> ${longitude.toFixed(6)}
            `;

            // 위치 정보 서버에 저장
            try {
                await apiCall('/locations', 'POST', { latitude, longitude });
                console.log('위치 정보 저장 완료');
            } catch (error) {
                console.error('위치 저장 실패:', error);
            }

            // 지도 표시
            displayMap(latitude, longitude);
        },
        (error) => {
            console.error('위치 정보 오류:', error);
            locationDiv.textContent = '위치 정보를 가져올 수 없습니다. 위치 권한을 확인해주세요.';
        },
        {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 0
        }
    );
}

// 지도 표시 (Google Maps Embed)
function displayMap(lat, lng) {
    const mapDiv = document.getElementById('map');

    // 구글 맵 embed
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

// 긴급 버튼 클릭 처리
document.getElementById('sosButton').addEventListener('click', async function() {
    if (!showConfirm('🚨 긴급 구조 요청을 보내시겠습니까?\n\n등록된 긴급 연락처로 현재 위치와 상황이 즉시 전송됩니다.')) {
        return;
    }

    const sosBtn = this;
    const originalHTML = sosBtn.innerHTML;
    sosBtn.disabled = true;
    sosBtn.innerHTML = '<div class="sos-text">전송 중...</div><div class="loading"></div>';

    // 현재 위치 가져오기
    navigator.geolocation.getCurrentPosition(
        async (position) => {
            const latitude = position.coords.latitude;
            const longitude = position.coords.longitude;

            try {
                // 긴급 신고 생성
                const report = await apiCall('/emergency-reports', 'POST', {
                    latitude,
                    longitude,
                    description: '긴급 구조 요청 - AI 음성 분석 대기 중',
                    audioUrl: null // 실제로는 아두이노에서 녹음된 음성 URL
                });

                showSuccess('✅ 긁급 구조 요청이 전송되었습니다!\n\n가족에게 알림이 발송되었습니다.');

                // 페이지 새로고침 (최근 신고 내역 업데이트)
                setTimeout(() => {
                    location.reload();
                }, 1500);
            } catch (error) {
                console.error('긴급 신고 실패:', error);
                showError('긴급 신고 전송에 실패했습니다. 다시 시도해주세요.');
                sosBtn.disabled = false;
                sosBtn.innerHTML = originalHTML;
            }
        },
        (error) => {
            console.error('위치 정보 오류:', error);
            showError('위치 정보를 가져올 수 없습니다. 위치 권한을 확인해주세요.');
            sosBtn.disabled = false;
            sosBtn.innerHTML = originalHTML;
        },
        {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 0
        }
    );
});

// 최근 신고 내역 로드
async function loadRecentReports() {
    const reportsList = document.getElementById('reportsList');

    try {
        const reports = await apiCall('/emergency-reports/my');

        if (reports.length === 0) {
            reportsList.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">📭</div>
                    <h3>신고 내역이 없습니다</h3>
                    <p>긴급 상황 발생 시 위의 긴급 버튼을 눌러주세요</p>
                </div>
            `;
            return;
        }

        // 최근 3개만 표시
        const recentReports = reports.slice(0, 3);

        reportsList.innerHTML = recentReports.map(report => `
            <div class="report-item" onclick="location.href='/detail.html?id=${report.id}'">
                <div class="report-header">
                    <div class="report-time">${formatDate(report.createdAt)}</div>
                    <div class="report-status ${getStatusClass(report.status)}">${getStatusText(report.status)}</div>
                </div>
                <div class="report-info">
                    <div class="report-location">📍 위도: ${report.latitude.toFixed(4)}, 경도: ${report.longitude.toFixed(4)}</div>
                    ${report.description ? `<div class="report-description">${report.description}</div>` : ''}
                </div>
            </div>
        `).join('');

        // 더 많은 내역이 있으면 버튼 추가
        if (reports.length > 3) {
            reportsList.innerHTML += `
                <div style="text-align: center; margin-top: 20px;">
                    <button class="btn btn-secondary" onclick="location.href='/history.html'">
                        전체 내역 보기 (${reports.length}개)
                    </button>
                </div>
            `;
        }
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

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', () => {
    loadUserInfo();
    getCurrentLocation();
    loadRecentReports();

    // 5분마다 위치 업데이트
    setInterval(() => {
        getCurrentLocation();
    }, 5 * 60 * 1000);
});