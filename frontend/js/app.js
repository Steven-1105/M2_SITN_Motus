// ====== Configuration des 4 microservices ======
const PLAYER = "http://localhost:8081";
const GAME = "http://localhost:8082";
const DICT = "http://localhost:8083";
const SCORE = "http://localhost:8084";
const MAX_ATTEMPTS = 6;

// ====== Etat de jeu ======
const state = {
  gameId: null, length: 6, maxAttempts: MAX_ATTEMPTS,
  row: 0, col: 0, board: [], finished: false,
  startTime: 0, timer: null, selectedLength: null, playerId: null,
};

// ====== Raccourcis DOM ======
const $ = (id) => document.getElementById(id);
const pseudoInput = $("pseudo");
const elLevels = $("levels"), elStart = $("startBtn");
const elSetup = $("setup"), elPlay = $("play"), elResult = $("result");
const elBoard = $("board"), elKeyboard = $("keyboard"), elMessage = $("message");
const elBanner = $("banner");

function showBanner(msg) { elBanner.textContent = msg; elBanner.hidden = false; }
function hideBanner() { elBanner.hidden = true; }
function show(screen) { elSetup.hidden = screen !== "setup"; elPlay.hidden = screen !== "play"; elResult.hidden = screen !== "result"; }

// ====== player-service : retrouver (ou creer) le joueur du pseudo ======
async function resolvePlayer(pseudo) {
  pseudo = (pseudo || "").trim() || "Invité";
  const cache = JSON.parse(localStorage.getItem("motus_playerids") || "{}");
  if (cache[pseudo]) return cache[pseudo];
  let id = null;
  try {
    const res = await fetch(`${PLAYER}/players`, {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username: pseudo, email: pseudo.toLowerCase() + "@motus.local", password: "motus1234" }),
    });
    if (res.ok) {
      id = (await res.json()).id;
    } else {
      // pseudo deja pris -> on le retrouve dans la liste
      const list = await (await fetch(`${PLAYER}/players`)).json();
      const found = list.find((p) => p.username === pseudo);
      id = found ? found.id : null;
    }
  } catch (e) {
    return null;
  }
  if (id != null) { cache[pseudo] = id; localStorage.setItem("motus_playerids", JSON.stringify(cache)); }
  return id;
}
function pseudoForId(id) {
  const cache = JSON.parse(localStorage.getItem("motus_playerids") || "{}");
  for (const [p, i] of Object.entries(cache)) if (i === id) return p;
  return "Joueur " + id;
}

// ====== Niveaux (dict-service) ======
async function loadLevels() {
  try {
    const levels = await (await fetch(`${DICT}/words/lengths`)).json();
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
    showBanner("⚠️ dict-service injoignable (port 8083). Lance la stack, puis recharge.");
    elLevels.innerHTML = '<div class="loading">Niveaux indisponibles.</div>';
  }
}

// ====== Demarrage d'une partie (game-service) ======
async function startGame() {
  const len = state.selectedLength || 6;
  const playerId = await resolvePlayer(pseudoInput.value);
  if (playerId == null) { showBanner("⚠️ player-service injoignable (port 8081)."); return; }
  state.playerId = playerId;
  let game;
  try {
    const res = await fetch(`${GAME}/games`, {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ playerId, wordLength: len, maxAttempts: MAX_ATTEMPTS }),
    });
    if (!res.ok) throw new Error("game");
    game = await res.json();
  } catch (e) {
    showBanner("⚠️ game-service injoignable (port 8082). Réessaie (dict peut être en train de démarrer).");
    return;
  }
  hideBanner();
  state.gameId = game.id;
  state.length = game.wordLength;
  state.maxAttempts = game.maxAttempts;
  state.row = 0; state.col = 0; state.finished = false;
  state.board = Array.from({ length: state.maxAttempts }, () => Array(state.length).fill(""));
  $("levelLabel").textContent = state.length;
  $("maxLabel").textContent = state.maxAttempts;
  $("attemptLabel").textContent = 1;
  elMessage.textContent = "";
  buildBoard();
  buildKeyboard();
  keyStateReset();
  show("play");
  startTimer();
}

function buildBoard() {
  elBoard.innerHTML = "";
  for (let r = 0; r < state.maxAttempts; r++) {
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
function tile(r, c) { return elBoard.querySelector(`.tile[data-row="${r}"][data-col="${c}"]`); }

// ====== Saisie ======
function onKey(letter) {
  if (state.finished || elPlay.hidden || state.col >= state.length) return;
  state.board[state.row][state.col] = letter;
  const t = tile(state.row, state.col);
  t.textContent = letter; t.classList.add("filled");
  state.col++;
}
function onBackspace() {
  if (state.finished || state.col <= 0) return;
  state.col--;
  state.board[state.row][state.col] = "";
  const t = tile(state.row, state.col);
  t.textContent = ""; t.classList.remove("filled");
}
let submitting = false;
async function onEnter() {
  if (state.finished || submitting) return;
  if (state.col < state.length) { flashMessage("Complète le mot."); return; }
  const guess = state.board[state.row].join("").toUpperCase();
  submitting = true;
  let results;
  try {
    const res = await fetch(`${GAME}/games/${state.gameId}/guess`, {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ word: guess }),
    });
    if (res.status === 400) { shakeRow(); flashMessage(`« ${guess} » n'est pas dans le dictionnaire.`); submitting = false; return; }
    if (!res.ok) throw new Error("guess");
    results = await res.json(); // [{ lettre, statut }]
  } catch (e) {
    shakeRow(); flashMessage("game-service injoignable."); submitting = false; return;
  }
  revealRow(results, guess);
}

const STATUT_CLASS = { BIEN_PLACE: "correct", MAL_PLACE: "present", ABSENT: "absent" };
function flashMessage(m) { elMessage.textContent = m; }
function shakeRow() {
  const row = elBoard.querySelector(`.row[data-row="${state.row}"]`);
  row.classList.add("shake");
  setTimeout(() => row.classList.remove("shake"), 400);
}

function revealRow(results, guess) {
  results.forEach((res, c) => {
    const cls = STATUT_CLASS[res.statut] || "absent";
    const t = tile(state.row, c);
    setTimeout(() => {
      t.classList.add("reveal");
      setTimeout(() => { t.classList.remove("filled"); t.classList.add(cls); paintKey(guess[c], cls); }, 250);
    }, c * 120);
  });
  const delay = state.length * 120 + 320;
  setTimeout(() => finishRowThenContinue(guess), delay);
}

// Apres l'animation : on demande a game-service l'etat de la partie (gagne / perdu / en cours).
async function finishRowThenContinue(guess) {
  let game;
  try {
    game = await (await fetch(`${GAME}/games/${state.gameId}`)).json();
  } catch (e) { game = null; }
  const statut = game ? game.statut : "EN_COURS";
  if (statut === "GAGNE") { endGame(true, guess, game); return; }
  if (statut === "PERDU") { endGame(false, guess, game); return; }
  // partie en cours : ligne suivante
  state.row++; state.col = 0;
  $("attemptLabel").textContent = state.row + 1;
  elMessage.textContent = "";
  submitting = false;
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
let keyState = {};
function keyStateReset() { keyState = {}; }
function paintKey(letter, cls) {
  const rank = { absent: 0, present: 1, correct: 2 };
  if (keyState[letter] && rank[keyState[letter]] >= rank[cls]) return;
  keyState[letter] = cls;
  const btn = elKeyboard.querySelector(`.key[data-key="${letter}"]`);
  if (btn) { btn.classList.remove("correct", "present", "absent"); btn.classList.add(cls); }
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

// ====== Fin de partie ======
// game-service a DEJA pousse le score vers score-service. On recupere juste le score a afficher.
async function endGame(won, lastGuess, game) {
  state.finished = true; submitting = false;
  clearInterval(state.timer);
  const seconds = Math.round((Date.now() - state.startTime) / 1000);
  const word = (game && game.motMystere && !game.motMystere.includes("*")) ? game.motMystere : lastGuess;
  let score = null;
  try {
    const parties = await (await fetch(`${SCORE}/scores/games?playerId=${state.playerId}`)).json();
    const p = parties.find((x) => x.gameId === state.gameId);
    if (p) score = p.score;
  } catch (e) { /* score-service indispo */ }
  renderResult(won, word, seconds, score);
  refreshRanking(); refreshStats(); refreshHistory();
  if (won) confetti();
}

function renderResult(won, word, seconds, score) {
  $("resultIcon").textContent = won ? "🎉" : "😶‍🌫️";
  $("resultTitle").textContent = won ? "Gagné !" : "Perdu…";
  $("resultWord").textContent = "Le mot était : " + word;
  $("scoreBadge").textContent = score != null ? (won ? "+" : "") + score + " points" : "";
  $("resultDetail").textContent = won
    ? `Trouvé en ${state.row + 1} essai${state.row > 0 ? "s" : ""} · ${seconds} s`
    : "Tu as utilisé tous tes essais.";
  const def = $("defBtn");
  def.href = "https://www.larousse.fr/dictionnaires/francais/" + encodeURIComponent(word.toLowerCase());
  show("result");
}

// ====== Classement / stats / historique (score-service) ======
async function refreshRanking() {
  try {
    const list = await (await fetch(`${SCORE}/scores/ranking`)).json();
    const me = state.playerId;
    const body = $("rankingBody");
    if (!list.length) { body.innerHTML = '<li class="muted small">Aucune partie pour l\'instant.</li>'; return; }
    body.innerHTML = "";
    list.slice(0, 10).forEach((r, i) => {
      const li = document.createElement("li");
      li.className = "rank-item" + (r.playerId === me ? " me" : "");
      const gap = r.pointsToNext > 0 ? ` · -${r.pointsToNext} pour remonter` : "";
      li.innerHTML = `<span class="rank-pos">${i + 1}</span>
        <span class="rank-name">${pseudoForId(r.playerId)}</span>
        <span class="rank-meta">${r.gamesPlayed} parties${gap}</span>
        <span class="rank-score">${r.totalScore}</span>`;
      body.appendChild(li);
    });
  } catch (e) { /* silencieux */ }
}

async function refreshStats() {
  const me = state.playerId || (await resolvePlayer(pseudoInput.value));
  if (me == null) return;
  try {
    const s = await (await fetch(`${SCORE}/scores/players/${me}`)).json();
    const cell = (v, k) => `<div class="stat"><div class="v">${v}</div><div class="k">${k}</div></div>`;
    $("statsBody").innerHTML =
      cell(s.gamesPlayed, "parties") + cell(s.wins, "victoires") +
      cell(Math.round(s.winRate * 100) + "%", "taux de réussite") +
      cell(Math.round(s.averageScore), "pts / partie moy.") +
      cell(s.totalScore, "points totaux") + cell(s.bestScore, "meilleur score");
  } catch (e) { /* silencieux */ }
}

async function refreshHistory() {
  const me = state.playerId || (await resolvePlayer(pseudoInput.value));
  if (me == null) return;
  try {
    const games = await (await fetch(`${SCORE}/scores/games?playerId=${me}`)).json();
    const body = $("historyBody");
    if (!games.length) { body.innerHTML = '<p class="muted small">Aucune partie jouée pour l\'instant.</p>'; return; }
    body.innerHTML = "";
    games.forEach((g) => {
      const dt = new Date(g.finishedAt.slice(0, 19));
      const when = dt.toLocaleDateString("fr-FR") + " · " + dt.toLocaleTimeString("fr-FR", { hour: "2-digit", minute: "2-digit" });
      const item = document.createElement("div");
      item.className = "hist-item " + (g.won ? "win" : "loss");
      item.innerHTML =
        `<span class="hist-res">${g.won ? "✅" : "❌"}</span>` +
        `<span class="hist-main"><b>${g.wordLength} lettres</b> · ${g.attempts} essai${g.attempts > 1 ? "s" : ""}<br>` +
        `<span class="muted small">${when}</span></span>` +
        `<span class="hist-score">${g.score} pts</span>`;
      body.appendChild(item);
    });
  } catch (e) { /* silencieux */ }
}

// ====== Confettis ======
function confetti() {
  const cv = $("confetti"), ctx = cv.getContext("2d");
  cv.width = innerWidth; cv.height = innerHeight;
  const colors = ["#58cc02", "#1cb0f6", "#ffc800", "#ff4b4b", "#a06bff"];
  const parts = Array.from({ length: 140 }, () => ({
    x: Math.random() * cv.width, y: -20 - Math.random() * cv.height * 0.3,
    r: 4 + Math.random() * 5, c: colors[(Math.random() * colors.length) | 0],
    vy: 2 + Math.random() * 3, vx: -1.5 + Math.random() * 3,
  }));
  let frames = 0;
  (function loop() {
    ctx.clearRect(0, 0, cv.width, cv.height);
    parts.forEach((p) => { p.y += p.vy; p.x += p.vx; ctx.fillStyle = p.c; ctx.fillRect(p.x, p.y, p.r, p.r * 1.6); });
    if (frames++ < 160) requestAnimationFrame(loop); else ctx.clearRect(0, 0, cv.width, cv.height);
  })();
}

// ====== Widgets (fenetres) ======
function openModal(name) {
  if (name === "stats") refreshStats();
  else if (name === "ranking") refreshRanking();
  else if (name === "history") refreshHistory();
  document.getElementById("modal-" + name).hidden = false;
}
document.querySelectorAll(".tool-btn").forEach((b) => (b.onclick = () => openModal(b.dataset.modal)));
document.querySelectorAll(".modal").forEach((m) => {
  m.addEventListener("click", (e) => { if (e.target === m || e.target.hasAttribute("data-close")) m.hidden = true; });
});

// ====== Branchements ======
document.addEventListener("keydown", (e) => {
  if (e.key === "Escape") { document.querySelectorAll(".modal").forEach((m) => (m.hidden = true)); return; }
  if (elPlay.hidden) return;
  const k = e.key.toUpperCase();
  if (k === "ENTER") onEnter();
  else if (k === "BACKSPACE") onBackspace();
  else if (/^[A-Z]$/.test(k)) onKey(k);
});
elStart.onclick = startGame;
$("againBtn").onclick = () => show("setup");
$("quitBtn").onclick = () => { clearInterval(state.timer); show("setup"); };
pseudoInput.addEventListener("change", () => {
  localStorage.setItem("motus_pseudo", pseudoInput.value);
  state.playerId = null; // on re-resolvera le joueur au prochain besoin
});

// ====== Init ======
pseudoInput.value = localStorage.getItem("motus_pseudo") || "";
loadLevels();
