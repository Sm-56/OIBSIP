"use strict";

/* ============================================================
   SwiftATM frontend
   ============================================================ */

const $ = (selector) => document.querySelector(selector);

const inr = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const ICONS = {
  withdraw:
    '<svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3v12"/><path d="M7 10l5 5 5-5"/><path d="M5 21h14"/></svg>',
  deposit:
    '<svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 21V9"/><path d="M7 14l5-5 5 5"/><path d="M5 3h14"/></svg>',
  transfer:
    '<svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 8h13"/><path d="M13 4l4 4-4 4"/><path d="M20 16H7"/><path d="M11 12l-4 4 4 4"/></svg>',
};

const OPS = {
  withdraw: {
    title: "Withdraw cash",
    hint: "Funds are debited instantly from your balance.",
    cta: "Withdraw",
    path: "/api/withdraw",
    icon: ICONS.withdraw,
    tone: "tone-danger",
    receiver: false,
  },
  deposit: {
    title: "Deposit cash",
    hint: "Add money into your account.",
    cta: "Deposit",
    path: "/api/deposit",
    icon: ICONS.deposit,
    tone: "tone-success",
    receiver: false,
  },
  transfer: {
    title: "Transfer money",
    hint: "Send money to another SwiftATM account.",
    cta: "Transfer",
    path: "/api/transfer",
    icon: ICONS.transfer,
    tone: "tone-info",
    receiver: true,
  },
};

const state = {
  token: localStorage.getItem("atm_token") || "",
  user: null,
  balance: 0,
  transactions: [],
  op: null,
};

/* ============================================================
   api helper
   ============================================================ */
async function api(path, { method = "GET", body } = {}) {
  const headers = { "Content-Type": "application/json" };
  if (state.token) headers["X-Token"] = state.token;

  let res;
  try {
    res = await fetch(path, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined,
    });
  } catch (e) {
    return { ok: false, message: "Cannot reach the server. Is it still running?" };
  }

  let data = null;
  try {
    data = await res.json();
  } catch (e) {
    data = { ok: false, message: "Unexpected server response." };
  }

  if (res.status === 401 && data.ok === false && path !== "/api/login") {
    signOut(false);
  }
  return data;
}

/* ============================================================
   views
   ============================================================ */
function showView(name) {
  $("#loginView").classList.toggle("hidden", name !== "login");
  $("#appView").classList.toggle("hidden", name !== "app");
}

function setLoginError(message) {
  const box = $("#loginError");
  if (!message) {
    box.classList.add("hidden");
    box.textContent = "";
  } else {
    box.textContent = message;
    box.classList.remove("hidden");
  }
}

function signOut(callServer = true) {
  if (callServer && state.token) {
    api("/api/logout", { method: "POST" });
  }
  state.token = "";
  state.user = null;
  state.balance = 0;
  state.transactions = [];
  localStorage.removeItem("atm_token");
  $("#loginForm").reset();
  setLoginError("");
  showView("login");
}

/* ============================================================
   rendering
   ============================================================ */
function render() {
  $("#balanceAmount").textContent = inr.format(state.balance);
  $("#cardUser").textContent = (state.user || "-").toUpperCase();
  $("#navUser").textContent = state.user || "-";
  $("#statUser").textContent = state.user || "-";
  $("#statCount").textContent = String(state.transactions.length);

  if (state.transactions.length > 0) {
    $("#statLast").textContent = "Latest: " + state.transactions[0].type;
  } else {
    $("#statLast").textContent = "No activity yet";
  }

  renderTable();
}

function badgeClass(type) {
  const key = String(type || "").toLowerCase();
  if (["withdraw", "deposit", "transfer", "received"].includes(key)) {
    return "badge badge-" + key;
  }
  return "badge badge-transfer";
}

function isDebit(type) {
  return type === "Withdraw" || type === "Transfer";
}

function cell(text, className) {
  const td = document.createElement("td");
  td.textContent = text;
  if (className) td.className = className;
  return td;
}

function renderTable() {
  const tbody = $("#txBody");
  const empty = $("#txEmpty");
  const table = $("#txTable");
  tbody.innerHTML = "";

  if (state.transactions.length === 0) {
    empty.classList.remove("hidden");
    table.classList.add("hidden");
    return;
  }

  empty.classList.add("hidden");
  table.classList.remove("hidden");

  state.transactions.forEach((t, index) => {
    const tr = document.createElement("tr");
    tr.appendChild(cell(String(index + 1), "col-id"));
    tr.appendChild(cell(t.time, "muted-cell"));

    const typeTd = document.createElement("td");
    const badge = document.createElement("span");
    badge.className = badgeClass(t.type);
    badge.textContent = t.type;
    typeTd.appendChild(badge);
    tr.appendChild(typeTd);

    tr.appendChild(cell(t.details));

    const debit = isDebit(t.type);
    const amountTd = document.createElement("td");
    amountTd.className = "col-amt " + (debit ? "amt-neg" : "amt-pos");
    amountTd.textContent = (debit ? "-" : "+") + inr.format(t.amount);
    tr.appendChild(amountTd);

    tbody.appendChild(tr);
  });
}

function toast(message, type = "success") {
  const el = document.createElement("div");
  el.className = "toast toast-" + type;

  const dot = document.createElement("span");
  dot.className = "toast-dot";

  const text = document.createElement("span");
  text.textContent = message;

  el.appendChild(dot);
  el.appendChild(text);
  $("#toasts").appendChild(el);

  requestAnimationFrame(() => {
    requestAnimationFrame(() => el.classList.add("show"));
  });

  setTimeout(() => {
    el.classList.remove("show");
    setTimeout(() => el.remove(), 350);
  }, 3600);
}

/* ============================================================
   state loading
   ============================================================ */
async function loadState() {
  const data = await api("/api/state");
  if (!data.ok) return false;

  state.user = data.userId;
  state.balance = data.balance;
  state.transactions = data.transactions || [];
  render();
  return true;
}

async function enterApp() {
  showView("app");
  await loadState();
}

/* ============================================================
   login
   ============================================================ */
$("#loginForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  setLoginError("");

  const userId = $("#userId").value.trim();
  const pin = $("#pin").value.trim();
  const btn = $("#loginBtn");
  const label = btn.querySelector(".btn-label");
  const original = label.textContent;

  btn.disabled = true;
  label.textContent = "Signing in...";

  const data = await api("/api/login", { method: "POST", body: { userId, pin } });

  btn.disabled = false;
  label.textContent = original;

  if (!data.ok) {
    setLoginError(data.message);
    return;
  }

  state.token = data.token;
  localStorage.setItem("atm_token", state.token);
  $("#loginForm").reset();
  await enterApp();
  toast("Welcome back, " + data.userId + "!");
});

document.querySelectorAll(".demo-chip").forEach((chip) => {
  chip.addEventListener("click", () => {
    $("#userId").value = chip.dataset.user;
    $("#pin").value = chip.dataset.pin;
    setLoginError("");
    $("#pin").focus();
  });
});

/* ============================================================
   modal / operations
   ============================================================ */
function openModal(kind) {
  const op = OPS[kind];
  if (!op) return;

  state.op = kind;
  $("#modalTitle").textContent = op.title;
  $("#modalHint").textContent = op.hint;
  $("#modalIco").innerHTML = op.icon;
  $("#opSubmit").textContent = op.cta;
  $("#receiverRow").classList.toggle("hidden", !op.receiver);

  $("#opForm").reset();
  $("#opError").classList.add("hidden");
  $("#opError").textContent = "";

  const overlay = $("#modalOverlay");
  overlay.classList.add("open");
  overlay.setAttribute("aria-hidden", "false");

  setTimeout(() => {
    const first = op.receiver ? $("#receiverId") : $("#amount");
    first.focus();
  }, 60);
}

function closeModal() {
  const overlay = $("#modalOverlay");
  overlay.classList.remove("open");
  overlay.setAttribute("aria-hidden", "true");
  state.op = null;
}

function setOpError(message) {
  const box = $("#opError");
  if (!message) {
    box.classList.add("hidden");
    box.textContent = "";
  } else {
    box.textContent = message;
    box.classList.remove("hidden");
  }
}

document.querySelectorAll(".action").forEach((btn) => {
  btn.addEventListener("click", () => openModal(btn.dataset.op));
});

$("#modalClose").addEventListener("click", closeModal);
$("#modalCancel").addEventListener("click", closeModal);

$("#modalOverlay").addEventListener("click", (e) => {
  if (e.target === $("#modalOverlay")) closeModal();
});

document.addEventListener("keydown", (e) => {
  if (e.key === "Escape" && $("#modalOverlay").classList.contains("open")) {
    closeModal();
  }
});

document.querySelectorAll(".chip").forEach((chip) => {
  chip.addEventListener("click", () => {
    $("#amount").value = chip.dataset.amt;
    setOpError("");
    $("#amount").focus();
  });
});

$("#opForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const op = OPS[state.op];
  if (!op) return;

  setOpError("");

  const amount = parseFloat($("#amount").value);
  if (!Number.isFinite(amount) || amount <= 0) {
    setOpError("Please enter a valid amount greater than zero.");
    return;
  }

  const body = { amount };
  if (op.receiver) {
    const receiverId = $("#receiverId").value.trim();
    if (!receiverId) {
      setOpError("Receiver User ID is required.");
      return;
    }
    body.receiverId = receiverId;
  }

  const btn = $("#opSubmit");
  const original = btn.textContent;
  btn.disabled = true;
  btn.textContent = "Processing...";

  const data = await api(op.path, { method: "POST", body });

  btn.disabled = false;
  btn.textContent = original;

  if (!data.ok) {
    setOpError(data.message);
    return;
  }

  closeModal();
  toast(data.message);
  await loadState();
});

/* ============================================================
   navbar actions
   ============================================================ */
$("#logoutBtn").addEventListener("click", () => {
  signOut(true);
  toast("Logged out successfully.");
});

$("#refreshBtn").addEventListener("click", async () => {
  const ok = await loadState();
  if (ok) toast("Transaction history refreshed.");
});

/* ============================================================
   boot
   ============================================================ */
(async function init() {
  if (state.token) {
    const ok = await loadState();
    if (ok) {
      showView("app");
      return;
    }
    localStorage.removeItem("atm_token");
    state.token = "";
  }
  showView("login");
})();
