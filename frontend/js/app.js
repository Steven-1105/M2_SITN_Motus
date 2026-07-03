// ====== Configuration des 4 microservices ======
const PLAYER = "http://localhost:8081";
const GAME = "http://localhost:8082";
const DICT = "http://localhost:8083";
const SCORE = "http://localhost:8084";
const MAX_ATTEMPTS = 6;

// ====== Etat de jeu ======
const state = {
  gameId: null, length: 6, maxAttempts: MAX_ATTEMPTS, firstLetter: "",
  row: 0, col: 1, board: [], finished: false,
  startTime: 0, timer: null, selectedLength: null, playerId: null,
};

const $ = (id) => document.getElementById(id);
const pseudoInput = $("pseudo");
const elLevels = $("levels"), elStart = $("startBtn");
const elSetup = $("setup"), elPlay = $("play"), elResult = $("result");
const elBoard = $("board"), elKeyboard = $("keyboard"), elMessage = $("message");
const elBanner = $("banner");

function showBanner(m) { elBanner.textContent = m; elBanner.hidden = false; }
function hideBanner() { elBanner.hidden = true; }
function show(screen) { elSetup.hidden = screen !== "setup"; elPlay.hidden = screen !== "play"; elResult.hidden = screen !== "result"; }
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

// ====== Le pendu (la potence se construit, la mascotte finit KO) ======
function penduReset() {
  const p = $("pendu"); if (!p) return;
  p.classList.remove("ko");
  p.querySelectorAll(".pt").forEach((el) => el.classList.remove("on"));
}
function updatePendu(wrong, ko = false) {
  const p = $("pendu"); if (!p) return;
  const revealed = ko ? 5 : Math.min(5, wrong);
  p.querySelectorAll(".pt").forEach((el) => { if (Number(el.dataset.stage) <= revealed) el.classList.add("on"); });
  if (ko || wrong >= state.maxAttempts) {
    p.querySelectorAll(".pt").forEach((el) => el.classList.add("on"));
    p.classList.add("ko");
  }
}
const KO_OWL_SVG = `<svg viewBox="0 0 80 82" aria-hidden="true">
  <path d="M22 26 L30 14 L38 26 Z" fill="#46a302"/><path d="M58 26 L50 14 L42 26 Z" fill="#46a302"/>
  <rect x="18" y="24" width="44" height="46" rx="20" fill="#46a302"/>
  <rect x="16" y="20" width="44" height="46" rx="20" fill="#58cc02"/>
  <path d="M24 42 l11 9 M35 42 l-11 9" stroke="#3c3c3c" stroke-width="3" stroke-linecap="round"/>
  <path d="M45 42 l11 9 M56 42 l-11 9" stroke="#3c3c3c" stroke-width="3" stroke-linecap="round"/>
  <path d="M30 56 q8 6 16 0" stroke="#2b6b00" stroke-width="2.6" fill="none" stroke-linecap="round"/>
  <path d="M34 48 l4 6 l4 -6 z" fill="#ffb020"/>
  <ellipse cx="38" cy="62" rx="5" ry="6" fill="#ff6b9d"/>
  <text x="6" y="26" fill="#ffc800" font-size="15">✦</text><text x="64" y="22" fill="#ffc800" font-size="15">✦</text>
</svg>`;

// Mascotte qui sourit (couronne + grands yeux + sourire) pour la victoire.
const HAPPY_OWL_SVG = `<svg viewBox="0 0 80 82" aria-hidden="true">
  <path d="M22 26 L30 14 L38 26 Z" fill="#46a302"/><path d="M58 26 L50 14 L42 26 Z" fill="#46a302"/>
  <rect x="18" y="24" width="44" height="46" rx="20" fill="#46a302"/>
  <rect x="16" y="20" width="44" height="46" rx="20" fill="#58cc02"/>
  <circle cx="26" cy="66" r="4.5" fill="#ff9bb3" opacity=".6"/>
  <circle cx="54" cy="66" r="4.5" fill="#ff9bb3" opacity=".6"/>
  <circle cx="27" cy="42" r="9" fill="#fff"/><circle cx="53" cy="42" r="9" fill="#fff"/>
  <circle cx="28" cy="43" r="4.5" fill="#3c3c3c"/><circle cx="54" cy="43" r="4.5" fill="#3c3c3c"/>
  <circle cx="29" cy="41" r="1.6" fill="#fff"/><circle cx="55" cy="41" r="1.6" fill="#fff"/>
  <path d="M40 50 l-5 6 l10 0 z" fill="#ffb020"/>
  <path d="M28 60 q12 10 24 0" stroke="#2b6b00" stroke-width="3" fill="none" stroke-linecap="round"/>
  <path d="M22 20 l5 -10 l5 8 l4 -12 l4 12 l5 -8 l5 10 z" fill="#ffc800" stroke="#e0ae00" stroke-width="1.2" stroke-linejoin="round"/>
  <circle cx="27" cy="10" r="1.6" fill="#ff4b4b"/><circle cx="40" cy="7" r="1.6" fill="#1cb0f6"/><circle cx="53" cy="10" r="1.6" fill="#a06bff"/>
</svg>`;

// ====== player-service : retrouver (ou creer) le joueur ======
async function resolvePlayer(pseudo) {
  pseudo = (pseudo || "").trim() || "Invité";
  const cache = JSON.parse(localStorage.getItem("motus_playerids") || "{}");
  if (cache[pseudo]) { state.playerId = cache[pseudo]; return cache[pseudo]; }
  let id = null;
  try {
    const res = await fetch(`${PLAYER}/players`, {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username: pseudo, email: pseudo.toLowerCase() + "@motus.local", password: "motus1234" }),
    });
    if (res.ok) id = (await res.json()).id;
    else {
      const list = await (await fetch(`${PLAYER}/players`)).json();
      const found = list.find((p) => p.username === pseudo);
      id = found ? found.id : null;
    }
  } catch (e) { return null; }
  if (id != null) { cache[pseudo] = id; localStorage.setItem("motus_playerids", JSON.stringify(cache)); state.playerId = id; }
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
  let game;
  try {
    const res = await fetch(`${GAME}/games`, {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ playerId, wordLength: len, maxAttempts: MAX_ATTEMPTS }),
    });
    if (!res.ok) throw new Error("game");
    game = await res.json();
  } catch (e) {
    showBanner("⚠️ game-service injoignable (port 8082). Réessaie (dict démarre peut-être).");
    return;
  }
  hideBanner();
  state.gameId = game.id;
  state.length = game.wordLength;
  state.maxAttempts = game.maxAttempts;
  state.firstLetter = (game.motMystere || "?")[0].toUpperCase(); // 1re lettre donnee (game revele "C****")
  state.row = 0; state.finished = false;
  state.board = Array.from({ length: state.maxAttempts }, () => Array(state.length).fill(""));
  $("levelLabel").textContent = state.length;
  $("maxLabel").textContent = state.maxAttempts;
  $("attemptLabel").textContent = 1;
  elMessage.textContent = "";
  buildBoard();
  buildKeyboard();
  keyState = {};
  penduReset();
  primeRow();
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

// La 1re lettre est donnee en INDICE (fantome, non validee). Le joueur doit la taper.
function primeRow() {
  state.col = 0;
  for (let c = 0; c < state.length; c++) {
    const t = tile(state.row, c);
    t.textContent = ""; t.classList.remove("filled", "given", "hint");
  }
  const t = tile(state.row, 0);
  t.textContent = state.firstLetter;
  t.classList.add("hint");           // affichage transparent, ne compte pas comme saisi
}

// ====== Saisie ======
function onKey(letter) {
  if (state.finished || elPlay.hidden || state.col >= state.length) return;
  state.board[state.row][state.col] = letter;
  const t = tile(state.row, state.col);
  t.textContent = letter; t.classList.remove("hint"); t.classList.add("filled");
  state.col++;
}
function onBackspace() {
  if (state.finished || state.col <= 0) return;
  state.col--;
  state.board[state.row][state.col] = "";
  const t = tile(state.row, state.col);
  t.textContent = ""; t.classList.remove("filled");
  if (state.col === 0) { t.textContent = state.firstLetter; t.classList.add("hint"); } // on ré-affiche l'indice
}
let submitting = false;
async function onEnter() {
  if (state.finished || submitting) return;
  for (let i = 0; i < state.length; i++) {
    if (!state.board[state.row][i]) { flashMessage("Complète le mot."); return; }
  }
  let guess = "";
  for (let i = 0; i < state.length; i++) guess += state.board[state.row][i];
  guess = guess.toUpperCase();

  submitting = true;
  let results;
  try {
    const res = await fetch(`${GAME}/games/${state.gameId}/guess`, {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ word: guess }),
    });
    if (res.status === 400) { shakeRow(); flashMessage(`« ${guess} » n'est pas dans le dictionnaire.`); submitting = false; return; }
    if (!res.ok) throw new Error("guess");
    results = await res.json();
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
    t.textContent = guess[c];
    setTimeout(() => {
      t.classList.add("reveal");
      setTimeout(() => { t.classList.remove("filled", "given", "hint"); t.classList.add(cls); paintKey(guess[c], cls); }, 250);
    }, c * 120);
  });
  const delay = state.length * 120 + 320;
  setTimeout(() => finishRowThenContinue(guess), delay);
}

async function finishRowThenContinue(guess) {
  let game;
  try { game = await (await fetch(`${GAME}/games/${state.gameId}`)).json(); } catch (e) { game = null; }
  const statut = game ? game.statut : "EN_COURS";
  if (statut === "GAGNE") { endGame(true, guess, game); return; }
  if (statut === "PERDU") { updatePendu(state.maxAttempts, true); endGame(false, guess, game); return; }
  state.row++;
  $("attemptLabel").textContent = state.row + 1;
  updatePendu(state.row);
  elMessage.textContent = "";
  primeRow();
  submitting = false;
}

// ====== Clavier AZERTY ======
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
async function endGame(won, lastGuess, game) {
  state.finished = true; submitting = false;
  clearInterval(state.timer);
  if (!won) await sleep(1200); // laisse le temps de voir la mascotte KO sur la potence
  const seconds = Math.round((Date.now() - state.startTime) / 1000);
  const word = (game && game.motMystere && !game.motMystere.includes("*")) ? game.motMystere : lastGuess;
  let score = null;
  try {
    const parties = await (await fetch(`${SCORE}/scores/games?playerId=${state.playerId}`)).json();
    const p = parties.find((x) => x.gameId === state.gameId);
    if (p) score = p.score;
  } catch (e) { /* score indispo */ }
  renderResult(won, word, seconds, score);
  refreshRanking(); refreshStats(); refreshHistory();
  if (won) confetti();
}

function renderResult(won, word, seconds, score) {
  $("resultIcon").innerHTML = won ? HAPPY_OWL_SVG : KO_OWL_SVG;
  $("resultTitle").textContent = won ? "Gagné !" : "Perdu…";
  $("resultWord").textContent = "Le mot était : " + word;
  $("scoreBadge").textContent = score != null ? (won ? "+" : "") + score + " points" : "";
  $("resultDetail").textContent = won
    ? `Trouvé en ${state.row + 1} essai${state.row > 0 ? "s" : ""} · ${seconds} s`
    : "Tu as utilisé tous tes essais.";
  $("defBtn").href = "https://www.larousse.fr/dictionnaires/francais/" + encodeURIComponent(word.toLowerCase());
  show("result");
}

// ====== Classement / stats / historique (score-service) ======
async function refreshRanking() {
  try {
    const list = await (await fetch(`${SCORE}/scores/ranking`)).json();
    const me = state.playerId;
    const summary = $("rankingSummary");
    const idx = me != null ? list.findIndex((r) => r.playerId === me) : -1;
    if (idx >= 0) {
      const rank = idx + 1, gap = list[idx].pointsToNext;
      let txt = `Tu es <b>${rank}${rank === 1 ? "er" : "e"}</b> sur <b>${list.length}</b>`;
      txt += gap > 0 ? ` — il te manque <b>${gap}</b> pts pour rattraper le joueur devant.` : " 🏆 En tête !";
      summary.innerHTML = txt; summary.hidden = false;
    } else { summary.hidden = true; }

    const body = $("rankingBody");
    if (!list.length) { body.innerHTML = '<li class="muted small">Aucune partie pour l\'instant.</li>'; return; }
    body.innerHTML = "";
    const MEDALS = ["🥇", "🥈", "🥉"];
    list.slice(0, 20).forEach((r, i) => {
      const li = document.createElement("li");
      li.className = "rank-item" + (r.playerId === me ? " me" : "") + (i < 3 ? " top" : "");
      const pos = i < 3 ? `<span class="medal">${MEDALS[i]}</span>` : `<span class="rank-pos">${i + 1}</span>`;
      const winRate = r.gamesPlayed > 0 ? Math.round((r.wins / r.gamesPlayed) * 100) : 0;
      li.innerHTML = `${pos}
        <div class="rank-info">
          <div class="rank-name">${pseudoForId(r.playerId)}</div>
          <div class="rank-stats">
            <span title="Parties jouées">🎮 <b>${r.gamesPlayed}</b> parties</span>
            <span title="Victoires">🏆 <b>${r.wins}</b> victoires</span>
            <span title="Taux de réussite">📈 <b>${winRate}%</b></span>
          </div>
        </div>
        <div class="rank-score"><b>${r.totalScore}</b><span class="rank-score-unit">pts</span></div>`;
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
  const body = $("historyBody");
  try {
    const games = await (await fetch(`${SCORE}/scores/games?playerId=${me}`)).json();
    if (!games.length) { body.innerHTML = '<p class="muted small">Aucune partie jouée pour l\'instant.</p>'; return; }
    // On recupere le mot mystere de chaque partie via game-service (parallele)
    const words = await Promise.all(games.map(async (g) => {
      try { const r = await fetch(`${GAME}/games/${g.gameId}`); if (!r.ok) return null;
            const j = await r.json(); return (j.motMystere || "").includes("*") ? null : j.motMystere; }
      catch (e) { return null; }
    }));
    body.innerHTML = "";
    games.forEach((g, i) => {
      const dt = new Date(g.finishedAt.slice(0, 19));
      const when = dt.toLocaleDateString("fr-FR") + " · " + dt.toLocaleTimeString("fr-FR", { hour: "2-digit", minute: "2-digit" });
      const duration = g.durationSeconds >= 60
        ? `${Math.floor(g.durationSeconds / 60)}min ${g.durationSeconds % 60}s`
        : `${g.durationSeconds}s`;
      const word = (words[i] || "?????").toUpperCase();
      const item = document.createElement("div");
      item.className = "hist-item " + (g.won ? "win" : "loss");
      const badge = g.won
        ? `<span class="hist-badge win" title="Gagné"><svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><path d="M4 7l3 12h10l3-12-5 4-3-6-3 6z"/></svg></span>`
        : `<span class="hist-badge loss" title="Perdu"><svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><path d="M20 12L4 12"/></svg></span>`;
      item.innerHTML =
        `${badge}` +
        `<div class="hist-main">` +
          `<div class="hist-word">${word}</div>` +
          `<div class="hist-meta">${when} · ⏱ ${duration} · ${g.attempts}/${g.maxAttempts} essais</div>` +
          `<a class="hist-def" href="https://www.larousse.fr/dictionnaires/francais/${encodeURIComponent(word.toLowerCase())}" target="_blank" rel="noopener">Définition</a>` +
        `</div>` +
        `<span class="hist-score ${g.won ? "" : "loss"}">${g.score} pts</span>`;
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
  $("modal-" + name).hidden = false;
}
document.querySelectorAll(".tool-btn").forEach((b) => (b.onclick = () => openModal(b.dataset.modal)));
document.querySelectorAll(".modal").forEach((m) => {
  m.addEventListener("click", (e) => { if (e.target === m || e.target.hasAttribute("data-close")) m.hidden = true; });
});

// ====== Connexion / deconnexion (clic sur le pseudo) ======
function setConnected(on) {
  pseudoInput.readOnly = on;
  pseudoInput.classList.toggle("connected", on);
}
pseudoInput.addEventListener("click", () => {
  if (pseudoInput.readOnly && pseudoInput.value.trim()) {
    $("logoutText").textContent = `Tu veux te déconnecter de « ${pseudoInput.value.trim()} » ?`;
    $("modal-logout").hidden = false;
  }
});
pseudoInput.addEventListener("change", () => {
  const v = pseudoInput.value.trim();
  localStorage.setItem("motus_pseudo", v);
  state.playerId = null;
  if (v) { setConnected(true); resolvePlayer(v).then(() => { refreshRanking(); refreshStats(); refreshHistory(); }); }
});
$("logoutConfirm").onclick = () => {
  pseudoInput.value = "";
  localStorage.removeItem("motus_pseudo");
  state.playerId = null;
  setConnected(false);
  $("modal-logout").hidden = true;
  $("rankingSummary").hidden = true;
  pseudoInput.focus();
};

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

// ====== Init ======
pseudoInput.value = localStorage.getItem("motus_pseudo") || "";
if (pseudoInput.value.trim()) { setConnected(true); resolvePlayer(pseudoInput.value); }
loadLevels();
