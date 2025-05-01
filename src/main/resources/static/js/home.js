function updateAuthUI(isLoggedIn) {
    const authText = document.getElementById('login-txt');
    authText.textContent = isLoggedIn ? 'Logout' : 'Login';
}

// 주소 변환 함수
async function getDistrictName(lat, lon) {
    const response = await fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json`);
    const data = await response.json();
    return data.address.city || data.address.county || data.address.town || "알 수 없음";
}

// 로그인 상태 체크용 함수
function checkLoginStatus() {
    fetch("/menu-login")
        .then(response => response.text())
        .then(status => {
            if (status === "already-logged-in") {
                alert("이미 로그인 하셨습니다!");
            } else if (status === "not-logged-in") {
                window.location.href = "/login";
            }
        })
        .catch(err => {
            console.error("로그인 상태 확인 실패:", err);
        });
}


document.addEventListener('DOMContentLoaded', function() {
    const authLink = document.getElementById('login-button');
    const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
    updateAuthUI(isLoggedIn);

    authLink.addEventListener('click', function (e) {
        e.preventDefault();
        const currentStatus = localStorage.getItem('isLoggedIn') === 'true';
        if (currentStatus) {
            localStorage.setItem('isLoggedIn', 'false');
            updateAuthUI(false);
            alert('로그아웃 되었습니다.');
        } else {
            location.href = '/login';
        }
    });
});
