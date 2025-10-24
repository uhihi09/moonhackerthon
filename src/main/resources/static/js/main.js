// js/main.js url=js/main.js
document.addEventListener('DOMContentLoaded', function() {
    // 지도 초기화 (여기서는 간단한 예시)
    initMap();

    // 긴급신고 버튼 이벤트
    document.querySelector('.police-btn').addEventListener('click', function() {
        if (confirm('112에 신고하시겠습니까?')) {
            window.location.href = 'tel:112';
        }
    });

    document.querySelector('.fire-btn').addEventListener('click', function() {
        if (confirm('119에 신고하시겠습니까?')) {
            window.location.href = 'tel:119';
        }
    });
});

function initMap() {
    // 실제로는 Google Maps API나 Kakao Maps API를 사용
    const map = document.getElementById('map');
    if (map) {
        // 지도 초기화 로직
        console.log('메인 지도 초기화');
    }
}