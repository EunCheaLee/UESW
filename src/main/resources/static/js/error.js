let gameLoopInterval;
let dx = 0, dy = 0;
let speed = 150;
let snake, food, score;

function showStartButton() {
  	document.querySelector(".bi-reply-fill").outerHTML = 
  		`<a href="/" style="text-decoration:none; color:inherit;">
                       <i class="bi bi-house-fill"></i>
         </a>`;
  	document.querySelector("#intro p").innerHTML = "너무 놀라지 마세요!<br/> 이제 홈페이지로 복귀하거나<br/>간단한 게임을 즐길 수 있습니다.";
  	document.getElementById("startContainer").style.display = "block";
}

function startGame() {
	// ⚠️ 오류 정보는 그대로 두고 인트로만 숨기고 게임 화면 보여줌
  document.getElementById("error-intro-wrapper").style.display = "none";
  document.getElementById("game").style.display = "block";
  
    // 🧠 안내 문구 2초간 출력
  const gameTitle = document.querySelector("#game h1");
  const originalText = gameTitle.textContent;
  gameTitle.textContent = "방향키를 움직이세요";
  setTimeout(() => {
    gameTitle.textContent = originalText;
  }, 1000);
  
  initGame();
  gameLoopInterval = setInterval(gameLoop, speed);
}

function initGame() {
  snake = [{ x: 10, y: 10 }];
  food = { x: 5, y: 5 };
  dx = dy = 0;
  score = 0;
  speed = 150;
}

function draw() {
  const canvas = document.getElementById("gameCanvas");
  const ctx = canvas.getContext("2d");
  const gridSize = 20;
  const tileCount = canvas.width / gridSize;

  ctx.fillStyle = "#000";
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  ctx.fillStyle = "lime";
  for (let s of snake) {
    ctx.fillRect(s.x * gridSize, s.y * gridSize, gridSize - 2, gridSize - 2);
  }

  ctx.fillStyle = "red";
  ctx.fillRect(food.x * gridSize, food.y * gridSize, gridSize - 2, gridSize - 2);

  document.getElementById("score").textContent = "점수: " + score;
}

function update() {
  const head = { x: snake[0].x + dx, y: snake[0].y + dy };
  const canvas = document.getElementById("gameCanvas");
  const tileCount = canvas.width / 20;

  if (head.x < 0 || head.x >= tileCount || head.y < 0 || head.y >= tileCount) {
    return resetGame();
  }

  for (let s of snake) {
    if (s.x === head.x && s.y === head.y) {
      return resetGame();
    }
  }

  snake.unshift(head);

  if (head.x === food.x && head.y === food.y) {
    score++;
    food = {
      x: Math.floor(Math.random() * tileCount),
      y: Math.floor(Math.random() * tileCount)
    };
    if (speed > 50) {
      speed -= 5;
      clearInterval(gameLoopInterval);
      gameLoopInterval = setInterval(gameLoop, speed);
    }
  } else {
    snake.pop();
  }
}

function gameLoop() {
  update();
  draw();
}

function resetGame() {
  clearInterval(gameLoopInterval);
  initGame();
  gameLoopInterval = setInterval(gameLoop, speed);
}

document.addEventListener("keydown", (e) => {
  if (e.key === "ArrowUp" && dy === 0) dx = 0, dy = -1;
  else if (e.key === "ArrowDown" && dy === 0) dx = 0, dy = 1;
  else if (e.key === "ArrowLeft" && dx === 0) dx = -1, dy = 0;
  else if (e.key === "ArrowRight" && dx === 0) dx = 1, dy = 0;
});

window.showStartButton = showStartButton;
window.startGame = startGame;