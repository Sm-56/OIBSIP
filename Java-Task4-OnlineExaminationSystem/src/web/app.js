const views = ['login-view', 'profile-view', 'exam-view', 'result-view'];
let questions = [];
let currentQuestionIndex = 0;
let answers = [];
let timerInterval;
let timeLeft = 30 * 60; // 30 minutes
let isExamSubmitted = false;

function showView(viewId) {
    views.forEach(v => document.getElementById(v).classList.remove('active'));
    document.getElementById(viewId).classList.add('active');
}

// 1. LOGIN
document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    const res = await fetch('/api/login', {
        method: 'POST',
        body: JSON.stringify({username, password})
    });
    const data = await res.json();
    if(res.ok && data.success) {
        document.getElementById('login-error').innerText = '';
        loadProfile();
        showView('profile-view');
    } else {
        document.getElementById('login-error').innerText = data.message || 'Login failed';
    }
});

// 2. PROFILE
async function loadProfile() {
    const res = await fetch('/api/profile');
    const data = await res.json();
    document.getElementById('prof-username').value = data.username;
    document.getElementById('prof-displayname').value = data.displayName;
    document.getElementById('student-name').innerText = "Student: " + data.displayName;
}

document.getElementById('btn-update-profile').addEventListener('click', async () => {
    const displayName = document.getElementById('prof-displayname').value;
    const password = document.getElementById('prof-password').value;
    
    if(!displayName.trim()) {
        alert("Display name cannot be empty.");
        return;
    }
    
    const res = await fetch('/api/profile', {
        method: 'POST',
        body: JSON.stringify({displayName, password})
    });
    if(res.ok) {
        document.getElementById('profile-success').innerText = 'Profile updated successfully!';
        setTimeout(() => document.getElementById('profile-success').innerText = '', 3000);
        loadProfile(); // refresh name
    }
});

document.getElementById('btn-start-exam').addEventListener('click', async () => {
    await fetchQuestions();
    showView('exam-view');
    startTimer();
});

// 3. EXAM
async function fetchQuestions() {
    const res = await fetch('/api/questions');
    questions = await res.json();
    answers = new Array(questions.length).fill(-1);
    currentQuestionIndex = 0;
    renderQuestion();
}

function renderQuestion() {
    const q = questions[currentQuestionIndex];
    document.getElementById('question-header').innerText = `Question ${currentQuestionIndex + 1} of ${questions.length}`;
    document.getElementById('question-text').innerText = q.questionText;
    
    const container = document.getElementById('options-container');
    container.innerHTML = '';
    
    q.options.forEach((opt, idx) => {
        const label = document.createElement('label');
        label.className = 'option-label' + (answers[currentQuestionIndex] === idx ? ' selected' : '');
        
        const radio = document.createElement('input');
        radio.type = 'radio';
        radio.name = 'option';
        radio.value = idx;
        if(answers[currentQuestionIndex] === idx) radio.checked = true;
        
        radio.addEventListener('change', () => {
            answers[currentQuestionIndex] = idx;
            document.querySelectorAll('.option-label').forEach(l => l.classList.remove('selected'));
            label.classList.add('selected');
        });
        
        label.appendChild(radio);
        label.appendChild(document.createTextNode(opt));
        container.appendChild(label);
    });
    
    document.getElementById('btn-prev').disabled = currentQuestionIndex === 0;
    document.getElementById('btn-next').disabled = currentQuestionIndex === questions.length - 1;
}

document.getElementById('btn-prev').addEventListener('click', () => {
    if(currentQuestionIndex > 0) {
        currentQuestionIndex--;
        renderQuestion();
    }
});

document.getElementById('btn-next').addEventListener('click', () => {
    if(currentQuestionIndex < questions.length - 1) {
        currentQuestionIndex++;
        renderQuestion();
    }
});

// 4. TIMER
function startTimer() {
    isExamSubmitted = false;
    timeLeft = 30 * 60;
    updateTimerDisplay();
    
    clearInterval(timerInterval);
    timerInterval = setInterval(() => {
        timeLeft--;
        updateTimerDisplay();
        
        if (timeLeft <= 0) {
            clearInterval(timerInterval);
            alert("Time is over! Your exam will be submitted automatically.");
            submitExam();
        }
    }, 1000);
}

function updateTimerDisplay() {
    const m = Math.floor(timeLeft / 60).toString().padStart(2, '0');
    const s = (timeLeft % 60).toString().padStart(2, '0');
    document.getElementById('timer').innerText = `Time Left: ${m}:${s}`;
}

// 5. SUBMIT
document.getElementById('btn-submit-exam').addEventListener('click', () => {
    if(confirm("Are you sure you want to submit the exam?")) {
        submitExam();
    }
});

async function submitExam() {
    if(isExamSubmitted) return;
    isExamSubmitted = true;
    clearInterval(timerInterval);
    
    const timeTaken = (30 * 60) - Math.max(0, timeLeft);
    
    const res = await fetch('/api/submit', {
        method: 'POST',
        body: JSON.stringify(answers)
    });
    const result = await res.json();
    
    document.getElementById('res-score').innerText = `${result.score} / ${result.total}`;
    document.getElementById('res-correct').innerText = result.correct;
    document.getElementById('res-incorrect').innerText = result.incorrect;
    
    const m = Math.floor(timeTaken / 60).toString().padStart(2, '0');
    const s = (timeTaken % 60).toString().padStart(2, '0');
    document.getElementById('res-time').innerText = `${m}:${s}`;
    
    const pct = ((result.score / result.total) * 100).toFixed(2);
    document.getElementById('res-percent').innerText = `${pct}%`;
    
    // Render detailed answer review
    const detailsContainer = document.getElementById('result-details');
    detailsContainer.innerHTML = '<h3>Answer Review</h3>';
    
    if (result.details) {
        result.details.forEach((item, index) => {
            const div = document.createElement('div');
            let statusClass = 'unanswered';
            let userAnsText = 'Not answered';
            
            if (item.userAnswer !== -1) {
                if (item.userAnswer === item.correctAnswer) {
                    statusClass = 'correct';
                    userAnsText = `<span class="user-ans">${item.options[item.userAnswer]} (Correct)</span>`;
                } else {
                    statusClass = 'incorrect';
                    userAnsText = `<span class="user-ans wrong">${item.options[item.userAnswer]} (Incorrect)</span>`;
                }
            }
            
            div.className = `result-item ${statusClass}`;
            div.innerHTML = `
                <h4>Q${index + 1}: ${item.questionText}</h4>
                <p>Your Answer: ${userAnsText}</p>
                <p>Correct Answer: <span class="correct-ans">${item.options[item.correctAnswer]}</span></p>
            `;
            detailsContainer.appendChild(div);
        });
    }
    
    showView('result-view');
}

// 6. WINDOW CLOSE PROTECTION
window.addEventListener('beforeunload', (e) => {
    if (document.getElementById('exam-view').classList.contains('active') && !isExamSubmitted) {
        e.preventDefault();
        e.returnValue = '';
    }
});

// 7. LOGOUT
document.getElementById('btn-logout').addEventListener('click', async () => {
    await fetch('/api/logout', { method: 'POST' });
    document.getElementById('login-form').reset();
    document.getElementById('password').value = '';
    document.getElementById('prof-password').value = '';
    showView('login-view');
});
