let openedInfoWindow = null; // ✅ 열려 있는 인포윈도우 추적용 전역 변수
let userLat = null;
let userLon = null;
let remainTimer = null;
let favonlyState = false;  // 초기엔 전체 보기

document.addEventListener("DOMContentLoaded", function() {
	if (navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(function(position) {
			const latitude = position.coords.latitude;
			const longitude = position.coords.longitude;

			// 위치 저장
			sessionStorage.setItem("latitude", latitude);
			sessionStorage.setItem("longitude", longitude);

			// Kakao 지도 초기화
			kakao.maps.load(function() {
				const container = document.getElementById('public-map');
				if (!container) return;

				const options = {
					center: new kakao.maps.LatLng(latitude, longitude),
					level: 3
				};

				window.mapInstance = new kakao.maps.Map(container, options);

				// 지도 이동 감지
				kakao.maps.event.addListener(mapInstance, 'idle', function() {
					if (!userLat || !userLon) return;

					const center = mapInstance.getCenter();
					const distance = getDistance(userLat, userLon, center.getLat(), center.getLng());

					const btn = document.getElementById('searchHereBtn');
					if (distance > 0.5) { // 예: 300m 이상 이동 시 표시
						btn.style.display = 'block';
					} else {
						btn.style.display = 'none';
					}
				});

				// 클러스터러 설정
				window.markerCluster = new kakao.maps.MarkerClusterer({
					map: mapInstance,
					averageCenter: true,
					minLevel: 5
				});

				const imageSrc = 'img/dot.png';
				const imageSize = new kakao.maps.Size(45, 45); // 마커이미지의 크기입니다
				const imageOption = { offset: new kakao.maps.Point(27, 69) }; // 마커이미지의 옵션입니다. 마커의 좌표와 일치시킬 이미지 안에서의 좌표를 설정합니다.

				// 마커의 이미지정보를 가지고 있는 마커이미지를 생성합니다
				const markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize, imageOption);

				// 사용자 위치 LatLng 객체 생성
				const markerPosition = new kakao.maps.LatLng(latitude, longitude);

				// 사용자 위치 마커 표시
				const marker = new kakao.maps.Marker({
					position: markerPosition,
					image: markerImage
				});
				marker.setMap(mapInstance);

				// ✅ 말풍선 HTML (닫기 버튼 포함)
				const overlayContent = `
				  <div class="custom-infowindow" onclick="closeOverlay()">
				    📍 내 위치<br/><small style="color:lightgray;">(눌러서 닫기)</small>
				  </div>
				`;

				const overlay = new kakao.maps.CustomOverlay({
					content: overlayContent,
					position: markerPosition,
					yAnchor: 1,
					zIndex: 3
				});

				// ✅ 오버레이 열기
				kakao.maps.event.addListener(marker, 'click', function() {
					overlay.setMap(mapInstance);
				});

				// ✅ 전역에서 닫을 수 있도록 함수 정의
				window.closeOverlay = function() {
					overlay.setMap(null);
				};
			});
		});
	}
	updateFavoriteButtons();
});

// ✅ 슬라이딩 박스 및 마커 변수
let slideIsOpen = false;
let currentMarkers = [];

// ✅ 최초에 한 번만 클릭 감지 이벤트 바인딩
document.addEventListener('click', function(event) {
	const btn = document.getElementById('toggleSubway');
	const slideBox = document.getElementById('slideBox');

	if (
		slideIsOpen &&
		!slideBox.contains(event.target) &&
		!btn.contains(event.target)
	) {
		slideBox.classList.remove('active');
		slideBox.innerHTML = '';
		document.body.style.overflow = '';
		btn.textContent = '실시간 지하철';
		slideIsOpen = false;
	}
});


// ✅ 폼 이벤트 및 자동완성 처리
function initSubwayScript() {
	// remainTimer 매번 새로 초기화
	if (remainTimer) clearInterval(remainTimer);
	remainTimer = setInterval(() => {
		document.querySelectorAll('.remain').forEach(el => {
			let sec = parseInt(el.dataset.seconds);
			if (isNaN(sec)) return;
			sec--;
			el.dataset.seconds = sec;
			el.textContent = sec > 0 ? `${Math.floor(sec / 60)}분 ${sec % 60}초` : '도착';
		});
	}, 1000);

	// ✅ 추가 부분: message 표시
	const messageBox = document.getElementById('message-box');
	if (messageBox && messageBox.textContent.trim() !== "") {
		messageBox.style.display = 'block';
		setTimeout(() => {
			messageBox.style.display = 'none';
		}, 3000);  // 3초 뒤에 사라짐
	}

	const stationForm = document.getElementById("station-form");
	const stationInput = document.getElementById('station-input');
	const datalist = document.getElementById('stations');
	let stationList = [];

	if (!stationForm || !stationInput || !datalist) {
		console.warn("역 추가 폼이 아직 렌더링되지 않았습니다.");
		return;
	}

	// datalist 초기화
	datalist.querySelectorAll("option").forEach(option => {
		stationList.push(option.value);
	});

	// 입력값이 바뀔 때마다 자동완성 필터링
	stationInput.addEventListener('focus', () => {
		datalist.innerHTML = '';

		stationList.slice(0, 5).forEach(station => {
			const opt = document.createElement('option');
			opt.value = station;
			datalist.appendChild(opt);
		});
	});
	stationInput.addEventListener('input', () => {
		const inputVal = stationInput.value.toLowerCase();
		datalist.innerHTML = '';

		stationList
			.filter(station => station.toLowerCase().includes(inputVal))
			.slice(0, 5)
			.forEach(station => {
				const opt = document.createElement('option');
				opt.value = station;
				datalist.appendChild(opt);
			});
	});
	document.querySelector("button[value='add']").addEventListener("click", () => submitWithAction("add"));
	document.querySelectorAll(".del-btn").forEach(button => {
		button.addEventListener("click", function() {
			const stationName = this.dataset.stationName;
			deleteStation(stationName);
		});
	});
	document.querySelectorAll(".star-btn").forEach(button => {
		button.addEventListener("click", function() {
			const stationName = this.closest("h2").querySelector("span").textContent;
			toggleFavorite(stationName);
		});
	});
	document.addEventListener("click", function(e) {
		if (e.target && e.target.classList.contains("filter-btn")) {
			const favonly = e.target.dataset.favonly === "1";
			filterFavorite(favonly);
		}
	});
};

function updateFavoriteButtons() {
	const favBtn = document.getElementById("fav-btn");
	const allBtn = document.getElementById("all-btn");

	if (favBtn) favBtn.style.display = favonlyState ? "none" : "inline-block";
	if (allBtn) allBtn.style.display = favonlyState ? "inline-block" : "none";
}

async function refreshStationBlock() {
	const res = await fetch(`/train_realtime/fragment?favonly=${favonlyState ? 1 : 0}`);
	const html = await res.text();
	document.querySelector('.info-section').innerHTML = html;
	initSubwayScript();  // 이벤트 바인딩 다시 적용
	updateFavoriteButtons(); // 버튼 상태도 갱신
}

// ✅ 추가 서버 요청
function submitWithAction(actionValue) {
	const stationForm = document.getElementById("station-form");
	const formData = new FormData(stationForm);
	formData.append("action", actionValue);  // ✅ 버튼 클릭될 때 value 바로 추가

	fetch("/train_realtime", {
		method: "POST",
		body: formData
	}).then(response => response.text())
		.then(async text => {
			if (text === "성공") {
				await refreshStationBlock();
			} else {
				alert("오류 발생");
			}
		});
}

// ✅ 삭제 처리 함수
function deleteStation(stationName) {
	fetch('/train_realtime/delete', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({ stationName })
	})
		.then(response => {
			if (response.ok) {
				alert(`${stationName} 삭제 성공`);
				refreshStationBlock();  // 부분 새로고침
			} else {
				alert("삭제 실패");
			}
		})
		.catch(error => {
			console.error('에러 발생:', error);
			alert("서버 오류");
		});
}

function toggleFavorite(stationName) {
	const formData = new FormData();
	formData.append("station", stationName);
	formData.append("action", "favorite");

	fetch("/train_realtime", {
		method: "POST",
		body: formData
	})
		.then(response => response.text())
		.then(async text => {
			if (text === "성공") {
				await refreshStationBlock();  // 별 눌렀을 때 실시간 새로고침
			} else {
				alert("오류 발생");
			}
		});
}

function filterFavorite(favonly) {
	favonlyState = favonly;  // 상태 갱신

	fetch(`/train_realtime/fragment?favonly=${favonly ? 1 : 0}`)
		.then(res => res.text())
		.then(html => {
			document.querySelector('.info-section').innerHTML = html;
			initSubwayScript();  // 다시 이벤트 바인딩
			updateFavoriteButtons();  // 버튼 상태 갱신
		})
		.catch(err => {
			console.error("필터링 오류", err);
			alert("데이터를 불러오는데 실패했습니다.");
		});
}

function updateFavoriteButtons() {
	const favBtn = document.getElementById('fav-btn');
	const allBtn = document.getElementById('all-btn');

	if (favBtn) {
		favBtn.style.display = favonlyState ? 'none' : 'inline-block';
	}
	if (allBtn) {
		allBtn.style.display = favonlyState ? 'inline-block' : 'none';
	}
}

function openMapPopup() {
	const win = window.open('', '_blank', 'width=1000,height=800');
	win.document.write(`
	        <html>
	          <head>
	            <title>노선도 전체 보기</title>
	            <style>
	              body {
	                margin: 0;
	                background: #fdfdfd;
	                font-family: Arial, sans-serif;
	                color: #333;
	                text-align: center;
	              }
	              h1 {
	                background: #3498db;
	                color: white;
	                margin: 0;
	                padding: 20px 0;
	                font-size: 24px;
	                box-shadow: 0 2px 5px rgba(0,0,0,0.1);
	              }
	              img {
	                max-width: 100%;
	                height: auto;
	                margin-top: 10px;
	                box-shadow: 0 4px 12px rgba(0,0,0,0.1);
	                border-radius: 8px;
	              }
	            </style>
	          </head>
	          <body>
	            <h1>서울 지하철 노선도</h1>
	            <img src="https://www.sisul.or.kr/open_content/skydome/images/img_subway.png" draggable="false">
	          </body>
	        </html>
	      `);
}

function toggleSubway() {
	const btn = document.getElementById('toggleSubway');
	const slideBox = document.getElementById('slideBox');

	if (!slideIsOpen) {
		slideBox.classList.add('active');
		fetch('/train_realtime')
			.then(res => res.text())
			.then(html => {
				slideBox.innerHTML = html;
				initSubwayScript();  // ✅ 폼 이벤트 & remainTimer 이 안에 포함됨
				setTimeout(() => {
					if (window.mapInstance) {
						window.mapInstance.relayout();
					}
				}, 400);
				btn.textContent = '닫기';
			});
	} else {
		slideBox.classList.remove('active');
		slideBox.innerHTML = '';
		btn.textContent = '실시간 지하철';
	}
	slideIsOpen = !slideIsOpen;
}

function toggleSubwayState() {
	if (!window.markerCluster) {
		console.warn("markerCluster가 아직 초기화되지 않았습니다.");
		return;
	}

	clearMap();
	subwayLocal();
}

function subwayLocal() {
	const ps = new kakao.maps.services.Places(mapInstance);
	ps.categorySearch('SW8', placesSearchCB, { useMapBounds: true });

	function placesSearchCB(data, status, pagination) {
		if (status === kakao.maps.services.Status.OK) {
			for (let i = 0; i < data.length; i++) {
				displayMarker(data[i]);
			}
		} else if (status === kakao.maps.services.Status.ZERO_RESULT) {
			alert('검색 결과 없음');
		} else {
			alert('검색 에러 발생');
		}
	}

	function displayMarker(place) {
		const lat = place.y;
		const lon = place.x;
		const name = place.place_name;

		const marker = new kakao.maps.Marker({
			map: mapInstance,
			position: new kakao.maps.LatLng(lat, lon)
		});

		currentMarkers.push(marker);

		const content = `<div class="custom-infowindow">🚇 ${name}</div>`;

		const overlay = new kakao.maps.CustomOverlay({
			content: content,
			position: new kakao.maps.LatLng(lat, lon),
			yAnchor: 1,
			zIndex: 3
		});

		// 마커 클릭 시 오버레이 표시
		kakao.maps.event.addListener(marker, 'click', function() {
			overlay.setMap(mapInstance);
		});

		// 지도를 클릭하면 모든 커스텀 오버레이 닫기
		kakao.maps.event.addListener(mapInstance, 'click', function() {
			overlay.setMap(null);
		});
	}
}

// 내 위치 저장
navigator.geolocation.getCurrentPosition(function(position) {
	userLat = position.coords.latitude;
	userLon = position.coords.longitude;
});

async function busLocal(userLat, userLon) {
	const myLat = userLat ?? parseFloat(sessionStorage.getItem("latitude"));
	const myLon = userLon ?? parseFloat(sessionStorage.getItem("longitude"));

	// 🔄 Spring Boot API로 정류소 목록 받아오기 -> 정상
	const response = await fetch("/api/bus/markers");
	const stations = await response.json();

	stations.forEach(station => {
		const lat = parseFloat(station.lat);
		const lon = parseFloat(station.lon);
		const name = station.name;
		const ars_id = station.arsId;
		const route = station.busRouteId;

		if (isNaN(lat) || isNaN(lon)) return;

		// 사용자와의 거리 계산
		const distance = getDistance(myLat, myLon, lat, lon);
		if (distance > 2) return;

		// 마커 생성
		const marker = new kakao.maps.Marker({
			position: new kakao.maps.LatLng(lat, lon),
			map: mapInstance
		});

		currentMarkers.push(marker);

		// 말풍선 내용
		const content = `
	      <div class="custom-infowindow">
	        🚌 ${name}<br/>
	        <small>${ars_id}</small>
			<br/><a>${route}</a>
	      </div>
	    `;

		const overlay = new kakao.maps.CustomOverlay({
			content: content,
			position: new kakao.maps.LatLng(lat, lon),
			yAnchor: 1,
			zIndex: 3
		});

		// 클릭 시 오버레이 표시
		kakao.maps.event.addListener(marker, 'click', () => {
			if (openedInfoWindow) openedInfoWindow.setMap(null);
			overlay.setMap(mapInstance);
			openedInfoWindow = overlay;
		});

		// 지도 클릭 시 말풍선 닫기
		kakao.maps.event.addListener(marker, 'click', async () => {
			if (openedInfoWindow) openedInfoWindow.setMap(null);
			overlay.setMap(mapInstance);
			openedInfoWindow = overlay;

			// 🚌 도착 정보 가져오기
			try {
				const res = await fetch(`/proxy/bus?busRouteId=${route}`);
				const xmlText = await res.text();
				const xml = new DOMParser().parseFromString(xmlText, "application/xml");

				const items = xml.getElementsByTagName("itemList");

				let html = `<h4>${name}</h4>`;
				if (items.length === 0) {
					html += `<p>🚫 도착 예정 정보 없음</p>`;
				} else {
					for (let item of items) {
						const rtNm = item.getElementsByTagName("rtNm")[0]?.textContent ?? "버스";
						const arrmsg1 = item.getElementsByTagName("arrmsg1")[0]?.textContent ?? "정보 없음";
						const arrmsg2 = item.getElementsByTagName("arrmsg2")[0]?.textContent ?? "";

						html += `
			        <div style="margin-bottom: 8px;">
			          <strong>🚌 ${rtNm}</strong><br/>
			          - ${arrmsg1}<br/>
			          ${arrmsg2 ? `- ${arrmsg2}<br/>` : ""}
			        </div>
			      `;
					}
				}

				const panel = document.getElementById("busInfoPanel");
				panel.innerHTML = html;
				panel.style.display = "block";

			} catch (e) {
				console.error("버스 도착 정보 불러오기 실패", e);
				const panel = document.getElementById("busInfoPanel");
				panel.innerHTML = "<p>❌ 도착 정보를 불러오는 데 실패했습니다.</p>";
				panel.style.display = "block";
			}
		});
	});

	// 재검색 버튼 이벤트
	const btn = document.getElementById('searchHereBtn');
	if (btn) {
		btn.addEventListener('click', () => {
			const center = mapInstance.getCenter();
			clearMap();
			busLocal(center.getLat(), center.getLng());
		});
	}
}

// ✅ 버스 마커 관련
function toggleBusState() {
	if (!window.markerCluster) {
		console.warn("markerCluster가 아직 초기화되지 않았습니다.");
		return;
	}

	clearMap();
	busLocal(userLat, userLon);
}

// ✅ 마커 제거
function clearMap() {
	if (!window.markerCluster) return;

	// 마커 숨기기
	currentMarkers.forEach(marker => {
		marker.setMap(null);						 // 지도에서 제거
		window.markerCluster.removeMarker(marker); // ✅ 클러스터에서도 제거
	});

	currentMarkers = [];
}

// ✅ 사용자 위치와 대상 위치 사이의 거리 계산 함수 (단위: km)
function getDistance(lat1, lon1, lat2, lon2) {
	const R = 6371; // 지구 반지름 (km)
	const dLat = (lat2 - lat1) * Math.PI / 180;
	const dLon = (lon2 - lon1) * Math.PI / 180;

	const a =
		Math.sin(dLat / 2) * Math.sin(dLat / 2) +
		Math.cos(lat1 * Math.PI / 180) *
		Math.cos(lat2 * Math.PI / 180) *
		Math.sin(dLon / 2) * Math.sin(dLon / 2);

	const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	return R * c;
}