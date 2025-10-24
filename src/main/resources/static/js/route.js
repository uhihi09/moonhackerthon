// js/route.js url=js/route.js
document.addEventListener('DOMContentLoaded', function() {
    initRouteMap();

    // 복사 버튼 이벤트
    document.querySelectorAll('.copy-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const coords = this.previousElementSibling.textContent;
            copyToClipboard(coords);
            alert('좌표가 복사되었습니다.');
        });
    });
});

function initRouteMap() {
    const map = document.getElementById('routeMap');
    if (map) {
        // 경로 지도 초기화 로직
        console.log('경로 지도 초기화');
    }
}

function copyToClipboard(text) {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand('copy');
    document.body.removeChild(textarea);
}