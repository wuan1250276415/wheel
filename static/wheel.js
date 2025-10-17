// 全局变量
let currentPlayer = null;
let wheelOptions = [];
let isSpinning = false;
let currentRotation = 0;

const canvas = document.getElementById('wheelCanvas');
const ctx = canvas.getContext('2d');
const spinButton = document.getElementById('spinButton');
const resultDisplay = document.getElementById('resultDisplay');

// 玩家选择
document.querySelectorAll('.player-btn').forEach(btn => {
    btn.addEventListener('click', function() {
        document.querySelectorAll('.player-btn').forEach(b => b.classList.remove('active'));
        this.classList.add('active');
        currentPlayer = this.dataset.player;

        document.getElementById('selectedPlayer').style.display = 'block';
        document.getElementById('currentPlayer').textContent = currentPlayer;
        spinButton.disabled = false;

        // 隐藏之前的结果
        resultDisplay.style.display = 'none';
    });
});

// 加载转盘选项
async function loadWheelOptions() {
    try {
        const response = await fetch('/api/options');
        wheelOptions = await response.json();
        drawWheel();
    } catch (error) {
        console.error('加载选项失败:', error);
        alert('加载失败,请刷新页面重试');
    }
}

// 绘制转盘
function drawWheel(rotation = 0) {
    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    const radius = 180;

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    if (wheelOptions.length === 0) return;

    const anglePerOption = (Math.PI * 2) / wheelOptions.length;

    wheelOptions.forEach((option, index) => {
        const startAngle = anglePerOption * index + rotation;
        const endAngle = startAngle + anglePerOption;

        // 绘制扇形
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.arc(centerX, centerY, radius, startAngle, endAngle);
        ctx.closePath();
        ctx.fillStyle = option.color;
        ctx.fill();

        // 绘制边框
        ctx.strokeStyle = 'white';
        ctx.lineWidth = 3;
        ctx.stroke();

        // 绘制文字
        ctx.save();
        ctx.translate(centerX, centerY);
        ctx.rotate(startAngle + anglePerOption / 2);
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';

        // emoji
        ctx.font = 'bold 30px Arial';
        ctx.fillStyle = 'white';
        ctx.fillText(option.emoji, radius * 0.65, -10);

        // 文字
        ctx.font = 'bold 16px Microsoft YaHei';
        ctx.fillStyle = '#333';
        ctx.fillText(option.text, radius * 0.65, 15);

        ctx.restore();
    });

    // 绘制中心圆
    ctx.beginPath();
    ctx.arc(centerX, centerY, 40, 0, Math.PI * 2);
    ctx.fillStyle = '#ff69b4';
    ctx.fill();
    ctx.strokeStyle = 'white';
    ctx.lineWidth = 4;
    ctx.stroke();

    // 中心文字
    ctx.fillStyle = 'white';
    ctx.font = 'bold 20px Microsoft YaHei';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('转盘', centerX, centerY);
}

// 转盘动画
async function spinWheel() {
    if (isSpinning || !currentPlayer) return;

    isSpinning = true;
    spinButton.disabled = true;
    spinButton.textContent = '转动中...';
    resultDisplay.style.display = 'none';

    try {
        // 调用API
        const response = await fetch('/api/spin', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ player_name: currentPlayer })
        });

        const result = await response.json();

        // 找到结果在转盘中的位置
        const resultIndex = wheelOptions.findIndex(opt => opt.text === result.result);

        // 计算目标角度
        // Canvas角度系统: 0在3点钟方向(右), -π/2在12点钟方向(顶部)
        // 指针在顶部,我们需要让选中的扇形中心对准指针
        const anglePerOption = (Math.PI * 2) / wheelOptions.length;

        // 当rotation=0时,resultIndex选项的中心角度
        const optionCenterAngle = resultIndex * anglePerOption + anglePerOption / 2;

        // 需要旋转多少才能让这个中心对准顶部(-π/2)
        // 目标: optionCenterAngle + rotation = -π/2 (或 3π/2)
        // 所以: rotation = -π/2 - optionCenterAngle
        const targetRotation = -Math.PI / 2 - optionCenterAngle;

        // 添加多圈旋转 (确保是正向旋转)
        const spins = 5; // 转5圈
        const totalRotation = currentRotation + (Math.PI * 2 * spins) + (targetRotation - currentRotation);

        // 动画参数
        const duration = 4000; // 4秒
        const startTime = Date.now();
        const startRotation = currentRotation;

        function animate() {
            const now = Date.now();
            const elapsed = now - startTime;
            const progress = Math.min(elapsed / duration, 1);

            // 使用缓动函数
            const easeOut = 1 - Math.pow(1 - progress, 3);

            currentRotation = startRotation + (totalRotation - startRotation) * easeOut;
            drawWheel(currentRotation);

            if (progress < 1) {
                requestAnimationFrame(animate);
            } else {
                // 动画结束,显示结果
                showResult(result);
                isSpinning = false;
                spinButton.disabled = false;
                spinButton.textContent = '🎯 再转一次';
            }
        }

        animate();

    } catch (error) {
        console.error('转盘失败:', error);
        alert('转盘失败,请重试');
        isSpinning = false;
        spinButton.disabled = false;
        spinButton.textContent = '🎯 开始转盘';
    }
}

// 显示结果
function showResult(result) {
    document.getElementById('resultEmoji').textContent = result.emoji;
    document.getElementById('resultText').textContent = result.result;

    const typeElement = document.getElementById('resultType');
    typeElement.textContent = result.result_type === 'reward' ? '🎁 奖励' : '😱 惩罚';
    typeElement.className = 'result-type ' + result.result_type;

    resultDisplay.style.display = 'block';

    // 播放庆祝动画
    confetti();
}

// 简单的五彩纸屑效果
function confetti() {
    // 这里可以添加更复杂的五彩纸屑效果
    // 为了简单起见,我们只添加一个简单的提示
    console.log('🎉 恭喜!');
}

// 初始化
spinButton.addEventListener('click', spinWheel);
loadWheelOptions();
