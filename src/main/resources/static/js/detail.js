// js/detail.js url=js/detail.js
document.addEventListener('DOMContentLoaded', function() {
    initDetailMap();

    // 복사 버튼
    document.querySelector('.copy-btn-small').addEventListener('click', function() {
        const coords = document.querySelector('.coords-small').textContent;
        copyToClipboard(coords);
        alert('좌표가 복사되었습니다.');
    });

    // 신고 버튼
    document.querySelector('.police-btn-large').addEventListener('click', function() {
        if (confirm('112에 신고하시겠습니까?')) {
            window.location.href = 'tel:112';
        }
    });

    document.querySelector('.fire-btn-large').addEventListener('click', function() {
        if (confirm('119에 신고하시겠습니까?')) {
            window.location.href = 'tel:119';
        }
    });
});

function initDetailMap() {
    const map = document.getElementById('detailMap');
    if (map) {
        // 상세 지도 초기화 로직
        console.log('상세 지도 초기화');
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