document.addEventListener("DOMContentLoaded", () => {
	const scrollTopBtn = document.getElementById("scroll-top-btn");
	if (scrollTopBtn) {
		scrollTopBtn.addEventListener("click", scrollToTop);
	}

	if (window.location.pathname.endsWith("/weather")) {
		updateBackgroundBySunset();
		setInterval(updateBackgroundBySunset, 60 * 60 * 1000);
	}
});

function scrollToTop() {
	window.scrollTo({ top: 0, behavior: "smooth" });
}

async function updateBackgroundBySunset() {
	if (!navigator.geolocation) {
		alert("위치 정보를 가져올 수 없습니다.");
		return;
	}

	navigator.geolocation.getCurrentPosition(async (position) => {
		try {
			const lat = position.coords.latitude;
			const lon = position.coords.longitude;

			const response = await fetch(`https://api.sunrise-sunset.org/json?lat=${lat}&lng=${lon}&formatted=0`);
			const data = await response.json();

			const now = new Date();

			const sunrise = new Date(new Date(data.results.sunrise).getTime());
			const sunset = new Date(new Date(data.results.sunset).getTime());

			if (now >= sunrise && now < sunset) {
				document.body.classList.add('daytime');
				document.body.classList.remove('nighttime');
			} else {
				document.body.classList.add('nighttime');
				document.body.classList.remove('daytime');
			}
		} catch (error) {
			console.error("API 호출 중 오류:", error);
		}
	}, (err) => {
		console.error("위치정보 획득 실패:", err);
		alert("위치 정보를 가져오는 데 실패했습니다.");
	});
}
