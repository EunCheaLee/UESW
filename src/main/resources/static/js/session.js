setInterval(function() {
    fetch("/session/refresh", { 
        method: "GET" 
    })
    .then(response => {
        if (response.status === 401) {  // 세션이 없을 경우 401 반환
            clearInterval(this);  // 리프레시 중단
            window.location.href = "/login";  // 로그인 페이지로 리디렉션
        }
    });
}, 5 * 60 * 1000); // 5분마다