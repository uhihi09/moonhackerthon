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

// 위치 이력 로드
async function loadLocationHistory() {
    const timelineDiv = document.getElementById('locationTimeline');
    const mapDiv = document.getElementById('locationMap');

    timelineDiv.innerHTML = '<div style="text-align: center; padding: 40px;"><div class="loading"></div></div>';

    try {
        const locations = await apiCall('/locations/my');

        if (locations.length === 0) {
            timelineDiv.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">📍</div>
                    <h3>위치 기록이 없습니다</h3>
                    <p>위치 정보가 수집되면 여기에 표시됩니다</p>
                </div>
            `;
            mapDiv.innerHTML = '<div style="text-align: center; padding: 100px; color: #7f8c8d;">위치 기록이 없습니다</div>';
            return;
        }

        // 최신순 정렬
        locations.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));

        // 타임라인 표시
        timelineDiv.innerHTML = locations.map(location => `
            <div class="location-point">
                <div class="location-time">${formatDate(location.timestamp)}</div>
                <div class="location-coords">
                    📍 위도: ${location.latitude.toFixed(6)}, 경도: ${location.longitude.toFixed(6)}
                </div>
            </div>
        `).join('');

        // 가장 최근 위치로 지도 표시
        const latestLocation = locations[0];
        displayLocationMap(latestLocation.latitude, latestLocation.longitude, locations);

    } catch (error) {
        console.error('위치 이력 로드 실패:', error);
        timelineDiv.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">⚠️</div>
                <h3>위치 이력을 불러올 수 없습니다</h3>
                <p>잠시 후 다시 시도해주세요</p>
            </div>
        `;
    }
}

// 지도에 경로 표시
function displayLocationMap(lat, lng, allLocations) {
    const mapDiv = document.getElementById('locationMap');

    // 구글 맵 embed (최근 위치 중심)
    mapDiv.innerHTML = `
        <iframe 
            width="100%" 
            height="500" 
            frameborder="0" 
            style="border:0; border-radius: 12px;"
            src="https://www.google.com/maps?q=${lat},${lng}&output=embed"
            allowfullscreen>
        </iframe>
    `;

    // TODO: 여러 위치를 연결하는 경로 표시는 Google Maps API 또는 Kakao Map API 필요
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', () => {
    loadLocationHistory();
});