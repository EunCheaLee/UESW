function find(keyword) {
    alert(keyword+"를 찾으시겠습니까?");
    return false;
}

  /*로그인 실패 스크립트*/
    let failedAttempts = 0;
    const maxAttempts = 3;

    const loginForm = document.getElementById('loginForm');
    const errorMessage = document.getElementById('error-message');
    const message = document.getElementById('message');

    loginForm.addEventListener('submit', function (event) {
      event.preventDefault();

      const username = document.getElementById('username').value;
      const password = document.getElementById('password').value;

      const correctUsername = 'user123';
      const correctPassword = 'password123';

      if (username === correctUsername && password === correctPassword) {
        failedAttempts = 0;
        message.textContent = '로그인 성공!';
        errorMessage.textContent = '';
      } else {
        failedAttempts++;
        message.textContent = '';
        if (failedAttempts >= maxAttempts) {
          errorMessage.textContent = '비밀번호 틀린 횟수가 너무 많습니다. 잠시 후 다시 시도해주세요.';
        } else {
          errorMessage.textContent = `비밀번호가 틀렸습니다. ${failedAttempts}번 틀렸습니다.`;
        }
      }
    });