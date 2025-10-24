// 로그인 처리
document.getElementById('loginForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;

    if (!username || !password) {
        showError('아이디와 비밀번호를 입력해주세요.');
        return;
    }

    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    showLoading(submitBtn);

    try {
        const data = await apiCall('/auth/login', 'POST', { username, password });

        // 토큰 저장
        localStorage.setItem('authToken', data.token);
        localStorage.setItem('username', username);

        showSuccess('로그인 성공!');
        window.location.href = '/dashboard.html';
    } catch (error) {
        hideLoading(submitBtn, originalText);
        showError('로그인 실패! 아이디와 비밀번호를 확인해주세요.');
    }
});

// 회원가입 처리
document.getElementById('signupForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const name = document.getElementById('name').value.trim();
    const phone = document.getElementById('phone').value.trim();

    // 유효성 검사
    if (!username || !password || !name || !phone) {
        showError('모든 항목을 입력해주세요.');
        return;
    }

    if (password !== confirmPassword) {
        showError('비밀번호가 일치하지 않습니다.');
        return;
    }

    if (password.length < 4) {
        showError('비밀번호는 최소 4자 이상이어야 합니다.');
        return;
    }

    const phoneRegex = /^010-\d{4}-\d{4}$/;
    if (!phoneRegex.test(phone)) {
        showError('전화번호 형식이 올바르지 않습니다. (010-1234-5678)');
        return;
    }

    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    showLoading(submitBtn);

    try {
        await apiCall('/auth/signup', 'POST', { username, password, name, phone });

        showSuccess('회원가입 완료! 로그인 페이지로 이동합니다.');
        setTimeout(() => {
            window.location.href = '/index.html';
        }, 1000);
    } catch (error) {
        hideLoading(submitBtn, originalText);
        showError('회원가입 실패! ' + (error.message || '다시 시도해주세요.'));
    }
});