
document.addEventListener('DOMContentLoaded', function () {
    // 로그인 상태 체크
    const authLink = document.getElementById('auth-link');
    const authIcon = document.getElementById('auth-icon');
    const authText = document.getElementById('auth-text');

    // 로컬스토리지에서 로그인 상태 확인
    const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';

    // 로그인 상태에 따라 아이콘과 텍스트 변경
    if (isLoggedIn) {
        authIcon.src = '/img/login.png';  // 로그아웃 아이콘
        authText.textContent = 'Logout';
    } else {
        authIcon.src = '/img/login.png';   // 로그인 아이콘
        authText.textContent = 'Login';
    }

    // 로그인/로그아웃 클릭 시 상태 변경
    authLink.addEventListener('click', function () {
        if (isLoggedIn) {
            // 로그아웃 처리
            localStorage.setItem('isLoggedIn', 'false');
            authIcon.src = '/img/login.png';
            authText.textContent = 'Login';
        } else {
            // 로그인 처리
            localStorage.setItem('isLoggedIn', 'true');
            authIcon.src = '/img/login.png';
            authText.textContent = 'Logout';
        }
    });
});
