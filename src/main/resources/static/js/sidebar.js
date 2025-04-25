let markerCluster, mapInstance;

const districts = [
  { name: "강남구", lat: 37.5172, lon: 127.0473 },
  { name: "강동구", lat: 37.5301, lon: 127.1238 },
  { name: "강북구", lat: 37.6396, lon: 127.0257 },
  { name: "강서구", lat: 37.5509, lon: 126.8495 },
  { name: "관악구", lat: 37.4781, lon: 126.9516 },
  { name: "광진구", lat: 37.5385, lon: 127.0823 },
  { name: "구로구", lat: 37.4955, lon: 126.887 },
  { name: "금천구", lat: 37.4569, lon: 126.8958 },
  { name: "노원구", lat: 37.6542, lon: 127.0568 },
  { name: "도봉구", lat: 37.6688, lon: 127.047 },
  { name: "동대문구", lat: 37.5744, lon: 127.0396 },
  { name: "동작구", lat: 37.5124, lon: 126.9393 },
  { name: "마포구", lat: 37.5663, lon: 126.9014 },
  { name: "서대문구", lat: 37.579, lon: 126.9368 },
  { name: "서초구", lat: 37.4836, lon: 127.0327 },
  { name: "성동구", lat: 37.5633, lon: 127.0367 },
  { name: "성북구", lat: 37.5894, lon: 127.0167 },
  { name: "송파구", lat: 37.5145, lon: 127.1059 },
  { name: "양천구", lat: 37.5169, lon: 126.8664 },
  { name: "영등포구", lat: 37.5264, lon: 126.8963 },
  { name: "용산구", lat: 37.5323, lon: 126.9907 },
  { name: "은평구", lat: 37.6173, lon: 126.9216 },
  { name: "종로구", lat: 37.5729, lon: 126.9794 },
  { name: "중구", lat: 37.5636, lon: 126.9972 },
  { name: "중랑구", lat: 37.6063, lon: 127.0927 }
];

const favoriteDistricts = JSON.parse(localStorage.getItem("favorite-districts") || "[]");

// 공통 함수: 지역 버튼 렌더링
function renderDistricts(list, container) {
  container.innerHTML = ''; // 기존 내용 제거

  list.forEach(d => {
    const isFav = favoriteDistricts.includes(d.name);
    
    // 버튼 및 스타 버튼을 감싸는 wrapper 생성
    const wrapper = document.createElement("div");
    wrapper.className = "district-wrapper";

    // 지역 버튼 생성
    const btn = document.createElement("button");
    btn.textContent = d.name;
    btn.className = "district-button";
    btn.onclick = () => getWeatherByDistrict(d.lat, d.lon, d.name);

    // 즐겨찾기 버튼 생성
    const starBtn = document.createElement("button");
    starBtn.innerHTML = isFav ? "⭐" : "☆";
    starBtn.className = "fav-btn";
    starBtn.onclick = () => {
      toggleDistrictFavorite(d.name);
      renderDistrictButtons(); // 즐겨찾기 상태가 바뀌면 다시 렌더링
    };

    // 버튼들을 wrapper에 추가
    wrapper.appendChild(btn);
    wrapper.appendChild(starBtn);

    // container에 wrapper 추가
    container.appendChild(wrapper);
  });
}

// 즐겨찾기 상태 토글
function toggleDistrictFavorite(name) {
  const index = favoriteDistricts.indexOf(name);
  if (index !== -1) favoriteDistricts.splice(index, 1);
  else favoriteDistricts.push(name);
  localStorage.setItem("favorite-districts", JSON.stringify(favoriteDistricts));
}

// 기존의 renderDistrictButtons 함수 수정
function renderDistrictButtons() {
  const districtButtonContainer = document.querySelector(".button-container");
  const sorted = [...districts].sort((a, b) => {
    const aFav = favoriteDistricts.includes(a.name);
    const bFav = favoriteDistricts.includes(b.name);
    return aFav === bFav ? 0 : aFav ? -1 : 1;
  });
  renderDistricts(sorted, districtButtonContainer);
  renderFavoriteSidebar(); // 즐겨찾기 사이드바도 새로 고침
}

// 기존의 renderFavoriteSidebar 함수 수정
function renderFavoriteSidebar() {
  const favContainer = document.querySelector(".favorite-districts");
  if (!favContainer) return;

  favContainer.innerHTML = "";
  renderDistricts(favoriteDistricts.map(name => districts.find(d => d.name === name)), favContainer);
}

// 날씨 데이터 불러오기
async function getWeatherByDistrict(lat, lon, name) {
  const apiKey = "8d7e97191ae9cf92a3a4e3b1e90b8651";
  const content = document.getElementById("main-content");
  const oldBox = document.querySelector(".weather-box");
  if (oldBox) oldBox.remove();

  const box = document.createElement("div");
  box.className = "weather-box";
  content.prepend(box);

  box.innerHTML = `<h1>기상정보</h1><p>🔄 ${name} 날씨 불러오는 중...</p>`;

  try {
    const [weatherResponse, airResponse] = await Promise.all([
      fetch(`https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&appid=${apiKey}&units=metric&lang=kr`),
      fetch(`https://api.openweathermap.org/data/2.5/air_pollution?lat=${lat}&lon=${lon}&appid=${apiKey}`)
    ]);

    const weatherData = await weatherResponse.json();
    const airData = await airResponse.json();

    if (!weatherData || !airData) throw new Error("데이터가 불완전합니다.");

    const aqi = airData.list[0].main.aqi;
    const pm25 = airData.list[0].components["pm2_5"];
    const aqiDescription = ["좋음", "보통", "나쁨", "매우 나쁨"][aqi - 1];

    const hour = new Date().getHours();
    const weatherMain = weatherData.weather[0].main.toLowerCase();
    let themeClass = "day-clear";
    if (hour >= 18 || hour < 6) themeClass = "night";
    else if (weatherMain.includes("cloud")) themeClass = "cloudy";
    else if (weatherMain.includes("rain")) themeClass = "rainy";
    else if (weatherMain.includes("snow")) themeClass = "snowy";

    box.classList.add(themeClass);
    box.innerHTML = `
      <h1>${name} 날씨</h1>
      <p>🌡️ 온도: ${weatherData.main.temp}°C</p>
      <p>🌤️ 상태: ${weatherData.weather[0].description}</p>
      <p>💧 습도: ${weatherData.main.humidity}%</p>
      <p>🌫️ 미세먼지: ${aqiDescription} (AQI: ${aqi})</p>
      <p>💨 초미세먼지 (PM2.5): ${pm25} µg/m³</p>
    `;
  } catch (err) {
    console.error(err);
    box.innerHTML = `<p>❌ 날씨 정보를 불러오지 못했습니다. 다시 시도해 주세요.</p>`;
  }
}

// 나머지 코드들 (교통 이벤트, CCTV 로드 등) 그대로 유지

// URL 파라미터에서 section 값 가져오기
function getSectionParam() {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get('section');
}

window.addEventListener('DOMContentLoaded', () => {
  const section = getSectionParam();
  if (section) {
    showContent(section); // 자동 실행
  }
});

function showContent(section) {
  const content = document.getElementById('main-content');

  if (section === 'traffic') {
    content.innerHTML = `<h1>교통속보</h1><div id="map"></div>`;
    setTimeout(() => {
      mapInstance = L.map("map").setView([37.55, 127.0], 12);
      L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution: "&copy; OpenStreetMap contributors"
      }).addTo(mapInstance);
      markerCluster = L.markerClusterGroup();
      mapInstance.addLayer(markerCluster);
      loadTrafficEvents();
    }, 0);
  } else if (section === 'weather') {
    content.innerHTML = `<h1>서울 기상정보</h1><div class="button-container"></div>`;
    renderDistrictButtons();
  } else if (section === 'settings') {
    content.innerHTML = `
      <h1>설정</h1>
      <label><input type="checkbox" id="dark-mode-toggle"/> 🌙 다크모드</label>`;
    const toggle = document.getElementById("dark-mode-toggle");
    const savedMode = localStorage.getItem("dark-mode");
    if (savedMode === "true") {
      document.body.classList.add("dark-mode");
      document.querySelector(".sidebar").classList.add("dark-mode");
      document.getElementById("main-content").classList.add("dark-mode");
      document.querySelector(".popup-content").classList.add("dark-mode");
      toggle.checked = true;
    }
    toggle.addEventListener("change", () => {
      const isDark = toggle.checked;
      document.body.classList.toggle("dark-mode", isDark);
      document.querySelector(".sidebar").classList.toggle("dark-mode", isDark);
      document.getElementById("main-content").classList.toggle("dark-mode", isDark);
      document.querySelector(".popup-content").classList.toggle("dark-mode", isDark);
      localStorage.setItem("dark-mode", isDark);
    });
  } else if (section === 'cctv') {
	content.innerHTML = `
	          <h1>CCTV 검색</h1>
	          <select id="cctv-type-select">
	            <option value="local">국도</option>
	            <option value="highway">고속도로 (서울권)</option>
	          </select>
	          <button id="load-cctv-btn">📡 CCTV 불러오기</button>
	          <div id="map"></div>`;
	        setTimeout(() => {
	          mapInstance = L.map("map").setView([37.55, 127.0], 12);
	          L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
	            attribution: "&copy; OpenStreetMap contributors"
	          }).addTo(mapInstance);
	          markerCluster = L.markerClusterGroup();
	          mapInstance.addLayer(markerCluster);
	          document.getElementById("load-cctv-btn").addEventListener("click", () => {
	            const sourceType = document.getElementById("cctv-type-select").value === "highway" ? "ex" : "its";
	            markerCluster.clearLayers();
	            loadCctv(sourceType);
	          });
	        }, 0);
	      }
}

function loadCctv(sourceType) {
  const apiKey = "8ef5e4da4f8c4627a93bb9d1f574a4c2";
  const url = `https://openapi.its.go.kr:9443/cctvInfo?apiKey=${apiKey}&type=${sourceType}&cctvType=1&minX=126.8&maxX=127.2&minY=37.4&maxY=37.7&getType=xml`;
  fetch(url)
    .then(res => res.text())
	.then(xmlText => {
	          const xml = new DOMParser().parseFromString(xmlText, "application/xml");
	          xml.querySelectorAll("data").forEach(item => {
	            const name = item.querySelector("cctvname")?.textContent;
	            const url = item.querySelector("cctvurl")?.textContent;
	            const format = item.querySelector("cctvformat")?.textContent;
	            const lat = parseFloat(item.querySelector("coordy")?.textContent);
	            const lon = parseFloat(item.querySelector("coordx")?.textContent);
	            if (format !== "HLS" || !lat || !lon || !url) return;
	            const marker = L.marker([lat, lon]);
	            marker.bindPopup(`<b>${name}</b><br><video id="video-${lat}" controls autoplay muted width="300"></video>`);
	            marker.on("click", () => {
	              setTimeout(() => {
	                const video = document.getElementById(`video-${lat}`);
	                if (Hls.isSupported()) {
	                  const hls = new Hls();
	                  hls.loadSource(url);
	                  hls.attachMedia(video);
	                } else {
	                  video.src = url;
	                }
	              }, 200);
	            });
	            markerCluster.addLayer(marker);
	          });
	        });
}

function loadTrafficEvents() {
  const apiKey = "49586b55684d414436315377796157";
  const url = `https://openapi.its.go.kr:9443/eventInfo?apiKey=${apiKey}&type=all&getType=json`;
  fetch(url)
    .then(res => res.json())
    .then(data => {
      data.body.items.forEach(ev => {
        const lat = parseFloat(ev.coordy);
        const lon = parseFloat(ev.coordx);
        const type = ev.eventtype;
        const color = type.includes("사고") ? "red" : type.includes("정체") ? "orange" : "gray";
        const marker = L.marker([lat, lon], {
          icon: L.divIcon({
            className: 'custom-icon',
            html: `<div style="background:${color};width:20px;height:20px;border-radius:50%;border:2px solid white"></div>`
          })
        });
        marker.bindPopup(`<b>${ev.eventtitle}</b><br>${ev.eventdetail}`);
        markerCluster.addLayer(marker);
      });
    });
}

function getTodayDateStr() {
  return new Date().toISOString().split("T")[0];
}


document.addEventListener("DOMContentLoaded", function () {
  showPopupOncePerDay("🚧 성수대교 인근 2차로 사고 발생! 주의하세요.");
});

function checkFavoritesAlertConditions() {
  const favorites = JSON.parse(localStorage.getItem('favoriteDistricts')) || [];
  
  favorites.forEach(districtName => {
    const districtElement = document.querySelector(`.district[data-name="${districtName}"]`);
    if (!districtElement) return;

    const weather = districtElement.dataset.weather || '';
    const air = districtElement.dataset.air || '';
    const traffic = districtElement.dataset.traffic || '';

    const hasRainOrSnow = weather.includes('비') || weather.includes('눈');
    const badAirQuality = air.includes('나쁨') || air.includes('매우나쁨');
    const hasAccident = traffic.includes('사고');

    if (hasRainOrSnow || badAirQuality || hasAccident) {
      showFavoriteAlert(districtName, { hasRainOrSnow, badAirQuality, hasAccident });
    }
  });
}

function showFavoriteAlert(districtName, conditions) {
  let messages = [];
  if (conditions.hasRainOrSnow) messages.push('☔ 비/눈');
  if (conditions.badAirQuality) messages.push('💨 대기질 나쁨');
  if (conditions.hasAccident) messages.push('🚧 교통사고');

  const message = `
    <div class="fav-alert">
      <strong>${districtName}</strong>에 주의: ${messages.join(', ')}
    </div>
  `;

  const alertBox = document.createElement('div');
  alertBox.innerHTML = message;
  alertBox.classList.add('fav-alert-box');
  document.body.appendChild(alertBox);

  setTimeout(() => {
    alertBox.remove();
  }, 8000);
}

// 자동 5분마다 체크
setInterval(checkFavoritesAlertConditions, 5 * 60 * 1000);

// 페이지 로딩 후 10초 뒤 첫 체크
setTimeout(checkFavoritesAlertConditions, 10000);

// nav 클릭 시 sidebar 접기
document.querySelectorAll(".sidebar nav a").forEach(navItem => {
  navItem.addEventListener("click", () => {
    document.querySelector(".sidebar").classList.add("collapsed");
  });
});
