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
  startTime: 0, timer: null, selectedLength: null,
  playerId: null, playerName: null, isGuest: false, role: null,
};

const $ = (id) => document.getElementById(id);
const elLevels = $("levels"), elStart = $("startBtn");
const elSetup = $("setup"), elPlay = $("play"), elResult = $("result");
const elBoard = $("board"), elKeyboard = $("keyboard"), elMessage = $("message");
const elBanner = $("banner");

// Cache global des pseudos (id -> username) pour eviter des appels repetes
const nameCache = JSON.parse(localStorage.getItem("motus_names") || "{}");
function saveNameCache() { localStorage.setItem("motus_names", JSON.stringify(nameCache)); }
async function resolveName(id) {
  if (nameCache[id]) return nameCache[id];
  try {
    const r = await fetch(`${PLAYER}/players/${id}`);
    if (!r.ok) return "Joueur " + id;
    const p = await r.json();
    nameCache[id] = p.username; saveNameCache();
    return p.username;
  } catch (e) { return "Joueur " + id; }
}
async function resolveNames(ids) {
  const unique = [...new Set(ids)].filter((id) => !nameCache[id]);
  await Promise.all(unique.map((id) => resolveName(id)));
}
function pseudoForId(id) { return nameCache[id] || ("Joueur " + id); }

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
// Mascotte KO (yeux X, langue, etoiles) - bonhomme rond blanc simple
const KO_OWL_SVG = `<svg viewBox="0 0 100 100" aria-hidden="true">
  <ellipse cx="50" cy="94" rx="30" ry="4" fill="#000" opacity=".1"/>
  <circle cx="50" cy="52" r="40" fill="#ffffff" stroke="#58a700" stroke-width="3.5"/>
  <ellipse cx="34" cy="66" rx="7" ry="4" fill="#ffb3c9" opacity=".85"/>
  <ellipse cx="66" cy="66" rx="7" ry="4" fill="#ffb3c9" opacity=".85"/>
  <path d="M32 42 l12 12 M44 42 l-12 12" stroke="#3c3c3c" stroke-width="3.2" stroke-linecap="round"/>
  <path d="M56 42 l12 12 M68 42 l-12 12" stroke="#3c3c3c" stroke-width="3.2" stroke-linecap="round"/>
  <ellipse cx="50" cy="72" rx="6" ry="5" fill="#ff6b9d"/>
  <text x="6" y="26" fill="#ffc800" font-size="18">✦</text>
  <text x="82" y="24" fill="#ffc800" font-size="18">✦</text>
  <text x="88" y="60" fill="#ffc800" font-size="14">✦</text>
</svg>`;

// Mascotte qui sourit (couronne + sourire) pour la victoire - bonhomme rond blanc simple
const HAPPY_OWL_SVG = `<svg viewBox="0 0 100 110" aria-hidden="true">
  <ellipse cx="50" cy="104" rx="30" ry="4" fill="#000" opacity=".1"/>
  <path d="M18 26 l6 -14 l7 12 l5 -16 l6 16 l6 -12 l5 12 l7 -16 l6 16 l4 -12 z" fill="#ffc800" stroke="#e0ae00" stroke-width="1.2" stroke-linejoin="round"/>
  <circle cx="26" cy="14" r="2" fill="#ff4b4b"/>
  <circle cx="42" cy="10" r="2" fill="#1cb0f6"/>
  <circle cx="58" cy="10" r="2" fill="#a06bff"/>
  <circle cx="74" cy="14" r="2" fill="#58cc02"/>
  <circle cx="50" cy="62" r="40" fill="#ffffff" stroke="#58a700" stroke-width="3.5"/>
  <circle cx="38" cy="56" r="7" fill="#3c3c3c"/>
  <circle cx="62" cy="56" r="7" fill="#3c3c3c"/>
  <circle cx="36" cy="53" r="2.5" fill="#fff"/>
  <circle cx="60" cy="53" r="2.5" fill="#fff"/>
  <ellipse cx="24" cy="72" rx="8" ry="5" fill="#ffb3c9" opacity=".85"/>
  <ellipse cx="76" cy="72" rx="8" ry="5" fill="#ffb3c9" opacity=".85"/>
  <path d="M36 78 Q50 92 64 78" fill="none" stroke="#3c3c3c" stroke-width="3.4" stroke-linecap="round"/>
</svg>`;

// ====== Auth (inscription / connexion / invite) ======
function setSession(player) {
  state.playerId = player.id; state.playerName = player.username; state.isGuest = false;
  state.role = player.role || "PLAYER";
  nameCache[player.id] = player.username; saveNameCache();
  localStorage.setItem("motus_session", JSON.stringify({ id: player.id, name: player.username, role: state.role }));
  updateUserUI();
}
function setGuest() {
  state.playerId = null; state.playerName = null; state.isGuest = true; state.role = null;
  localStorage.setItem("motus_session", JSON.stringify({ guest: true }));
  updateUserUI();
}
function clearSession() {
  state.playerId = null; state.playerName = null; state.isGuest = false; state.role = null;
  localStorage.removeItem("motus_session");
  updateUserUI();
}
function updateUserUI() {
  const chip = $("userChip"), name = $("userName"), openBtn = $("authOpenBtn"), chev = $("userChevron");
  const connected = state.playerId != null;
  const anyone = connected || state.isGuest;
  chip.hidden = !anyone;
  openBtn.hidden = anyone;
  name.textContent = connected ? state.playerName : (state.isGuest ? "Invité" : "");
  chip.classList.toggle("guest", state.isGuest);
  chev.textContent = state.isGuest ? "→" : "▾";
  chip.title = state.isGuest ? "Se connecter" : "Se déconnecter";
  document.querySelectorAll(".need-account").forEach((b) => { b.style.display = connected ? "" : "none"; });
  $("adminBtn").hidden = state.role !== "ADMIN";
}

async function apiRegister(email, username, password) {
  const r = await fetch(`${PLAYER}/players`, { method: "POST", headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, email, password }) });
  if (!r.ok) { const e = await r.json().catch(() => ({})); throw new Error(e.error || "Inscription impossible"); }
  return r.json();
}
async function apiLogin(identifiant, password) {
  const r = await fetch(`${PLAYER}/players/login`, { method: "POST", headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ identifiant, password }) });
  if (!r.ok) { const e = await r.json().catch(() => ({})); throw new Error(e.error || "Identifiants invalides"); }
  return r.json();
}

// ====== Niveaux (dict-service) ======
async function loadLevels() {
  try {
    const levels = await (await fetch(`${DICT}/words/lengths`)).json();
    hideBanner();
    elLevels.innerHTML = "";
    // Uniquement les longueurs 5-9 (le back les expose deja, ceinture + bretelles)
    const jouables = levels.filter((lv) => lv.length >= 5 && lv.length <= 9);
    jouables.forEach((lv) => {
      const stars = "★".repeat(Math.max(1, lv.length - 4));
      const div = document.createElement("div");
      div.className = `level lv-${lv.length}`;
      div.innerHTML = `<div class="n">${lv.length}</div><div class="lbl">lettres</div><div class="stars">${stars}</div>`;
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
  if (state.playerId == null && !state.isGuest) { openAuth(); return; }
  // Mode invite : playerId=0, filtre du classement cote score-service.
  const playerId = state.playerId != null ? state.playerId : 0;
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
  refreshPlayRank();
}

// Chip qui s'affiche pendant la partie : position dans le classement + ecart.
async function refreshPlayRank() {
  const el = $("playRank");
  if (state.playerId == null) { el.hidden = true; return; }
  try {
    const raw = await (await fetch(`${SCORE}/scores/ranking`)).json();
    const list = raw.filter((r) => r.playerId > 0);
    const idx = list.findIndex((r) => r.playerId === state.playerId);
    if (idx < 0) {
      el.innerHTML = `Pas encore classé — joue ta 1<sup>re</sup> partie pour apparaître !`;
    } else if (idx === 0) {
      const next = list[1];
      const avance = next ? list[0].totalScore - next.totalScore : 0;
      el.innerHTML = next
        ? `🥇 1<sup>er</sup>/${list.length} — <b>${avance}</b> pts d'avance sur le suivant`
        : `🥇 1<sup>er</sup>/${list.length} — seul en tête !`;
    } else {
      const rank = idx + 1;
      const gap = list[idx].pointsToNext;
      const suffix = rank === 1 ? "er" : "e";
      el.innerHTML = `${rank}<sup>${suffix}</sup>/${list.length} — il te manque <b>${gap}</b> pts pour rattraper le joueur devant`;
    }
    el.hidden = false;
  } catch (e) { el.hidden = true; }
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
    const raw = await (await fetch(`${SCORE}/scores/ranking`)).json();
    const list = raw.filter((r) => r.playerId > 0);
    await resolveNames(list.map((r) => r.playerId));   // recupere les pseudos manquants
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
  const me = state.playerId;
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
  const me = state.playerId;
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

// ====== Admin : lister/rechercher les parties (joueur, date) ======
let adminPlayersLoaded = false;
async function loadAdminPlayers() {
  if (adminPlayersLoaded) return;
  try {
    const players = await (await fetch(`${PLAYER}/players`)).json();
    const sel = $("adminPlayer");
    players
      .sort((a, b) => a.username.localeCompare(b.username))
      .forEach((p) => {
        const opt = document.createElement("option");
        opt.value = p.id; opt.textContent = p.username;
        sel.appendChild(opt);
      });
    adminPlayersLoaded = true;
  } catch (e) { /* silencieux */ }
}
async function openAdmin() {
  await loadAdminPlayers();
  runAdminSearch();
}
async function runAdminSearch() {
  const playerId = $("adminPlayer").value;
  const from = $("adminFrom").value;
  const to = $("adminTo").value;
  const body = $("adminBody"), count = $("adminCount");
  body.innerHTML = '<p class="muted small">Recherche…</p>';
  const params = new URLSearchParams();
  if (playerId) params.set("playerId", playerId);
  if (from) params.set("from", from);
  if (to) params.set("to", to);
  try {
    const games = await (await fetch(`${SCORE}/scores/games?${params}`)).json();
    await resolveNames(games.map((g) => g.playerId));
    count.textContent = games.length + " partie" + (games.length > 1 ? "s" : "");
    if (!games.length) { body.innerHTML = '<p class="muted small">Aucune partie ne correspond à ces critères.</p>'; return; }
    // Recupere le mot cible de chaque partie via game-service (en parallele)
    const sorted = games.slice().sort((a, b) => b.finishedAt.localeCompare(a.finishedAt));
    const words = await Promise.all(sorted.map(async (g) => {
      try { const r = await fetch(`${GAME}/games/${g.gameId}`); if (!r.ok) return null;
            const j = await r.json(); return (j.motMystere || "").includes("*") ? null : j.motMystere; }
      catch (e) { return null; }
    }));
    body.innerHTML = "";
    sorted.forEach((g, i) => {
      const dt = new Date(g.finishedAt.slice(0, 19));
      const when = dt.toLocaleDateString("fr-FR") + " · " + dt.toLocaleTimeString("fr-FR", { hour: "2-digit", minute: "2-digit" });
      const word = (words[i] || "?????").toUpperCase();
      const item = document.createElement("div");
      item.className = "hist-item admin " + (g.won ? "win" : "loss");
      const badge = g.won
        ? `<span class="hist-badge win" title="Gagné">🏆</span>`
        : `<span class="hist-badge loss" title="Perdu">✕</span>`;
      item.innerHTML =
        `${badge}` +
        `<div class="hist-main">` +
          `<div class="hist-player">${pseudoForId(g.playerId)} — <span class="hist-word-inline">${word}</span></div>` +
          `<div class="hist-meta">${when} · ${g.wordLength} lettres · ${g.attempts}/${g.maxAttempts} essais</div>` +
        `</div>` +
        `<span class="hist-score ${g.won ? "" : "loss"}">${g.score} pts</span>`;
      body.appendChild(item);
    });
  } catch (e) {
    body.innerHTML = '<p class="muted small">⚠️ score-service injoignable (port 8084).</p>';
    count.textContent = "";
  }
}
$("adminForm").addEventListener("submit", (e) => { e.preventDefault(); runAdminSearch(); });
$("adminReset").onclick = () => {
  $("adminPlayer").value = ""; $("adminFrom").value = ""; $("adminTo").value = "";
  runAdminSearch();
};

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
  if (name === "share")   { openShare(); return; }
  if (name === "stats")   refreshStats();
  if (name === "ranking") refreshRanking();
  if (name === "history") refreshHistory();
  if (name === "admin")   openAdmin();
  $("modal-" + name).hidden = false;
}
document.querySelectorAll(".tool-btn").forEach((b) => (b.onclick = () => openModal(b.dataset.modal)));
document.querySelectorAll(".modal").forEach((m) => {
  m.addEventListener("click", (e) => { if (e.target === m || e.target.hasAttribute("data-close")) m.hidden = true; });
});

// ====== Modale d'authentification ======
let authMode = "login";     // "login" | "register"
function openAuth() { setAuthMode("login"); $("modal-auth").hidden = false; setTimeout(() => $("authIdent").focus(), 60); }
function setAuthMode(mode) {
  authMode = mode;
  document.querySelectorAll(".auth-tab").forEach((t) => t.classList.toggle("active", t.dataset.tab === mode));
  document.querySelectorAll(".field[data-only]").forEach((f) => { f.style.display = f.dataset.only === mode ? "" : "none"; });
  $("authIdentLabel").textContent = mode === "login" ? "Pseudo ou e-mail" : "Choisis un pseudo";
  $("authIdent").placeholder = mode === "login" ? "ton pseudo" : "ex. amelie";
  $("authSubmit").textContent = mode === "login" ? "Se connecter" : "Créer mon compte";
  $("authError").hidden = true;
}
document.querySelectorAll(".auth-tab").forEach((t) => t.addEventListener("click", () => setAuthMode(t.dataset.tab)));
$("authOpenBtn").onclick = openAuth;
$("guestBtn").onclick = () => { setGuest(); $("modal-auth").hidden = true; };
$("authForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const err = $("authError"); err.hidden = true;
  const ident = $("authIdent").value.trim();
  const pwd = $("authPassword").value;
  try {
    let player;
    if (authMode === "register") {
      const email = $("authEmail").value.trim();
      if (!email || !ident || pwd.length < 6) throw new Error("Remplis tous les champs (mot de passe ≥ 6 caractères).");
      player = await apiRegister(email, ident, pwd);
    } else {
      if (!ident || !pwd) throw new Error("Remplis tous les champs.");
      player = await apiLogin(ident, pwd);
    }
    setSession(player);
    $("modal-auth").hidden = true;
    refreshRanking(); refreshStats(); refreshHistory();
  } catch (ex) { err.textContent = ex.message || "Erreur inconnue"; err.hidden = false; }
});

// Clic sur la puce utilisateur :
// - Si connecte : proposer la deconnexion (comme avant)
// - Si invite : pas de deconnexion, on ouvre directement l'ecran de connexion / inscription
$("userChip").addEventListener("click", () => {
  if (state.isGuest) { clearSession(); openAuth(); return; }
  $("logoutText").textContent = `Tu veux te déconnecter de « ${state.playerName} » ?`;
  $("modal-logout").hidden = false;
});
$("logoutConfirm").onclick = () => {
  clearSession();
  $("modal-logout").hidden = true;
  $("rankingSummary").hidden = true;
  openAuth();
};

// ====== Modale de partage ======
function openShare() {
  const url = window.location.href.split("#")[0].split("?")[0];
  $("shareUrl").value = url;
  const moi = state.playerName ? ` (${state.playerName})` : "";
  const message =
    `🟩 Coucou !${moi}\n\n` +
    `Je t'invite à jouer à *Motus* avec moi 🎮\n` +
    `Le but : deviner le mot juste en 6 essais.\n` +
    `Plus le mot est long, plus tu gagnes de points !\n\n` +
    `👉 Rejoins-moi ici :\n${url}\n\n` +
    `Bonne chance 💚`;
  $("waBtn").href = `https://wa.me/?text=${encodeURIComponent(message)}`;
  $("qrBox").hidden = true;
  $("modal-share").hidden = false;
}
$("copyBtn").onclick = async () => {
  const url = $("shareUrl").value;
  try { await navigator.clipboard.writeText(url); $("copyBtn").textContent = "Copié !"; }
  catch (e) { $("shareUrl").select(); document.execCommand("copy"); $("copyBtn").textContent = "Copié !"; }
  setTimeout(() => { $("copyBtn").textContent = "Copier"; }, 1500);
};
$("qrBtn").onclick = () => {
  const box = $("qrBox"), canvas = $("qrCanvas");
  box.hidden = false;
  // qrcode.js utilise un div ; on cree un div temporaire, on recupere l'image, on la dessine
  const tmp = document.createElement("div");
  new QRCode(tmp, { text: $("shareUrl").value, width: 220, height: 220, colorDark: "#2b6b00", colorLight: "#ffffff" });
  setTimeout(() => {
    const img = tmp.querySelector("img") || tmp.querySelector("canvas");
    if (!img) return;
    canvas.width = 220; canvas.height = 220;
    const ctx = canvas.getContext("2d");
    if (img.tagName === "IMG") { const i = new Image(); i.onload = () => ctx.drawImage(i, 0, 0, 220, 220); i.src = img.src; }
    else ctx.drawImage(img, 0, 0, 220, 220);
  }, 50);
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
(function init() {
  const saved = JSON.parse(localStorage.getItem("motus_session") || "null");
  if (saved && saved.guest) { setGuest(); }
  else if (saved && saved.id) {
    state.playerId = saved.id; state.playerName = saved.name; state.role = saved.role || "PLAYER";
    nameCache[saved.id] = saved.name; saveNameCache();
    updateUserUI();
  } else { updateUserUI(); openAuth(); }   // premier passage : on affiche l'auth
  loadLevels();
})();
