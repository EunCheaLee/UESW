document.addEventListener("DOMContentLoaded", function () {
  // 쿠키 확인
  const cookies = document.cookie.split(';').map(c => c.trim());
  const hideNotice = cookies.find(c => c === 'hideNotice=true');

  if (!hideNotice) {
    // 도로 돌발상황 모달 로드
    fetch('/api/notice')
      .then(response => response.text())
      .then(xml => {
        const parser = new DOMParser();
        const xmlDoc = parser.parseFromString(xml, "text/xml");
        const items = xmlDoc.getElementsByTagName("event");

        let html = '';
        for (let i = 0; i < Math.min(items.length, 5); i++) {
          const eventType = items[i].getElementsByTagName("eventType")[0]?.textContent || '알 수 없음';
          const roadName = items[i].getElementsByTagName("roadName")[0]?.textContent || '미확인 도로';
          const msg = items[i].getElementsByTagName("eventDetail")[0]?.textContent || '상세 정보 없음';
          html += `<p><strong>[${eventType}]</strong> ${roadName} - ${msg}</p>`;
        }

        document.getElementById('notice-content').innerHTML = html || '표시할 데이터가 없습니다.';
        document.getElementById('noticeModal').style.display = 'block';
      })
      .catch(err => {
        document.getElementById('notice-content').innerHTML = '도로 정보를 불러오지 못했습니다.';
        document.getElementById('noticeModal').style.display = 'block';
        console.error(err);
      });
  }
});

// 오늘 하루 보지 않기 버튼 함수
function hideToday() {
  const today = new Date();
  today.setHours(23, 59, 59); // 오늘 자정까지
  document.cookie = "hideNotice=true; path=/; expires=" + today.toUTCString();
  document.getElementById('noticeModal').style.display = 'none';
}