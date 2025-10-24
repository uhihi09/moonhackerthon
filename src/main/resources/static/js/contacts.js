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

// 연락처 목록 로드
async function loadContacts() {
    const contactsList = document.getElementById('contactsList');

    try {
        const contacts = await apiCall('/emergency-contacts');

        if (contacts.length === 0) {
            contactsList.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">📞</div>
                    <h3>등록된 긴급 연락처가 없습니다</h3>
                    <p>위의 폼에서 긴급 연락처를 추가해주세요</p>
                </div>
            `;
            return;
        }

        contactsList.innerHTML = contacts.map(contact => `
            <div class="contact-item">
                <div class="contact-info">
                    <div class="contact-name">${contact.name}</div>
                    <div class="contact-phone">📱 ${contact.phoneNumber}</div>
                    <span class="contact-relationship">${contact.relationship}</span>
                </div>
                <button class="btn btn-danger" onclick="deleteContact(${contact.id})">삭제</button>
            </div>
        `).join('');
    } catch (error) {
        console.error('연락처 로드 실패:', error);
        contactsList.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">⚠️</div>
                <h3>연락처를 불러올 수 없습니다</h3>
                <p>잠시 후 다시 시도해주세요</p>
            </div>
        `;
    }
}

// 연락처 추가
document.getElementById('addContactForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const name = document.getElementById('contactName').value.trim();
    const phoneNumber = document.getElementById('contactPhone').value.trim();
    const relationship = document.getElementById('contactRelationship').value.trim();

    if (!name || !phoneNumber || !relationship) {
        showError('모든 항목을 입력해주세요.');
        return;
    }

    // 전화번호 형식 검증
    const phoneRegex = /^010-\d{4}-\d{4}$/;
    if (!phoneRegex.test(phoneNumber)) {
        showError('전화번호 형식이 올바르지 않습니다. (010-1234-5678)');
        return;
    }

    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    showLoading(submitBtn);

    try {
        await apiCall('/emergency-contacts', 'POST', {
            name,
            phoneNumber,
            relationship
        });

        showSuccess('긴급 연락처가 추가되었습니다!');

        // 폼 초기화
        e.target.reset();

        // 목록 새로고침
        loadContacts();
    } catch (error) {
        console.error('연락처 추가 실패:', error);
        showError('연락처 추가에 실패했습니다. 다시 시도해주세요.');
    } finally {
        hideLoading(submitBtn, originalText);
    }
});

// 연락처 삭제
async function deleteContact(id) {
    if (!showConfirm('이 연락처를 삭제하시겠습니까?')) {
        return;
    }

    try {
        await apiCall(`/emergency-contacts/${id}`, 'DELETE');
        showSuccess('연락처가 삭제되었습니다.');
        loadContacts();
    } catch (error) {
        console.error('연락처 삭제 실패:', error);
        showError('연락처 삭제에 실패했습니다.');
    }
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', () => {
    loadContacts();
});