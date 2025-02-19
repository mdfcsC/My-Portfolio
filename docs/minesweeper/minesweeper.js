// 宏定义常量
const ROWS = 20;
const COLS = 20;
const ALLTILES = ROWS * COLS;
const BOMB_PROBABLITY = 0.10;

// 开始游戏按钮
const buttonNewGame = document.querySelector(".newGame");
// 炸弹数组
let mines = [];
// 未点击的瓦片数组
let untouchedTiles = [];
// 布尔，游戏是否可玩
let playable;
// 布尔，是否为自动点击方块
let autoClick;
// 计时器
let timer = document.querySelector(".timer");
// 计时中断间隔
let timerInterval;
// 轮数
let roundTimer = document.querySelector(".roundTimer");
// 记分板
const buttonShowRanking = document.querySelector(".showRanking");
// 布尔，本轮分数是否已记录
let scoreRecorded;

// 开始游戏按钮点击事件
buttonNewGame.addEventListener("click", () => {
    const board = document.querySelector(".board");
    // 清空棋盘
    board.replaceChildren();
    // 清空数组
    mines.length = 0;
    untouchedTiles.length = 0;
    // 游戏可玩
    playable = true;
    // 计时器归零
    timer.textContent = "00 : 00";
    // 清除旧的计时中断器
    clearInterval(timerInterval);
    // 增加轮数
    if (roundTimer.textContent === "?") {
        roundTimer.textContent = "1";
    } else {
        roundTimer.textContent++;
    }
    // 分数未记录
    scoreRecorded = false;

    // 生成棋盘瓦片
    for(let i = 1; i <= ALLTILES; i++) {
        const tile = document.createElement("div"); // 注意这里不需要加尖括号
        tile.setAttribute("id", `tile_${i}`);
        // 添加瓦片点击事件
        tile.addEventListener("click", () => touchTile(i, false));
        tile.addEventListener("contextmenu", (event) => {
            event.preventDefault(); // 阻止默认右键菜单
            leaveFlag(i);
        });
        board.appendChild(tile);

        // 生成炸弹
        const p = Math.random();
        if (p <= BOMB_PROBABLITY) {
            mines.push(`tile_${i}`);
        }

        // 生成未点击瓦片数组,事实上初始化时包括了所有瓦片
        untouchedTiles.push(`tile_${i}`);
    }

    // 开始计时
    startTimer(timer);

    // //调试阶段：控制台输出炸弹数组
    // console.log("bombs location: ", mines)
});

// 点击瓦片函数
function touchTile(tileId, autoClick) {
    if (!playable || !untouchedTiles.includes(`tile_${tileId}`)) {
        return;
    }

    if (autoClick) {
        // 该瓦片是炸弹，不进行点击
        if (mines.includes(`tile_${tileId}`)) {
            return;
        }
        if (mineNeighbours(tileId) > 0 && Math.random() < 0.8) {
            return;
        }
    }

    const t = document.getElementById(`tile_${tileId}`);

    // 该瓦片是炸弹，游戏结束
    if (mines.includes(`tile_${tileId}`)) {
        t.className = "bomb";
        t.textContent = "*";
        playable = false;
        // 时差显示所有炸弹、时差弹窗提示游戏结束
        setTimeout(() => {
            revealAllMines();
            setTimeout(() => {
                alert("GAME OVER!");
            }, 250);
        }, 250);
        return;
    }
    // 该瓦片不是炸弹，显示周围炸弹数量
    else {
        // 从未点击数组中删除该瓦片
        untouchedTiles.splice(untouchedTiles.indexOf(`tile_${tileId}`), 1);
        t.className = "clear";
        t.textContent = ""; // 清空原有内容（旗子）
        // 检查四周瓦片是否有炸弹，若有则该格瓦片显示周围炸弹数量
        const bombNeighbour = mineNeighbours(tileId);
        if (bombNeighbour > 0) {
            t.textContent = `${bombNeighbour}`;
            switch (bombNeighbour) {
                case 1:
                    t.style.color = "blue";
                    break;
                case 2:
                    t.style.color = "green";
                    break;
                case 3:
                    t.style.color = "black";
                    break;
                // case 4, 5, 6, 7, 8: // 错误写法，js中switch-case不支持逗号分隔多个值
                default:
                    t.style.color = "purple";
                    break;
            }
        }
        // 若周围没有炸弹，则递归调用本函数，帮助自动点击周围瓦片
        else {
            const neighbours = getNeighbours(tileId);
            for(let j = 0; j < neighbours.length; j++) {
                let oneNeighbour = neighbours[j].split("_");
                touchTile(oneNeighbour[1], true);
            }
        }
    }

    // 比较未点击数组和炸弹数组，若相同则游戏胜利（剩下的未点击瓦片刚好全都是炸弹）
    if (mines.length === untouchedTiles.length) {
        for (let j = 0; j < mines.length; j++) {
            if (mines[j] !== untouchedTiles[j]) {
                alert("ERROR! Start a new game please!");
                return;
            }
        }
        // 该局游戏结束，不可再游玩
        playable = false;
        // 时差弹窗提示游戏胜利
        setTimeout(() => {
            revealAllMines();
            setTimeout(() => {
                alert("YOU WIN!")
            }, 250);
        }, 250);
        // 记分
        getScores();
        return;
    }
}

// 计算瓦片周围炸弹数量
function mineNeighbours(tileId) {
    // 周围瓦片数组
    const neighbours = getNeighbours(tileId);
    let bombCnt = 0;
    for (let j = 0; j < neighbours.length; j++) {
        if (mines.includes(neighbours[j])) {
            bombCnt++;
        }
    }
    return bombCnt;
}

// 获取瓦片四周瓦片
function getNeighbours(tileId) {
    const neighbours = [];
    // 该瓦片所在行列位置
    const row = Math.ceil(tileId / COLS);
    const col = (tileId - 1) % COLS + 1;

    // 检查八个可能的方向
    for (let j = row - 1; j <= row + 1; j++) {
        for (let k = col - 1; k <= col + 1; k++) {
            // 排除超出棋盘范围的瓦片和自身
            if (j >= 1 && j <= ROWS && k >= 1 && k <= COLS && !(j === row && k === col)) {
                // 将有效的四周瓦片加入数组
                neighbours.push(`tile_${(j - 1) * COLS + k}`);
            }
        }
    }

    // // 调试阶段：控制台输出周围瓦片数组
    // console.log("neighbours: ", neighbours);

    return neighbours;
}

// 显示所有炸弹
function revealAllMines() {
    for (let i = 0; i < mines.length; i++) {
        const mine = document.getElementById(mines[i]);
        mine.className = "bomb";
        mine.textContent = "*";
    }
}

// 标记疑似炸弹
function leaveFlag(tileId) {
    if (!playable || !untouchedTiles.includes(`tile_${tileId}`)) {
        return;
    }
    const t = document.getElementById(`tile_${tileId}`);
    // classList.toggle()方法：如果元素存在指定的类名，则删除它，如果不存在，则添加它
    // className只能有一个类，而classList可以有多个类（不会覆盖以前的类）
    t.classList.toggle("flag");
    t.textContent = t.textContent === "F" ? "" : "F";
}

// 计时器
function startTimer(timer) {
    let minute;
    let second;
    let time = timer.textContent.split(" : ");
    minute = parseInt(time[0]);
    second = parseInt(time[1]);
    timerInterval = setInterval(() => {
        second++;
        if (second === 60) {
            second = 0;
            minute++;
        }

        // 超出时间，弹窗游戏结束
        if (minute === 99 && second === 59) {
            playable = false;
            alert("TIME OUT!");
            revealAllMines();
        }

        timer.textContent = `${minute < 10 ? "0" + minute : minute} : ${second < 10 ? "0" + second : second}`;
        if (!playable) {
            clearInterval(timerInterval); // 清除计时中断器
        }
    }, 1000);
}

// 记分板
buttonShowRanking.addEventListener("click", () => {
    const scoreBoardDialog = document.getElementById("myDialog");
    scoreBoardDialog.showModal();

    getRanking();

    const buttonCloseDialog = document.querySelector(".closeDialog");
    buttonCloseDialog.addEventListener("click", () => {
        scoreBoardDialog.close();
    });
});

// 获取分数
function getScores() {
    if (scoreRecorded) {
        return;
    }
    scoreRecorded = true;
    const scoreBoard = document.getElementById("scoreBoard");
    const scoreRound = document.createElement("div");
    const scoreFinal = document.createElement("div");
    scoreRound.className = "scoreRound";
    scoreFinal.className = "scoreFinal";
    scoreRound.textContent = `#${roundTimer.textContent}`;
    scoreFinal.textContent = timer.textContent;
    scoreBoard.appendChild(scoreRound);
    scoreBoard.appendChild(scoreFinal);
}

// 获取排名
function getRanking() {
    const scoreBoard = document.getElementById("scoreBoard");
    let scoreRound = [];
    let scoreFinal = [];
    scoreRound = scoreBoard.querySelectorAll(".scoreRound");
    scoreFinal = scoreBoard.querySelectorAll(".scoreFinal");

    // 时间转为秒
    let seconds = [];
    for (let i = 0; i < scoreFinal.length; i++) {
        let time = scoreFinal[i].textContent.split(" : ");
        seconds.push(parseInt(time[0]) * 60 + parseInt(time[1]));
    }

    // 排序
    let data= [];
    for (let i = 0; i < scoreRound.length; i++) {
        data.push({
            round: scoreRound[i].textContent,
            final: scoreFinal[i].textContent,
            second: seconds[i]
        });
    }
    // 根据秒数从小到大排序
    data.sort((a, b) => {
        return a.second - b.second;
    });

    // 更新排名
    for (let i = 0; i < data.length; i++) {
        scoreRound[i].textContent = data[i].round;
        scoreFinal[i].textContent = data[i].final;
    }
}

// 调试阶段：作弊
const buttonCheat = document.querySelector(".cheat");
buttonCheat.addEventListener("click", () => {
    if (!playable || scoreRecorded) {
        alert("At least start a new game ...");
        return;
    }
    revealAllMines();
    getScores();
    alert("Cheating score added, you poor thing!");
});
