document.addEventListener('DOMContentLoaded', function () {
    const authLink = document.getElementById('login-button');
    const authIcon = document.getElementById('login-img');
    const authText = document.getElementById('login-txt');

    // 로컬스토리지에서 로그인 상태 확인
    const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';

    // 페이지 로딩 시 로그인 상태에 맞게 UI 업데이트
    updateAuthUI(isLoggedIn);

    // 로그인/로그아웃 클릭 시 상태 변경
    authLink.addEventListener('click', function (e) {
        e.preventDefault(); // 링크 클릭 시 페이지 이동 방지

        const currentStatus = localStorage.getItem('isLoggedIn') === 'true';

        if (currentStatus) {
            // 로그아웃 처리
            localStorage.setItem('isLoggedIn', 'false');
            updateAuthUI(false);  // UI 업데이트
            alert('로그아웃 되었습니다.');
            // 로그아웃 후 리다이렉트 (필요시)
            // location.href = '/'; // 홈 페이지로 이동
        } else {
            // 로그인 처리 (로그인 페이지로 이동)
            location.href = '/login'; // 로그인 페이지 경로
        }
    });
});




