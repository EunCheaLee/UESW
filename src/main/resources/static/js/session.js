// 4시간(14400000ms)마다 세션을 새로고침
setInterval(function() {
    fetch('/session/refresh')
        .then(response => {
            if (response.status === 401) {
                // 세션 만료된 경우 로그인 페이지로 이동
                alert('세션이 만료되었습니다. 다시 로그인해주세요.');
                window.location.href = '/login';
            }
        })
        .catch(error => {
            console.error('Session refresh error:', error);
        });
}, 14400000); // 4시간마다 실행 (4 * 60 * 60 * 1000ms)