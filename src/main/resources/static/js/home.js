let map = null;
let polyline = null;
let markers = [];
let ps = null;
let clickedLatLng = null;
let currentLocationMarker = null;
let currentCircle = null;

document.addEventListener("DOMContentLoaded", () => {
	initDarkMode();
	initMap();
	initSearchInputs();
	initNavScrollEffect();
});

/*===================로그인====================*/


/*===================다크모드====================*/
function initDarkMode() {
	const toggleCheckbox = document.getElementById("toggle");
	if (!toggleCheckbox) {
		console.warn("⚠️ 'toggle' 요소를 찾을 수 없습니다.");
		return;
	}

	const darkModeClassList = [
		document.body,
		document.querySelector(".sidebar"),
		document.getElementById("main-content"),
		document.querySelector(".popup-content")
	];

	if (localStorage.getItem("dark-mode") === "true") {
		toggleCheckbox.checked = true;
		darkModeClassList.forEach(el => el?.classList.add("dark-mode"));
	}

	toggleCheckbox.addEventListener("change", () => {
		const isDark = toggleCheckbox.checked;
		darkModeClassList.forEach(el => el?.classList.toggle("dark-mode", isDark));
		localStorage.setItem("dark-mode", isDark);
	});
}

/*===================현재 위치====================*/
function initMap() {
	if (!navigator.geolocation) {
		alert("위치 정보를 사용할 수 없습니다.");
		return;
	}

	navigator.geolocation.getCurrentPosition(position => {
		const { latitude, longitude } = position.coords;
		sessionStorage.setItem("latitude", latitude);
		sessionStorage.setItem("longitude", longitude);

		console.log("지도 초기화 시작"); // ✅ 디버그
		kakao.maps.load(() => {
			const mapContainer = document.getElementById('map');
			if (!mapContainer) {
				console.error("#map 요소를 찾을 수 없습니다!");
				return;
			}
			const mapOption = {
				center: new kakao.maps.LatLng(latitude, longitude),
				level: 3
			};
			const mapInstance = new kakao.maps.Map(mapContainer, mapOption);
			const zoomControl = new kakao.maps.ZoomControl();
			mapInstance.addControl(zoomControl, kakao.maps.ControlPosition.BOTTOMLEFT);

			const marker = new kakao.maps.Marker({
				position: new kakao.maps.LatLng(latitude, longitude)
			});
			marker.setMap(mapInstance);

			map = mapInstance;
			ps = new kakao.maps.services.Places();

			updateHistoryUI();
			updateFavoritesUI();
			trackUserLocation();

			document.addEventListener('click', e => {
				const target = e.target;
				let className = '';

				// className을 안전하게 문자열로 변환
				if (typeof target.className === 'string') {
					className = target.className;
				} else if (typeof target.className === 'object' && typeof target.className.baseVal === 'string') {
					className = target.className.baseVal;
				}

				if (!className.includes('autocomplete-item')) {
					document.getElementById('start-list').innerHTML = '';
					document.getElementById('end-list').innerHTML = '';
				}
			});

			kakao.maps.event.addListener(map, 'click', function(mouseEvent) {
				clickedLatLng = mouseEvent.latLng;
				const clickBox = document.getElementById('map-click-assign');
				clickBox.style.display = 'block';
				clickBox.style.left = `${mouseEvent.point.x + 10}px`;
				clickBox.style.top = `${mouseEvent.point.y + 10}px`;
			});

			const searchBtn = document.getElementById('search-btn');
			searchBtn.removeEventListener('click', findRoutes);
			searchBtn.addEventListener('click', findRoutes);
			searchBtn.disabled = false;
		});
	}, () => {
		alert("위치 정보를 가져오는 데 실패했습니다.");
	});
}

function trackUserLocation() {
	if (!navigator.geolocation) return;
	navigator.geolocation.watchPosition(pos => {
		const lat = pos.coords.latitude, lng = pos.coords.longitude;
		const position = new kakao.maps.LatLng(lat, lng);
		if (currentLocationMarker) currentLocationMarker.setMap(null);
		if (currentCircle) currentCircle.setMap(null);

		currentLocationMarker = new kakao.maps.Marker({ position, map, zIndex: 10 });
		currentCircle = new kakao.maps.Circle({
			center: position,
			radius: 50,
			strokeWeight: 1,
			strokeColor: '#007aff',
			strokeOpacity: 0.8,
			fillColor: '#87cefa',
			fillOpacity: 0.4,
			map
		});
	});
}

/*===================네비게이션====================*/
function initSearchInputs() {
	const startInput = document.getElementById('start');
	const endInput = document.getElementById('end');

	startInput.addEventListener('input', () => autocomplete(startInput, 'start-list'));
	endInput.addEventListener('input', () => autocomplete(endInput, 'end-list'));
}

function autocomplete(inputEl, listId) {
	const keyword = inputEl.value.trim();
	const listBox = document.getElementById(listId);
	if (!keyword) return (listBox.innerHTML = '');

	ps.keywordSearch(keyword, (data, status) => {
		listBox.innerHTML = '';
		if (status === kakao.maps.services.Status.OK && data.length > 0) {
			data.slice(0, 7).forEach(place => {
				const div = document.createElement('div');
				div.className = 'autocomplete-item';
				div.innerText = place.place_name;
				div.onclick = () => {
					inputEl.value = place.place_name;
					listBox.innerHTML = '';
				};
				listBox.appendChild(div);
			});
		} else {
			listBox.innerHTML = '<div class="autocomplete-item">검색 결과 없음</div>';
		}
	});
}

function updateHistoryUI() {
	const arr = JSON.parse(localStorage.getItem('searchHistory') || '[]');
	const box = document.getElementById('history-list');
	box.innerHTML = arr.length ? '' : '기록 없음';
	arr.slice(0, 3).forEach((item, i) => {
		const div = document.createElement('div');
		div.className = 'item';

		const span = document.createElement('span');
		span.innerHTML = `<p>${item.start} <br/>→ ${item.end}</p>`;
		span.onclick = () => {
			document.getElementById('start').value = item.start;
			document.getElementById('end').value = item.end;
			findRoutes();
		};

		const dbtn = document.createElement('button');
		dbtn.className = 'delete-btn';
		dbtn.innerHTML = '<i class="bi bi-x-square-fill"></i>';
		dbtn.onclick = () => {
			arr.splice(i, 1); // 전체 배열에서 삭제
			localStorage.setItem('searchHistory', JSON.stringify(arr));
			updateHistoryUI();
		};

		div.appendChild(span);
		div.appendChild(dbtn);
		box.appendChild(div);
	});
}

function swapPlaces() {
	const s = document.getElementById('start');
	const e = document.getElementById('end');
	s.classList.add('swap-effect');
	e.classList.add('swap-effect');
	setTimeout(() => {
		[s.value, e.value] = [e.value, s.value];
		s.classList.remove('swap-effect');
		e.classList.remove('swap-effect');
	}, 150);
}

function addToFavorites() {
	const s = document.getElementById('start').value;
	const e = document.getElementById('end').value;
	if (!s || !e) return alert('즐겨찾기를 추가하려면 출발지와 도착지를 입력하세요.');
	let arr = JSON.parse(localStorage.getItem('favorites') || '[]');
	arr.unshift({ start: s, end: e });
	arr = arr.slice(0, 10);
	localStorage.setItem('favorites', JSON.stringify(arr));
	updateFavoritesUI();

	// ✅ 아이콘 바꾸기
	const icon = document.getElementById('fav-icon');
	icon.classList.remove('bi-star');
	icon.classList.add('bi-star-fill');

	// ✅ 0.5초 뒤에 다시 원래대로 복귀
	setTimeout(() => {
		icon.classList.remove('bi-star-fill');
		icon.classList.add('bi-star');
	}, 500);
}

function updateFavoritesUI() {
	const arr = JSON.parse(localStorage.getItem('favorites') || '[]');
	const box = document.getElementById('favorite-list');
	box.innerHTML = arr.length ? '' : '즐겨찾기 없음';
	arr.forEach((item, i) => {
		const div = document.createElement('div');
		div.className = 'item';
		const span = document.createElement('span');
		span.innerHTML = `<p>${item.start} <br/>→ ${item.end}</p>`;
		span.onclick = () => {
			document.getElementById('start').value = item.start;
			document.getElementById('end').value = item.end;
			findRoutes();
		};
		const dbtn = document.createElement('button');
		dbtn.className = 'delete-btn';
		dbtn.innerHTML = '<i class="bi bi-x-square-fill"></i>';
		dbtn.onclick = () => {
			arr.splice(i, 1);
			localStorage.setItem('favorites', JSON.stringify(arr));
			updateFavoritesUI();
		};
		div.appendChild(span);
		div.appendChild(dbtn);
		box.appendChild(div);
	});
}

function saveToHistory(s, e) {
	let arr = JSON.parse(localStorage.getItem('searchHistory') || '[]');
	arr.unshift({ start: s, end: e });
	arr = arr.slice(0, 5);
	localStorage.setItem('searchHistory', JSON.stringify(arr));
	updateHistoryUI();
}

function setCurrentLocation() {
	if (navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(pos => {
			const lat = pos.coords.latitude, lng = pos.coords.longitude;
			new kakao.maps.services.Geocoder().coord2Address(lng, lat, (res, status) => {
				if (status === kakao.maps.services.Status.OK) {
					document.getElementById('start').value = res[0].address.address_name;
				} else {
					alert('주소를 찾을 수 없습니다.');
				}
			});
		}, () => alert('위치 정보를 가져올 수 없습니다.'));
	}
}

function setClickedPoint(type) {
	const geocoder = new kakao.maps.services.Geocoder();
	geocoder.coord2Address(clickedLatLng.getLng(), clickedLatLng.getLat(), (res, status) => {
		if (status === kakao.maps.services.Status.OK) {
			const addr = res[0].address.address_name;
			document.getElementById(type).value = addr;
			findRoutes();
		} else {
			alert('주소를 찾을 수 없습니다.');
		}
	});
	document.getElementById('map-click-assign').style.display = 'none';
}

function trackUserLocation() {
	if (!navigator.geolocation) return;
	navigator.geolocation.watchPosition(pos => {
		const lat = pos.coords.latitude, lng = pos.coords.longitude;
		const position = new kakao.maps.LatLng(lat, lng);
		if (currentLocationMarker) currentLocationMarker.setMap(null);
		if (currentCircle) currentCircle.setMap(null);
		currentLocationMarker = new kakao.maps.Marker({
			position: position,
			map: map,
			zIndex: 10
		});
		currentCircle = new kakao.maps.Circle({
			center: position,
			radius: 50,
			strokeWeight: 1,
			strokeColor: '#007aff',
			strokeOpacity: 0.8,
			fillColor: '#87cefa',
			fillOpacity: 0.4,
			map: map
		});
	});
}

function findRoutes() {
	const s = document.getElementById('start').value;
	const e = document.getElementById('end').value;
	const t = document.getElementById('departureTime').value;
	if (!s || !e) return alert('출발지와 도착지를 입력하세요.');
	saveToHistory(s, e);
	document.getElementById('loading').style.display = 'block';

	fetch('/api/navigation/routes', {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ origin: s, destination: e, departureTime: t })
	})
		.then(r => r.json())
		.then(data => {
			document.getElementById('loading').style.display = 'none';
			if (data.error) return alert(data.error);

			const coords = data.path.map(p => new kakao.maps.LatLng(p.y, p.x));
			if (polyline) polyline.setMap(null);
			markers.forEach(m => m.setMap(null));
			markers = [];

			polyline = new kakao.maps.Polyline({
				path: coords,
				strokeWeight: 5,
				strokeColor: '#FF0000',
				strokeOpacity: 0.9,
				strokeStyle: 'solid'
			});
			polyline.setMap(map);

			const sm = new kakao.maps.Marker({ position: new kakao.maps.LatLng(data.start.y, data.start.x) });
			const em = new kakao.maps.Marker({ position: new kakao.maps.LatLng(data.end.y, data.end.x) });
			sm.setMap(map);
			em.setMap(map);
			markers.push(sm, em);

			const bounds = new kakao.maps.LatLngBounds();
			coords.forEach(p => bounds.extend(p));
			map.setBounds(bounds);

			document.getElementById('result').innerHTML =
				`<div class="route-card">
            <div class="route-title">${s} → ${e}</div>
            <div class="route-info">약 ${(data.summary.duration / 60).toFixed(0)}분 / ${(data.summary.distance / 1000).toFixed(2)}km</div>
            <div style="font-size:12px; color:#888; margin-top:6px;">
              ⚠️ 실제 소요 시간과 거리는 교통 상황에 따라 달라질 수 있습니다.
            </div>
          </div>`;
		});
}


/*===================메뉴 색 변경====================*/
function initNavScrollEffect() {
	const navbar = document.querySelector(".navbar");
	const menu = document.querySelector(".menu");
	const sections = document.querySelectorAll(".home-section");

	function updateNavbarByActiveDot() {
		let closestIndex = 0;
		let minDiff = Infinity;
		const viewportCenter = window.scrollY + window.innerHeight / 2;

		sections.forEach((section, index) => {
			const sectionCenter = section.offsetTop + section.offsetHeight / 2;
			const diff = Math.abs(sectionCenter - viewportCenter);

			if (diff < minDiff) {
				minDiff = diff;
				closestIndex = index;
			}
		});

		if (closestIndex === 1) {
			navbar.classList.add("navbar-scrolled");
			menu.classList.add("menu-scrolled");
		} else {
			navbar.classList.remove("navbar-scrolled");
			menu.classList.remove("menu-scrolled");
		}
	}

	window.addEventListener("scroll", updateNavbarByActiveDot);
	window.addEventListener("load", updateNavbarByActiveDot); // ✅ DOM 완전히 로드 후 한 번 실행
}