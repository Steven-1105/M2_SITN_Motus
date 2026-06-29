// ====== Configuration des services (Liya) ======
const DICT = "http://localhost:8083";
const SCORE = "http://localhost:8084";
const MAX_ATTEMPTS = 6;

// ====== Petit mapping pseudo <-> playerId (cote front, le serveur ne gere que des ids) ======
function playerMap() { return JSON.parse(localStorage.getItem("motus_players") || "{}"); }
function getPlayerId(pseudo) {
  pseudo = (pseudo || "").trim() || "Invité";
  const map = playerMap();
  if (!map[pseudo]) {
    const ids = Object.values(map);
    map[pseudo] = ids.length ? Math.max(...ids) + 1 : 1;
    localStorage.setItem("motus_players", JSON.stringify(map));
  }
  return map[pseudo];
}
function pseudoForId(id) {
  const map = playerMap();
  for (const [p, i] of Object.entries(map)) if (i === id) return p;
  return "Joueur " + id;
}

// ====== Etat de jeu ======
const state = {
  target: "", length: 6, row: 0, col: 1,
  board: [], finished: false, startTime: 0, timer: null, selectedLength: null,
};

// ====== Raccourcis DOM ======
const $ = (id) => document.getElementById(id);
const pseudoInput = $("pseudo");
const elLevels = $("levels"), elStart = $("startBtn");
const elSetup = $("setup"), elPlay = $("play"), elResult = $("result");
const elBoard = $("board"), elKeyboard = $("keyboard"), elMessage = $("message");
const elBanner = $("banner");

// ====== Helpers API ======
async function api(url, options) {
  const res = await fetch(url, options);
  if (!res.ok && res.status >= 500) throw new Error("server");
  return res;
}
function showBanner(msg) { elBanner.textContent = msg; elBanner.hidden = false; }
function hideBanner() { elBanner.hidden = true; }

// ====== Navigation entre ecrans ======
function show(screen) {
  elSetup.hidden = screen !== "setup";
  elPlay.hidden = screen !== "play";
  elResult.hidden = screen !== "result";
}

// ====== Niveaux (longueurs disponibles) ======
async function loadLevels() {
  try {
    const res = await api(`${DICT}/words/lengths`);
    const levels = await res.json();
    hideBanner();
    elLevels.innerHTML = "";
    levels.forEach((lv) => {
      const stars = "★".repeat(Math.max(1, lv.length - 4));
      const div = document.createElement("div");
      div.className = "level";
      div.innerHTML = `<div class="n">${lv.length}</div><div class="lbl">lettres · ${lv.count} mots</div><div class="stars">${stars}</div>`;
      div.onclick = () => {
        document.querySelectorAll(".level").forEach((e) => e.classList.remove("selected"));
        div.classList.add("selected");
        state.selectedLength = lv.length;
        elStart.disabled = false;
      };
      elLevels.appendChild(div);
    });
  } catch (e) {
    showBanner("⚠️ dict-service injoignable (port 8083). Lance-le, puis recharge la page.");
    elLevels.innerHTML = '<div class="loading">Niveaux indisponibles.</div>';
  }
}

// ====== Demarrage d'une partie ======
async function startGame() {
  const len = state.selectedLength || 6;
  try {
    const res = await api(`${DICT}/words/random?length=${len}`);
    const data = await res.json();
    state.target = (data.word || "").toUpperCase();
  } catch (e) {
    showBanner("⚠️ Impossible de tirer un mot (dict-service injoignable).");
    return;
  }
  state.length = state.target.length;
  state.row = 0; state.col = 1; state.finished = false;
  state.board = Array.from({ length: MAX_ATTEMPTS }, () => Array(state.length).fill(""));
  $("levelLabel").textContent = state.length;
  $("maxLabel").textContent = MAX_ATTEMPTS;
  $("attemptLabel").textContent = 1;
  elMessage.textContent = "";
  buildBoard();
  buildKeyboard();
  primeRow();
  show("play");
  startTimer();
}

function buildBoard() {
  elBoard.innerHTML = "";
  elBoard.style.gridTemplateRows = `repeat(${MAX_ATTEMPTS}, auto)`;
  for (let r = 0; r < MAX_ATTEMPTS; r++) {
    const row = document.createElement("div");
    row.className = "row"; row.dataset.row = r;
    for (let c = 0; c < state.length; c++) {
      const t = document.createElement("div");
      t.className = "tile"; t.dataset.row = r; t.dataset.col = c;
      row.appendChild(t);
    }
    elBoard.appendChild(row);
  }
}

// La premiere lettre est donnee (comme au vrai Motus).
function primeRow() {
  const first = state.target[0];
  state.board[state.row][0] = first;
  state.col = 1;
  const t = tile(state.row, 0);
  t.textContent = first; t.classList.add("given");
}
function tile(r, c) { return elBoard.querySelector(`.tile[data-row="${r}"][data-col="${c}"]`); }

// ====== Saisie ======
function onKey(letter) {
  if (state.finished || elPlay.hidden) return;
  if (state.col >= state.length) return;
  state.board[state.row][state.col] = letter;
  const t = tile(state.row, state.col);
  t.textContent = letter; t.classList.add("filled");
  state.col++;
}
function onBackspace() {
  if (state.finished || state.col <= 1) return;
  state.col--;
  state.board[state.row][state.col] = "";
  const t = tile(state.row, state.col);
  t.textContent = ""; t.classList.remove("filled");
}
async function onEnter() {
  if (state.finished || state.col < state.length) { flashMessage("Complète le mot."); return; }
  const guess = state.board[state.row].join("").toUpperCase();
  // Verification : le mot doit exister dans le dictionnaire
  try {
    const res = await api(`${DICT}/words/validate`, {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ word: guess }),
    });
    const { valid } = await res.json();
    if (!valid) { shakeRow(); flashMessage(`« ${guess} » n'est pas dans le dictionnaire.`); return; }
  } catch (e) {
    shakeRow(); flashMessage("Validation impossible (dict-service injoignable)."); return;
  }
  revealRow(guess);
}

function flashMessage(m) { elMessage.textContent = m; }
function shakeRow() {
  const row = elBoard.querySelector(`.row[data-row="${state.row}"]`);
  row.classList.add("shake");
  setTimeout(() => row.classList.remove("shake"), 400);
}

// ====== Comparaison Motus (gere les lettres en double) ======
function compare(guess, target) {
  const n = target.length;
  const result = Array(n).fill("absent");
  const used = Array(n).fill(false);
  for (let i = 0; i < n; i++) if (guess[i] === target[i]) { result[i] = "correct"; used[i] = true; }
  for (let i = 0; i < n; i++) {
    if (result[i] === "correct") continue;
    for (let j = 0; j < n; j++) {
      if (!used[j] && guess[i] === target[j]) { result[i] = "present"; used[j] = true; break; }
    }
  }
  return result;
}

function revealRow(guess) {
  const statuses = compare(guess, state.target);
  statuses.forEach((st, c) => {
    const t = tile(state.row, c);
    setTimeout(() => {
      t.classList.add("reveal");
      setTimeout(() => { t.classList.remove("given", "filled"); t.classList.add(st); paintKey(guess[c], st); }, 250);
    }, c * 120);
  });
  const delay = state.length * 120 + 300;
  if (guess === state.target) { setTimeout(() => endGame(true), delay); return; }
  if (state.row >= MAX_ATTEMPTS - 1) { setTimeout(() => endGame(false), delay); return; }
  setTimeout(() => {
    state.row++; primeRow();
    $("attemptLabel").textContent = state.row + 1;
    elMessage.textContent = "";
  }, delay);
}

// ====== Clavier a l'ecran (AZERTY) ======
const KB = [["A","Z","E","R","T","Y","U","I","O","P"],["Q","S","D","F","G","H","J","K","L","M"],["↵","W","X","C","V","B","N","⌫"]];
function buildKeyboard() {
  elKeyboard.innerHTML = "";
  KB.forEach((rowKeys) => {
    const row = document.createElement("div"); row.className = "krow";
    rowKeys.forEach((k) => {
      const b = document.createElement("button");
      b.className = "key" + (k.length > 1 ? " wide" : "");
      b.textContent = k; b.dataset.key = k;
      b.onclick = () => { if (k === "↵") onEnter(); else if (k === "⌫") onBackspace(); else onKey(k); };
      row.appendChild(b);
    });
    elKeyboard.appendChild(row);
  });
}
const keyState = {};
function paintKey(letter, st) {
  const prev = keyState[letter];
  const rank = { absent: 0, present: 1, correct: 2 };
  if (prev && rank[prev] >= rank[st]) return;
  keyState[letter] = st;
  const btn = elKeyboard.querySelector(`.key[data-key="${letter}"]`);
  if (btn) { btn.classList.remove("correct", "present", "absent"); btn.classList.add(st); }
}

// ====== Chrono ======
function startTimer() {
  state.startTime = Date.now();
  clearInterval(state.timer);
  state.timer = setInterval(() => {
    const s = Math.floor((Date.now() - state.startTime) / 1000);
    $("timer").textContent = `${Math.floor(s / 60)}:${String(s % 60).padStart(2, "0")}`;
  }, 1000);
}

// ====== Fin de partie + envoi du score ======
async function endGame(won) {
  state.finished = true;
  clearInterval(state.timer);
  const durationSeconds = Math.round((Date.now() - state.startTime) / 1000);
  const attempts = state.row + 1;
  const playerId = getPlayerId(pseudoInput.value);
  const payload = {
    gameId: Date.now(), playerId, won, attempts,
    maxAttempts: MAX_ATTEMPTS, wordLength: state.length, durationSeconds,
  };
  let score = localScore(won, attempts, MAX_ATTEMPTS, state.length, durationSeconds);
  try {
    const res = await api(`${SCORE}/scores/results`, {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    const saved = await res.json();
    if (typeof saved.score === "number") score = saved.score;
    hideBanner();
  } catch (e) {
    showBanner("⚠️ score-service injoignable (port 8084) : score affiché localement, non enregistré.");
  }
  renderResult(won, score, attempts, durationSeconds);
  refreshRanking();
  refreshStats();
  if (won) confetti();
}

// Même formule que score-service (utilisée seulement si le service est down).
function localScore(won, attempts, maxAttempts, wordLength, seconds) {
  if (!won) return 0;
  const essais = Math.min(attempts, maxAttempts);
  const bonusEssais = (maxAttempts - essais) * 20;
  const bonusTemps = Math.max(0, 50 - Math.floor(seconds / 5));
  const bonusLongueur = Math.max(0, wordLength - 5) * 15;
  return 100 + bonusEssais + bonusTemps + bonusLongueur;
}

function renderResult(won, score, attempts, seconds) {
  $("resultIcon").textContent = won ? "🎉" : "😶‍🌫️";
  $("resultTitle").textContent = won ? "Gagné !" : "Perdu…";
  $("resultWord").textContent = "Le mot était : " + state.target;
  $("scoreBadge").textContent = (won ? "+" : "") + score + " points";
  $("resultDetail").textContent = won
    ? `Trouvé en ${attempts} essai${attempts > 1 ? "s" : ""} · ${seconds} s`
    : "Tu as utilisé tous tes essais.";
  const def = $("defBtn");
  def.href = "https://www.larousse.fr/dictionnaires/francais/" + encodeURIComponent(state.target.toLowerCase());
  show("result");
}

// ====== Classement & statistiques ======
async function refreshRanking() {
  try {
    const res = await api(`${SCORE}/scores/ranking`);
    const list = await res.json();
    const me = getPlayerId(pseudoInput.value);
    const body = $("rankingBody");
    if (!list.length) { body.innerHTML = '<li class="muted small">Aucune partie pour l\'instant.</li>'; return; }
    body.innerHTML = "";
    list.slice(0, 8).forEach((r, i) => {
      const li = document.createElement("li");
      li.className = "rank-item" + (r.playerId === me ? " me" : "");
      const gap = r.pointsToNext > 0 ? ` · -${r.pointsToNext} pour remonter` : "";
      li.innerHTML = `<span class="rank-pos">${i + 1}</span>
        <span class="rank-name">${pseudoForId(r.playerId)}</span>
        <span class="rank-meta">${r.gamesPlayed} parties${gap}</span>
        <span class="rank-score">${r.totalScore}</span>`;
      body.appendChild(li);
    });
  } catch (e) { /* score-service down : on garde l'affichage courant */ }
}

async function refreshStats() {
  const me = getPlayerId(pseudoInput.value);
  try {
    const res = await api(`${SCORE}/scores/players/${me}`);
    const s = await res.json();
    const cell = (v, k) => `<div class="stat"><div class="v">${v}</div><div class="k">${k}</div></div>`;
    $("statsBody").innerHTML =
      cell(s.gamesPlayed, "parties") + cell(s.wins, "victoires") +
      cell(Math.round(s.winRate * 100) + "%", "taux de réussite") +
      cell(Math.round(s.averageScore), "pts / partie moy.") +
      cell(s.totalScore, "points totaux") + cell(s.bestScore, "meilleur score");
  } catch (e) { /* silencieux */ }
}

// ====== Confettis ======
function confetti() {
  const cv = $("confetti"), ctx = cv.getContext("2d");
  cv.width = innerWidth; cv.height = innerHeight;
  const colors = ["#6c7bff", "#a06bff", "#2fae6f", "#e7a33e", "#ff6b8a"];
  const parts = Array.from({ length: 140 }, () => ({
    x: Math.random() * cv.width, y: -20 - Math.random() * cv.height * 0.3,
    r: 4 + Math.random() * 5, c: colors[(Math.random() * colors.length) | 0],
    vy: 2 + Math.random() * 3, vx: -1.5 + Math.random() * 3, a: Math.random() * Math.PI,
  }));
  let frames = 0;
  (function loop() {
    ctx.clearRect(0, 0, cv.width, cv.height);
    parts.forEach((p) => {
      p.y += p.vy; p.x += p.vx; p.a += 0.1;
      ctx.fillStyle = p.c;
      ctx.fillRect(p.x, p.y, p.r, p.r * 1.6);
    });
    if (frames++ < 160) requestAnimationFrame(loop);
    else ctx.clearRect(0, 0, cv.width, cv.height);
  })();
}

// ====== Branchements ======
document.addEventListener("keydown", (e) => {
  if (elPlay.hidden) return;
  const k = e.key.toUpperCase();
  if (k === "ENTER") onEnter();
  else if (k === "BACKSPACE") onBackspace();
  else if (/^[A-Z]$/.test(k)) onKey(k);
});
elStart.onclick = startGame;
$("againBtn").onclick = () => { show("setup"); };
$("quitBtn").onclick = () => { clearInterval(state.timer); show("setup"); };
pseudoInput.addEventListener("change", () => {
  localStorage.setItem("motus_pseudo", pseudoInput.value);
  refreshStats(); refreshRanking();
});

// ====== Init ======
pseudoInput.value = localStorage.getItem("motus_pseudo") || "";
loadLevels();
refreshRanking();
refreshStats();
