// 최상단
window.mapInstance = null;
window.markerCluster = null;

// CSS 로딩 함수 정의
function loadCssFile(filename) {
  const link = document.createElement("link");
  link.rel = "stylesheet";
  link.href = filename;
  link.type = "text/css";
  document.head.appendChild(link);
}

// ========== CCTV ===========
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
	content.innerHTML = `
	<div class="traffic-map-wrapper">
	    <div id="traffic-map"></div>
		<div id="traffic-news-title" class="slide-down"></div>
	</div>
	  `;
    setTimeout(() => {
      window.mapInstance = L.map("traffic-map").setView([37.55, 127.0], 12);
      L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution: "&copy; OpenStreetMap contributors"
      }).addTo(mapInstance);
	  
	  if (!window.markerCluster) {
	    window.markerCluster = L.markerClusterGroup();
	  }
	  window.mapInstance.addLayer(window.markerCluster);
	  
      loadTrafficEvents();
    }, 0);
	
  } else if (section === 'amenities') {
	content.innerHTML = `
		<div class="map-wrapper">
		    <div id="amenities-map"></div>
		</div>
		    <div id="amenities-toolbar">
		      <button onclick="toggleToiletSeoul()">🚽 공공화장실<br/>(서울)</button>
		      <button onclick="toggleToilet()">🚽 공공화장실<br/>(경기도)</button>
		      <button onclick="togglePark()">🚗 공영주차장</button>
		      <button onclick="clearMap()">🧹 지도<br/>초기화</button>
		    </div>
	  `;
	  setTimeout(() => {
	    window.mapInstance = L.map("amenities-map").setView([37.55, 127.0], 12);
	    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
	      attribution: "&copy; OpenStreetMap contributors"
	    }).addTo(mapInstance);
	  
	  if (!window.markerCluster) {
	    window.markerCluster = L.markerClusterGroup();
	  }
	  window.mapInstance.addLayer(window.markerCluster);
	  }, 0);
	
  }  else if (section === 'weather') {
		  content.innerHTML = `
							<div id="weather-result"></div>  <!-- ✅ 이 부분 추가 -->
		  					<div id="weather_button">
							<div class="button-container"></div>
							</div>
							`;
		  renderDistrictButtons();

		  // 🧭 종로구 기본 날씨 표시
		  const jongnoLat = 37.5729;
		  const jongnoLon = 126.9794;
		  const jongnoName = "종로구";

		  // 종로구 날씨 정보 불러오기
		  getWeatherByDistrict(jongnoLat, jongnoLon, jongnoName);

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
	<div class="cctv-box">
      <select id="cctv-type-select">
        <option value="local">국도</option>
        <option value="highway">고속도로 (서울권)</option>
		<option value="seoul"> 서울 </option>
      </select>
      <button id="load-cctv-btn">📡 CCTV 불러오기</button>
	  <div class="map-wrapper">
      	<div id="map"></div>
	  </div>
	  </div>`;
    setTimeout(() => {
      window.mapInstance = L.map("map").setView([37.55, 127.0], 10);
      L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution: "&copy; OpenStreetMap contributors"
      }).addTo(mapInstance);
      window.markerCluster = L.markerClusterGroup();
      window.mapInstance.addLayer(window.markerCluster);
      document.getElementById("load-cctv-btn").addEventListener("click", () => {
        const selectedType = document.getElementById("cctv-type-select").value;
        window.markerCluster.clearLayers();
		
		if (selectedType === "seoul") {
			loadSeoulCctv(); // 🔴 서울일 때는 이 함수 실행
		} else {
			const sourceType = document.getElementById("cctv-type-select").value === "highway" ? "ex" : "its"; // 국도 or 고속도로
			loadCctv(sourceType); // 국도 or 고속도로
		}
      });
    }, 0);
  }
}

function loadCctv(sourceType) {
  const apiKey = CONFIG.ITS_CCTV_API_KEY;
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
	            window.markerCluster.addLayer(marker);
	          });
	        });
}

function loadSeoulCctv() {
    const apiKey = CONFIG.UTIC_API_KEY;

    fetch('/xml/cctv.xml')
        .then(res => res.text())
        .then(str => new window.DOMParser().parseFromString(str, 'text/xml'))
        .then(data => {
            const cctvs = data.getElementsByTagName("cctv");

            Array.from(cctvs).forEach(cctv => {
                const cctvid = cctv.getElementsByTagName("cctvid")[0].textContent;
                const rawname = cctv.getElementsByTagName("cctvname")[0].textContent.trim();
                const id = cctv.getElementsByTagName("id")[0].textContent;
                const cctvch = cctv.getElementsByTagName("cctvch")[0].textContent;
                const lat = parseFloat(cctv.getElementsByTagName("ycoord")[0].textContent);
                const lng = parseFloat(cctv.getElementsByTagName("xcoord")[0].textContent);
                
                if (isNaN(lat) || isNaN(lng)) return;

                const marker = L.marker([lat, lng]);

                marker.bindPopup(`
                    <div style="width: 250px;">
                        <h3 style="margin-bottom: 5px;">📍 ${rawname}</h3>
                        <p id="info-${cctvid}">영상 확인 중...</p>
                        <button id="btn-${cctvid}" style="display:none;">▶ 영상 보기</button>
                        <p style="font-size:9pt; color:gray;">경찰청(UTIC) 제공</p>
                    </div>
                `);

                marker.on("click", async () => {
                    const infoText = document.getElementById(`info-${cctvid}`);
                    const videoBtn = document.getElementById(`btn-${cctvid}`);

                    const workingUrl = await getWorkingCctvUrl({ apiKey, cctvid, cctvname: rawname, kind: "Seoul", id, cctvch });

                    if (id === "0" || !workingUrl) {
                        videoBtn.style.display = "none";
                        infoText.innerText = "🚫 이 CCTV는 영상이 제공되지 않습니다.";
                    } else {
                        // URL 유효성 검사 시도
                        const valid = await isCctvUrlWorking(workingUrl);
                        if (!valid) {
                            videoBtn.style.display = "none";
                            infoText.innerText = "❌ 현재 해당 CCTV 서버 연결 불가";
                        } else {
                            videoBtn.style.display = "inline-block";
                            infoText.innerText = "🔴 영상 보기를 누르면 새 창에서 재생됩니다.";
                            videoBtn.onclick = () => {
                                try {
                                    window.open(workingUrl, "_blank", "width=640,height=480");
                                } catch (err) {
                                    alert("팝업 차단을 해제해 주세요.");
                                }
                            }
                        }
                    }
                });

                markerCluster.addLayer(marker);
            });
        });
}

async function getWorkingCctvUrl({ apiKey, cctvid, cctvname, kind, id, cctvch }) {
    const baseUrl = "https://www.utic.go.kr/jsp/map/openDataCctvStream.jsp";
    const url = `${baseUrl}?key=${apiKey}&cctvid=${cctvid}&cctvName=${encodeURIComponent(cctvname)}&kind=${kind}&cctvip=undefined&cctvch=${cctvch}&id=${id}&cctvpasswd=undefined&cctvport=undefined`;
    return url;
}

// 스트림 유효성 검사 (HEAD 요청 활용)
async function isCctvUrlWorking(url) {
    try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 3000);
        const response = await fetch(url, { method: 'HEAD', signal: controller.signal });
        clearTimeout(timeoutId);
        return response.ok;
    } catch (err) {
        return false;
    }
}

function loadTrafficEvents() {
	const apiKey = CONFIG.UTIC_API_KEY; 
	const url = `http://www.utic.go.kr/guide/imsOpenData.do?key=${apiKey}`;

	fetch(url)
	  .then(res => res.text())
	  .then(xmlText => {
		const xml = new DOMParser().parseFromString(xmlText, "application/xml");
	    const records = xml.querySelectorAll("record");
	    
		if (!records || records.length === 0) {
		      console.warn("🚫 유효한 교통 이벤트 항목이 없습니다.");
		      return;
	    }
		
		// 🔽 선언 추가
		const grouped = {};  // 또는 let grouped = {};
		const titles = [];
		
		records.forEach(record => {
			const title = record.querySelector("incidentTitle")?.textContent || "제목 없음";
			const lat = parseFloat(record.querySelector("locationDataY")?.textContent);
			const lon = parseFloat(record.querySelector("locationDataX")?.textContent);
			const typeCd = record.querySelector("incidenteTypeCd")?.textContent;
			const start = record.querySelector("startDate")?.textContent
			const end = record.querySelector("endDate")?.textContent
			const roadname = record.querySelector("roadName")?.textContent
			
			// 좌표가 유효한 경우에만 진행해야 함
			if (isNaN(lat) || isNaN(lon)) return;
			
			titles.push(title); // 제목 저장
		  		
			const key = `${lat},${lon}`;
				if (!grouped[key]) {
					grouped[key] = {
						titles: [],
						roadname,
						start,
						end,
						typeCd
					};
				}
				grouped[key].titles.push(title);
			});
		
		// ✅ 한 번만 실행되는 애니메이션 함수
		      const titleBox = document.getElementById("traffic-news-title");
		      let index = 0;

			  function showNextTitle() {
			    // 기존 텍스트 제거
			    titleBox.innerHTML = "";

			    // 새 텍스트 div 생성
			    const div = document.createElement("div");
			    div.className = "text";
			    div.textContent = titles[index];

			    // 추가하고, 약간 지연 후 애니메이션 클래스 부여
			    titleBox.appendChild(div);
			    setTimeout(() => {
			      div.classList.add("show");
			    }, 50);

			    index = (index + 1) % titles.length;
			  }


		      showNextTitle();
		      setInterval(showNextTitle, 5000);
			  
	  // ✅ 마커 생성
		Object.entries(grouped).forEach(([key, value]) => {
		  const [lat, lon] = key.split(',').map(parseFloat);
		  const { titles, roadname, start, end, typeCd } = value;

		  // 사고 유형에 따른 색상 구분
		  let color = "gray";
		  if (typeCd === "1") color = "red";      // 사고
		  else if (typeCd === "2") color = "orange";  // 고장
		  else if (typeCd === "3") color = "blue";    // 통제
		  
		  const icon = L.divIcon({
			className: "custom-sign",
	        html: `<div style="background:${color}; color:white; font-weight:bold; text-align:center; width:30px; height:30px; line-height:30px; border-radius:5px;">🚧</div>`,
	        iconSize: [30, 30],
	        iconAnchor: [15, 15]
	      });

		  const marker = L.marker([lat, lon], { icon });

		  const popupHtml = `
		    <h2>${roadname || '도로명 없음'}</h2>
		    ${titles.map(t => `<b>${t}</b>`).join('<br/>')}
		    <br/><br/>
		    <b style="font-size:8pt;">[시작일: ${start || '-'}] <br/> [종료일: ${end || '-'}]</b>
		    <br/>
		    <p style="font-size:9pt; color:lightgray">경찰청(UTIC) 제공</p>
		  `;

		  marker.bindPopup(popupHtml);

		  window.markerCluster.addLayer(marker);
		});
	  })
	  .catch(err => {
	    console.error("❌ 교통 이벤트 불러오기 실패:", err);
	  });
}

function findPublicToilet(){
	const api_key = CONFIG.DATA_GG_API_KEY;

	let currentPage = 1;
	const totalPages = 11;

	const interval = setInterval(() => {
	  if (currentPage > totalPages) {
	    clearInterval(interval);
	    console.log("✅ 화장실 데이터 불러오기 완료");
	    return;
	  }

	  const url = `https://openapi.gg.go.kr/Publtolt?KEY=${api_key}&Type=xml&pIndex=${currentPage}&pSize=100`;
	  console.log(`📦 요청 중: ${url}`);

	  fetch(url)
	    .then(res => res.text())
	    .then(str => new window.DOMParser().parseFromString(str, 'text/xml'))
	    .then(data => {
	      const rows = data.getElementsByTagName("row");
	      console.log(`✅ ${currentPage}페이지: ${rows.length}건 수신됨`);

	      Array.from(rows).forEach(row => {
	        const loc = row.getElementsByTagName("PBCTLT_PLC_NM")[0]?.textContent;
	        const address = row.getElementsByTagName("REFINE_LOTNO_ADDR")[0]?.textContent;
	        const lat = parseFloat(row.getElementsByTagName("REFINE_WGS84_LAT")[0]?.textContent);
	        const lon = parseFloat(row.getElementsByTagName("REFINE_WGS84_LOGT")[0]?.textContent);

	        if (isNaN(lat) || isNaN(lon)) return;

	        const icon = L.divIcon({
	          className: "custom-sign",
	          html: `<div style="background:#5a87ff; color:white; font-size:14px; width:28px; height:28px; text-align:center; line-height:28px; border-radius:50%;">🚻</div>`,
	          iconSize: [28, 28],
	          iconAnchor: [14, 14],
	        });

	        const marker = L.marker([lat, lon], { icon });
	        marker.bindPopup(`<strong>${loc}</strong><br>${address}`);
	        window.markerCluster.addLayer(marker);
	      });
	    })
	    .catch(err => {
	      console.error(`❌ ${currentPage}페이지 요청 실패:`, err);
	    });

	  currentPage++;
	}, 1000); // 1초 간격
		}
		
function findPublicToiletSeoul(){
	const mapDiv = document.getElementById("amenities-map");
	if (!mapDiv) {
	  console.warn("🚫 지도 container(#amenities-map)가 없습니다. 아직 DOM에 추가되지 않았을 수 있습니다.");
	  return;
	}

	    // XML 파일 불러오기
	    fetch('/xml/toilet.xml') // 파일 경로는 실제 서버 경로에 맞게 조정
	      .then(res => res.text())
	      .then(str => new window.DOMParser().parseFromString(str, 'text/xml'))
	      .then(data => {
	        const toilets = data.getElementsByTagName("toilet");

	        Array.from(toilets).forEach(t => {
	          const name = t.getElementsByTagName("name")[0]?.textContent;
	          const address = t.getElementsByTagName("roadadd")[0]?.textContent;
	          const lat = parseFloat(t.getElementsByTagName("lat")[0]?.textContent);
	          const lon = parseFloat(t.getElementsByTagName("lot")[0]?.textContent);

	          if (isNaN(lat) || isNaN(lon)) return;

	          const icon = L.divIcon({
	            className: "custom-sign",
	            html: `<div style="background:#5a87ff; color:white; font-size:14px; width:28px; height:28px; text-align:center; line-height:28px; border-radius:50%;">🚻</div>`,
	            iconSize: [28, 28],
	            iconAnchor: [14, 14],
	          });

	          const marker = L.marker([lat, lon], { icon });
	          marker.bindPopup(`<strong>${name}</strong><br>${address}`);
	          window.markerCluster.addLayer(marker);
	        });
	      })
	      .catch(err => console.error("❌ 서울 공중화장실 XML 로딩 오류:", err));
		}

function parkState(){
	const api_key = CONFIG.DATA_SEOUL_API_KEY;
	
	let currentPage = 1;
	const totalPages = 9;
  	const seen = new Set(); // 중복 체크용 Set 선언
	
	const interval = setInterval(() => {
		  if (currentPage > totalPages) {
		    clearInterval(interval);
		    console.log("✅ 주차장 데이터 불러오기 완료");
		    return;
		  }

		  const url = `http://openapi.seoul.go.kr:8088/${api_key}/xml/GetParkInfo/${currentPage}/1000/`;
		  console.log(`📦 요청 중: ${url}`);

		  fetch(url)
		    .then(res => res.text())
		    .then(str => new window.DOMParser().parseFromString(str, 'text/xml'))
		    .then(data => {
		      const rows = data.getElementsByTagName("row");
		      console.log(`✅ ${currentPage}페이지: ${rows.length}건 수신됨`);

			  
		      Array.from(rows).forEach(row => {
		        const name = row.getElementsByTagName("PKLT_NM")[0]?.textContent;
		        const address = row.getElementsByTagName("ADDR")[0]?.textContent;
		        const lat = parseFloat(row.getElementsByTagName("LAT")[0]?.textContent);
		        const lon = parseFloat(row.getElementsByTagName("LOT")[0]?.textContent);
				const code = row.getElementsByTagName("PKLT_CD")[0]?.textContent;  // 고유 코드
				
		        if (!code || isNaN(lat) || isNaN(lon)) return;

				if (seen.has(code)) return;  // 주차장 코드가 이미 있으면 건너뜀
			  	seen.add(code);              // 처음 본 코드만 처리
				
		        const icon = L.divIcon({
		          className: "custom-sign",
		          html: `<div style="background:#5a87ff; color:white; font-size:14px; width:28px; height:28px; text-align:center; line-height:28px; border-radius:50%;">🅿️</div>`,
		          iconSize: [28, 28],
		          iconAnchor: [14, 14],
		        });

				const marker = L.marker([lat, lon], { icon });
				  marker.bindPopup(`<strong>${name}</strong><br>${address}`);
				  window.markerCluster.addLayer(marker);
		      });
		    })
		    .catch(err => {
		      console.error(`❌ ${currentPage}페이지 요청 실패:`, err);
		    });

		  currentPage++;
		}, 1000); // 1초 간격
}



function toggleTraffic() {
  markerCluster.clearLayers();
  loadTrafficEvents(); // 교통 이벤트 마커만 다시 로드
}

function togglePark(){
 	markerCluster.clearLayers();
	parkState();
}  

function toggleToilet() {
	if (!markerCluster) {
	  console.warn("markerCluster가 아직 초기화되지 않았습니다.");
	  return;
	}
	
	  markerCluster.clearLayers();
	  findPublicToilet();
}

function toggleToiletSeoul() {
	if (!markerCluster) {
	  console.warn("markerCluster가 아직 초기화되지 않았습니다.");
	  return;
	}
	
	  markerCluster.clearLayers();
	  findPublicToiletSeoul();
}

function clearMap() {
  markerCluster.clearLayers(); // 모든 마커 제거
}

function startTrafficEventUpdates() {
  loadTrafficEvents();
  setInterval(() => {
    markerCluster.clearLayers();
    loadTrafficEvents();
  }, 5 * 60 * 1000); // 5분마다 갱신
}

function getTodayDateStr() {
  return new Date().toISOString().split("T")[0];
}


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
